package hoang.l3s.attt.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class HumanEvaluationSummary {
	static void getPrecisionTweetLevel(String pathManualResults, String pathTweetTopic, String model, int nTopics,
			double threshold) {
		try {
			String filename = String.format("%s/REL_CON_JUDGEMENT_%s_%d.csv", pathManualResults, model, nTopics);
			HashSet<Integer> relevantTopics = new HashSet<Integer>();
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				if (tokens[1].equals("1")) {
					relevantTopics.add(Integer.parseInt(tokens[0]));
				}
			}
			br.close();

			filename = String.format("%s/%s/%d/tweetTopic.csv", pathTweetTopic, model, nTopics);
			br = new BufferedReader(new FileReader(filename));
			int nTweets = 0;
			int nRelevantTweets = 0;
			while ((line = br.readLine()) != null) {
				nTweets++;
				String[] tokens = line.split("\t");
				int topic = Integer.parseInt(tokens[2]);
				if (!relevantTopics.contains(topic)) {
					continue;
				}
				double prob = Double.parseDouble(tokens[4]);
				if (prob < threshold) {
					continue;
				}
				nRelevantTweets++;
			}
			br.close();
			double prec = ((double) nRelevantTweets) / nTweets;
			// System.out.printf("model = %s nTopics = %d threshold = %f prec =
			// %f\n", model, nTopics, threshold, prec);
			System.out.printf("model = %s nTopics = %d #tweets = %d #relevantTweets = %d prec = %f\n", model, nTopics,
					nTweets, nRelevantTweets, prec);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	static double getCoverage(HashSet<String> p, HashSet<String> q) {
		int nCoveredTweets = 0;
		for (String tweet : q) {
			if (p.contains(tweet))
				nCoveredTweets++;
		}
		return ((double) nCoveredTweets) / q.size();
	}

	static void getPairwiseRecallTweetLevel(String pathManualResults, String pathTweetTopic, String srcModel,
			String desModel, int nTopics, double threshold) {
		try {

			HashMap<Integer, HashSet<String>> srcTweets = new HashMap<Integer, HashSet<String>>();
			String filename = String.format("%s/REL_CON_JUDGEMENT_%s_%d.csv", pathManualResults, srcModel, nTopics);
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				if (tokens[1].equals("1")) {
					srcTweets.put(Integer.parseInt(tokens[0]), new HashSet<String>());
				}
			}
			br.close();

			filename = String.format("%s/%s/%d/tweetTopic.csv", pathTweetTopic, srcModel, nTopics);
			br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				int topic = Integer.parseInt(tokens[2]);
				if (!srcTweets.containsKey(topic)) {
					continue;
				}
				double prob = Double.parseDouble(tokens[4]);
				if (prob < threshold) {
					continue;
				}
				srcTweets.get(topic).add(tokens[1]);
			}
			br.close();

			HashMap<Integer, HashSet<String>> desTweets = new HashMap<Integer, HashSet<String>>();
			filename = String.format("%s/REL_CON_JUDGEMENT_%s_%d.csv", pathManualResults, desModel, nTopics);
			br = new BufferedReader(new FileReader(filename));
			line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				if (tokens[1].equals("1")) {
					desTweets.put(Integer.parseInt(tokens[0]), new HashSet<String>());
				}
			}
			br.close();

			filename = String.format("%s/%s/%d/tweetTopic.csv", pathTweetTopic, desModel, nTopics);
			br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				int topic = Integer.parseInt(tokens[2]);
				if (!desTweets.containsKey(topic)) {
					continue;
				}
				double prob = Double.parseDouble(tokens[4]);
				if (prob < threshold) {
					continue;
				}
				desTweets.get(topic).add(tokens[1]);
			}
			br.close();
			for (Map.Entry<Integer, HashSet<String>> desIter : desTweets.entrySet()) {
				int desTopic = desIter.getKey();
				double sum = 0;
				for (Map.Entry<Integer, HashSet<String>> srcIter : srcTweets.entrySet()) {
					int srcTopic = srcIter.getKey();
					double c = getCoverage(srcIter.getValue(), desIter.getValue());
					sum += c;
					System.out.printf("%d %d %f\n", desTopic, srcTopic, c);
				}
				System.out.printf("desModel = %s topic = %d coverage = %f #desTweets = %d\n", desModel, desTopic, sum,
						desIter.getValue().size());
				System.out.println("****************************");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	static void getGroundTruth(String pathManualResults, String pathTweetTopic, String[] models, int nTopics,
			double threshold) {
		try {

			HashSet<String> relevantTweets = new HashSet<String>();
			for (int m = 0; m < models.length; m++) {

				HashSet<Integer> relevantTopics = new HashSet<Integer>();
				String filename = String.format("%s/REL_CON_JUDGEMENT_%s_%d.csv", pathManualResults, models[m],
						nTopics);
				BufferedReader br = new BufferedReader(new FileReader(filename));
				String line = br.readLine();
				while ((line = br.readLine()) != null) {
					String[] tokens = line.split(",");
					if (tokens[1].equals("1")) {
						relevantTopics.add(Integer.parseInt(tokens[0]));
					}
				}
				br.close();

				filename = String.format("%s/%s/%d/tweetTopic.csv", pathTweetTopic, models[m], nTopics);
				br = new BufferedReader(new FileReader(filename));
				while ((line = br.readLine()) != null) {
					String[] tokens = line.split("\t");
					int topic = Integer.parseInt(tokens[2]);
					if (!relevantTopics.contains(topic)) {
						continue;
					}
					double prob = Double.parseDouble(tokens[4]);
					if (prob < threshold) {
						continue;
					}
					relevantTweets.add(tokens[1]);
				}
				br.close();
			}

			long[] tweetIds = new long[relevantTweets.size()];
			int i = 0;
			for (String tweetId : relevantTweets) {
				tweetIds[i] = Long.parseLong(tweetId);
				i++;
			}

			Arrays.sort(tweetIds);
			String filename = String.format("%s/groundTruth_%d_%.2f.csv", pathManualResults, nTopics, threshold);
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (i = 0; i < tweetIds.length; i++) {
				bw.write(String.format("%d\n", tweetIds[i]));
			}
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void getPrecisionRecall(String groundTruthFile, String filteredFile, String outputFile) {
		try {

			HashSet<String> relevantTweets = new HashSet<String>();
			BufferedReader br = new BufferedReader(new FileReader(groundTruthFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				relevantTweets.add(line);
			}
			br.close();

			long[] relevantTweetIds = new long[relevantTweets.size() + 1];
			int i = 0;
			for (String tweetId : relevantTweets) {
				relevantTweetIds[i] = Long.parseLong(tweetId);
				i++;
			}

			relevantTweetIds[relevantTweetIds.length - 1] = Long.MAX_VALUE;
			Arrays.sort(relevantTweetIds);

			int[] nFilteredTweets = new int[relevantTweetIds.length];
			int[] nTrulyRelevantTweets = new int[relevantTweetIds.length];
			for (i = 0; i < relevantTweetIds.length; i++) {
				nFilteredTweets[i] = 0;
				nTrulyRelevantTweets[i] = 0;
			}

			System.out.printf("relevantTweetIds.size = %d\n", relevantTweetIds.length);

			br = new BufferedReader(new FileReader(filteredFile));
			i = 0;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				String tweetId = tokens[0];
				while (Long.parseLong(tokens[0]) > relevantTweetIds[i]) {
					nFilteredTweets[i + 1] = nFilteredTweets[i];
					nTrulyRelevantTweets[i + 1] = nTrulyRelevantTweets[i];
					i++;
				}
				nFilteredTweets[i]++;
				if (relevantTweets.contains(tweetId)) {
					nTrulyRelevantTweets[i]++;
				}
			}
			br.close();

			while (i < relevantTweetIds.length - 1) {
				nFilteredTweets[i + 1] = nFilteredTweets[i];
				nTrulyRelevantTweets[i + 1] = nTrulyRelevantTweets[i];
				i++;
			}

			// System.exit(-1);
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			for (i = 0; i < relevantTweetIds.length; i++) {
				double precision = ((double) nTrulyRelevantTweets[i]) / nFilteredTweets[i];
				if (nFilteredTweets[i] == 0) {
					precision = 0;
				}
				double recall = ((double) nTrulyRelevantTweets[i]) / (i + 1);
				double f1 = 2 * precision * recall / (precision + recall);
				if (nTrulyRelevantTweets[i] == 0) {
					f1 = 0;
				}
				bw.write(String.format("%d,%d,%d,%d,%f,%f,%f\n", i + 1, relevantTweetIds[i], nFilteredTweets[i],
						nTrulyRelevantTweets[i], precision, recall, f1));
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public static void main(String[] args) {
		String[] models = new String[] { "kw", "ps", "graph" };
		int[] nTopics = new int[] { 10, 20, 30 };
		String pathManualResults = "C:/Users/Tuan-Anh Hoang/Desktop/attt/london_attack/manual/RelCon/Tuan-Anh";
		String pathTweetTopic = "C:/Users/Tuan-Anh Hoang/Desktop/attt/london_attack";

		double threshold = 0.9;
		/*
		 * for (int i = 0; i < 1; i++) { for (int m = 0; m < 3; m++) {
		 * getPrecisionTweetLevel(pathManualResults, pathTweetTopic, models[m],
		 * nTopics[i], threshold); } }
		 */

		String srcModel = "graph";
		String desModel = "ps";
		// getPairwiseRecallTweetLevel(pathManualResults, pathTweetTopic,
		// srcModel, desModel, 30, threshold);

		// getGroundTruth(pathManualResults, pathTweetTopic, models, 30,
		// threshold);
		String groundTruthFile = "C:/Users/Tuan-Anh Hoang/Desktop/attt/london_attack/manual/RelCon/Tuan-Anh/groundTruth_30_0.90.csv";
		String filteredFile = "C:/Users/Tuan-Anh Hoang/Desktop/attt/london_attack/londonAttack_2017-03-22_kwFilteredTweets.txt";
		String outputFile = "C:/Users/Tuan-Anh Hoang/Desktop/attt/london_attack/kwPerf.csv";
		getPrecisionRecall(groundTruthFile, filteredFile, outputFile);
	}
}
