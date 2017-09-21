package hoang.l3s.attt.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

public class Crisis {
	public static void getPrecisionRecall(String groundTruthFile, String filteredFile, String outputFile) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			HashMap<String, Integer> nRelevantTweets = new HashMap<String, Integer>();
			HashSet<String> relevantTweets = new HashSet<String>();
			BufferedReader br = new BufferedReader(new FileReader(groundTruthFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				if (tokens[1].equals("on-topic")) {
					if (relevantTweets.contains(tokens[0])) {
						System.out.printf("replicated tweet: %s\n", tokens[0]);
					}
					relevantTweets.add(tokens[0]);
				}
				nRelevantTweets.put(tokens[0], relevantTweets.size());

			}
			br.close();

			System.out.printf("#relevantTweets = %d\n", relevantTweets.size());

			HashSet<String> filteredTweets = new HashSet<String>();
			br = new BufferedReader(new FileReader(filteredFile));
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				if (filteredTweets.contains(tokens[0])) {
					System.out.printf("replicated tweet: %s\n", tokens[0]);
				}
				filteredTweets.add(tokens[0]);
			}
			br.close();

			// System.exit(-1);

			String alignedTweet = null;
			int nFilteredTweets = 0;
			int nTrulyRelevantTweets = 0;
			br = new BufferedReader(new FileReader(filteredFile));
			while ((line = br.readLine()) != null) {
				nFilteredTweets++;
				String[] tokens = line.split("\t");
				String tweetId = tokens[0];
				if (relevantTweets.contains(tweetId)) {
					nTrulyRelevantTweets++;
				}
				alignedTweet = tokens[1];
				double precision = (double) nTrulyRelevantTweets / nFilteredTweets;
				double recall = (double) nTrulyRelevantTweets / nRelevantTweets.get(alignedTweet);
				double f1Score = 2 * precision * recall / (precision + recall);
				System.out.printf("#nFiltered = %d nTruly = %d nRelevants = %d precision = %f recall = %f f1 = %f\n",
						nFilteredTweets, nTrulyRelevantTweets, nRelevantTweets.get(alignedTweet), precision, recall,
						f1Score);
				bw.write(String.format("%d,%d,%d,%f,%f,%f\n", nFilteredTweets, nTrulyRelevantTweets,
						nRelevantTweets.get(alignedTweet), precision, recall, f1Score));
			}
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public static void main(String[] args) {
		getPrecisionRecall(
				"E:/code/java/AdaptiveTopicTrackingTwitter/data/crisis/odered/2012_Sandy_Hurricane-ontopic_offtopic.csv",
				// "C:/Users/Tuan-Anh Hoang/Desktop/attt/psFilteredTweets.txt",
				// "C:/Users/Tuan-Anh Hoang/Desktop/attt/psPerf.csv");
				"C:/Users/Tuan-Anh Hoang/Desktop/attt/2012_Sandy_Hurricane_psFilteredTweets.txt",
				"C:/Users/Tuan-Anh Hoang/Desktop/attt/psPerf.csv");
	}
}
