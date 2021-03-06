package hoang.l3s.attt.model.languagemodel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import hoang.l3s.attt.configure.Configure;

public class LMSmoothingUtils {

	private static double alpha = 0.3;
	private static double lambda = 0.3;
	private static double delta = 0.9;
	private static double mu = 5000;

	public String formatPrintProb(String preTerm, String term, double preCount, double count, double prob) {
		return String.format("P(%s|%s) = %.0f|%.0f = %f", term, preTerm, count, preCount, prob);
	}

	/***
	 * get probability of term given prefix in a prefix-2-term probability map
	 * 
	 * @param prefix
	 * @param term
	 * @param prefix2TermProbMap
	 * @return
	 */
	public double getProbability(String prefix, String term,
			HashMap<String, HashMap<String, Double>> prefix2TermProbMap) {
		if (prefix2TermProbMap.containsKey(prefix)) {
			if (prefix2TermProbMap.get(prefix).containsKey(term))
				return prefix2TermProbMap.get(prefix).get(term);
		}
		return 0;
	}

	public double getSmoothedProb(int prefixCount, int termCount, double bgProb, int nNewTermsInFgCount,
			Configure.SmoothingType smoothingType) {

		double prob = 0;
		switch (smoothingType) {
		case STUPID_BACKOFF:
			if (termCount != 0) {
				prob = (1 / (1 + alpha)) * (termCount / prefixCount);
			} else {
				prob = (alpha / (1 + alpha)) * bgProb;
			}
			// if (termCount != 0) {
			// prob = (termCount / preTermCount);
			// } else {
			// prob = alpha * bgProb;
			// }
			break;

		case JELINEK_MERCER:
			prob = lambda * termCount / prefixCount + (1 - lambda) * bgProb;
			break;

		case BAYESIAN:
			prob = (termCount + mu * bgProb) / (prefixCount + mu);
			break;

		case ABSOLUTE_DISCOUNTING:
			prob = Math.max(termCount - delta, 0) / prefixCount + (delta * nNewTermsInFgCount / prefixCount) * bgProb;
			break;

		case NO_SMOOTHING:
			prob = ((double) termCount) / prefixCount;

		default:
			break;
		}

		return prob;
	}

	public void updatePrefix2TermProbMap(String prefix, String term, double prob,
			HashMap<String, HashMap<String, Double>> prefix2TermProbMap) {
		HashMap<String, Double> termProbs = prefix2TermProbMap.get(prefix);
		if (termProbs != null) {
			termProbs.put(term, prob);
		} else {
			termProbs = new HashMap<String, Double>();
			termProbs.put(term, prob);
			prefix2TermProbMap.put(prefix, termProbs);
		}
	}

	/*
	 * caculate the number of unique terms in the foreground model
	 */
	private int countNNewTermsInFg(HashMap<String, HashMap<String, Integer>> fgPrefix2TermCountMap,
			HashMap<String, HashMap<String, Double>> bgPrefix2TermProbMap) {
		int newTermCountInFg = 0;
		Set<String> prefixes = fgPrefix2TermCountMap.keySet();
		Iterator<String> iter = prefixes.iterator();
		while (iter.hasNext()) {
			String prefix = iter.next();
			HashMap<String, Integer> fgTermCount = fgPrefix2TermCountMap.get(prefix);
			Set<String> terms = fgTermCount.keySet();
			Iterator<String> iter1 = terms.iterator();
			while (iter1.hasNext()) {
				String term = iter1.next();
				if (!(bgPrefix2TermProbMap.containsKey(prefix) && bgPrefix2TermProbMap.get(prefix).containsKey(term))) {
					newTermCountInFg++;
				}
			}
		}

		return newTermCountInFg;
	}

