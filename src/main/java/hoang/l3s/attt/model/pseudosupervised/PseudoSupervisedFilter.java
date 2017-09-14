package hoang.l3s.attt.model.pseudosupervised;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.configure.Configure.UpdatingScheme;
import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
import hoang.l3s.attt.utils.RankingUtils;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class PseudoSupervisedFilter extends FilteringModel {
	private List<Tweet> recentRelevantTweets;
	private LinkedList<Tweet> recentTweets;
	private Classifier classifier;
	private HashSet<String> keywords;
	

	public PseudoSupervisedFilter(LinkedList<Tweet> _recentTweets) {
		recentTweets = _recentTweets;
		preprocessingUtils = new TweetPreprocessingUtils();
		rand = new Random(0);
		nRelevantTweets = 0;
	}

	public void init(List<Tweet> _recentRelevantTweets) {
		recentRelevantTweets = _recentRelevantTweets;
		// train the first classifier
		trainClassifier();
	}

	/***
	 * train the classifier for classifying tweets' relevance
	 */
	public void trainClassifier() {
		getKeyWords();
		List<Tweet> nonRelevantTweets = sampleNonRelevantTweets();
		classifier = new Classifier(recentRelevantTweets, nonRelevantTweets, preprocessingUtils);
	}

	/***
	 * sample non-relevant tweets from recent tweets
	 * 
	 * @return
	 */
	public List<Tweet> sampleNonRelevantTweets() {
		List<Tweet> samples = new ArrayList<Tweet>();
		double sampleRatio = (double) Configure.NONRELEVANT_TWEET_SAMPLING_RATIO * recentRelevantTweets.size()
				/ recentTweets.size();
		for (Tweet tweet : recentTweets) {
			List<String> terms = tweet.getTerms(preprocessingUtils);
			boolean flag = true;
			for (String term : terms) {
				if (keywords.contains(term)) {
					flag = false;
					break;
				}
			}
			// sample
			if (flag) {
				if (rand.nextDouble() < sampleRatio)
					samples.add(tweet);
			}
		}
		return samples;
	}

	/***
	 * get tfIdf of all terms in recent relevant tweets
	 * 
	 * @return
	 */
	public HashMap<String, Double> getTermTfIdf() {
		HashMap<String, Double> tfIdfTermsMap = new HashMap<String, Double>();
		HashMap<String, Integer> termTf = new HashMap<String, Integer>();
		HashMap<String, Integer> termIdf = new HashMap<String, Integer>();
		// get tf
		int nTerms = 0;
		for (Tweet tweet : recentRelevantTweets) {
			List<String> terms = tweet.getTerms(preprocessingUtils);
			int len = terms.size();
			for (int j = 0; j < len; j++) {
				String word = terms.get(j);
				if (!termTf.containsKey(word))
					termTf.put(word, 1);
				else {
					int count = termTf.get(word);
					termTf.put(word, count + 1);
				}
				nTerms++;
			}
		}

		// get idf
		for (Tweet tweet : recentTweets) {
			List<String> terms = tweet.getTerms(preprocessingUtils);
			for (String term : terms) {
				if (!termTf.containsKey(term))
					continue;
				if (termIdf.containsKey(term)) {
					int count = termIdf.get(term);
					termIdf.put(term, count + 1);
				} else
					termIdf.put(term, 1);
			}
		}
		// get tf-idf
		int nRecentTweets = recentTweets.size();
		Set<String> keys = termTf.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			String term = iter.next();
			double tf = termTf.get(term);
			if (termIdf.containsKey(term)) {
				tfIdfTermsMap.put(term, (tf / nTerms) * Math.log((double) nRecentTweets / termIdf.get(term)));
			}

		}
		return tfIdfTermsMap;
	}

	/***
	 * get set of keywords
	 * 
	 */
	private void getKeyWords() {
		HashMap<String, Double> tfIdfTermsMap = getTermTfIdf();
		keywords = RankingUtils.getTopKTfIdfTerms(Configure.NUMBER_EXCLUSION_TERMS, tfIdfTermsMap);
	}

	public double relevantScore(Tweet tweet) {
		if (classifier.classify(tweet).equals(Configure.RELEVANT_CLASS)) {
			return 1;
		}
		return 0;
	}

	/***
	 * 
	 */
	public void update(Tweet relevantTweet) {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> update");
		long diff = relevantTweet.getPublishedTime() - startTime;
		int time = (int) (diff / Configure.TIME_STEP_WIDTH);
		timeStep = time;

		// remove top oldest relevant tweets
		removeOldestRelevantTweets();

		// re-train the classifier
		getKeyWords();
		List<Tweet> nonRelevantTweets = sampleNonRelevantTweets();
		classifier = new Classifier(recentRelevantTweets, nonRelevantTweets, preprocessingUtils);
	}

	/***
	 * remove some oldest relevant tweets
	 */
	public void removeOldestRelevantTweets() {
		int nRemovingTweets = (int) (recentRelevantTweets.size() * Configure.OLD_RELEVANT_TWEET_REMOVING_RATIO);
		for (int i = nRemovingTweets; i < recentRelevantTweets.size(); i++) {
			recentRelevantTweets.set(i - nRemovingTweets, recentRelevantTweets.get(i));
		}
		for (int i = 0; i < nRemovingTweets; i++) {
			recentRelevantTweets.remove(recentRelevantTweets.size() - 1);
		}
	}

	public void filter(TweetStream stream, String outputPath) {
		// determine startTime
		System.out.println("determining startTime");
		super.setStartTime(stream, recentRelevantTweets.get(recentRelevantTweets.size() - 1));
		System.out.println("done!");

		// start filtering
		String filteredTweetFile = String.format("%s/pseudo_supervised/psFilteredTweets.txt", outputPath);
		File file = new File(filteredTweetFile);
		if (file.exists()) {
			file.delete();
		}

		Tweet tweet = null;
		while ((tweet = stream.getTweet()) != null) {
			if (tweet.getTerms(preprocessingUtils).size() == 0) {
				continue;
			}
			String result = classifier.classify(tweet);
			if (result.equals(Configure.RELEVANT_CLASS)) {
				recentRelevantTweets.add(tweet);
				outputTweet(tweet, filteredTweetFile);
				nRelevantTweets++;
				if (Configure.updatingScheme == UpdatingScheme.TWEET_COUNT) {
					// check if is the update time for update
					if (super.isToUpdate(tweet)) {
						update(tweet);
					}
				}
			} else {
				// insert t to Window and remove some old tweets in W
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>REMOVE");
				for (int i = 0; i < Configure.NUMBER_OLD_TWEET_REMOVING_WINDOW; i++)
					recentTweets.removeFirst();
				recentTweets.add(tweet);
			}

			// check if is the update time for update
			if (Configure.updatingScheme == UpdatingScheme.PERIODIC) {
				if (super.isToUpdate(tweet)) {
					update(tweet);
				}
			}
		}

	}

}
