package hoang.l3s.attt.model.graphbased;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.utils.RankingUtils;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class ModelInspection {
	public static void getTopTerms(int endTimeStep, String prefix) {
		try {
			String[][] topTerms = new String[endTimeStep + 1][];
			double[][] topScores = new double[endTimeStep + 1][];
			int maxLength = -1;

			for (int timeStep = 0; timeStep <= endTimeStep; timeStep++) {
				String filename = "/home/hoang/attt/output/graph";
				if (timeStep == 0) {
					filename = String.format("%s/%s_terms_init.csv", filename, prefix);
				} else {
					filename = String.format("%s/%s_terms_%d.csv", filename, prefix, timeStep);
				}

				BufferedReader br = new BufferedReader(new FileReader(filename));
				List<String> terms = new ArrayList<String>();
				List<Double> scores = new ArrayList<Double>();
				String line = null;
				while ((line = br.readLine()) != null) {
					if (line.contains("[[null]]"))
						continue;
					System.out.printf("[%d] line = %s\n", timeStep, line);
					String[] tokens = line.split("\\]\\],");
					String term = tokens[0].split(",\\[\\[")[1];
					double score = Double.parseDouble(tokens[1].split(",")[5]);
					terms.add(term);
					scores.add(score);
				}
				br.close();
				List<Integer> topIndexes = RankingUtils.getIndexTopElements(Math.min(Configure.MAX_NUMBER_KEY_TERMS,
						(int) (Configure.PROPORTION_OF_KEYTERMS * scores.size())), scores);
				topTerms[timeStep] = new String[topIndexes.size()];
				topScores[timeStep] = new double[topIndexes.size()];
				for (int i = 0; i < topIndexes.size(); i++) {
					int j = topIndexes.get(i);
					topTerms[timeStep][topTerms[timeStep].length - i - 1] = terms.get(j);
					topScores[timeStep][topScores[timeStep].length - i - 1] = scores.get(j);
				}
				if (topIndexes.size() > maxLength) {
					maxLength = topIndexes.size();
				}
			}

			System.out.printf("maxlength = %d\n", maxLength);

			BufferedWriter bw = new BufferedWriter(
					new FileWriter(String.format("/home/hoang/attt/output/graph/%s_topTerms.csv", prefix)));
			for (int i = 0; i < maxLength; i++) {
				if (topTerms[0].length > i) {
					bw.write(String.format("%s (%f)", topTerms[0][i], topScores[0][i]));
				} else {
					bw.write("- (-)");
				}
				for (int timeStep = 1; timeStep <= endTimeStep; timeStep++) {
					if (topTerms[timeStep].length > i) {
						bw.write(String.format(",%s (%f)", topTerms[timeStep][i], topScores[timeStep][i]));
					} else {
						bw.write(",- (-)");
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

	public static void getKeyTerms(int endTimeStep, String prefix) {
		try {
			String[][] topTerms = new String[endTimeStep][];
			double[][] topScores = new double[endTimeStep][];
			int maxLength = -1;

			for (int timeStep = 0; timeStep < endTimeStep; timeStep++) {
				String filename = "/home/hoang/attt/output/graph";
				filename = String.format("%s/%s_event_keyTerms_%d.csv", filename, prefix, timeStep + 1);

				BufferedReader br = new BufferedReader(new FileReader(filename));
				List<String> terms = new ArrayList<String>();
				List<Double> scores = new ArrayList<Double>();
				String line = null;
				while ((line = br.readLine()) != null) {
					if (line.contains("[[null]]"))
						continue;
					System.out.printf("[%d] line = %s\n", timeStep + 1, line);
					String[] tokens = line.split("\\]\\],");
					String term = tokens[0].split("\\[\\[")[1];
					double score = Double.parseDouble(tokens[1].split(",")[0]);
					terms.add(term);
					scores.add(score);
				}
				br.close();
				List<Integer> topIndexes = RankingUtils.getIndexTopElements(scores.size(), scores);
				topTerms[timeStep] = new String[topIndexes.size()];
				topScores[timeStep] = new double[topIndexes.size()];
				for (int i = 0; i < topIndexes.size(); i++) {
					int j = topIndexes.get(i);
					topTerms[timeStep][topTerms[timeStep].length - i - 1] = terms.get(j);
					topScores[timeStep][topScores[timeStep].length - i - 1] = scores.get(j);
				}
				if (topIndexes.size() > maxLength) {
					maxLength = topIndexes.size();
				}
			}

			System.out.printf("maxlength = %d\n", maxLength);

			BufferedWriter bw = new BufferedWriter(
					new FileWriter(String.format("/home/hoang/attt/output/graph/%s_allKeyTerms.csv", prefix)));
			for (int i = 0; i < maxLength; i++) {
				if (topTerms[0].length > i) {
					bw.write(String.format("%s (%f)", topTerms[0][i], topScores[0][i]));
				} else {
					bw.write("- (-)");
				}
				for (int timeStep = 0; timeStep < endTimeStep; timeStep++) {
					if (topTerms[timeStep].length > i) {
						bw.write(String.format(",%s (%f)", topTerms[timeStep][i], topScores[timeStep][i]));
					} else {
						bw.write(",- (-)");
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

	public static void inspectTermGraph() {
		new Configure();
		String graphfile = "C:/Users/Tuan-Anh Hoang/Desktop/attt/2012_Sandy_Hurricane_event_graph_14.csv";
		String termfile = "C:/Users/Tuan-Anh Hoang/Desktop/attt/2012_Sandy_Hurricane_event_terms_14.csv";
		int time = 14;
		TermGraph graph = new TermGraph(termfile, graphfile, time);
		graph.setMaxNKeyTerms(Configure.MAX_NUMBER_KEY_TERMS);
		graph.updateTermImportance(time - Configure.TEMPORAL_WINDOW_SIZE);
		graph.updateKeyTerms();
		graph.saveTermInfo("C:/Users/Tuan-Anh Hoang/Desktop/attt/2012_Sandy_Hurricane_event_terms_14_test.csv");
		graph.saveGraphToFile("C:/Users/Tuan-Anh Hoang/Desktop/attt/2012_Sandy_Hurricane_event_graph_14_test.csv");
		graph.saveKeyTermToFile("C:/Users/Tuan-Anh Hoang/Desktop/attt/2012_Sandy_Hurricane_event_keyTerms_14_test.csv");

		TweetPreprocessingUtils preprocessingUtils = new TweetPreprocessingUtils();
		String text = "#sex exspose italian girl east west hardcore bass https://t.co/ouAiZeRhRW";

		Tweet tweet = new Tweet(null, text, null, 1485622596000L);
		double s = graph.getScore(tweet, preprocessingUtils);
		System.out.printf("score = %f", s);

	}

	public static void main(String[] args) {
		// getTopTerms(90, "event");
		// getTopTerms(90, "bg");
		// getKeyTerms(50, "2012_Sandy_Hurricane");
		inspectTermGraph();
	}
}
