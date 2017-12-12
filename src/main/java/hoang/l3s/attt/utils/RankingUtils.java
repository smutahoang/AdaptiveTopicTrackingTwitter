package hoang.l3s.attt.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import hoang.l3s.attt.utils.graph.AdjacentTerm;

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

	public static HashSet<String> getTopKFrequentTerms(int k, HashMap<String, Integer> tfTermsMap) {
		PriorityBlockingQueue<KeyValue_Pair> queue = new PriorityBlockingQueue<KeyValue_Pair>();
		for (Map.Entry<String, Integer> tfIdfTermsIter : tfTermsMap.entrySet()) {
			String term = tfIdfTermsIter.getKey();
			int tfIdfTerm = tfIdfTermsIter.getValue();
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

	public static List<Integer> getIndexTopElements(double[] array, double threshold) {
		PriorityBlockingQueue<KeyValue_Pair> queue = new PriorityBlockingQueue<KeyValue_Pair>();
		for (int i = 0; i < array.length; i++) {
			queue.add(new KeyValue_Pair(i, array[i]));
		}

		KeyValue_Pair[] sortedArray = new KeyValue_Pair[array.length];
		int i = 0;
		while (!queue.isEmpty()) {
			sortedArray[i] = queue.poll();
			i++;
		}
		i = i - 1;
		double s = 0;
		List<Integer> topWords = new ArrayList<Integer>();
		while (s < threshold) {
			topWords.add(sortedArray[i].getIntKey());
			s += sortedArray[i].getDoubleValue();
			i--;
			if (i < 0)
				break;
		}
		return topWords;
	}

	public static void main(String[] args) {
		/*
		 * PriorityBlockingQueue<KeyValue_Pair> queue = new
		 * PriorityBlockingQueue<KeyValue_Pair>(); queue.add(new
		 * KeyValue_Pair(3, 0.3)); queue.add(new KeyValue_Pair(7, 0.7));
		 * queue.add(new KeyValue_Pair(6, 6)); queue.add(new KeyValue_Pair(5,
		 * 5)); queue.add(new KeyValue_Pair(1, 1)); queue.add(new
		 * KeyValue_Pair(4, 4)); queue.add(new KeyValue_Pair(2, 2));
		 * 
		 * while (!queue.isEmpty()) { KeyValue_Pair pair = queue.poll();
		 * System.out.printf("key = %d value = %f\n", pair.getIntKey(),
		 * pair.getDoubleValue()); }
		 */

		List<Double> a = new ArrayList<Double>();
		a.add(5.0);
		a.add(6.0);
		a.add(2.0);
		a.add(1.0);
		List<Integer> topIndexes = getIndexTopElements(3, a);
		for (int i = 0; i < topIndexes.size(); i++) {
			System.out.printf("index = %d value = %f\n", topIndexes.get(i), a.get(topIndexes.get(i)));
		}

		double[] b = new double[] { 0.2, 0.6, 0.1, 0.05, 0.05 };
		System.out.println("before sorting");
		for (int i = 0; i < b.length; i++) {
			System.out.printf("b[%d] = %f\n", i, b[i]);
		}
		System.out.println("after sorting");
		Arrays.sort(b);
		for (int i = 0; i < b.length; i++) {
			System.out.printf("b[%d] = %f\n", i, b[i]);
		}
	}
}
