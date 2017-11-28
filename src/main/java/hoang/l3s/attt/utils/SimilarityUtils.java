package hoang.l3s.attt.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import hoang.l3s.attt.evaluation.Tweet;

public class SimilarityUtils {
	public static double getTweetSimilarity(Tweet tweetA, Tweet tweetB, double[] weights) {
		HashSet<Integer> A_terms = new HashSet<Integer>();
		double denominator = 0;
		for (int i = 0; i < tweetA.words.length; i++) {
			if (A_terms.contains(tweetA.words[i]))
				continue;
			A_terms.add(tweetA.words[i]);
			denominator += weights[tweetA.words[i]];
		}

		HashSet<Integer> B_terms = new HashSet<Integer>();
		double numerator = 0;
		for (int i = 0; i < tweetB.words.length; i++) {
			if (B_terms.contains(tweetB.words[i]))
				continue;
			B_terms.add(tweetB.words[i]);
			if (A_terms.add(tweetB.words[i])) {
				numerator += weights[tweetB.words[i]];
			} else {
				denominator += weights[tweetB.words[i]];
			}
		}

		return numerator / denominator;
	}

	public static double cosineSimilarity(HashMap<Integer, Double> p, HashMap<Integer, Double> q) {
		double pnorm = 0;
		double qnorm = 0;
		double product = 0;
		for (Map.Entry<Integer, Double> pair : p.entrySet()) {
			int j = pair.getKey();
			double w = pair.getValue();
			pnorm += w * w;
			if (q.containsKey(j)) {
				product += w * q.get(j);
			}
		}
		for (Map.Entry<Integer, Double> pair : q.entrySet()) {
			double w = pair.getValue();
			qnorm += w * w;
		}
		return product / Math.sqrt(pnorm * qnorm);
	}
}
