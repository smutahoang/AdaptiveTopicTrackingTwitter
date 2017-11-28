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
	protected List<Tweet> recentRelevantTweets;
	private Classifier classifier;
	private HashMap<String, Integer> termRelevantTweetCount;
	private HashMap<String, Integer> termRecentTweetCount;
	private HashSet<String> keywords;

	public PseudoSupervisedFilter(String _dataset, List<Tweet> _eventDescriptionTweets, List<Tweet> _recentTweets,
			TweetStream _stream, long _endTime, String _outputPath, String _outputPrefix) {
		super.dataset = _dataset;
		super.outputPath = _outputPath;
		super.outputPrefix = _outputPrefix;
		super.eventDescriptionTweets = _eventDescriptionTweets;
		recentRelevantTweets = _eventDescriptionTweets;
		super.recentTweets = _recentTweets;
		preprocessingUtils = new TweetPreprocessingUtils();
		rand = new Random(0);
		super.nRelevantTweets = 0;
		trainClassifier();
		super.endTime = _endTime;
		super.stream = _stream;
		currentTime = 0;
	}

	/***
	 * train the classifier for classifying tweets' relevance
	 */
	private void trainClassifier() {
		getKeyWords();
		subSampleRelevantTweets();
		List<Tweet> nonRelevantTweets = sampleNonRelevantTweets();
		classifier = new Classifier(recentRelevantTweets, nonRelevantTweets, termRelevantTweetCount,
				preprocessingUtils);
		classifier.saveClassifier(
				String.format("%s/%s_%s_classifier_%d.csv", outputPath, dataset, outputPrefix, currentTime));
	}

	/***
	 * sample non-relevant tweets from recent tweets
	 * 
	 * @return
	 */
	private List<Tweet> sampleNonRelevantTweets() {
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
				if (rand.nextDouble() < sampleRatio) {
					samples.add(tweet);
					if (samples.size() >= Configure.MAX_NUMBER_NONRELEVANT_TWEET) {
						break;
					}
				}
			}
		}
		return samples;
	}

	/***
	 * sub-sample relevant tweets
	 * 
	 * @return
	 */
	private void subSampleRelevantTweets() {
		int l = recentRelevantTweets.size();
		while (l > Configure.MAX_NUMBER_RELEVANT_TWEET) {
			int i = rand.nextInt(l);
			recentRelevantTweets.set(i, recentRelevantTweets.get(l - 1));
			recentRelevantTweets.remove(l - 1);
			l--;
		}
	}

	/***
	 * get tfIdf of all terms in recent relevant tweets
	 * 
	 * @return
	 */
	private HashMap<String, Double> getTermTfIdf() {
		HashMap<String, Double> tfIdfTermsMap = new HashMap<String, Double>();
		termRelevantTweetCount = new HashMap<String, Integer>();
		termRecentTweetCount = new HashMap<String, Integer>();
		// get tf
		int nTerms = 0;
		for (Tweet tweet : recentRelevantTweets) {
			List<String> terms = tweet.getTerms(preprocessingUtils);
			int len = terms.size();
			for (int j = 0; j < len; j++) {
				String word = terms.get(j);
				if (!termRelevantTweetCount.containsKey(word))
					termRelevantTweetCount.put(word, 1);
				else {
					int count = termRelevantTweetCount.get(word);
					termRelevantTweetCount.put(word, count + 1);
				}
				nTerms++;
			}
		}

		// get idf
		for (Tweet tweet : recentTweets) {
			List<String> terms = tweet.getTerms(preprocessingUtils);
			for (String term : terms) {
				if (!termRelevantTweetCount.containsKey(term))
					continue;
				if (termRecentTweetCount.containsKey(term)) {
					int count = termRecentTweetCount.get(term);
					termRecentTweetCount.put(term, count + 1);
				} else
					termRecentTweetCount.put(term, 1);
			}
		}
		// get tf-idf
		int nRecentTweets = recentTweets.size();
		Set<String> keys = termRelevantTweetCount.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			String term = iter.next();
			double tf = termRelevantTweetCount.get(term);
			if (termRecentTweetCount.containsKey(term)) {
				tfIdfTermsMap.put(term,
						(tf / nTerms) * Math.log((double) nRecentTweets / termRecentTweetCount.get(term)));
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
	public void update() {
		currentTime++;
		nextUpdateTime += Configure.TIME_STEP_WIDTH;
		// remove top oldest relevant tweets
		removeOldestRelevantTweets();
		// re-train the classifier
		getKeyWords();
		subSampleRelevantTweets();
		List<Tweet> nonRelevantTweets = sampleNonRelevantTweets();
		classifier = new Classifier(recentRelevantTweets, nonRelevantTweets, termRelevantTweetCount,
				preprocessingUtils);
		classifier.saveClassifier(
				String.format("%s/%s_%s_classifier_%d.csv", outputPath, dataset, outputPrefix, currentTime));
	}

	/***
	 * remove some oldest relevant tweets
	 */
	private void removeOldestRelevantTweets() {
		int nRemovingTweets = (int) (recentRelevantTweets.size() * Configure.OLD_RELEVANT_TWEET_REMOVING_RATIO);
		for (int i = nRemovingTweets; i < recentRelevantTweets.size(); i++) {
			recentRelevantTweets.set(i - nRemovingTweets, recentRelevantTweets.get(i));
		}
		for (int i = 0; i < nRemovingTweets; i++) {
			recentRelevantTweets.remove(recentRelevantTweets.size() - 1);
		}
	}

	public void filter() {
		// determine startTime
		System.out.println("determining startTime");
		super.setStartTime(stream, recentRelevantTweets.get(recentRelevantTweets.size() - 1));
		System.out.println("done!");

		String output_filename = String.format("%s/%s_%s_psFilteredTweets.txt", outputPath, dataset, outputPrefix);

		// start filtering
		File file = new File(output_filename);
		if (file.exists()) {
			file.delete();
		}

		Tweet tweet = null;
		while ((tweet = stream.getTweet()) != null) {
			if (super.isInvalidTweet(tweet)) {
				continue;
			}
			if (tweet.getPublishedTime() > endTime) {
				return;
			}
			String result = classifier.classify(tweet);
			if (result.equals(Configure.RELEVANT_CLASS)) {
				recentRelevantTweets.add(tweet);
				outputTweet(tweet, output_filename);
				nRelevantTweets++;
			} else {
				// insert t to Window and remove some old tweets in W
				// System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>REMOVE");
				for (int i = 0; i < Configure.NUMBER_OLD_TWEET_REMOVING_WINDOW; i++)
					((LinkedList<Tweet>) recentTweets).removeFirst();
				recentTweets.add(tweet);
			}
			// check if is the update time for update
			if (super.isToUpdate(tweet)) {
				update();
			}
		}
	}
}
