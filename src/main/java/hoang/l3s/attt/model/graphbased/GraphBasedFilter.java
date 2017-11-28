package hoang.l3s.attt.model.graphbased;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.configure.Configure.UpdatingScheme;
import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
import hoang.l3s.attt.utils.DescriptiveStats;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;
import hoang.l3s.attt.utils.graph.TermGraph;

public class GraphBasedFilter extends FilteringModel {
	private TermGraph eventGraph;
	private TermGraph bgGraph;

	private DescriptiveStats ratioStats;
	private DescriptiveStats eventScoreStats;

	private double currentRatioMean;
	private double currentRatioSqrtVariance;

	private double currentEventScoreMean;
	private double currentEventScoreVariance;

	public GraphBasedFilter(String _dataset, List<Tweet> _eventDescriptionTweets, List<Tweet> _recentTweets,
			TweetStream _stream, long _endTime, String _outputPath, String _outputPrefix) {
		// io paths
		super.dataset = _dataset;
		super.outputPath = _outputPath;
		super.outputPrefix = _outputPrefix;

		// event description
		super.eventDescriptionTweets = _eventDescriptionTweets;

		// recent tweets
		rand = new Random(0);
		recentTweets = new ArrayList<Tweet>();
		for (Tweet tweet : _recentTweets) {
			if (rand.nextDouble() < Configure.BACKGROUND_TWEET_SAMPLING_RATIO) {
				recentTweets.add(tweet);
			}
		}
		for (Tweet tweet : eventDescriptionTweets) {
			recentTweets.add(tweet);
		}
		// utilities
		super.preprocessingUtils = new TweetPreprocessingUtils();
		eventScoreStats = new DescriptiveStats();
		ratioStats = new DescriptiveStats();

		super.nRelevantTweets = 0;
		currentTime = 0;

		// graphs
		eventGraph = new TermGraph(eventDescriptionTweets, preprocessingUtils);
		eventGraph.setMaxNKeyTerms(Configure.MAX_NUMBER_KEY_TERMS);
		eventGraph.updateTermImportance(currentTime - Configure.TEMPORAL_WINDOW_SIZE);
		eventGraph.updateKeyTerms();

		bgGraph = new TermGraph(recentTweets, preprocessingUtils);
		bgGraph.setMaxNKeyTerms(Integer.MAX_VALUE);
		bgGraph.updateTermImportance(currentTime - Configure.TEMPORAL_WINDOW_SIZE);
		bgGraph.updateKeyTerms();

		// score stats
		double bgScore = 0;
		double eventScore = 0;
		for (Tweet tweet : recentTweets) {
			bgScore = backgroundScore(tweet);
			if (bgScore > 0) {
				eventScore = relevantScore(tweet);
				ratioStats.update(eventScore / bgScore);
			}
		}
		currentRatioMean = ratioStats.getMean();
		currentRatioSqrtVariance = ratioStats.getSqrtVariance();

		for (Tweet tweet : eventDescriptionTweets) {
			eventScore = relevantScore(tweet);
			eventScoreStats.update(eventScore);
			System.out.printf("[%f] %s\n", eventScore, tweet.getText());
		}
		currentEventScoreMean = eventScoreStats.getMean();
		currentEventScoreVariance = eventScoreStats.getSqrtVariance();

		// eventGraph.saveTermInfo("/home/hoang/attt/output/graph/event_terms_init.csv");
		// eventGraph.saveGraphToFile("/home/hoang/attt/output/graph/event_graph_init.csv");
		// bgGraph.saveTermInfo("/home/hoang/attt/output/graph/bg_terms_init.csv");
		// bgGraph.saveGraphToFile("/home/hoang/attt/output/graph/bg_graph_init.csv");

		System.out.printf("init done: mean-ratio = %f var-ratio = %f\n", currentRatioMean, currentRatioSqrtVariance);

		super.stream = _stream;
		super.endTime = _endTime;
	}

	protected double relevantScore(Tweet tweet) {
		return eventGraph.getScore(tweet, preprocessingUtils);
	}

	protected double backgroundScore(Tweet tweet) {
		return bgGraph.getScore(tweet, preprocessingUtils);
	}

	private void updateGraphsTerms(Tweet tweet) {
		if (tweet.isRetweet()) {
			return;
		}
		double weight = Math.pow(Configure.AMPLIFY_FACTOR, currentTime);

		eventGraph.updateTermEdges(tweet.getTerms(preprocessingUtils), currentTime, weight);
		eventGraph.updateTermNRelevantTweets(tweet, preprocessingUtils);
		eventGraph.updateTermNAllTweets(tweet, preprocessingUtils);

		bgGraph.updateTermEdges(tweet.getTerms(preprocessingUtils), currentTime, weight);
		bgGraph.updateTermNRelevantTweets(tweet, preprocessingUtils);
		bgGraph.updateTermNAllTweets(tweet, preprocessingUtils);

	}

