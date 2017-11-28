package hoang.l3s.attt.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Random;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.configure.Configure.UpdatingScheme;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public abstract class FilteringModel {

	protected List<Tweet> eventDescriptionTweets;
	protected List<Tweet> recentTweets;
	protected TweetStream stream;
	protected long endTime;
	protected int nRelevantTweets;
	protected int currentTime;
	protected long startTime;
	protected long nextUpdateTime;
	protected TweetPreprocessingUtils preprocessingUtils;
	protected Random rand;

	protected String outputPath;
	protected String outputPrefix;
	protected String dataset;

	protected void setStartTime(TweetStream stream, Tweet lastTweet) {
		// lastTweet.print("last tweet");
		/*
		 * if (lastTweet.getPublishedTime() < 0) { Tweet tweet = null; while
		 * ((tweet = stream.getTweet()) != null) { if
		 * (tweet.getTweetId().equals(lastTweet.getTweetId())) { startTime =
		 * tweet.getPublishedTime(); nextUpdateTime = startTime +
		 * Configure.TIME_STEP_WIDTH; return; } } System.out.println(
		 * "something wrong!!! lastTweet not found in stream"); System.exit(-1);
		 * } else { startTime = lastTweet.getPublishedTime(); nextUpdateTime =
		 * startTime + Configure.TIME_STEP_WIDTH; }
		 */
		startTime = stream.getTweet().getPublishedTime();
		nextUpdateTime = startTime + Configure.TIME_STEP_WIDTH;
	}

	/**
	 * check if the tweet is not valid for further processing
	 * 
	 * @param tweet
	 * @return
	 */
	protected boolean isInvalidTweet(Tweet tweet) {
		if (tweet.getPublishedTime() < startTime) {
			return true;
		}
		if (tweet.getTerms(preprocessingUtils).size() == 0) {
			return true;
		}
		return false;
	}

	/***
	 * compute the relevant score of a new tweet
	 * 
	 * @param tweet
	 * @return relevant score
	 */
	protected abstract double relevantScore(Tweet tweet);

	protected boolean isToUpdate(Tweet tweet) {
		if (Configure.updatingScheme == UpdatingScheme.PERIODIC) {
			return (tweet.getPublishedTime() >= nextUpdateTime);
		} else {
			if (nRelevantTweets > 0) {
				if (nRelevantTweets % Configure.NUMBER_NEW_RELEVANT_TWEETS == 0) {
					return true;
				}
			}
		}
		return false;
	}

	/***
	 * update model once a new relevant tweet is identified
	 * 
	 * @param tweet
	 */
	protected abstract void update();

	/***
	 * output a tweet
	 * 
	 * @param tweet
	 */
	protected void outputTweet(Tweet tweet, String outputPath) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath, true));
			bw.write(String.format("%s\t%s\t%s\t%s\t%s\n", tweet.getTweetId(), tweet.getAlignedTweet(),
					tweet.getPublishedTime(), tweet.getUser(), tweet.getText().replace('\n', ' ').replace('\r', ' ')));
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/***
	 * filter out relevant tweets from a tweet stream
	 * 
	 * @param stream
	 */
	public abstract void filter();

}
