package hoang.l3s.attt.model.languagemodel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class LanguageModel {

	private final String UNIGRAM_PLACE_HOLDER_KEY = "[[UNIGRAM]]";

	private int nGram;

	private int nUpdates;

	private HashMap<String, Integer> prefixCountMap; // HashMap<prefix, count>
	// HashMap<prefix, HashMap<term, count>>
	private HashMap<String, HashMap<String, Integer>> prefix2TermCountMap;

	private TweetPreprocessingUtils preprocessingUtils;
	private LMSmoothingUtils lmSmoothingUtils;
	private HashMap<String, HashMap<String, Double>> prefix2TermProbMap;
	private HashMap<String, Integer> prefixStartCount;
	// #times a prefix starts a tweet
	private HashMap<String, Integer> prefixEndCount;
	// #times a prefix ends a tweet

	private int totalPrefixStartCount;
	private int totalPrefixEndCount;

	// utilities variables
	private StringBuffer prefixBuilder;

	/***
	 * to train a language model based on a set of tweets
	 * 
	 * @param tweets
	 */
	public LanguageModel(int _nGram, TweetPreprocessingUtils _preprocessingUtils, LMSmoothingUtils _lmSmoothingUtils) {
		this.nGram = _nGram;
		this.preprocessingUtils = _preprocessingUtils;
		this.lmSmoothingUtils = _lmSmoothingUtils;
		this.totalPrefixStartCount = 0;
		this.totalPrefixEndCount = 0;
		// utility variables
		prefixBuilder = new StringBuffer(150);
		nUpdates = 0;
	}

	public int nGram() {
		return nGram;
	}

	public int getNUpdates() {
		return nUpdates;
	}

	public void incNUpdates() {
		nUpdates++;
	}

	public HashMap<String, HashMap<String, Integer>> getPrefix2TermCountMap() {
		return prefix2TermCountMap;
	}

	public HashMap<String, Integer> getPrefixCountMap() {
		return prefixCountMap;
	}

	public HashMap<String, Integer> getPrefixStartCount() {
		return prefixStartCount;
	}

	public HashMap<String, Integer> getPrefixEndCount() {
		return prefixEndCount;
	}

	public HashMap<String, HashMap<String, Double>> getPrefix2TermProbMap() {
		return prefix2TermProbMap;
	}

	public int getTotalPrefixStartCount() {
		return totalPrefixStartCount;
	}

	public void setTotalPrefixStartCount(int _count) {
		totalPrefixStartCount = _count;
	}

	public int getTotalPrefixEndCount() {
		return totalPrefixEndCount;
	}

	public void setTotalPrefixEndCount(int _count) {
		totalPrefixEndCount = _count;
	}

	/*
	 * process one tweet
	 */
	private void addPrefix2TermCount(List<String> terms) {
		String prefix = null;
		int nTerms = terms.size() - (nGram - 1);
		for (int j = 0; j < nTerms; j++) {
			prefixBuilder.delete(0, 150);
			// int k = 0;
			for (int k = 0; k < nGram - 1; k++) {
				// preTerm.append(terms.get(j + k) + " ");
				prefixBuilder.append(terms.get(j + k));
				prefixBuilder.append(' ');
			}
			// String term = terms.get(j + k);

			prefix = prefixBuilder.toString();
			String term = terms.get(j + nGram - 1);

			if (j == 0) {// this prefix starts the tweet
				totalPrefixStartCount++;
				if (prefixStartCount.containsKey(prefix)) {
					prefixStartCount.put(prefix, prefixStartCount.get(prefix) + 1);
				} else {
					prefixStartCount.put(prefix, 1);
				}
			}

			if (prefixCountMap.containsKey(prefix)) {
				int count = prefixCountMap.get(prefix);
				prefixCountMap.put(prefix, count + 1);

				HashMap<String, Integer> termCountMap = prefix2TermCountMap.get(prefix);
				if (termCountMap.containsKey(term)) {
					count = termCountMap.get(term);
					termCountMap.put(term, count + 1);
				} else {
					termCountMap.put(term, 1);
				}

				prefix2TermCountMap.put(prefix, termCountMap);
			} else {
				prefixCountMap.put(prefix, 1);

				HashMap<String, Integer> termCountMap = new HashMap<String, Integer>();
				termCountMap.put(term, 1);
				prefix2TermCountMap.put(prefix, termCountMap);
			}
		}

		prefixBuilder.delete(0, 150);
		nTerms = terms.size();
		// int k = 0;
		for (int k = Math.max(0, terms.size() - (nGram - 1)); k < nTerms; k++) {
			// preTerm.append(terms.get(j + k) + " ");
			prefixBuilder.append(terms.get(k));
			prefixBuilder.append(' ');
		}

		prefix = prefixBuilder.toString();
		totalPrefixEndCount++;
		if (prefixEndCount.containsKey(prefix)) {
			prefixEndCount.put(prefix, prefixEndCount.get(prefix) + 1);
		} else {
			prefixEndCount.put(prefix, 1);
		}

	}

	private void countPrefixesTerms(List<Tweet> tweets) {
		prefixCountMap = new HashMap<String, Integer>();
		prefix2TermCountMap = new HashMap<String, HashMap<String, Integer>>();
		prefixStartCount = new HashMap<String, Integer>();
		prefixEndCount = new HashMap<String, Integer>();
		int nTweets = tweets.size();
		for (int i = 0; i < nTweets; i++) {
			List<String> terms = tweets.get(i).getTerms(preprocessingUtils);
			addPrefix2TermCount(terms);
		}
	}

	private void countUnigram(List<Tweet> tweets) {
		HashMap<String, Integer> unigramCountMap = new HashMap<String, Integer>();
		int totalCount = 0;
		int nTweets = tweets.size();
		for (int i = 0; i < nTweets; i++) {
			List<String> terms = tweets.get(i).getTerms(preprocessingUtils);
			int nTerms = terms.size();
			for (int j = 0; j < nTerms; j++) {
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
		prefixCountMap = new HashMap<String, Integer>();
		prefix2TermCountMap = new HashMap<String, HashMap<String, Integer>>();
		prefixCountMap.put(UNIGRAM_PLACE_HOLDER_KEY, totalCount);
		prefix2TermCountMap.put(UNIGRAM_PLACE_HOLDER_KEY, unigramCountMap);

	}

	/***
	 * update background language model given a set of tweets
	 * 
	 * @param tweets
	 * @param smoothingType
	 */
	public void train(List<Tweet> tweets, Configure.SmoothingType smoothingType) {
		if (nGram == 1) {
			countUnigram(tweets);
		} else {
			countPrefixesTerms(tweets);
		}
		prefix2TermProbMap = new HashMap<String, HashMap<String, Double>>();
		lmSmoothingUtils.smoothing(prefixCountMap, prefix2TermCountMap, prefix2TermProbMap, smoothingType);
		printLM(prefix2TermProbMap);
		// System.exit(-1);
	}

	public void countNGrams(List<Tweet> tweets) {
		if (nGram == 1) {
			countUnigram(tweets);
		} else {
			countPrefixesTerms(tweets);
		}
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
				if (prefix2TermCountMap.containsKey(preTerm) && prefix2TermCountMap.get(preTerm).containsKey(term)) {
					preCount = prefixCountMap.get(preTerm);
					count = prefix2TermCountMap.get(preTerm).get(term);
				}
				double prob = termProbMap.get(term);

				System.out.println(lmSmoothingUtils.formatPrintProb(preTerm, term, preCount, count, prob));

				num++;
				sum += prob;
			}
		}

		System.out.println("total size:" + num + " *******sum:" + sum);
	}

	public void save(String filename) {
		try {
			int num = 0;
			double sum = 0;
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (Map.Entry<String, HashMap<String, Double>> prefixPair : prefix2TermProbMap.entrySet()) {
				String prefix = prefixPair.getKey();
				HashMap<String, Double> termProbMap = prefixPair.getValue();
				int prefixCount = prefixCountMap.get(prefix);
				for (Map.Entry<String, Double> termPair : termProbMap.entrySet()) {
					String term = termPair.getKey();
					int termCount = prefix2TermCountMap.get(prefix).get(term);
					double prob = termPair.getValue();
					bw.write(String.format("%d,%d,%f,%s,%s\n", prefixCount, termCount, prob, prefix, term));
					num++;
					sum += prob;
				}
			}
			bw.write(String.format("total size: %d,sum:%f", num, sum));
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private double getPrefixStartProb(String prefix) {
		// Add-One smoothing
		if (prefixStartCount.containsKey(prefix)) {
			return (1.0 + prefixStartCount.get(prefix)) / (totalPrefixStartCount + prefixStartCount.size());
		} else {
			return 1.0 / (totalPrefixStartCount + prefixStartCount.size());
		}

	}

	private double getPrefixEndProb(String prefix) {
		// Add-One smoothing
		if (prefixEndCount.containsKey(prefix)) {
			return (1.0 + prefixEndCount.get(prefix)) / (prefixEndCount.size() + totalPrefixEndCount);
		} else {
			return 1.0 / (prefixEndCount.size() + totalPrefixEndCount);
		}
	}

	private double getPrefix2TermProb(String prefix, String term) {
		if (prefix2TermProbMap.containsKey(prefix)) {
			// Add-One smoothing
			HashMap<String, Double> termProbs = prefix2TermProbMap.get(prefix);
			int nTerms = prefix2TermCountMap.get(prefix).size();
			int C = prefixCountMap.get(prefix);
			if (termProbs.containsKey(term)) {
				return (1.0 + C * termProbs.get(term)) / (nTerms + C);
			} else {
				return 1.0 / (nTerms + C);
			}
		} else {
			return 1.0;
		}
	}

	private List<Double> getProbilities(Tweet tweet, HashMap<String, HashMap<String, Double>> probMap) {
		List<Double> probilitiesList = new ArrayList<Double>();
		List<String> terms = tweet.getTerms(preprocessingUtils);
		// System.out.println("term size:" + terms.size());
		String term = null;
		String prefix = null;

		int nTerms = terms.size() - nGram + 1;

		for (int i = 0; i < nTerms; i++) {
			prefixBuilder.delete(0, 150);
			if (nGram == 1) {
				prefixBuilder.append(UNIGRAM_PLACE_HOLDER_KEY);
			} else {
				for (int j = 0; j < nGram - 1; j++) {
					// preTerm.append(terms.get(i + j) + " ");
					prefixBuilder.append(terms.get(i + j));
					prefixBuilder.append(' ');
				}
			}
			prefix = prefixBuilder.toString();
			if (i == 0) {
				if (nGram > 1) {
					probilitiesList.add(getPrefixStartProb(prefix));
				}
			}

			term = terms.get(i + nGram - 1);

			double prob = getPrefix2TermProb(prefix, term);
			probilitiesList.add(prob);
			System.out.printf("term = %s prefix = %s prob = %f\n", term, prefix, prob);
		}
		if (nGram > 1) {
			prefixBuilder.delete(0, 150);
			nTerms = terms.size();
			// int k = 0;
			for (int k = Math.max(0, terms.size() - (nGram - 1)); k < nTerms; k++) {
				// preTerm.append(terms.get(j + k) + " ");
				prefixBuilder.append(terms.get(k));
				prefixBuilder.append(' ');
			}
			prefix = prefixBuilder.toString();
			probilitiesList.add(getPrefixEndProb(prefix));
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
		List<Double> probList = getProbilities(tweet, prefix2TermProbMap);
		int count = probList.size();
		if (count == 0) {
			System.out.println("SOMETHING WRONG with getProbilities!!!");
			System.exit(-1);
		}
		for (int i = 0; i < count; i++) {
			double pro = probList.get(i);
			sum += Math.log(pro);
		}
		sum = sum * (-1.0 / count);

		perplexity = Math.exp(sum);

		System.out.println("perplexity:" + perplexity + " probList.size:" + probList.size());
		if (Double.isInfinite(sum) || Double.isNaN(sum)) {
			System.out.println("SOMETHING WRONG IN getPerplexity function");
			System.exit(-1);
		}
		return perplexity;
	}
}