package hoang.l3s.attt.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import hoang.l3s.attt.utils.*;

public class CompareWithGroundTruth {
	private String groundtruthPath;
	private String learntPath;
	private String distance;
	private String outputPath;

	private int nDocs;
	private int nWords;
	private int ngTopics;
	private int nlTopics;
	private int nTopics;

	// groundtruth params are prefixed by "g"
	private double[][] g_topics;
	private double[] gTopicWeights;
	private double[][] g_docTopicDistributions;

	// learnt params are prefixed by "l"
	private double[][] l_topics;
	private double[][] l_docTopicDistributions;

	private int[] glMatch;
	private int[] lgMatch;
	private double[][] topicDistance;

	public CompareWithGroundTruth(String _groundtruthPath, String _learntPath, String _distance, String _outputPath) {
		groundtruthPath = _groundtruthPath;
		learntPath = _learntPath;
		distance = _distance;
		outputPath = _outputPath;
	}

	private void getGroundTruth() {
		try {
			// topics
			String filename = String.format("%s/topicWordDistributions.csv", groundtruthPath);
			BufferedReader br = new BufferedReader(new FileReader(filename));
			ngTopics = 1;
			String line = br.readLine();
			nWords = line.split(",").length;
			while ((line = br.readLine()) != null) {
				ngTopics++;
			}
			br.close();

			g_topics = new double[ngTopics][nWords];
			gTopicWeights = new double[ngTopics];
			for (int t = 0; t < ngTopics; t++) {
				gTopicWeights[t] = 0;
			}
			br = new BufferedReader(new FileReader(filename));
			line = null;
			int t = 0;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				for (int i = 0; i < tokens.length; i++) {
					g_topics[t][i] = Double.parseDouble(tokens[i]);
				}
				t++;
			}
			br.close();

			// documents' topic distributions;
			filename = String.format("%s/docTopicDistributions.csv", groundtruthPath);
			br = new BufferedReader(new FileReader(filename));
			nDocs = 0;
			line = null;
			while ((line = br.readLine()) != null) {
				nDocs++;
			}
			br.close();
			g_docTopicDistributions = new double[nDocs][ngTopics];
			br = new BufferedReader(new FileReader(filename));
			int d = 0;
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				double sum = 0;
				for (t = 0; t < tokens.length; t++) {
					g_docTopicDistributions[d][t] = Double.parseDouble(tokens[t]);
					sum += g_docTopicDistributions[d][t];
				}
				for (t = 0; t < ngTopics; t++) {
					g_docTopicDistributions[d][t] /= sum;
					gTopicWeights[t] += g_docTopicDistributions[d][t];
				}

				d++;
			}
			br.close();
			for (t = 0; t < ngTopics; t++) {
				gTopicWeights[t] /= nDocs;
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void getLearntParams() {
		try {
			// topics

			String filename = String.format("%s/mode-topics.dat", learntPath);
			BufferedReader br = new BufferedReader(new FileReader(filename));
			nlTopics = 1;
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				nlTopics++;
			}
			br.close();

			l_topics = new double[nlTopics][nWords];
			br = new BufferedReader(new FileReader(filename));
			line = null;
			int t = 0;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(" ");
				int sum = 0;
				for (int i = 0; i < tokens.length; i++) {
					l_topics[t][i] = Integer.parseInt(tokens[i]);
					sum += l_topics[t][i];
					// System.out.printf("l_topics[%d][%d] = %f\n", t, i,
					// l_topics[t][i]);
				}
				for (int i = 0; i < tokens.length; i++) {
					l_topics[t][i] /= sum;
				}
				t++;
			}
			br.close();

			BufferedWriter bw = new BufferedWriter(new FileWriter(String.format("%s/learntTopics.csv", learntPath)));
			for (t = 0; t < nlTopics; t++) {
				bw.write(String.format("%f", l_topics[t][0]));
				for (int i = 1; i < nWords; i++) {
					bw.write(String.format(",%f", l_topics[t][i]));
				}
				bw.write("\n");
			}
			bw.close();

			// documents' topic distributions;
			filename = String.format("%s/mode-word-assignments.dat", learntPath);
			l_docTopicDistributions = new double[nDocs][nlTopics];

			int[] ndwords = new int[nDocs];
			int[] nzwords = new int[nlTopics];
			for (int d = 0; d < nDocs; d++) {
				for (int z = 0; z < nlTopics; z++) {
					l_docTopicDistributions[d][z] = 0;
				}
				ndwords[d] = 0;
			}
			for (int z = 0; z < nlTopics; z++) {
				nzwords[z] = 0;
			}

			br = new BufferedReader(new FileReader(filename));
			br.readLine();
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(" ");
				int d = Integer.parseInt(tokens[0]);
				int z = Integer.parseInt(tokens[2]);
				l_docTopicDistributions[d][z] += 1;
				ndwords[d]++;
				nzwords[z]++;
			}
			br.close();
			for (int d = 0; d < nDocs; d++) {
				for (int z = 0; z < nlTopics; z++) {
					l_docTopicDistributions[d][z] /= ndwords[d];
				}
			}

