package hoang.l3s.attt.model.pseudosupervised;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

	private Classifier classifier;
	private TweetPreprocessingUtils preprocessingUtils;
	private long publishedTimeofLastTweet;
	private static int nExclusionTerms = 50;
	private HashSet<String> keywords;
	public PseudoSupervisedFilter() {
		// TODO Auto-generated constructor stub
	}
	public void init(List<Tweet> tweets) {
		// TODO Auto-generated method stub

	}
	public void init(List<Tweet> firstOfTweets, TweetStream stream) {
		preprocessingUtils = new TweetPreprocessingUtils();
		keywords = getKeyWords(firstOfTweets, stream);
		List<Tweet> negativeTweets = getNegativeSamples(firstOfTweets, stream);
		classifier = new Classifier(firstOfTweets, negativeTweets);
	}
	public void getPublishedTimeofLastTweets(List<Tweet> firstOfTweets) {
		publishedTimeofLastTweet = firstOfTweets.get(0).getPublishedTime();
		for(int i = 1; i < firstOfTweets.size(); i++) { 
			long time = firstOfTweets.get(i).getPublishedTime();
			if(publishedTimeofLastTweet < time)
				publishedTimeofLastTweet = time;
		}
			
	}
	
	//get negative samples
	public List<Tweet> getNegativeSamples(List<Tweet> firstOfTweets, TweetStream stream) {
		List<Tweet> negativeSamples = new ArrayList<Tweet>();
		
		int nNegativeSamples = firstOfTweets.size() * 10;
		int count = 0; // count the number of negative sample
		Tweet tweet = null;
		while((tweet = stream.getTweet()) != null) {
			if(tweet.getPublishedTime() > publishedTimeofLastTweet) 
				break;
			List<String> terms = tweet.getTerms(preprocessingUtils);
			boolean flag = true;
			for(String term : terms) {
				if(keywords.contains(term)) { // check if  term is one of the keyword
					flag = false;
					break;
				}
			}
			// set the size of nagative samples to be ten times the size of positive sample
			if(flag) {
				if(count < nNegativeSamples) {
					negativeSamples.add(tweet);
					count++;
				}
			}
		}
		return negativeSamples;
	}
	
	//get tfIdf of all terms
	public HashMap<String, Double> gettfIdfTermsMap(List<Tweet> firstOfTweets, TweetStream stream) {
		HashMap<String, Double>  tfIdfTermsMap = new HashMap<String, Double>();
		HashMap<String, Double> tfTermsMap = new HashMap<String, Double>();
		HashMap<String, Double> idfTermsMap = new HashMap<String, Double>();
		
		int nFirstTweets = firstOfTweets.size();
		int totalFirstTerms = 0;
		int nRandomTweets = 0;
		
		//get tf
		for(int i = 0; i< nFirstTweets; i++) {
			List<String> terms = firstOfTweets.get(i).getTerms(preprocessingUtils);
			int len = terms.size();
			for(int j = 0; j<len; j++) {
				String word = terms.get(j);
				if(!tfTermsMap.containsKey(word)) 
					tfTermsMap.put(word, 1.0);
				else {
					double count = tfTermsMap.get(word);
					tfTermsMap.put(word, count + 1.0);
				}
				totalFirstTerms++;
			}
		}
		
		//get tf-idf
		Tweet tweet = null;
		while((tweet = stream.getTweet()) != null) {
			if(tweet.getPublishedTime() > publishedTimeofLastTweet) 
				break;
			nRandomTweets++;
			List<String>  terms = tweet.getTerms(preprocessingUtils);
			for(String term: terms) {
				if(!tfTermsMap.containsKey(term))
					continue;
				if(idfTermsMap.containsKey(term)) {
					double count = idfTermsMap.get(term);
					idfTermsMap.put(term, count + 1.0);
				} else
					idfTermsMap.put(term, 1.0);
			}
		}
		
		//get tf-idf
		Set<String> keys = tfTermsMap.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			String term = iter.next();
			double tf = tfTermsMap.get(term);
			
			//check if a term dont appear in any tweets?
			if(idfTermsMap.containsKey(term)) {
				double idf = idfTermsMap.get(term);
				tfIdfTermsMap.put(term, (tf/totalFirstTerms) * Math.log(nRandomTweets/idf));
			}
			
		}
		return tfIdfTermsMap;
	}
	
	
	//get set of keywords E
	public HashSet<String> getKeyWords(List<Tweet> firstOfTweets, TweetStream stream)  {
		HashSet<String> keywords = new HashSet<String>();
		
		HashMap<String, Double> tfIdfTermsMap = gettfIdfTermsMap(firstOfTweets, stream);
	
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
			Tweet tweet = null;
			while ((tweet = stream.getTweet()) != null) {
				String result = classifier.classify(tweet);
				if (result.equalsIgnoreCase("relevant")) {
					out.write(tweet.getText());
					out.write('\n');
				}
				//check if is the update time for update
//				long date = tweet.getPublisshedTime(); 
//				if(date > publishedTimeofLastTweet) {
//					keywords = getKeyWords(firstOfTweets, stream)
//				}
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
