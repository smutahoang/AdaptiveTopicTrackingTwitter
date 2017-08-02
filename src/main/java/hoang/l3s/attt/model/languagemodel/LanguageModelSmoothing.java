package hoang.l3s.attt.model.languagemodel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import hoang.l3s.attt.configure.Configure;

public class LanguageModelSmoothing {

	public enum SmoothingType {
		StupidBackoff, JelinekMercer, Bayesian, AbsoluteDiscounting, NoSmoothing
	}

	private static double alpha = 0.3;
	private static double lambda = 0.3;
	private static double delta = 0.9;
	private static double mu = 5000;
	
	public String formatPrintProb(int ngram,String preTerm, String term, double preCount, double count, double prob) {
		return String.format("n:%d,P(%s|%s) = %.0f|%.0f = %f",ngram,term,preTerm,count,preCount,prob);
	}

	public double getBgProb(String preTerm, String term, HashMap<String, HashMap<String, Double>> bgProbMap) {
		double bgProb = 0;
		if (bgProbMap.containsKey(preTerm) && bgProbMap.get(preTerm).containsKey(term)) {
			bgProb = bgProbMap.get(preTerm).get(term);
		}
		return bgProb;
	}

	public double caculateProb(double preTermCount, double termCount, double bgProb, double newTermInFgCount,
			SmoothingType type) {

		double prob = 0;
		switch (type) {
		case StupidBackoff:
			if (termCount != 0) {
				prob = (1 / (1 + alpha)) * (termCount / preTermCount);
			} else {
				prob = (alpha / (1 + alpha)) * bgProb;
			}
//			if (termCount != 0) {
//				prob = (termCount / preTermCount);
//			} else {
//				prob = alpha * bgProb;
//			}
			break;

		case JelinekMercer:
			prob = lambda * termCount / preTermCount + (1 - lambda) * bgProb;
			break;

		case Bayesian:
			prob = (termCount + mu * bgProb) / (preTermCount + mu);
			break;

		case AbsoluteDiscounting:
			prob = Math.max(termCount - delta, 0) / preTermCount + (delta * newTermInFgCount / preTermCount) * bgProb;
			break;
			
		case NoSmoothing:
			prob = termCount / preTermCount;
			
		default:
			break;
		}

		return prob;
	}

	public HashMap<String, HashMap<String, Double>> updateBgLM(String preTerm, String term, double prob,
			HashMap<String, HashMap<String, Double>> bgProbMap) {

		HashMap<String, Double> bgTermProbMap = null;
		if (bgProbMap.containsKey(preTerm)) {
			bgTermProbMap = bgProbMap.get(preTerm);
		} else {
			bgTermProbMap = new HashMap<String, Double>();
		}
		bgTermProbMap.put(term, prob);
		bgProbMap.put(preTerm, bgTermProbMap);
		return bgProbMap;
	}
	
	/*
	 * caculate the number of unique terms in the foreground model
	 */
	public int caculateNewTermCountInFg(HashMap<String, HashMap<String, Double>> fgnTermCountMap,
			HashMap<String, HashMap<String, Double>> bgProbMap) {
		int newTermCountInFg = 0;
		Set<String> preKeys = fgnTermCountMap.keySet();
		Iterator<String> iter = preKeys.iterator();
		while (iter.hasNext()) {
			String preTerm = iter.next();
			HashMap<String, Double> fgTermCount = fgnTermCountMap.get(preTerm);
			Set<String> keys = fgTermCount.keySet();
			Iterator<String> iter1 = keys.iterator();
			while (iter1.hasNext()) {
				String term = iter1.next();
				if (!(bgProbMap.containsKey(preTerm) && bgProbMap.get(preTerm).containsKey(term))) {
					newTermCountInFg++;
				}
			}
		}
		
		return newTermCountInFg;
	}


