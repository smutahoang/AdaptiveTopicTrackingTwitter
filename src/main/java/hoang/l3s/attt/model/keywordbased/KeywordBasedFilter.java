package hoang.l3s.attt.model.keywordbased;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;

import hoang.l3s.attt.utils.TweetPreprocessingUtils;
import hoang.l3s.attt.utils.graph.TermGraph;

public class KeywordBasedFilter extends FilteringModel {

	private TermGraph termGraph;
	Configure.KEYWORD_ADAPTATION kwAdaptation;

	public KeywordBasedFilter(String _dataset, List<Tweet> _eventDescriptionTweets, List<Tweet> _recentTweets,
			Configure.KEYWORD_ADAPTATION _kwAdaptation, TweetStream _stream, long _endTime, String _outputPath,
			String _outputPrefix) {
		// io paths
		super.dataset = _dataset;
		super.outputPath = _outputPath;
		super.outputPrefix = _outputPrefix;

		// event description
		super.eventDescriptionTweets = _eventDescriptionTweets;

		kwAdaptation = _kwAdaptation;

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

		super.nRelevantTweets = 0;
		currentTime = 0;

		// graphs
		termGraph = new TermGraph(eventDescriptionTweets, preprocessingUtils);
		termGraph.setMaxNKeyTerms(Configure.MAX_NUMBER_KEY_TERMS);
		termGraph.updateTermImportance(currentTime - Configure.TEMPORAL_WINDOW_SIZE);
		termGraph.updateImportantTerms();

		super.stream = _stream;
		super.endTime = _endTime;
	}

	protected double relevantScore(Tweet tweet) {
		if (termGraph.containImportantTerm(tweet, preprocessingUtils))
			return 1;
		else
			return -1;
	}

	private void updateTermGraph(Tweet tweet) {
		if (tweet.isRetweet()) {
			return;
		}
		double weight = Math.pow(Configure.AMPLIFY_FACTOR, currentTime);
		termGraph.updateTermEdges(tweet.getTerms(preprocessingUtils), currentTime, weight);
		termGraph.updateTermNRelevantTweets(tweet, preprocessingUtils);
		termGraph.updateTermNAllTweets(tweet, preprocessingUtils);
	}

	/***
	 * update the graph
	 */
	public void update() {
		System.out.printf("updating term graph ...");
		currentTime++;
		nextUpdateTime += Configure.TIME_STEP_WIDTH;
		// event graph
		termGraph.updateTermImportance(currentTime - Configure.TEMPORAL_WINDOW_SIZE);
		if (kwAdaptation == Configure.KEYWORD_ADAPTATION.TOP_IMPORTANT) {
			termGraph.updateImportantTerms();
		} else {
			termGraph.expandImportantTerms();
		}

		termGraph.saveTermInfo(String.format("%s/%s_event_terms_%d.csv", outputPath, dataset, currentTime));
		termGraph.saveImportantTermToFile(
				String.format("%s/%s_event_keyTerms_%d.csv", outputPath, dataset, currentTime));
		termGraph.saveGraphToFile(String.format("%s/%s_event_graph_%d.csv", outputPath, dataset, currentTime));
		System.out.println(" done!");
	}

	public void filter() {

		// determine startTime
		System.out.println("determining starting point");
		super.setStartTime(stream, eventDescriptionTweets.get(eventDescriptionTweets.size() - 1));
		System.out.println("done!");

		String filteredTweetFile = String.format("%s/%s_%s_kwFilteredTweets.txt", outputPath, dataset, outputPrefix);
		Tweet tweet = null;
		System.out.println("***********FILTERING*****************");
		while ((tweet = stream.getTweet()) != null) {
			if (super.isInvalidTweet(tweet)) {
				continue;
			}
			if (tweet.getPublishedTime() > endTime) {
				return;
			}
			if (relevantScore(tweet) > 0) {
				super.nRelevantTweets++;
				outputTweet(tweet, filteredTweetFile);
				updateTermGraph(tweet);
			} else {
				termGraph.updateTermNAllTweets(tweet, preprocessingUtils);
			}
			if (super.isToUpdate(tweet)) {
				update();
			}
		}

	}

}
