package hoang.l3s.attt.model.pseudosupervised;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
import hoang.l3s.attt.model.graphbased.AdjacentTerm;
import hoang.l3s.attt.utils.KeyValue_Pair;
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

				if (r < (double) Configure.negativeSamplesRatio / 100)

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
			// ??
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
		keywords = RankingUtils.getTopKTfIdfTerms(Configure.nExclusionTerms, tfIdfTermsMap);
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

		int nRemovingTweets = tweets.size() * Configure.removingRatio / 100;
		for (int i = nRemovingTweets; i < tweets.size(); i++) {
			tweets.set(i - nRemovingTweets, tweets.get(i));
		}
		for (int i = 0; i < nRemovingTweets; i++) {
			tweets.remove(tweets.size()-1);
		}
		
	}
	
	// remove k oldest tweets in window
	public List<Tweet> removeTweetinWindow(List<Tweet> tweets, int k) {
		List<Tweet> results = new ArrayList<Tweet>();
		PriorityQueue<KeyValue_Pair> queue = new PriorityQueue<KeyValue_Pair>();
		int n = tweets.size();
		for (int i = 0; i< n; i++) {
			Tweet currentTweet = tweets.get(i);
			if (queue.size() < n - k) {
				queue.add(new KeyValue_Pair(i, currentTweet.getPublishedTime()));
			} else {
				KeyValue_Pair head = queue.peek();
				if (head.getDoubleValue() < currentTweet.getPublishedTime()) {
					queue.poll();
					queue.add(new KeyValue_Pair(i, currentTweet.getPublishedTime()));
					
				}
			}
		}
		
		while(!queue.isEmpty())
			results.add(tweets.get(queue.poll().getIntKey()));
		return results;
	}
	public void filter(TweetStream stream, String ouputPath, List<Tweet> firstOfTweets, List<Tweet> windowTweets) {
		File file = new File(ouputPath);
		if (file.exists()) {
			file.delete();
		}
		Tweet tweet = null;
		int count = 0;
		while ((tweet = stream.getTweet()) != null) {
			if(count < 100000) {
				String result = classifier.classify(tweet);
				
				if (result.equalsIgnoreCase(Configure.rClassName)) {
					firstOfTweets.add(tweet); // add tweet into the set of positive tweets
					outputTweet(tweet, ouputPath);
					
					nRelevantTweets++;

					// check if is the update time for update
					long time = tweet.getPublishedTime();
					startTime = windowTweets.get(0).getPublishedTime();
					if(isToUpdate(tweet)) {
						// re-training 
						System.out.println("....................................update");
						publishedTimeofLastTweet = time;
						
						//(optional) remove top N oldest tweets in set of first tweets
						//removeOldestTweets(firstOfTweets);
						
						keywords = getKeyWords(firstOfTweets, windowTweets);// window with new time
						List<Tweet> negativeSamples = getNegativeSamples(firstOfTweets, windowTweets);
						classifier = new Classifier(firstOfTweets, negativeSamples, preprocessingUtils);
					}
				}  else {
					//insert t to Window and remove some old tweets in W (probabilistically)
					windowTweets = removeTweetinWindow(windowTweets, Configure.nTweetsRemovedFromWindow);
					windowTweets.add(tweet);
				}
				count++;
			} else {
				break;
			}
		}
		
	}

}
