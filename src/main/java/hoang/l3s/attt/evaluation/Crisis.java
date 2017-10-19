package hoang.l3s.attt.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

public class Crisis {
	public static void getPrecisionRecall(String groundTruthFile, int nDescriptionTweets, String filteredFile,
			String outputFile) {
		try {

			HashSet<String> relevantTweets = new HashSet<String>();
			BufferedReader br = new BufferedReader(new FileReader(groundTruthFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				if (!tokens[1].equals("on-topic")) {
					continue;
				}
				if (relevantTweets.contains(tokens[0])) {
					System.out.printf("groundtruth: replicated tweet: %s\n", tokens[0]);
				}
				relevantTweets.add(tokens[0]);
			}
			br.close();

			long[] relevantTweetIds = new long[relevantTweets.size() - nDescriptionTweets + 1];
			relevantTweets = new HashSet<String>();
			br = new BufferedReader(new FileReader(groundTruthFile));
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				if (!tokens[1].equals("on-topic")) {
					continue;
				}
				if (relevantTweets.contains(tokens[0])) {
					continue;
				}
				if (relevantTweets.size() >= nDescriptionTweets) {
					relevantTweetIds[relevantTweets.size() - nDescriptionTweets] = Long.parseLong(tokens[0]);
				}
				relevantTweets.add(tokens[0]);

			}
			br.close();

			relevantTweetIds[relevantTweetIds.length - 1] = Long.MAX_VALUE;

			int[] nFilteredTweets = new int[relevantTweetIds.length];
			int[] nTrulyRelevantTweets = new int[relevantTweetIds.length];
			for (int i = 0; i < relevantTweetIds.length; i++) {
				nFilteredTweets[i] = 0;
				nTrulyRelevantTweets[i] = 0;
			}

			System.out.printf("relevantTweetIds.size = %d\n", relevantTweetIds.length);

			br = new BufferedReader(new FileReader(filteredFile));
			int i = 0;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				String tweetId = tokens[0];
				long alignedTweetId = Long.parseLong(tokens[1]);
				while (alignedTweetId > relevantTweetIds[i]) {
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
		getPrecisionRecall(
				"E:/code/java/AdaptiveTopicTrackingTwitter/data/crisis/odered/2012_Sandy_Hurricane-ontopic_offtopic.csv",
				50, "C:/Users/Tuan-Anh Hoang/Desktop/attt/2012_Sandy_Hurricane_psFilteredTweets.txt",
				"C:/Users/Tuan-Anh Hoang/Desktop/attt/2012_Sandy_Hurricane_psPerf.csv");
	}
}
