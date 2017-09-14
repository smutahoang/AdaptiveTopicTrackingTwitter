package hoang.l3s.attt.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Random;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.configure.Configure.UpdatingScheme;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public abstract class FilteringModel {
	protected int nRelevantTweets;
	protected int timeStep;
	protected long startTime;
	protected TweetPreprocessingUtils preprocessingUtils;
	protected Random rand;

	/***
	 * initialize the filtering model from a set of first tweets
	 * 
	 * @param tweets
	 *            set of first tweets about event
	 */
	public abstract void init(List<Tweet> tweets);

	protected void setStartTime(TweetStream stream, Tweet lastTweet) {
		Tweet tweet = null;
		while ((tweet = stream.getTweet()) != null) {
			if (tweet.getTweetId().equals(lastTweet.getTweetId())) {
				startTime = tweet.getPublishedTime();
				return;
			}
		}
		System.out.println("something wrong!!! lastTweet not found in stream");
		System.exit(-1);
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
			long diff = tweet.getPublishedTime() - startTime;
			int time = (int) (diff / Configure.TIME_STEP_WIDTH);
			if (time > timeStep) {
				return true;
			}
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
	protected abstract void update(Tweet tweet);

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
	public abstract void filter(TweetStream stream, String outputPath);

}
