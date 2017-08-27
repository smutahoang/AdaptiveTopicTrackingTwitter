package hoang.l3s.attt.model.pseudosupervised;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
import hoang.l3s.attt.utils.RankingUtils;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class PseudoSupervisedFilter extends FilteringModel {

	private Classifier classifier;
	private TweetPreprocessingUtils preprocessingUtils;
	private long publishedTimeofLastTweet;
	private HashSet<String> keywords;

	public PseudoSupervisedFilter() {
		// TODO Auto-generated constructor stub
	}

	public void init(List<Tweet> tweets) {
		// TODO Auto-generated method stub

	}

	public void filter(TweetStream stream, String ouputPath) {
		// TODO Auto-generated method stub
	}

	public void init(List<Tweet> firstOfTweets, List<Tweet> windowTweets) {
		preprocessingUtils = new TweetPreprocessingUtils();
		getPublishedTimeofLastTweets(firstOfTweets);
		keywords = getKeyWords(firstOfTweets, windowTweets);
		List<Tweet> negativeTweets = getNegativeSamples(firstOfTweets, windowTweets);
		classifier = new Classifier(firstOfTweets, negativeTweets, preprocessingUtils);
	}

	public void getPublishedTimeofLastTweets(List<Tweet> firstOfTweets) {
		publishedTimeofLastTweet = firstOfTweets.get(0).getPublishedTime();
		for (int i = 1; i < firstOfTweets.size(); i++) {
			long time = firstOfTweets.get(i).getPublishedTime();
			if (publishedTimeofLastTweet < time)
				publishedTimeofLastTweet = time;
		}
	}

	// get negative samples
	public List<Tweet> getNegativeSamples(List<Tweet> firstOfTweets, List<Tweet> windowTweets) {
		List<Tweet> negativeSamples = new ArrayList<Tweet>();
		for (Tweet tweet : windowTweets) {
			if (tweet.getPublishedTime() > publishedTimeofLastTweet)
				break;
			List<String> terms = tweet.getTerms(preprocessingUtils);
			boolean flag = true;
			for (String term : terms) {
				if (keywords.contains(term)) {
					flag = false;
					break;
				}
			}
			// positive sample
			if (flag) {
				double r = Math.random();
				// set the size of negative sample to be ten times the size of positive sample
				double nNegativeSamples = (double) Configure.NONRELEVANT_TWEET_SAMPLING_RATIO * firstOfTweets.size() / 100;
				if (r <  nNegativeSamples)
					negativeSamples.add(tweet);
			}
		}
		return negativeSamples;
	}

	// get tfIdf of all terms
	public HashMap<String, Double> gettfIdfTermsMap(List<Tweet> firstOfTweets, List<Tweet> windowTweets) {
		HashMap<String, Double> tfIdfTermsMap = new HashMap<String, Double>();

		HashMap<String, Integer> tfTermsMap = new HashMap<String, Integer>();
		HashMap<String, Integer> idfTermsMap = new HashMap<String, Integer>();

		int nFirstTweets = firstOfTweets.size();
		int totalFirstTerms = 0;
		int nTweetsInWindow = 0;

		// get tf
		for (int i = 0; i < nFirstTweets; i++) {
			List<String> terms = firstOfTweets.get(i).getTerms(preprocessingUtils);
			int len = terms.size();
			for (int j = 0; j < len; j++) {
				String word = terms.get(j);
				if (!tfTermsMap.containsKey(word))
					tfTermsMap.put(word, 1);
				else {
					int count = tfTermsMap.get(word);
					tfTermsMap.put(word, count + 1);
				}
				totalFirstTerms++;
			}
		}

		// get idf

		for (Tweet tweet : windowTweets) {
			if (tweet.getPublishedTime() > publishedTimeofLastTweet)
				break;
			nTweetsInWindow++;
			List<String> terms = tweet.getTerms(preprocessingUtils);
			for (String term : terms) {
				if (!tfTermsMap.containsKey(term))
					continue;
				if (idfTermsMap.containsKey(term)) {
					int count = idfTermsMap.get(term);
					idfTermsMap.put(term, count + 1);
				} else
					idfTermsMap.put(term, 1);
			}
		}

		// get tf-idf
		Set<String> keys = tfTermsMap.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			String term = iter.next();
			double tf = tfTermsMap.get(term);

			// check if a term dont appear in any tweets?
			if (idfTermsMap.containsKey(term)) {
				tfIdfTermsMap.put(term,
						(tf / totalFirstTerms) * Math.log((double) nTweetsInWindow / idfTermsMap.get(term)));
			}

		}
		return tfIdfTermsMap;
	}

	// get set of keywords E
	public HashSet<String> getKeyWords(List<Tweet> firstOfTweets, List<Tweet> windowTweets) {
		HashSet<String> keywords = new HashSet<String>();
		HashMap<String, Double> tfIdfTermsMap = gettfIdfTermsMap(firstOfTweets, windowTweets);
		// getTopLTfIdfTerms is a function that I added into RankingUtils.java
		keywords = RankingUtils.getTopKTfIdfTerms(Configure.NUMBER_EXCLUSION_TERMS, tfIdfTermsMap);
		return keywords;
	}

	public double relevantScore(Tweet tweet) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void update(Tweet tweet) {
		// TODO Auto-generated method stub
	}

	public void removeOldestTweets(List<Tweet> tweets) {

		int nRemovingTweets = tweets.size() * Configure.OLD_RELEVANT_TWEET_REMOVING_RATIO / 100;
		for (int i = nRemovingTweets; i < tweets.size(); i++) {
			tweets.set(i - nRemovingTweets, tweets.get(i));
		}
		for (int i = 0; i < nRemovingTweets; i++) {
			tweets.remove(tweets.size() - 1);
		}

	}

	/*public void filter(TweetStream stream, String ouputPath, List<Tweet> firstOfTweets,
			PriorityQueue<Tweet> windowTweets) {
		// This is only to say that: maintaining a queue for windowTweets is
		// more efficient
	}*/

	public void filter(TweetStream stream, String ouputPath, List<Tweet> firstOfTweets, LinkedList<Tweet> windowTweets) {
		File file = new File(ouputPath);
		if (file.exists()) {
			file.delete();
		}
		Tweet tweet = null;
		int count = 0;
		while ((tweet = stream.getTweet()) != null) {
			if (count < 100000) {

				if (tweet.getTerms(preprocessingUtils).size() == 0)
					continue;
				String result = classifier.classify(tweet);
				if (result.equalsIgnoreCase(Configure.RELEVANT_CLASS)) {
					firstOfTweets.add(tweet); // add tweet into the set of
												// positive tweets
					outputTweet(tweet, ouputPath);

					nRelevantTweets++;

					// check if is the update time for update
					long time = tweet.getPublishedTime();
					startTime = windowTweets.get(0).getPublishedTime();			
					
					if (isToUpdate(tweet)) {
						// re-training
						System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> update");
						
						publishedTimeofLastTweet = time;

						// (optional) remove top N oldest tweets in set of first
						// tweets
						removeOldestTweets(firstOfTweets);

						keywords = getKeyWords(firstOfTweets, windowTweets);
						List<Tweet> negativeSamples = getNegativeSamples(firstOfTweets, windowTweets);
						classifier = new Classifier(firstOfTweets, negativeSamples, preprocessingUtils);
					}
				} else {
					// insert t to Window and remove some old tweets in W
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>REMOVE");
					for(int i = 0; i<Configure.NUMBER_OLD_TWEET_REMOVING_WINDOW; i++)
						windowTweets.removeFirst();
					windowTweets.add(tweet);
				}
				count++;
			} else {
				break;
			}
		}

	}

}