			for (int z = 0; z < nlTopics; z++) {
				System.out.printf("nzwords[%d] = %d\n", z, nzwords[z]);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void topicMatching() {

		Vector vector = new Vector();
		nTopics = ngTopics > nlTopics ? ngTopics : nlTopics;
		topicDistance = new double[nTopics][nTopics];
		for (int t = 0; t < nTopics; t++) {
			for (int k = 0; k < nTopics; k++) {
				if (t >= ngTopics || k >= nlTopics) {
					topicDistance[t][k] = 1000;
				} else if (distance.equals("euclidean")) {
					topicDistance[t][k] = vector.euclideanDistance(g_topics[t], l_topics[k]);
				} else {
					topicDistance[t][k] = vector.jensenShallonDistance(g_topics[t], l_topics[k]);
				}
				if (topicDistance[t][k] < 0) {
					System.out.println("something wrong!!!!");
					System.exit(-1);
				}
			}
		}
		System.out.println("Cost:");
		for (int t = 0; t < nTopics; t++) {
			if (t >= ngTopics) {
				break;
			}
			System.out.printf("%f", topicDistance[t][0]);
			for (int k = 1; k < nTopics; k++) {
				System.out.printf(" %f", topicDistance[t][k]);
			}
			System.out.println("");
			
		}

		HungaryMethod matcher = new HungaryMethod(topicDistance);
		glMatch = matcher.execute();
		lgMatch = new int[nTopics];
		for (int i = 0; i < nTopics; i++) {
			int j = glMatch[i];
			lgMatch[j] = i;
		}

		for (int i = 0; i < ngTopics; i++) {
			System.out.printf("glMatch[%d] = %d cost = %f\n", i, glMatch[i], topicDistance[i][glMatch[i]]);
		}

	}

	public void measureGoodness() {
		try {
			System.out.println("getting groundtruth");
			getGroundTruth();
			System.out.println("getting learnt parameters");
			getLearntParams();

			System.out.printf("#words = %d #users = %d #grountruth_topics = %d  #learnt_topics = %d\n", nWords, nDocs,
					ngTopics, nlTopics);

			System.out.println("matching topics");
			topicMatching();
			String filename = String.format("%s/topicDistance.csv", outputPath);
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (int t = 0; t < nTopics; t++) {
				bw.write(String.format("%d,%f\n", t, topicDistance[t][glMatch[t]]));
			}
			bw.close();

			System.out.println("measuring documents' topic distribution distance");

			Vector vector = new Vector();
			filename = String.format("%s/documentTopicDistance.csv", outputPath);
			bw = new BufferedWriter(new FileWriter(filename));
			for (int d = 0; d < nDocs; d++) {
				if (distance.equals("euclidean")) {
					bw.write(String.format("%s,%f\n", d, vector.weightedEuclideanDistance(g_docTopicDistributions[d],
							l_docTopicDistributions[d], glMatch, g_docTopicDistributions[d])));
				} else {
					bw.write(String.format("%s,%f\n", d, vector.jensenShallonDistance(g_docTopicDistributions[d],
							l_docTopicDistributions[d], glMatch, lgMatch)));
				}
			}
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public static void main(String[] args) {
		int a = 5;
		int b = 3;
		int x;
		x = a > b ? a : b;
		System.out.println("x = " + x);
		CompareWithGroundTruth comparator = new CompareWithGroundTruth(
				"E:/code/java/AdaptiveTopicTrackingTwitter/data/synthetic/",
				"E:/code/java/AdaptiveTopicTrackingTwitter/data/synthetic/", "euclidean",
				"E:/code/java/AdaptiveTopicTrackingTwitter/data/synthetic/");
		comparator.measureGoodness();
	}
}