	/*
	 * iterate F.LM if "w" is in B.LM, update Prob(w| B.ML) else add it into
	 * Prob(w| B.ML)
	 */
	private void iterateFgLM(HashMap<String, Integer> fgPrefixCountMap,
			HashMap<String, HashMap<String, Integer>> fgPrefix2TermCountMap,
			HashMap<String, HashMap<String, Double>> bgPrefix2TermProbMap, Configure.SmoothingType smoothingType,
			int newTermCountInFg) {

		int num = 0;
		Set<String> fgPrefixes = fgPrefix2TermCountMap.keySet();
		Iterator<String> fgPrefixIter = fgPrefixes.iterator();
		while (fgPrefixIter.hasNext()) {
			String fgPrefix = fgPrefixIter.next();
			Set<String> fgTerms = fgPrefix2TermCountMap.get(fgPrefix).keySet();
			Iterator<String> fgTermIter = fgTerms.iterator();
			while (fgTermIter.hasNext()) {
				String fgTerm = fgTermIter.next();

				int fgPrefixCount = fgPrefixCountMap.get(fgPrefix);
				int fgTermCount = fgPrefix2TermCountMap.get(fgPrefix).get(fgTerm);
				double bgProb = getProbability(fgPrefix, fgTerm, bgPrefix2TermProbMap);

				double prob = getSmoothedProb(fgPrefixCount, fgTermCount, bgProb, newTermCountInFg, smoothingType);
				updatePrefix2TermProbMap(fgPrefix, fgTerm, prob, bgPrefix2TermProbMap);

				System.out.println("find in F.LM:" + formatPrintProb(fgPrefix, fgTerm, fgPrefixCount, fgTermCount, prob)
						+ " with bgProb:" + bgProb);
				num++;
			}
		}
		System.out.println("F.LM size:" + num);
	}

	/*
	 * iterate B.LM if "w" is in F.LM, do nothing, because we have processed
	 * when iterating F.LM previously, if "w" is not in F.LM, modify the Prob(w|
	 * B.ML)
	 */
	private void iterateBgLM(HashMap<String, Integer> fgPrefixCountMap,
			HashMap<String, HashMap<String, Integer>> fgPrefix2TermCountMap,
			HashMap<String, HashMap<String, Double>> bgPrefix2TermProbMap, Configure.SmoothingType smoothingType,
			int newTermCountInFg) {
		int num = 0;
		int noPreTermNum = 0;
		for (Map.Entry<String, HashMap<String, Double>> bgPrefix2TermProb : bgPrefix2TermProbMap.entrySet()) {
			String bgPrefix = bgPrefix2TermProb.getKey();
			HashMap<String, Double> bgTermProbs = bgPrefix2TermProb.getValue();
			for (Map.Entry<String, Double> bgTermProb : bgTermProbs.entrySet()) {
				String bgTerm = bgTermProb.getKey();
				int fgPrefixCount = 0;
				if (fgPrefix2TermCountMap.containsKey(bgPrefix)) {
					if (fgPrefix2TermCountMap.get(bgPrefix).containsKey(bgTerm)) {
						continue;
					} else {
						fgPrefixCount = fgPrefixCountMap.get(bgPrefix);
					}
				} else {
					noPreTermNum++;
					continue;
				}
				double bgProb = bgTermProbs.get(bgTerm);
				double prob = getSmoothedProb(fgPrefixCount, 0, bgProb, newTermCountInFg, smoothingType);
				updatePrefix2TermProbMap(bgPrefix, bgTerm, prob, bgPrefix2TermProbMap);

				System.out.println("find in B.LM:" + formatPrintProb(bgPrefix, bgTerm, fgPrefixCount, 0, prob)
						+ " with bgProb:" + bgProb);
				num++;
			}
		}
		System.out.println("B.LM size:" + num + " noPreTermNum:" + noPreTermNum);

	}

	/*
	 * To resolve the problem the sum of the prob will not be 1 after some
	 * smoothing technology, we use normalize.
	 * 
	 * Because the function traverseFgLM() and traverseBgLM() maybe ignore some
	 * term, I need traverse the bgProbMap again.
	 */
	private void normalizedSmoothing(HashMap<String, HashMap<String, Double>> prefix2TermProbMap) {
		Set<String> prefixes = prefix2TermProbMap.keySet();
		Iterator<String> prefixIter = prefixes.iterator();
		while (prefixIter.hasNext()) {
			double sumProb = 0;
			String prefix = prefixIter.next();
			Set<String> terms = prefix2TermProbMap.get(prefix).keySet();
			Iterator<String> termIter = terms.iterator();
			while (termIter.hasNext()) {
				String term = termIter.next();
				sumProb += prefix2TermProbMap.get(prefix).get(term);
			}

			termIter = terms.iterator();
			while (termIter.hasNext()) {
				String term = termIter.next();
				double prob = prefix2TermProbMap.get(prefix).get(term) / sumProb;
				updatePrefix2TermProbMap(prefix, term, prob, prefix2TermProbMap);
			}
		}
	}

	/***
	 * update background model by combining with a foreground model using a
	 * smoothing technique
	 * 
	 * @param fgPrefixCountMap
	 * @param fgPrefix2TermCountMap
	 * @param bgPrefix2TermProbMap
	 * @param smoothingType
	 */

