package hoang.l3s.attt.evaluation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import hoang.l3s.attt.utils.StatTool;

/***
 * generate synthetic dataset following TwitterHDP model
 * 
 * @author Tuan-Anh Hoang
 *
 */
public class SyntheticDataGeneration {
	private double mass = 0.9;
	private double topicSkewness = 0.01;// each topic focuses on 1% of words
										// whose probabilities summing up to 90%
	private double documentSkewness = 0.1;

	private int minNWords = 100;
	private int maxNWords = 200;

	private StatTool statTool = new StatTool();

	private double alpha = 1;
	private double gamma = 1;
	private double beta = 1;

	private Random rand = new Random(System.currentTimeMillis());
	private int[][] empiricalTopicWords;
	private int[] sumEmpiricalTopicWords;

	private double[] genTopics(int nWords) {
		return statTool.sampleDirichletSkew(beta, nWords, topicSkewness, mass, rand);
	}

	public void genCRPDocuments(int nDocs, int nWords, String outputPath) {
		try {

			empiricalTopicWords = new int[100][];
			sumEmpiricalTopicWords = new int[100];

			BufferedWriter bw_log = new BufferedWriter(new FileWriter(String.format("%s/logs.csv", outputPath)));
			BufferedWriter bw_data = new BufferedWriter(new FileWriter(String.format("%s/data", outputPath)));
			BufferedWriter bw_docTopicCount = new BufferedWriter(
					new FileWriter(String.format("%s/docTopicDistributions.csv", outputPath)));
			BufferedWriter bw_docTableCount = new BufferedWriter(
					new FileWriter(String.format("%s/docTableCounts.csv", outputPath)));

			int[] topicTableCounts = new int[100];
			for (int z = 0; z < topicTableCounts.length; z++) {
				topicTableCounts[z] = 0;
			}
			int[] tableWordCounts = new int[100];

			int nTopics = 0;
			int nTables = 0;

			int[] tableToTopic = new int[100];
			double[][] topicWordDistributions = new double[100][];

			for (int d = 0; d < nDocs; d++) {
				HashMap<Integer, Integer> words = new HashMap<Integer, Integer>();
				for (int t = 0; t < tableWordCounts.length; t++) {
					tableWordCounts[t] = 0;
				}
				for (int t = 0; t < tableToTopic.length; t++) {
					tableToTopic[t] = -1;
				}

				int ndwords = rand.nextInt(maxNWords - minNWords) + minNWords;
				int ndTables = 0;

				for (int i = 0; i < ndwords; i++) {
					int t = statTool.sampleCRP(tableWordCounts, ndTables, i, alpha, rand);
					tableWordCounts[t]++;
					if (t == ndTables) {// new table
						ndTables++;
						int z = statTool.sampleCRP(topicTableCounts, nTopics, nTables, gamma, rand);
						nTables++;
						if (z == nTopics) {// new topic
							nTopics++;
							topicWordDistributions[z] = genTopics(nWords);
							empiricalTopicWords[z] = new int[nWords];
							for (int w = 0; w < nWords; w++) {
								empiricalTopicWords[z][w] = 0;
							}
							sumEmpiricalTopicWords[z] = 0;
						}
						topicTableCounts[z]++;
						tableToTopic[t] = z;
					}
					int z = tableToTopic[t];
					int w = statTool.sampleMult(topicWordDistributions[z], false, rand);
					empiricalTopicWords[z][w]++;
					sumEmpiricalTopicWords[z]++;
					if (words.containsKey(w)) {
						words.put(w, 1 + words.get(w));
					} else {
						words.put(w, 1);
					}
					System.out.printf("d = %d t = %d z = %d w = %d\n", d, t, z, w);
					bw_log.write(String.format("%d,%d,%d,%d\n", d, t, z, w));
				}
				bw_data.write(String.format("%d", words.size()));
				for (Map.Entry<Integer, Integer> word : words.entrySet()) {
					bw_data.write(String.format(" %d:%d", word.getKey(), word.getValue()));
				}
				bw_data.write("\n");

				int[] dTopicCounts = new int[nTopics];
				for (int z = 0; z < nTopics; z++) {
					dTopicCounts[z] = 0;
				}

				for (int t = 0; t < ndTables; t++) {
					int z = tableToTopic[t];
					dTopicCounts[z] += tableWordCounts[t];
				}

				bw_docTopicCount.write(String.format("%d", dTopicCounts[0]));
				for (int z = 1; z < nTopics; z++) {
					bw_docTopicCount.write(String.format(",%d", dTopicCounts[z]));
				}
				bw_docTopicCount.write("\n");

				bw_docTableCount.write(String.format("%d (%d)", tableWordCounts[0], tableToTopic[0]));
				for (int t = 1; t < ndTables; t++) {
					bw_docTableCount.write(String.format(",%d (%d)", tableWordCounts[t], tableToTopic[t]));
				}
				bw_docTableCount.write("\n");

			}
			bw_docTopicCount.close();
			bw_data.close();
			bw_log.close();
			bw_docTableCount.close();

			BufferedWriter bw_topics = new BufferedWriter(
					new FileWriter(String.format("%s/topicWordDistributions.csv", outputPath)));
			BufferedWriter bw_empiricalTopics = new BufferedWriter(
					new FileWriter(String.format("%s/empiricalTopicWordDistributions.csv", outputPath)));
			for (int z = 0; z < nTopics; z++) {
				bw_topics.write(String.format("%f", topicWordDistributions[z][0]));
				bw_empiricalTopics
						.write(String.format("%f", ((double) empiricalTopicWords[z][0]) / sumEmpiricalTopicWords[0]));
				for (int w = 1; w < nWords; w++) {
					bw_topics.write(String.format(",%f", topicWordDistributions[z][w]));
					bw_empiricalTopics.write(
							String.format(",%f", ((double) empiricalTopicWords[z][w]) / sumEmpiricalTopicWords[z]));
				}
				bw_topics.write("\n");
				bw_empiricalTopics.write("\n");
			}
			bw_topics.close();
			bw_empiricalTopics.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void genLDADocuments(int nDocs, int nTopics, int nWords, String outputPath) {
		try {

			empiricalTopicWords = new int[nTopics][nWords];
			sumEmpiricalTopicWords = new int[nTopics];

			BufferedWriter bw_data = new BufferedWriter(new FileWriter(String.format("%s/data", outputPath)));
			BufferedWriter bw_docTopicCount = new BufferedWriter(
					new FileWriter(String.format("%s/docTopicDistributions.csv", outputPath)));

			double[][] topicWordDistributions = new double[nTopics][];
			for (int z = 0; z < nTopics; z++) {
				topicWordDistributions[z] = genTopics(nWords);
			}

			for (int d = 0; d < nDocs; d++) {
				HashMap<Integer, Integer> words = new HashMap<Integer, Integer>();
				int[] dTopicCounts = new int[nTopics];
				for (int z = 0; z < nTopics; z++) {
					dTopicCounts[z] = 0;
				}

				double[] dTopicDistribution = statTool.sampleDirichletSkew(alpha, nTopics, documentSkewness, mass, rand);
				int ndwords = rand.nextInt(maxNWords - minNWords) + minNWords;
				for (int i = 0; i < ndwords; i++) {
					int z = statTool.sampleMult(dTopicDistribution, false, rand);
					dTopicCounts[z]++;
					int w = statTool.sampleMult(topicWordDistributions[z], false, rand);
					empiricalTopicWords[z][w]++;
					sumEmpiricalTopicWords[z]++;
					if (words.containsKey(w)) {
						words.put(w, 1 + words.get(w));
					} else {
						words.put(w, 1);
					}
				}
				bw_data.write(String.format("%d", words.size()));
				for (Map.Entry<Integer, Integer> word : words.entrySet()) {
					bw_data.write(String.format(" %d:%d", word.getKey(), word.getValue()));
				}
				bw_data.write("\n");

				bw_docTopicCount.write(String.format("%d", dTopicCounts[0]));
				for (int z = 1; z < nTopics; z++) {
					bw_docTopicCount.write(String.format(",%d", dTopicCounts[z]));
				}
				bw_docTopicCount.write("\n");
			}
			bw_docTopicCount.close();
			bw_data.close();

			BufferedWriter bw_topics = new BufferedWriter(
					new FileWriter(String.format("%s/topicWordDistributions.csv", outputPath)));
			BufferedWriter bw_empiricalTopics = new BufferedWriter(
					new FileWriter(String.format("%s/empiricalTopicWordDistributions.csv", outputPath)));
			for (int z = 0; z < nTopics; z++) {
				bw_topics.write(String.format("%f", topicWordDistributions[z][0]));
				bw_empiricalTopics
						.write(String.format("%f", ((double) empiricalTopicWords[z][0]) / sumEmpiricalTopicWords[0]));
				for (int w = 1; w < nWords; w++) {
					bw_topics.write(String.format(",%f", topicWordDistributions[z][w]));
					bw_empiricalTopics.write(
							String.format(",%f", ((double) empiricalTopicWords[z][w]) / sumEmpiricalTopicWords[z]));
				}
				bw_topics.write("\n");
				bw_empiricalTopics.write("\n");
			}
			bw_topics.close();
			bw_empiricalTopics.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		SyntheticDataGeneration generator = new SyntheticDataGeneration();
		generator.genLDADocuments(1000, 10, 1000, "E:/code/java/AdaptiveTopicTrackingTwitter/data/synthetic");

	}

}
