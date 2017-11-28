package hoang.l3s.attt.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import hoang.l3s.attt.utils.DescriptiveStats;
import weka.estimators.NDConditionalEstimator;

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

	public static void evaluate() {
		List<String> startDate = new ArrayList<String>();
		// *******
		startDate.add("2017-01-27");
		startDate.add("2017-03-01");
		startDate.add("2017-04-01");
		// *******
		startDate.add("2017-01-28");
		startDate.add("2017-03-02");
		startDate.add("2017-04-02");
		// *******
		startDate.add("2017-01-29");
		startDate.add("2017-03-03");
		startDate.add("2017-04-03");
		// *******
		startDate.add("2017-01-30");
		startDate.add("2017-03-04");
		startDate.add("2017-04-04");
		// *******
		startDate.add("2017-01-31");
		startDate.add("2017-03-05");
		startDate.add("2017-04-05");

		List<String> datasets = new ArrayList<String>();
		List<Integer> nFirstTweets = new ArrayList<Integer>();
		// 3-day event
		datasets.add("2012_Sandy_Hurricane");
		nFirstTweets.add(50);
		// 5-day event
		datasets.add("2013_Boston_Bombings");
		nFirstTweets.add(75);
		// 6-day event
		datasets.add("2013_Queensland_Floods");
		nFirstTweets.add(100);
		// 11-day event
		datasets.add("2013_Alberta_Floods");
		nFirstTweets.add(200);
		// 11-day event
		datasets.add("2013_Oklahoma_Tornado");
		nFirstTweets.add(200);
		// 11-day event
		datasets.add("2013_West_Texas_Explosion");
		nFirstTweets.add(200);

		List<String> models = new ArrayList<String>();
		models.add("kw");
		models.add("ps");
		models.add("graph");

		for (int d = 0; d < 10; d++) {
			for (int i = 0; i < 2; i++) {
				String groundtruthFile = String.format(
						"E:/code/java/AdaptiveTopicTrackingTwitter/data/crisis/odered/%s-ontopic_offtopic.csv",
						datasets.get(i));
				for (int m = 0; m < models.size(); m++) {
					String filteredFile = String.format(
							"C:/Users/Tuan-Anh Hoang/Desktop/attt/%s_%s_%sFilteredTweets.txt", datasets.get(i),
							startDate.get(d), models.get(m));
					String outputFile = String.format("C:/Users/Tuan-Anh Hoang/Desktop/attt/%s_%s_%s_Perf.csv",
							datasets.get(i), startDate.get(d), models.get(m));
					getPrecisionRecall(groundtruthFile, nFirstTweets.get(i), filteredFile, outputFile);
				}
			}
		}
	}

	public static void summary() {
		List<String> startDate = new ArrayList<String>();
		// *******
		startDate.add("2017-01-27");
		startDate.add("2017-03-01");
		startDate.add("2017-04-01");
		// *******
		startDate.add("2017-01-28");
		startDate.add("2017-03-02");
		startDate.add("2017-04-02");
		// *******
		startDate.add("2017-01-29");
		startDate.add("2017-03-03");
		startDate.add("2017-04-03");
		// *******
		startDate.add("2017-01-30");
		startDate.add("2017-03-04");
		startDate.add("2017-04-04");
		// *******
		startDate.add("2017-01-31");
		startDate.add("2017-03-05");
		startDate.add("2017-04-05");

		List<String> datasets = new ArrayList<String>();
		List<Integer> nFirstTweets = new ArrayList<Integer>();
		// 3-day event
		datasets.add("2012_Sandy_Hurricane");
		nFirstTweets.add(50);
		// 5-day event
		datasets.add("2013_Boston_Bombings");
		nFirstTweets.add(75);
		// 6-day event
		datasets.add("2013_Queensland_Floods");
		nFirstTweets.add(100);
		// 11-day event
		datasets.add("2013_Alberta_Floods");
		nFirstTweets.add(200);
		// 11-day event
		datasets.add("2013_Oklahoma_Tornado");
		nFirstTweets.add(200);
		// 11-day event
		datasets.add("2013_West_Texas_Explosion");
		nFirstTweets.add(200);

		List<String> models = new ArrayList<String>();
		models.add("kw");
		models.add("ps");
		models.add("graph");

		int nDays = 10;

		try {
			for (int i = 0; i < 2; i++) {
				String summaryFile = String.format("C:/Users/Tuan-Anh Hoang/Desktop/attt/summary_%s_Perf.csv",
						datasets.get(i));
				BufferedWriter bw = new BufferedWriter(new FileWriter(summaryFile));
				bw.write("model,num_tweets,prec_mean,prec_se,rec_mean,rec_se,f1_mean,f1_se\n");

				for (int m = 0; m < models.size(); m++) {
					String perfFile = String.format("C:/Users/Tuan-Anh Hoang/Desktop/attt/%s_%s_%s_Perf.csv",
							datasets.get(0), startDate.get(0), models.get(m));
					int nTweets = 0;
					BufferedReader br = new BufferedReader(new FileReader(perfFile));
					while (br.readLine() != null) {
						nTweets++;
					}
					br.close();

					double[][] precision = new double[nTweets][nDays];
					double[][] recall = new double[nTweets][nDays];
					double[][] f1Score = new double[nTweets][nDays];
					for (int d = 0; d < nDays; d++) {
						perfFile = String.format("C:/Users/Tuan-Anh Hoang/Desktop/attt/%s_%s_%s_Perf.csv",
								datasets.get(i), startDate.get(d), models.get(m));
						br = new BufferedReader(new FileReader(perfFile));
						String line = null;
						nTweets = 0;
						while ((line = br.readLine()) != null) {
							String[] tokens = line.split(",");
							precision[nTweets][d] += Double.parseDouble(tokens[4]);
							recall[nTweets][d] += Double.parseDouble(tokens[5]);
							f1Score[nTweets][d] += Double.parseDouble(tokens[6]);
							nTweets++;
						}
					}

					for (int j = 0; j < nTweets; j++) {
						DescriptiveStats preStats = new DescriptiveStats();
						DescriptiveStats recStats = new DescriptiveStats();
						DescriptiveStats f1Stats = new DescriptiveStats();
						for (int d = 0; d < nDays; d++) {
							preStats.update(precision[j][d]);
							recStats.update(recall[j][d]);
							f1Stats.update(f1Score[j][d]);
						}
						bw.write(String.format("%s,%d,%f,%f,%f,%f,%f,%s\n", models.get(m), j, preStats.getMean(),
								preStats.getSqrtVariance(), recStats.getMean(), recStats.getSqrtVariance(),
								f1Stats.getMean(), f1Stats.getSqrtVariance()));
					}
				}
				bw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		// evaluate();
		// summary();

		String groundtruthFile = String.format(
				"E:/code/java/AdaptiveTopicTrackingTwitter/data/crisis/odered/2012_Sandy_Hurricane-ontopic_offtopic.csv");

		String filteredFile = String.format(
				"C:/Users/Tuan-Anh Hoang/Desktop/attt/kw_expansion/2012_Sandy_Hurricane_2017-01-27_kwFilteredTweets.txt");
		String outputFile = String
				.format("C:/Users/Tuan-Anh Hoang/Desktop/attt/kw_expansion/2012_Sandy_Hurricane_kwexpansion_Perf.csv");
		getPrecisionRecall(groundtruthFile, 50, filteredFile, outputFile);

	}
}
