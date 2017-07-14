package hoang.l3s.attt.model.languagemodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class LanguageModel {

	private int n;

	private HashMap<String, Double> existedPreWordCountsMap;
	private HashMap<String, HashMap<String, Double>> existedWordCountsMap;
	public HashMap<String, Double> unigramCountsMap;
	public int totalCount;

	private TweetPreprocessingUtils preprocessingUtils;

	/***
	 * to train a language model based on a set of tweets
	 * 
	 * @param tweets
	 */
	public LanguageModel(int _ngram, TweetPreprocessingUtils _preprocessingUtils) {
		this.n = _ngram;
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
					Set<String> keys = termCountMap.keySet();
					Iterator<String> iter = keys.iterator();
					boolean find = false;
					while (iter.hasNext()) {
						String aTerm = iter.next();
						if (aTerm.equals(term)) {
							double cnt = termCountMap.get(term);
							termCountMap.put(term, cnt + 1.0);
							find = true;
							break;
						}
					}
					if (!find) {
						termCountMap.put(term, 1.0);
					}
				} else {
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
			getCountsMap(terms, this.n);
		}
	}

	public void getLM() {
		System.out.println("++++++++++++++++++++++++");
		List<NGram> lmList = new ArrayList<NGram>();

		Set<String> preKeys = existedPreWordCountsMap.keySet();
		Iterator<String> preIter = preKeys.iterator();
		while (preIter.hasNext()) {
			String preWord = preIter.next();
			double preCount = existedPreWordCountsMap.get(preWord);

			HashMap<String, Double> termCountMap = existedWordCountsMap.get(preWord);
			Set<String> keys = termCountMap.keySet();
			Iterator<String> iter = keys.iterator();
			while (iter.hasNext()) {
				String term = iter.next();
				double count = termCountMap.get(term);

				NGram lm = new NGram();
				lm.n = this.n;
				lm.preTerm = preWord;
				lm.term = term;
				lm.probility = count / preCount;
				lmList.add(lm);

				System.out.println("n:" + this.n + ",P(" + lm.term + "|" + lm.preTerm + ")" + " = " + count + "/"
						+ preCount + " = " + lm.probility);
			}
		}
	}

	public void trainNgramLM(List<Tweet> tweets) {
		getCount(tweets);
		getLM();
	}

	public void trainUnigramLM(List<Tweet> tweets) {
		unigramCountsMap = new HashMap<String, Double>();
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

		List<NGram> lmList = new ArrayList<NGram>();
		Set<String> keys = unigramCountsMap.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			String aTerm = iter.next();

			NGram lm = new NGram();
			lm.n = 1;
			lm.preTerm = null;
			lm.term = aTerm;
			lm.probility = unigramCountsMap.get(aTerm) / totalCount;
			lmList.add(lm);
			System.out.println("n:" + 1 + ",P(" + lm.term + ")" + " = " + unigramCountsMap.get(aTerm) + "/" + totalCount
					+ " = " + lm.probility);
		}
	}

	public void train(List<Tweet> tweets) {
		if (this.n == 1) {
			trainUnigramLM(tweets);
		} else {
			trainNgramLM(tweets);
		}
	}

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
