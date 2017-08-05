package hoang.l3s.attt.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import hoang.l3s.attt.model.graphbased.AdjacentTerm;

public class RankingUtils {

	public static List<Integer> getTopAdjTerms(int k, HashMap<Integer, AdjacentTerm> adjTerms) {
		PriorityBlockingQueue<KeyValue_Pair> queue = new PriorityBlockingQueue<KeyValue_Pair>();
		for (Map.Entry<Integer, AdjacentTerm> adjTermIter : adjTerms.entrySet()) {
			int j = adjTermIter.getKey();
			AdjacentTerm adjTerm = adjTermIter.getValue();
			if (queue.size() < k) {
				queue.add(new KeyValue_Pair(j, adjTerm.getWeight()));
			} else {
				KeyValue_Pair head = queue.peek();
				if (head.getDoubleValue() < adjTerm.getWeight()) {
					queue.poll();
					queue.add(new KeyValue_Pair(j, adjTerm.getWeight()));
				}
			}
		}

		List<Integer> topAdjTerms = new ArrayList<Integer>();
		while (!queue.isEmpty()) {
			topAdjTerms.add(topAdjTerms.size(), queue.poll().getIntKey());
		}
		return topAdjTerms;
	}	

	public static List<Integer> getTopElements(int k, double[] wordDistributions) {
		PriorityBlockingQueue<KeyValue_Pair> queue = new PriorityBlockingQueue<KeyValue_Pair>();
		for (int i = 0; i < wordDistributions.length; i++) {
			if (queue.size() < k) {
				queue.add(new KeyValue_Pair(i, wordDistributions[i]));
			} else {
				KeyValue_Pair head = queue.peek();
				if (head.getDoubleValue() < wordDistributions[i]) {
					queue.poll();
					queue.add(new KeyValue_Pair(i, wordDistributions[i]));
				}
			}
		}

		List<Integer> topWords = new ArrayList<Integer>();
		while (!queue.isEmpty()) {
			topWords.add(topWords.size(), queue.poll().getIntKey());
		}
		return topWords;
	}

	public static void main(String[] args) {
		PriorityBlockingQueue<KeyValue_Pair> queue = new PriorityBlockingQueue<KeyValue_Pair>();
		queue.add(new KeyValue_Pair(1, 0.1));
		queue.add(new KeyValue_Pair(4, 0.4));
		queue.add(new KeyValue_Pair(2, 0.2));
		queue.add(new KeyValue_Pair(3, 0.3));
		queue.add(new KeyValue_Pair(7, 0.7));
		queue.add(new KeyValue_Pair(6, 0.6));
		queue.add(new KeyValue_Pair(5, 0.5));

		while (!queue.isEmpty()) {
			KeyValue_Pair pair = queue.poll();
			System.out.printf("key = %d value = %f\n", pair.getIntKey(), pair.getDoubleValue());
		}

	}
}
