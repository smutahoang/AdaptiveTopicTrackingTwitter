package hoang.l3s.attt.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

import hoang.l3s.attt.utils.RankingUtils;
import hoang.l3s.attt.utils.SimilarityUtils;

public class ManualJudgementPreparation {
	static double threshold = 0.9;
	static int nCandidates = 3;

	static void getRelevantJudgmentFile(String[] models, int[] nTopics) {
		try {
			for (int m = 0; m < models.length; m++) {
				for (int i = 0; i < nTopics.length; i++) {

					System.out.printf("model = %s #topics = %d\n", models[m], nTopics[i]);
					String filename = String.format(
							"C:/Users/Tuan-Anh Hoang/Desktop/attt/ua/%s/%d/REL_CON_JUDGEMENT_%s_%d.csv",
							models[m], nTopics[i], models[m], nTopics[i]);
					BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
					bw.write("Topic,Relevance,Concentration\n");
					for (int z = 0; z < nTopics[i]; z++) {
						// topic
						bw.write(String.format("%d,,\n", z));
					}
					bw.close();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	static int[][] getCloseTopics(String srcModel, String desModel, int nTopics) {
		try {
			String filename = String.format("C:/Users/Tuan-Anh Hoang/Desktop/attt/travel_ban/%s/%d/words.csv", srcModel,
					nTopics);
			HashMap<Integer, String> srcWords = new HashMap<Integer, String>();
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				srcWords.put(Integer.parseInt(tokens[0]), tokens[1]);
			}
			br.close();

			//

			filename = String.format("C:/Users/Tuan-Anh Hoang/Desktop/attt/travel_ban/%s/%d/words.csv", desModel,
					nTopics);
			HashMap<Integer, String> desWords = new HashMap<Integer, String>();
			br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				desWords.put(Integer.parseInt(tokens[0]), tokens[1]);
			}
			br.close();

			//

			double[][] srcTopics = new double[nTopics][srcWords.size()];
			filename = String.format("C:/Users/Tuan-Anh Hoang/Desktop/attt/travel_ban/%s/%d/topicWordDistributions.csv",
					srcModel, nTopics);
			br = new BufferedReader(new FileReader(filename));
			for (int z = 0; z < nTopics; z++) {
				String[] tokens = br.readLine().split(",");
				for (int i = 1; i < tokens.length; i++) {
					srcTopics[z][i - 1] = Double.parseDouble(tokens[i]);
				}
			}
			br.close();

			//

			double[][] desTopics = new double[nTopics][desWords.size()];
			filename = String.format("C:/Users/Tuan-Anh Hoang/Desktop/attt/travel_ban/%s/%d/topicWordDistributions.csv",
					desModel, nTopics);
			br = new BufferedReader(new FileReader(filename));
			for (int z = 0; z < nTopics; z++) {
				String[] tokens = br.readLine().split(",");
				for (int i = 1; i < tokens.length; i++) {
					desTopics[z][i - 1] = Double.parseDouble(tokens[i]);
				}
			}
			br.close();

			//

			HashMap<Integer, HashMap<String, Double>> srcTopicTopWords = new HashMap<Integer, HashMap<String, Double>>();
			for (int z = 0; z < nTopics; z++) {
				List<Integer> topIndexes = RankingUtils.getIndexTopElements(srcTopics[z], threshold);
				HashMap<String, Double> topWords = new HashMap<String, Double>();
				for (int i = 0; i < topIndexes.size(); i++) {
					int j = topIndexes.get(i);
					topWords.put(srcWords.get(j), srcTopics[z][j] / threshold);
				}
				srcTopicTopWords.put(z, topWords);
			}

			//

			HashMap<Integer, HashMap<String, Double>> desTopicTopWords = new HashMap<Integer, HashMap<String, Double>>();
			for (int z = 0; z < nTopics; z++) {
				List<Integer> topIndexes = RankingUtils.getIndexTopElements(desTopics[z], threshold);
				HashMap<String, Double> topWords = new HashMap<String, Double>();
				for (int i = 0; i < topIndexes.size(); i++) {
					int j = topIndexes.get(i);
					topWords.put(desWords.get(j), desTopics[z][j] / threshold);
				}
				desTopicTopWords.put(z, topWords);
			}

			/*
			 * System.out.printf("model = %s #words = %d\n", srcModel,
			 * srcTopicTopWords.get(0).size()); for (Map.Entry<String, Double>
			 * word : srcTopicTopWords.get(0).entrySet()) { System.out.printf(
			 * "model = %s word = %s prob = %f\n", srcModel, word.getKey(),
			 * word.getValue()); } System.out.printf("model = %s #words = %d\n",
			 * desModel, desTopicTopWords.get(0).size()); for (Map.Entry<String,
			 * Double> word : desTopicTopWords.get(0).entrySet()) {
			 * System.out.printf("model = %s word = %s prob = %f\n", desModel,
			 * word.getKey(), word.getValue()); }
			 */

			//
			int[][] closeTopics = new int[nTopics][nCandidates];
			for (int z = 0; z < nTopics; z++) {
				double[] distances = new double[nTopics];
				for (int k = 0; k < nTopics; k++) {
					distances[k] = -SimilarityUtils.jsDistance(srcTopicTopWords.get(z), desTopicTopWords.get(k));
				}
				List<Integer> closeDesTopics = RankingUtils.getIndexTopElements(nCandidates, distances);
				System.out.printf("%s <-- %s:", srcModel, desModel);
				for (int j = nCandidates - 1; j >= 0; j--) {
					closeTopics[z][j] = closeDesTopics.get(j);
					System.out.printf("\t%d (%f)", closeTopics[z][j], distances[closeTopics[z][j]]);
				}
				System.out.println();
			}
			return closeTopics;

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	static void getJudgmentFile(String[] models, int[] nTopics) {
		try {
			for (int m = 0; m < models.length; m++) {
				for (int i = 0; i < nTopics.length; i++) {

					System.out.printf("model = %s #topics = %d\n", models[m], nTopics[i]);

					int[][][] closeTopics = new int[models.length][][];
					for (int g = 0; g < models.length; g++) {
						if (g == m) {
							continue;
						}
						closeTopics[g] = getCloseTopics(models[m], models[g], nTopics[i]);
					}
					String filename = String.format(
							"C:/Users/Tuan-Anh Hoang/Desktop/attt/travel_ban/%s/%d/MANUAL_JUDGEMENT_%s_%d.csv",
							models[m], nTopics[i], models[m], nTopics[i]);
					BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
					bw.write("Topic,Relevant,<*****>");
					for (int g = 0; g < models.length; g++) {
						if (g == m) {
							continue;
						}
						for (int j = 0; j < closeTopics[g][0].length; j++) {
							bw.write(String.format(",%s-%d,COVERAGE", models[g], j));
						}
						bw.write(",<*****>");
					}
					bw.write("\n");
					for (int z = 0; z < nTopics[i]; z++) {
						// topic
						bw.write(String.format("%d", z));
						// relevant
						bw.write(",,<*****>");
						for (int g = 0; g < models.length; g++) {
							if (g == m) {
								continue;
							}
							for (int j = 0; j < closeTopics[g][z].length; j++) {
								bw.write(String.format(",%d,", closeTopics[g][z][j]));
							}
							bw.write(",<*****>");
						}
						bw.write("\n");
					}
					bw.close();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		String[] models = new String[] { "kw", "ps", "graph" };
		int[] nTopics = new int[] { 10, 20, 30 };
		getRelevantJudgmentFile(models, nTopics);
		// getJudgmentFile(models, nTopics);
		// getCloseTopics("ps", "graph", 10);
	}
}
