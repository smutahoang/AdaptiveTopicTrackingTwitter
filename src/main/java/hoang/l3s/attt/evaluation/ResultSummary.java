package hoang.l3s.attt.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import hoang.l3s.attt.utils.RankingUtils;

public class ResultSummary {
	static void getTopTerms(int K, String path, int startTimeStep, int endTimeStep) {
		try {
			int nSteps = endTimeStep - startTimeStep + 1;
			String[][] topTerms = new String[nSteps][];
			double[][] topTermsRank = new double[nSteps][];
			for (int i = 0; i < nSteps; i++) {
				String filename = String.format("%s/terms_%d.csv", path, startTimeStep + i);
				BufferedReader br = new BufferedReader(new FileReader(filename));
				String line = null;
				int nTerms = 0;
				while ((line = br.readLine()) != null) {
					if (line.contains("]],DELETED"))
						continue;
					nTerms++;
				}
				br.close();

				String[] terms = new String[nTerms];
				double[] rank = new double[nTerms];

				nTerms = 0;
				br = new BufferedReader(new FileReader(filename));
				while ((line = br.readLine()) != null) {
					if (line.contains("]],DELETED"))
						continue;
					String[] tokens = line.split("\\]\\],");
					// System.out.printf("tokens[0] = %s tokens[1] = %s\n",
					// tokens[0], tokens[1]);
					terms[nTerms] = tokens[0].split(",\\[\\[")[1];
					rank[nTerms] = Double.parseDouble(tokens[1].split(",")[5]);
					nTerms++;
				}
				br.close();

				List<Integer> topIndexes = RankingUtils.getTopElements(K, rank);
				topTerms[i] = new String[topIndexes.size()];
				topTermsRank[i] = new double[topIndexes.size()];
				for (int j = 0; j < topIndexes.size(); j++) {
					topTerms[i][j] = terms[topIndexes.get(topIndexes.size() - j - 1)];
					topTermsRank[i][j] = rank[topIndexes.get(topIndexes.size() - j - 1)];
				}
			}
			String filename = String.format("%s/topTerms.csv", path);
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			bw.write(String.format("STEP-0"));
			for (int i = 1; i < nSteps; i++) {
				bw.write(String.format(",STEP-%d", i));
			}
			bw.write("\n");
			for (int k = 0; k < K; k++) {
				if (k < topTerms[0].length) {
					bw.write(String.format("%s(%f)", topTerms[0][k], topTermsRank[0][k]));
				}
				for (int i = 1; i < nSteps; i++) {
					if (k < topTerms[i].length) {
						bw.write(String.format(",%s(%f)", topTerms[i][k], topTermsRank[i][k]));
					} else {
						bw.write(",");
					}
				}
				bw.write("\n");
			}

			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	static void getHTMLFiles(int nTopics, int nTopWords, int nTopTweets, String inputPath, String outputPath) {
		try {
			for (int z = 0; z < nTopics; z++) {
				String htmlFilename = String.format("%s/%d.html", outputPath, z);
				BufferedWriter bw = new BufferedWriter(new FileWriter(htmlFilename));
				bw.write(String.format("<p><b><font color=\"red\">TOPIC-%d</font></b></p>\n", z));

				String topWordFilename = String.format("%s/tweetTopicTopWords.csv", inputPath);
				BufferedReader br = new BufferedReader(new FileReader(topWordFilename));
				String line = null;
				String topicLine = String.format("***[[TOPIC-%d]]***", z);
				while ((line = br.readLine()) != null) {
					if (!line.contains(topicLine))
						continue;
					bw.write("<p><b>TOP WORDS</b></p>\n");
					bw.write("<p>");
					line = br.readLine();
					String[] tokens = line.split(",");
					bw.write(String.format("%s (%s)", tokens[0], tokens[1]));
					for (int i = 1; i < nTopWords; i++) {
						line = br.readLine();
						System.out.printf("i = %d line = %s\n", i, line);
						tokens = line.split(",");
						bw.write(String.format(", %s (%s)", tokens[0], tokens[1]));
					}
					bw.write("</p>\n");
					break;
				}
				br.close();

				String topTweetFilename = String.format("%s/tweetTopicRepresentativeTweets.csv", inputPath);
				br = new BufferedReader(new FileReader(topTweetFilename));
				line = null;
				while ((line = br.readLine()) != null) {
					if (!line.contains(topicLine))
						continue;
					bw.write("<p><b>TOP TWEETS</b></p>\n");
					for (int i = 0; i < nTopTweets; i++) {
						line = br.readLine();
						String[] tokens = line.split("\t");
						bw.write(String.format("<p>%s", tokens[1]));
						/*for (int j = 3; j < tokens.length; j++) {
							bw.write(String.format(", %s", tokens[j]));
						}*/
						bw.write("</p>\n");

					}
					break;
				}
				br.close();
				bw.close();
			}

			String htmlFilename = String.format("%s/topicIndex.html", outputPath);
			BufferedWriter bw = new BufferedWriter(new FileWriter(htmlFilename));
			for (int z = 0; z < nTopics; z++) {
				bw.write(String.format("<p><a href=\"%d.html\" target=\"topicDescription\">TOPIC-%d</a><p>\n", z, z));
			}
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		// getTopTerms(20, "C:/Users/Tuan-Anh Hoang/Desktop/attt/terms", 1,
		// 288);
		getHTMLFiles(20, 20, 20, "C:/Users/Tuan-Anh Hoang/Desktop/attt", "C:/Users/Tuan-Anh Hoang/Desktop/attt/html");
	}
}