	/*
	 * traverse F.LM if "w" is in B.LM, update Prob(w| B.ML) else add it into
	 * Prob(w| B.ML)
	 */
	public void traverseFgLM(HashMap<String, Double> fgPreTermCountMap,
			HashMap<String, HashMap<String, Double>> fgnTermCountMap,
			HashMap<String, HashMap<String, Double>> bgProbMap, SmoothingType type,int newTermCountInFg) {

		int num = 0;
		Set<String> preKeys = fgnTermCountMap.keySet();
		Iterator<String> iter = preKeys.iterator();
		while (iter.hasNext()) {
			String preTerm = iter.next();
			HashMap<String, Double> termUnderPreTermCountMap = fgnTermCountMap.get(preTerm);
			Set<String> keys = termUnderPreTermCountMap.keySet();
			Iterator<String> iter1 = keys.iterator();
			while (iter1.hasNext()) {
				String term = iter1.next();


				double preTermCount = fgPreTermCountMap.get(preTerm);
				double termCount = fgnTermCountMap.get(preTerm).get(term);

				double bgProb = getBgProb(preTerm, term, bgProbMap);
				
				double prob = caculateProb(preTermCount, termCount, bgProb, newTermCountInFg, type);
				bgProbMap = updateBgLM(preTerm, term, prob, bgProbMap);
				System.out.println("find in F.LM:" + formatPrintProb(Configure.nGram,preTerm,term,preTermCount,termCount,prob) + " with bgProb:" + bgProb);
				num++;
			}
		}
		System.out.println("F.LM size:" + num);
	}
	
	/*
	 * traverse B.LM if "w" is in F.LM, do nothing, because we have processed when
	 * traversing F.LM if "w" is not in F.LM, modify the Prob(w| B.ML)
	 */
	public void traverseBgLM(HashMap<String, Double> fgPreTermCountMap,
			HashMap<String, HashMap<String, Double>> fgnTermCountMap,
			HashMap<String, HashMap<String, Double>> bgProbMap, SmoothingType type,int newTermCountInFg) {
		int num = 0;
		int noPreTermNum = 0;
		Set<String> preKeys = bgProbMap.keySet();
		Iterator<String> iter = preKeys.iterator();
		while (iter.hasNext()) {
			String preTerm = iter.next();
			HashMap<String, Double> bgPreTermProbMap = bgProbMap.get(preTerm);
			Set<String> keys = bgPreTermProbMap.keySet();
			Iterator<String> iter1 = keys.iterator();
			while (iter1.hasNext()) {
				String term = iter1.next();

				double termCount = 0;
				double preTermCount = 0;
				if (fgnTermCountMap.containsKey(preTerm) && fgnTermCountMap.get(preTerm).containsKey(term)) {
					continue;
				} else if (!fgnTermCountMap.containsKey(preTerm)) {
					noPreTermNum ++;
					continue;
				} 
				else {
					preTermCount = fgPreTermCountMap.get(preTerm);
					termCount = 0;
				}

				double bgProb = bgProbMap.get(preTerm).get(term);
				
				double prob = caculateProb(preTermCount, termCount, bgProb, newTermCountInFg, type);
				bgProbMap = updateBgLM(preTerm, term, prob, bgProbMap);
				System.out.println("find in B.LM:" + formatPrintProb(Configure.nGram,preTerm,term,preTermCount,termCount,prob) + " with bgProb:" + bgProb);
				num++;
			}
		}
		System.out.println("B.LM size:" + num + " noPreTermNum:" + noPreTermNum);
	}

	public void smoothing(HashMap<String, Double> fgPreTermCountMap,
			HashMap<String, HashMap<String, Double>> fgnTermCountMap,
			HashMap<String, HashMap<String, Double>> bgProbMap, SmoothingType type) {

		int newTermCountInFg = 0;
		if (type == SmoothingType.AbsoluteDiscounting) {
			newTermCountInFg = caculateNewTermCountInFg(fgnTermCountMap,bgProbMap);
		}

		traverseFgLM(fgPreTermCountMap,fgnTermCountMap,bgProbMap,type,newTermCountInFg);
		
		if(type == SmoothingType.NoSmoothing)
			return;
		traverseBgLM(fgPreTermCountMap,fgnTermCountMap,bgProbMap,type,newTermCountInFg);
	}
}