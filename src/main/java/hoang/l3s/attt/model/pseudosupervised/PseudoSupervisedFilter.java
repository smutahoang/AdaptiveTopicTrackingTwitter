package hoang.l3s.attt.model.pseudosupervised;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class PseudoSupervisedFilter extends FilteringModel {

	private Classifier classifer;
	private TweetPreprocessingUtils preprocessingUtils;
	private int nExclusionTerms;
	private int percentOfNegativeSamples;
	public PseudoSupervisedFilter() {
		// TODO Auto-generated constructor stub
	}
	public void init(List<Tweet> tweets) {
		// TODO Auto-generated method stub

	}
	public void init(List<Tweet> firstOfTweets, List<Tweet> tweetsWindow) {
		preprocessingUtils = new TweetPreprocessingUtils();
		
		List<Tweet> randomNegativeTweets = getRandomNegativeSamples(tweetsWindow);
		List<String> keywords = getKeyWords(firstOfTweets, tweetsWindow);
		List<Tweet> negativeSample = getNegativeSamples(tweetsWindow, getExcludedTweets(randomNegativeTweets, keywords));
		classifer = new Classifier(firstOfTweets, negativeSample);
	}
	
	public PseudoSupervisedFilter(int nExclusionTerms, int percenOfNegativeSamples) {
		this.nExclusionTerms = nExclusionTerms;
		this.percentOfNegativeSamples = percenOfNegativeSamples;
		
	}
	
	//get random negative samples
	public List<Tweet> getRandomNegativeSamples(List<Tweet> tweets) {
		List<Tweet> randomNegativeSamples = new ArrayList<Tweet>();
		for(int i = 0; i < tweets.size(); i++) {
			while(Math.random() < percentOfNegativeSamples/100) {
				randomNegativeSamples.add(tweets.get(i));
			}
		}
		return randomNegativeSamples;
	}
	//get negative samples
	public List<Tweet> getNegativeSamples(List<Tweet> tweets, List<Tweet> excludedTweets) {
		List<Tweet> negativeSamples = (ArrayList<Tweet>) tweets;
		for(Tweet t: excludedTweets) 
			negativeSamples.remove(t);
		return negativeSamples;
	}
	
	//get tfIdf of all terms
	public HashMap<String, Double> gettfIdfTermsMap(List<Tweet> firstOfTweets, List<Tweet> tweetsWindow) {
		HashMap<String, Double>  tfIdfTermsMap = new HashMap<String, Double>();
		int nTweets = firstOfTweets.size();
		int totalFirstTerms = 0;
		
		//get tf
		for(int i = 0; i< nTweets; i++) {
			List<String> terms = firstOfTweets.get(i).getTerms(preprocessingUtils);
			int len = terms.size();
			for(int j = 0; j<len; j++) {
				String word = terms.get(j);
				if(!tfIdfTermsMap.containsKey(word)) 
					tfIdfTermsMap.put(word, 1.0);
				else {
					double count = tfIdfTermsMap.get(word);
					tfIdfTermsMap.put(word, count + 1.0);
				}
				totalFirstTerms++;
			}
		}
		
		//get tf-idf
		Set<String> keys = tfIdfTermsMap.keySet();
		Iterator<String> iter = keys.iterator();
		int nTweetsWindow = tweetsWindow.size();
		while (iter.hasNext()) {
			String term = iter.next();
			//System.out.println(term);
			int countTweets = 0;
			for(Tweet tweet: tweetsWindow) {
				if(tweet.getTerms(preprocessingUtils).contains(term))
					countTweets++;
			}
			double tfTerm = tfIdfTermsMap.get(term);
			//System.out.println(countTweets);
			tfIdfTermsMap.put(term, (tfTerm/totalFirstTerms) * Math.log(nTweetsWindow/countTweets));
		}
		
		
		return tfIdfTermsMap;
	}
	
	
	//get set of keywords E
	public List<String> getKeyWords(List<Tweet> firstOfTweets, List<Tweet> tweetsWindow)  {
		List<String> keywords = new ArrayList();
		HashMap<String, Double> tfIdfTermsMap = gettfIdfTermsMap(firstOfTweets, tweetsWindow);
	
		List list = new LinkedList(tfIdfTermsMap.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	                  .compareTo(((Map.Entry) (o2)).getValue());
	            }
	       });

	       int count = 0; 
	       
	       for (ListIterator it = list.listIterator(list.size()); it.hasPrevious();) {
	              Map.Entry entry = (Map.Entry) it.previous();
	              keywords.add((String)entry.getKey());
	              count ++;
	              if(count == nExclusionTerms) break;
	      } 
		return keywords;
	}

	public double relevantScore(Tweet tweet) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	// get excluded tweets
	public List<Tweet> getExcludedTweets(List<Tweet> tweets, List<String> keywords) {
		List<Tweet> excludedTweets = new ArrayList<Tweet>();
		for(Tweet tweet: tweets) {
			for(String word: keywords)
				if(tweet.getTerms(preprocessingUtils).contains(word)) 
					excludedTweets.add(tweet);
		}
		
		return tweets;
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
			while (true) {
				Tweet tweet = stream.getTweet();

				double relevantScore = relevantScore(tweet);
				
				if (relevantScore > 0.5) {
					out.write(tweet.getText());
					out.write('\n');
				}
				//check if is the update time for update
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
	

}
