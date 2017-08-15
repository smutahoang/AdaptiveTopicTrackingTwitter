package hoang.l3s.attt.model.pseudosupervised;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
import hoang.l3s.attt.utils.RankingUtils;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class PseudoSupervisedFilter extends FilteringModel {

	private Classifier classifier;
	private TweetPreprocessingUtils preprocessingUtils;
	private long publishedTimeofLastTweet;
	private static int nExclusionTerms = 200;
	private static int negativeSampleRatio = 20;
	private HashSet<String> keywords;

	public PseudoSupervisedFilter() {
		// TODO Auto-generated constructor stub
	}

	public void init(List<Tweet> tweets) {
		// TODO Auto-generated method stub

	}

	public void init(List<Tweet> firstOfTweets, List<Tweet> windowTweets) {
		preprocessingUtils = new TweetPreprocessingUtils();
		getPublishedTimeofLastTweets(firstOfTweets);
		keywords = getKeyWords(firstOfTweets, windowTweets);
		
		List<Tweet> negativeTweets = getNegativeSamples(firstOfTweets, windowTweets);
		
		classifier = new Classifier(firstOfTweets, negativeTweets);
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

		for(Tweet tweet: windowTweets) {
			if (tweet.getPublishedTime() > publishedTimeofLastTweet)
				break;
			List<String> terms = tweet.getTerms(preprocessingUtils);
			boolean flag = true;
			for (String term : terms) {
				if (keywords.contains(term)) { // check if term is one of the keyword
					flag = false;
					break;
				}
			}
			// positive sample
			if (flag) {
				double r = Math.random();
				if(r < (double) negativeSampleRatio/100)
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
		
		for(Tweet tweet: windowTweets) {
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
				// ?? should have "casting to double" if "idf" is of int type
				tfIdfTermsMap.put(term, (tf / totalFirstTerms) * Math.log((double)nTweetsInWindow / idfTermsMap.get(term)));
			}

		}
		return tfIdfTermsMap;
	}

	// get set of keywords E
	public HashSet<String> getKeyWords(List<Tweet> firstOfTweets, List<Tweet> windowTweets) {
		HashSet<String> keywords = new HashSet<String>();

		HashMap<String, Double> tfIdfTermsMap = gettfIdfTermsMap(firstOfTweets, windowTweets);
		
		// getTopLTfIdfTerms is a function that I added into RankingUtils.java
		keywords = RankingUtils.getTopKTfIdfTerms(nExclusionTerms, tfIdfTermsMap);
		return keywords;
	}

	public double relevantScore(Tweet tweet) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void update(Tweet tweet) {
		// TODO Auto-generated method stub

	}

	public void filter(TweetStream stream, String ouputPath) {
		BufferedWriter out = null;
		try {
			File file = new File(ouputPath);
			if (file.exists()) {
				file.delete();
			}
			out = new BufferedWriter(new FileWriter(file, true));
			Tweet tweet = null;

			while ((tweet = stream.getTweet()) != null) {
					String result = classifier.classify(tweet);
					if (result.equalsIgnoreCase("relevant")) {
						out.write(tweet.getText());
						out.write('\n');
					}
				// check if is the update time for update
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	
//	//test the result of training model on positive samples
//	public void filter(List<Tweet> stream, String ouputPath) {
//		BufferedWriter out = null;
//		try {
//			File file = new File(ouputPath);
//			if (file.exists()) {
//				file.delete();
//			}
//			out = new BufferedWriter(new FileWriter(file, true));
//			int count = 0;
//			int relevantCount = 0;
//			for(Tweet tweet: stream) {
//				if(count<1000) {
//					String result = classifier.classify(tweet);
//					if (result.equalsIgnoreCase("relevant")) {
//						out.write(tweet.getText());
//						out.write('\n');
//						relevantCount++;
//					}
//				} else
//					break;
//				
//				count++;
//				// check if is the update time for update
//			}
//			System.out.println("the number of relevant tweets is: "+ relevantCount);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				out.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//	}
}
