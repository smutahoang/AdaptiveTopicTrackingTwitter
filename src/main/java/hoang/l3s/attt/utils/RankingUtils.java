package hoang.l3s.attt.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

	public static HashSet<String> getTopKTfIdfTerms(int k, HashMap<String, Double> tfIdfTermsMap) {
		PriorityBlockingQueue<KeyValue_Pair> queue = new PriorityBlockingQueue<KeyValue_Pair>();
		for (Map.Entry<String, Double> tfIdfTermsIter : tfIdfTermsMap.entrySet()) {
			String term = tfIdfTermsIter.getKey();
			Double tfIdfTerm = tfIdfTermsIter.getValue();
			if (queue.size() < k) {
				queue.add(new KeyValue_Pair(term, tfIdfTerm));
			} else {
				KeyValue_Pair head = queue.peek();
				if (head.getDoubleValue() < tfIdfTerm) {
					queue.poll();
					queue.add(new KeyValue_Pair(term, tfIdfTerm));
				}
			}
		}

		HashSet<String> topTfIdfTerms = new HashSet<String>();
		while (!queue.isEmpty()) {
			topTfIdfTerms.add(queue.poll().getStrKey());
		}
		return topTfIdfTerms;
	}

	public static List<Integer> getIndexTopElements(int k, double[] array) {
		PriorityBlockingQueue<KeyValue_Pair> queue = new PriorityBlockingQueue<KeyValue_Pair>();
		for (int i = 0; i < array.length; i++) {
			if (queue.size() < k) {
				queue.add(new KeyValue_Pair(i, array[i]));
			} else {
				KeyValue_Pair head = queue.peek();
				if (head.getDoubleValue() < array[i]) {
					queue.poll();
					queue.add(new KeyValue_Pair(i, array[i]));
				}
			}
		}

		List<Integer> topWords = new ArrayList<Integer>();
		while (!queue.isEmpty()) {
			topWords.add(topWords.size(), queue.poll().getIntKey());
		}
		return topWords;
	}

	public static List<Integer> getIndexTopElements(int k, List<Double> array) {
		PriorityBlockingQueue<KeyValue_Pair> queue = new PriorityBlockingQueue<KeyValue_Pair>();
		for (int i = 0; i < array.size(); i++) {
			if (queue.size() < k) {
				queue.add(new KeyValue_Pair(i, array.get(i)));
			} else {
				KeyValue_Pair head = queue.peek();
				if (head.getDoubleValue() < array.get(i)) {
					queue.poll();
					queue.add(new KeyValue_Pair(i, array.get(i)));
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
		queue.add(new KeyValue_Pair(3, 3));
		queue.add(new KeyValue_Pair(7, 7));
		queue.add(new KeyValue_Pair(6, 6));
		queue.add(new KeyValue_Pair(5, 5));
		queue.add(new KeyValue_Pair(1, 1));
		queue.add(new KeyValue_Pair(4, 4));
		queue.add(new KeyValue_Pair(2, 2));

		while (!queue.isEmpty()) {
			KeyValue_Pair pair = queue.poll();
			System.out.printf("key = %d value = %f\n", pair.getIntKey(), pair.getDoubleValue());
		}

	}
}