	private void updateBgGraphTerms(Tweet tweet) {
		double weight = Math.pow(Configure.AMPLIFY_FACTOR, currentTime);
		bgGraph.updateTermEdges(tweet.getTerms(preprocessingUtils), currentTime, weight);
		bgGraph.updateTermNRelevantTweets(tweet, preprocessingUtils);
		bgGraph.updateTermNAllTweets(tweet, preprocessingUtils);
	}

	/***
	 * update the graph with a new relevant tweet
	 */
	public void update() {
		currentTime++;
		nextUpdateTime += Configure.TIME_STEP_WIDTH;
		// event graph
		eventGraph.updateTermImportance(currentTime - Configure.TEMPORAL_WINDOW_SIZE);
		eventGraph.updateKeyTerms();
		eventGraph.saveTermInfo(String.format("%s/%s_event_terms_%d.csv", outputPath, dataset, currentTime));
		eventGraph.saveKeyTermToFile(String.format("%s/%s_event_keyTerms_%d.csv", outputPath, dataset, currentTime));
		eventGraph.saveGraphToFile(String.format("%s/%s_event_graph_%d.csv", outputPath, dataset, currentTime));
		// background graph
		bgGraph.updateTermImportance(currentTime - Configure.TEMPORAL_WINDOW_SIZE);
		bgGraph.updateKeyTerms();
		// bgGraph.saveTermInfo(String.format("%s/%s_bg_terms_%d.csv",
		// outputPath, dataset, currentTime));
		// bgGraph.saveKeyTermToFile(String.format("%s/%s_bg_keyTerms_%d.csv",
		// outputPath, dataset, currentTime));
		// bgGraph.saveGraphToFile(String.format("%s/%s_bg_graph_%d.csv",
		// outputPath, dataset, currentTime));

		// ratio
		currentRatioMean = ratioStats.getMean();
		currentRatioSqrtVariance = ratioStats.getSqrtVariance();

		// event score
		currentEventScoreMean = eventScoreStats.getMean();
		currentEventScoreVariance = eventScoreStats.getSqrtVariance();
	}

	/***
	 * output candidate tweet, together with its score
	 */
	private void outputCandidateTweet(Tweet tweet, double bgScore, double eventScore, boolean flag, String outputPath) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath, true));
			bw.write(String.format("%d,%f,%f,%f,%f,%s,%s,%s\n", currentTime, bgScore, eventScore, currentRatioMean,
					currentRatioSqrtVariance, tweet.getIsRelevant(), flag,
					tweet.getText().replace('\n', ' ').replace('\r', ' ').replace(',', ' ')));
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void filter() {

		// determine startTime
		System.out.println("determining starting point");
		super.setStartTime(stream, eventDescriptionTweets.get(eventDescriptionTweets.size() - 1));
		System.out.println("done!");

		String filteredTweetFile = String.format("%s/%s_%s_graphFilteredTweets.txt", outputPath, dataset, outputPrefix);
		String candidateTweetFile = String.format("%s/%s_%s_candidateTweets.csv", outputPath, dataset, outputPrefix);

		Tweet tweet = null;

		boolean flag = false;
		double eventScore = 0;
		double bgScore = 0;
		double r = 0;
		System.out.println("***********FILTERING*****************");
		while ((tweet = stream.getTweet()) != null) {
			if (super.isInvalidTweet(tweet)) {
				continue;
			}
			if (tweet.getPublishedTime() > endTime) {
				return;
			}
			eventScore = relevantScore(tweet);
			bgScore = backgroundScore(tweet);
			flag = false;
			if (bgScore > 0) {// not a totally novel tweet
				r = eventScore / bgScore;
				ratioStats.update(r);
				if ((r - currentRatioMean) / currentRatioSqrtVariance < Configure.MIN_RATIO_DEVIATION) {
					continue;
				}
				if ((currentEventScoreMean - eventScore)
						/ currentEventScoreVariance > Configure.MAX_EVENT_SCORE_DEVIATION) {
					continue;
				}
				eventScoreStats.update(eventScore);
				flag = true;
				System.out.printf(
						"timeStep = %d eventScore = %f bgScore = %f  mean-ratio = %f sqrt(var-ratio) = %f mean-event-socre = %f sqrt(var-event-score) = %f\n",
						currentTime, eventScore, bgScore, currentRatioMean, currentRatioSqrtVariance,
						currentEventScoreMean, currentEventScoreVariance);
				super.nRelevantTweets++;
				outputTweet(tweet, filteredTweetFile);

				updateGraphsTerms(tweet);
				if (Configure.updatingScheme == UpdatingScheme.TWEET_COUNT) {
					// check if is the update time for update
					if (super.isToUpdate(tweet)) {
						update();
					}
				}
			}

			if (!flag) {
				eventGraph.updateTermNAllTweets(tweet, preprocessingUtils);
				if (rand.nextDouble() < Configure.BACKGROUND_TWEET_SAMPLING_RATIO) {
					updateBgGraphTerms(tweet);
				}
			}
			if (eventScore > bgScore || tweet.getIsRelevant() == true) {
				outputCandidateTweet(tweet, bgScore, eventScore, flag, candidateTweetFile);
			}
			if (super.isToUpdate(tweet)) {
				update();
			}
		}

	}
}
