package hoang.l3s.attt.model.graphbased;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class GraphBasedFilter extends FilteringModel {
	private TermGraph eventGraph;
	private TermGraph bgGraph;
	private double ratio;
	private double relativeRatio;

	private double bgThreshold;

	private List<Tweet> descriptionTweets;
	private int nRecentBackgroundTweets;
	private double sumRecentBackgroundTweetScrore;

	public GraphBasedFilter(List<Tweet> _recentTweets, double _ratio) {
		recentTweets = new LinkedList<Tweet>();
		rand = new Random(0);
		for (Tweet tweet : _recentTweets) {
			if (rand.nextDouble() < Configure.BACKGROUND_TWEET_SAMPLING_RATIO) {
				recentTweets.add(tweet);
			}
		}
		preprocessingUtils = new TweetPreprocessingUtils();
		ratio = _ratio;
		// bgGraph = new TermGraph(recentTweets, preprocessingUtils);
		super.nRelevantTweets = 0;

	}

	public void init(List<Tweet> tweets) {
		descriptionTweets = tweets;
		eventGraph = new TermGraph(descriptionTweets, preprocessingUtils);
		eventGraph.setMaxNKeyTerms(Configure.MAX_NUMBER_KEY_TERMS);
		eventGraph.updateTermRank(timeStep - Configure.TEMPORAL_WINDOW);
		for (Tweet tweet : tweets) {
			recentTweets.add(tweet);
		}
		bgGraph = new TermGraph(recentTweets, preprocessingUtils);
		bgGraph.setMaxNKeyTerms(Integer.MAX_VALUE);
		bgGraph.updateTermRank(timeStep - Configure.TEMPORAL_WINDOW);
		for (Tweet tweet : recentTweets) {
			bgThreshold += bgScore(tweet);
		}

		timeStep = 0;

		// eventGraph.saveTermInfo("/home/hoang/attt/output/graph/event_terms_init.csv");
		// eventGraph.saveGraphToFile("/home/hoang/attt/output/graph/event_graph_init.csv");
		// bgGraph.saveTermInfo("/home/hoang/attt/output/graph/bg_terms_init.csv");
		// bgGraph.saveGraphToFile("/home/hoang/attt/output/graph/bg_graph_init.csv");

		bgThreshold /= recentTweets.size();
		bgThreshold /= Configure.MAX_DEVIATION_FROM_MEAN_RELEVANT_SCORE;
		relativeRatio = ratio * bgGraph.getNActiveTerms() / eventGraph.getNActiveTerms();

	}

	protected double relevantScore(Tweet tweet) {
		return eventGraph.getLikelihood(tweet, preprocessingUtils);
	}

	protected double bgScore(Tweet tweet) {
		return bgGraph.getLikelihood(tweet, preprocessingUtils);
	}

	private void updateTermNAllTweet(Tweet tweet) {
		eventGraph.updateTermNAllTweets(tweet, preprocessingUtils);
	}

	/***
	 * update the graph with a new relevant tweet
	 */
	public void update(Tweet tweet) {
		long diff = tweet.getPublishedTime() - startTime;
		int time = (int) (diff / Configure.TIME_STEP_WIDTH);
		double weight = Math.pow(Configure.AMPLIFY_FACTOR, time);

		if (!tweet.getText().trim().startsWith("RT @")) {
			eventGraph.updateTermEdges(tweet.getTerms(preprocessingUtils), time, weight);
			eventGraph.updateTermNRelevantTweets(tweet, preprocessingUtils);
			eventGraph.updateTermNAllTweets(tweet, preprocessingUtils);

			bgGraph.updateTermEdges(tweet.getTerms(preprocessingUtils), time, weight);
			bgGraph.updateTermNRelevantTweets(tweet, preprocessingUtils);
			bgGraph.updateTermNAllTweets(tweet, preprocessingUtils);
		}

		if (super.isToUpdate(tweet)) {
			timeStep = time;
			// event graph
			eventGraph.updateTermRank(timeStep - Configure.TEMPORAL_WINDOW);
			eventGraph.saveTermInfo(String.format("%s/%s_event_terms_%d.csv", outputPath, dataset, timeStep));
			eventGraph.saveGraphToFile(String.format("%s/%s_event_graph_%d.csv", outputPath, dataset, timeStep));
			// background graph
			bgGraph.updateTermRank(timeStep - Configure.TEMPORAL_WINDOW);
			bgGraph.saveTermInfo(String.format("%s/%s_bg_terms_%d.csv", outputPath, dataset, timeStep));
			bgGraph.saveGraphToFile(String.format("%s/%s_bg_graph_%d.csv", outputPath, dataset, timeStep));

			// ratio
			relativeRatio = ratio * bgGraph.getNActiveTerms() / eventGraph.getNActiveTerms();

			// threshold
			bgThreshold = sumRecentBackgroundTweetScrore / nRecentBackgroundTweets;
			bgThreshold /= Configure.MAX_DEVIATION_FROM_MEAN_RELEVANT_SCORE;
			nRecentBackgroundTweets = 0;
			sumRecentBackgroundTweetScrore = 0;

		}
	}

	private void updateBgGraphOnly(Tweet tweet) {
		long diff = tweet.getPublishedTime() - startTime;
		int time = (int) (diff / Configure.TIME_STEP_WIDTH);
		double weight = Math.pow(Configure.AMPLIFY_FACTOR, time);

		bgGraph.updateTermEdges(tweet.getTerms(preprocessingUtils), time, weight);
		bgGraph.updateTermNRelevantTweets(tweet, preprocessingUtils);
		bgGraph.updateTermNAllTweets(tweet, preprocessingUtils);
	}

	/***
	 * output candidate tweet, together with its score
	 */
	private void outputCandidateTweet(Tweet tweet, double bgScore, double eventScore, boolean flag, String outputPath) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath, true));
			bw.write(String.format("%d,%f,%f,%f,%f,%s,%s,%s\n", timeStep, bgThreshold, bgScore, eventScore,
					relativeRatio, tweet.getIsRelevant(), flag,
					tweet.getText().replace('\n', ' ').replace('\r', ' ').replace(',', ' ')));
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void filter(TweetStream stream, String _outputPath, String _dataset) {
		// determine startTime
		System.out.println("determining startTime");
		super.setStartTime(stream, descriptionTweets.get(descriptionTweets.size() - 1));
		System.out.println("done!");

		outputPath = _outputPath;
		dataset = _dataset;

		String filteredTweetFile = String.format("%s/%s_graphFilteredTweets.txt", outputPath, dataset);
		String candidateTweetFile = String.format("%s/%s_candidateTweets.csv", outputPath, dataset);

		for (Tweet tweet : recentTweets) {
			updateTermNAllTweet(tweet);
		}

		Tweet tweet = null;

		nRecentBackgroundTweets = 0;
		sumRecentBackgroundTweetScrore = 0;
		boolean flag = false;
		System.out.println("***********FILTERING*****************");
		while ((tweet = stream.getTweet()) != null) {
			if (tweet.getPublishedTime() < startTime) {
				continue;
			}
			double eventScore = relevantScore(tweet);
			double bgScore = bgScore(tweet);
			flag = false;
			if (bgScore > bgThreshold) {// not too novel
				if (eventScore >= bgScore * relativeRatio) {
					flag = true;
					System.out.printf("eventScore = %f bgScore = %f ratio = %f\n", eventScore, bgScore, relativeRatio);
					super.nRelevantTweets++;
					outputTweet(tweet, filteredTweetFile);
					update(tweet);
				}
			} else {
				updateTermNAllTweet(tweet);
				if (rand.nextDouble() < Configure.BACKGROUND_TWEET_SAMPLING_RATIO) {
					updateBgGraphOnly(tweet);
				}
			}
			if (eventScore > bgScore || tweet.getIsRelevant() == true) {
				outputCandidateTweet(tweet, bgScore, eventScore, flag, candidateTweetFile);
			}
			if (bgScore > 0) {
				nRecentBackgroundTweets++;
				sumRecentBackgroundTweetScrore += bgScore;
			}
		}

	}
}
