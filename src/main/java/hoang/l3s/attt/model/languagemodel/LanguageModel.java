package hoang.l3s.attt.model.languagemodel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class LanguageModel {

	private int nGram;

	private HashMap<String, Double> existedPreWordCountsMap;                  //HashMap<preWord, count>
	private HashMap<String, HashMap<String, Double>> existedWordCountsMap;    //HashMap<preWord, HashMap<term, count>>
	public HashMap<String, Double> unigramCountsMap;
	public int totalCount;

	private TweetPreprocessingUtils preprocessingUtils;

	/***
	 * to train a language model based on a set of tweets
	 * 
	 * @param tweets
	 */
	public LanguageModel(int _ngram, TweetPreprocessingUtils _preprocessingUtils) {
		this.nGram = _ngram;
		this.preprocessingUtils = _preprocessingUtils;
	}

	public void getCountsMap(List<String> terms, int count) {

		for (int j = 0; j < terms.size() - (count - 1); j++) {
			StringBuffer preWord = new StringBuffer("");

			int k = 0;
			for (k = 0; k < count - 1; k++) {
				preWord.append(terms.get(j + k) + " ");
			}
			String term = terms.get(j + k);

			if (existedPreWordCountsMap.containsKey(preWord.toString())) {
				double preCnt = existedPreWordCountsMap.get(preWord.toString());
				existedPreWordCountsMap.put(preWord.toString(), preCnt + 1.0);

				HashMap<String, Double> termCountMap = existedWordCountsMap.get(preWord.toString());
				if (termCountMap != null) {
					if(termCountMap.containsKey(term)) {
						double cnt = termCountMap.get(term);
						termCountMap.put(term, cnt + 1.0);
					}else {
						termCountMap.put(term, 1.0);
					}
				}else {
					termCountMap = new HashMap<String, Double>();
					termCountMap.put(term, 1.0);
				}
				
				existedWordCountsMap.put(preWord.toString(), termCountMap);
			} else {
				existedPreWordCountsMap.put(preWord.toString(), 1.0);

				HashMap<String, Double> termCountMap = new HashMap<String, Double>();
				termCountMap.put(term, 1.0);
				existedWordCountsMap.put(preWord.toString(), termCountMap);
			}
		}
	}

	public void getCount(List<Tweet> tweets) {
		existedPreWordCountsMap = new HashMap<String, Double>();
		existedWordCountsMap = new HashMap<String, HashMap<String, Double>>();
		int nTweets = tweets.size();
		for (int i = 0; i < nTweets; i++) {
			List<String> terms = tweets.get(i).getTerms(preprocessingUtils);
			getCountsMap(terms, this.nGram);
		}
	}

	public HashMap<String,HashMap<String,Double>> getLM() {
		System.out.println("++++++++++++++++++++++++");
		HashMap<String,HashMap<String,Double>> ngramProMap = new HashMap<String,HashMap<String,Double>>();

		Set<String> preKeys = existedPreWordCountsMap.keySet();
		Iterator<String> preIter = preKeys.iterator();
		while (preIter.hasNext()) {
			String preWord = preIter.next();
			double preCount = existedPreWordCountsMap.get(preWord);

			HashMap<String,Double> termMap = new HashMap<String,Double>();
			HashMap<String, Double> termCountMap = existedWordCountsMap.get(preWord);
			Set<String> keys = termCountMap.keySet();
			Iterator<String> iter = keys.iterator();
			while (iter.hasNext()) {
				String term = iter.next();
				double count = termCountMap.get(term);
				
				termMap.put(term, count / preCount);
				ngramProMap.put(preWord,termMap);
				System.out.println("n:" + this.nGram + ",P(" + term + "|" + preWord + ")" + " = " + count + "/"
						+ preCount + " = " + count / preCount);				
			}
		}
		
		return ngramProMap;
	}

	public HashMap<String,HashMap<String,Double>> trainNgramLM(List<Tweet> tweets) {
		getCount(tweets);
		return getLM();
	}

	public HashMap<String,Double> trainUnigramLM(List<Tweet> tweets) {
		unigramCountsMap = new HashMap<String, Double>();
		HashMap<String,Double> unigramProMap = new HashMap<String,Double>();
		int nTweets = tweets.size();
		for (int i = 0; i < nTweets; i++) {
			List<String> terms = tweets.get(i).getTerms(preprocessingUtils);
			int len = terms.size();
			for (int j = 0; j < len; j++) {
				String word = terms.get(j);
				if (!unigramCountsMap.containsKey(word)) {
					unigramCountsMap.put(word, 1.0);
				} else {
					double count = unigramCountsMap.get(word);
					unigramCountsMap.put(word, count + 1.0);
				}
				totalCount++;
			}
		}

		Set<String> keys = unigramCountsMap.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			String term = iter.next();
			double count = unigramCountsMap.get(term);
			
			unigramProMap.put(term,  count/ totalCount);
			System.out.println("n:" + 1 + ",P(" + term + ")" + " = " + count + "/" + totalCount
					+ " = " + count / totalCount);

//			NGram lm = new NGram();
//			lm.n = 1;
//			lm.preTerm = null;
//			lm.term = aTerm;
//			lm.probility = unigramCountsMap.get(aTerm) / totalCount;
//
//			System.out.println("n:" + 1 + ",P(" + lm.term + ")" + " = " + unigramCountsMap.get(aTerm) + "/" + totalCount
//					+ " = " + lm.probility);
		}
		
		return unigramProMap;
	}

//	public List<NGram> train(List<Tweet> tweets) {
//		List<NGram> lmList = null;
//		if (this.nGram == 1) {
//			lmList = trainUnigramLM(tweets);
//		} else {
//			lmList = trainNgramLM(tweets);
//		}
//		return lmList;
//	}

	/***
	 * compute perplexity of a tweet
	 * 
	 * @param tweet
	 * @return
	 */
	public double getPerplexity(Tweet tweet) {
		return 0;
	}

	/***
	 * update the language model given a new tweet
	 * 
	 * @param tweet
	 */
	public void update(Tweet tweet) {

	}
}
