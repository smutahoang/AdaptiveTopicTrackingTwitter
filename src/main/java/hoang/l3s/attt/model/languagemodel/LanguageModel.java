package hoang.l3s.attt.model.languagemodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.languagemodel.LanguageModelSmoothing.SmoothingType;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class LanguageModel {

	private String placeholderKey = "[[UNIGRAM]]";

	private int nGram;

	private HashMap<String, Integer> preTermCountMap; // HashMap<preTerm, count>
	private HashMap<String, HashMap<String, Integer>> nTermCountMap; // HashMap<preTerm,
																		// HashMap<term,
																		// count>>

	private TweetPreprocessingUtils preprocessingUtils;
	private LanguageModelSmoothing smoother;
	private HashMap<String, HashMap<String, Double>> bgProbMap;

	/***
	 * to train a language model based on a set of tweets
	 * 
	 * @param tweets
	 */
	public LanguageModel(int _nGram, TweetPreprocessingUtils _preprocessingUtils, LanguageModelSmoothing _smoother) {
		this.nGram = _nGram;
		this.preprocessingUtils = _preprocessingUtils;
		this.smoother = _smoother;
		this.bgProbMap = new HashMap<String, HashMap<String, Double>>();
	}

	/*
	 * process one tweet
	 */
	public void getCountMap(List<String> terms) {

		for (int j = 0; j < terms.size() - (nGram - 1); j++) {
			StringBuffer preTerm = new StringBuffer("");

			int k = 0;
			for (k = 0; k < nGram - 1; k++) {
				preTerm.append(terms.get(j + k) + " ");
			}
			String term = terms.get(j + k);

			if (preTermCountMap.containsKey(preTerm.toString())) {
				int preCnt = preTermCountMap.get(preTerm.toString());
				preTermCountMap.put(preTerm.toString(), preCnt + 1);

				HashMap<String, Integer> termCountMap = nTermCountMap.get(preTerm.toString());
				if (termCountMap != null) {
					if (termCountMap.containsKey(term)) {
						int count = termCountMap.get(term);
						termCountMap.put(term, count + 1);
					} else {
						termCountMap.put(term, 1);
					}
				} else {
					termCountMap = new HashMap<String, Integer>();
					termCountMap.put(term, 1);
				}

				nTermCountMap.put(preTerm.toString(), termCountMap);
			} else {
				preTermCountMap.put(preTerm.toString(), 1);

				HashMap<String, Integer> termCountMap = new HashMap<String, Integer>();
				termCountMap.put(term, 1);
				nTermCountMap.put(preTerm.toString(), termCountMap);
			}
		}
	}

	public void getNgramCount(List<Tweet> tweets) {
		preTermCountMap = new HashMap<String, Integer>();
		nTermCountMap = new HashMap<String, HashMap<String, Integer>>();
		int nTweets = tweets.size();
		for (int i = 0; i < nTweets; i++) {
			List<String> terms = tweets.get(i).getTerms(preprocessingUtils);
			getCountMap(terms);
		}
	}

	public void getUnigramCount(List<Tweet> tweets) {
		HashMap<String, Integer> unigramCountMap = new HashMap<String, Integer>();
		int totalCount = 0;
		int nTweets = tweets.size();
		for (int i = 0; i < nTweets; i++) {
			List<String> terms = tweets.get(i).getTerms(preprocessingUtils);
			int len = terms.size();
			for (int j = 0; j < len; j++) {
				String term = terms.get(j);
				if (!unigramCountMap.containsKey(term)) {
					unigramCountMap.put(term, 1);
				} else {
					int count = unigramCountMap.get(term);
					unigramCountMap.put(term, count + 1);
				}
				totalCount++;
			}
		}
		/*
		 * To use the same smoothing technology, we make the same format of
		 * unigram as ngram, the method is to add an placeholder key as the
		 * preWord of unigram, and consider the totalcount of pre as the
		 * preWord's count
		 */
		preTermCountMap = new HashMap<String, Integer>();
		nTermCountMap = new HashMap<String, HashMap<String, Integer>>();
		preTermCountMap.put(placeholderKey, totalCount);
		nTermCountMap.put(placeholderKey, unigramCountMap);

	}

	public void trainLM(List<Tweet> tweets, SmoothingType smoothingType) {
		if (nGram == 1) {
			getUnigramCount(tweets);
		} else {
			getNgramCount(tweets);
		}

		this.smoother.smoothing(preTermCountMap, nTermCountMap, bgProbMap, smoothingType);

		printLM(bgProbMap);
	}

	public void printLM(HashMap<String, HashMap<String, Double>> ngramProbMap) {
		Set<String> preKeys = ngramProbMap.keySet();
		Iterator<String> preIter = preKeys.iterator();
		int num = 0;
		double sum = 0;
		while (preIter.hasNext()) {
			String preTerm = preIter.next();

			HashMap<String, Double> termProbMap = ngramProbMap.get(preTerm);
			Set<String> keys = termProbMap.keySet();
			Iterator<String> iter = keys.iterator();
			while (iter.hasNext()) {
				String term = iter.next();

				double preCount = 0;
				double count = 0;
				if (nTermCountMap.containsKey(preTerm) && nTermCountMap.get(preTerm).containsKey(term)) {
					preCount = preTermCountMap.get(preTerm);
					count = nTermCountMap.get(preTerm).get(term);
				}
				double prob = termProbMap.get(term);

				System.out.println(smoother.formatPrintProb(this.nGram, preTerm, term, preCount, count, prob));

				num++;
				sum += prob;
			}
		}

		System.out.println("total size:" + num + " *******sum:" + sum);
	}

	public List<Double> getProbilities(Tweet tweet, HashMap<String, HashMap<String, Double>> probMap) {
		List<Double> probilitiesList = new ArrayList<Double>();
		List<String> terms = tweet.getTerms(preprocessingUtils);
		System.out.println("term size:" + terms.size());

		for (int i = 0; i < terms.size() - nGram + 1; i++) {
			StringBuffer preTerm = new StringBuffer("");
			if (nGram == 1) {
				preTerm.append(placeholderKey);
			} else {
				for (int j = 0; j < nGram - 1; j++) {
					// preTerm.append(terms.get(i + j) + " ");
					preTerm.append(terms.get(i + j));
					preTerm.append(" ");
				}
			}

			String term = terms.get(i + nGram - 1);
			System.out.println(term + "|" + preTerm);

			if (probMap.containsKey(preTerm.toString()) && probMap.get(preTerm.toString()).containsKey(term)) {
				probilitiesList.add(probMap.get(preTerm.toString()).get(term));
				System.out.println("-----------pro:" + probMap.get(preTerm.toString()).get(term));
			}
		}

		return probilitiesList;

	}

	/***
	 * compute perplexity of a tweet
	 * 
	 * @param tweet
	 * @return
	 */
	public double getPerplexity(Tweet tweet) {

		double perplexity = 0;
		double sum = 0;
		List<Double> probList = getProbilities(tweet, bgProbMap);
		int count = probList.size();

		if (count == 0) {
			return -1;
		}

		for (int i = 0; i < count; i++) {
			double pro = probList.get(i);
			sum += Math.log(pro) / Math.log(2);
			// System.out.print("a pro:" + pro + " log:" + Math.log(pro) /
			// Math.log(2) + "
			// sum:" + sum + "\n");
		}

		sum = sum * (-1.0 / count);
		// System.out.println("sum:" + sum);
		perplexity = Math.pow(2, sum);

		System.out.println("perplexity:" + perplexity + " probList.size:" + probList.size());

		return perplexity;
	}

	/***
	 * update the language model given a new tweet
	 * 
	 * @param tweet
	 */
	public void update(Tweet tweet) {

	}
}