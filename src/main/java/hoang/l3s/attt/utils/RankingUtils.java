package hoang.l3s.attt.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import hoang.l3s.attt.model.graphbased.AdjacentTerm;

public class RankingUtils {
	public static List<Integer> getTopAdjTerms(int k, HashMap<Integer, AdjacentTerm> adjTerms) {
		PriorityBlockingQueue<IntKeyDoubleValue_Pair> queue = new PriorityBlockingQueue<IntKeyDoubleValue_Pair>();
		for (Map.Entry<Integer, AdjacentTerm> adjTermIter : adjTerms.entrySet()) {
			int j = adjTermIter.getKey();
			AdjacentTerm adjTerm = adjTermIter.getValue();
			if (queue.size() < k) {
				queue.add(new IntKeyDoubleValue_Pair(j, adjTerm.getWeight()));
			} else {
				IntKeyDoubleValue_Pair head = queue.peek();
				if (head.getValue() < adjTerm.getWeight()) {
					queue.poll();
					queue.add(new IntKeyDoubleValue_Pair(j, adjTerm.getWeight()));
				}
			}
		}

		List<Integer> topAdjTerms = new ArrayList<Integer>();
		while (!queue.isEmpty()) {
			topAdjTerms.add(topAdjTerms.size(), queue.poll().getKey());
		}
		return topAdjTerms;
	}

	public static void main(String[] args) {
		PriorityBlockingQueue<IntKeyDoubleValue_Pair> queue = new PriorityBlockingQueue<IntKeyDoubleValue_Pair>();
		queue.add(new IntKeyDoubleValue_Pair(1, 0.1));
		queue.add(new IntKeyDoubleValue_Pair(4, 0.4));
		queue.add(new IntKeyDoubleValue_Pair(2, 0.2));
		queue.add(new IntKeyDoubleValue_Pair(3, 0.3));
		queue.add(new IntKeyDoubleValue_Pair(7, 0.7));
		queue.add(new IntKeyDoubleValue_Pair(6, 0.6));
		queue.add(new IntKeyDoubleValue_Pair(5, 0.5));

		while (!queue.isEmpty()) {
			IntKeyDoubleValue_Pair pair = queue.poll();
			System.out.printf("key = %d value = %f\n", pair.getKey(), pair.getValue());
		}

	}
}