	public void smoothing(HashMap<String, Integer> fgPrefixCountMap,
			HashMap<String, HashMap<String, Integer>> fgPrefix2TermCountMap,
			HashMap<String, HashMap<String, Double>> bgPrefix2TermProbMap, Configure.SmoothingType smoothingType) {

		int newTermCountInFg = 0;
		if (smoothingType == Configure.SmoothingType.ABSOLUTE_DISCOUNTING) {
			newTermCountInFg = countNNewTermsInFg(fgPrefix2TermCountMap, bgPrefix2TermProbMap);
		}

		iterateFgLM(fgPrefixCountMap, fgPrefix2TermCountMap, bgPrefix2TermProbMap, smoothingType, newTermCountInFg);

		if (smoothingType == Configure.SmoothingType.NO_SMOOTHING)
			return;
		iterateBgLM(fgPrefixCountMap, fgPrefix2TermCountMap, bgPrefix2TermProbMap, smoothingType, newTermCountInFg);

		normalizedSmoothing(bgPrefix2TermProbMap);
	}

	/***
	 * update background model by combining with a foreground model using a
	 * smoothing technique
	 * 
	 * @param bgLM
	 * @param fgLM
	 * @param smoothingType
	 */
	public void update(LanguageModel bgLM, LanguageModel fgLM, Configure.SmoothingType smoothingType) {
		HashMap<String, Integer> fgPrefixCountMap = fgLM.getPrefixCountMap();
		HashMap<String, HashMap<String, Integer>> fgPrefix2TermCountMap = fgLM.getPrefix2TermCountMap();
		HashMap<String, HashMap<String, Double>> bgPrefix2TermProbMap = bgLM.getPrefix2TermProbMap();
		smoothing(fgPrefixCountMap, fgPrefix2TermCountMap, bgPrefix2TermProbMap, smoothingType);

		bgLM.setTotalPrefixStartCount(bgLM.getTotalPrefixStartCount() + fgLM.getTotalPrefixStartCount());
		bgLM.setTotalPrefixEndCount(bgLM.getTotalPrefixEndCount() + fgLM.getTotalPrefixEndCount());

		HashMap<String, Integer> bgPrefixCountMap = bgLM.getPrefixCountMap();
		HashMap<String, HashMap<String, Integer>> bgPrefix2TermCountMap = bgLM.getPrefix2TermCountMap();

		for (Map.Entry<String, Integer> prefixPair : fgPrefixCountMap.entrySet()) {
			String prefix = prefixPair.getKey();
			int count = prefixPair.getValue();

			if (bgPrefixCountMap.containsKey(prefix)) {
				bgPrefixCountMap.put(prefix, bgPrefixCountMap.get(prefix) + count);
				HashMap<String, Integer> bgTermCount = bgPrefix2TermCountMap.get(prefix);
				for (Map.Entry<String, Integer> termPair : fgPrefix2TermCountMap.get(prefix).entrySet()) {
					String term = termPair.getKey();
					count = termPair.getValue();
					if (bgTermCount.containsKey(term)) {
						bgTermCount.put(term, count + bgTermCount.get(term));
					} else {
						bgTermCount.put(term, count);
					}
				}
			} else {
				bgPrefixCountMap.put(prefix, count);
				bgPrefix2TermCountMap.put(prefix, fgPrefix2TermCountMap.get(prefix));
			}

		}

		if (bgLM.nGram() == 1)
			return;

		HashMap<String, Integer> bgPrefixStartCount = bgLM.getPrefixStartCount();
		HashMap<String, Integer> fgPrefixStartCount = fgLM.getPrefixStartCount();

		for (Map.Entry<String, Integer> prefix : fgPrefixStartCount.entrySet()) {
			if (bgPrefixStartCount.containsKey(prefix.getKey())) {
				bgPrefixStartCount.put(prefix.getKey(), prefix.getValue() + bgPrefixStartCount.get(prefix.getKey()));
			} else {
				bgPrefixStartCount.put(prefix.getKey(), prefix.getValue());
			}
		}

		HashMap<String, Integer> bgPrefixEndCount = bgLM.getPrefixEndCount();
		HashMap<String, Integer> fgPrefixEndCount = fgLM.getPrefixEndCount();

		for (Map.Entry<String, Integer> prefix : fgPrefixEndCount.entrySet()) {
			if (bgPrefixEndCount.containsKey(prefix.getKey())) {
				bgPrefixEndCount.put(prefix.getKey(), prefix.getValue() + bgPrefixEndCount.get(prefix.getKey()));
			} else {
				bgPrefixEndCount.put(prefix.getKey(), prefix.getValue());
			}
		}

	}

}
