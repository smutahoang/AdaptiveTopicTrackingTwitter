package hoang.l3s.attt.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.configure.Configure.UpdatingScheme;

public abstract class FilteringModel {
	protected int nRelevantTweets;
	protected int timeStep;
	protected long startTime;

	/***
	 * initialize the filtering model from a set of first tweets
	 * 
	 * @param tweets
	 *            set of first tweets about event
	 */
	public abstract void init(List<Tweet> tweets);

	/***
	 * compute the relevant score of a new tweet
	 * 
	 * @param tweet
	 * @return relevant score
	 */
	public abstract double relevantScore(Tweet tweet);

	public boolean isToUpdate(Tweet tweet) {
		if (Configure.updatingScheme == UpdatingScheme.PERIODIC) {
			long diff = tweet.getPublishedTime() - startTime;
			int time = (int) (diff / Configure.TIME_STEP_WIDTH);
			if (time > timeStep) {
				return true;
			}
		} else {
			if (nRelevantTweets % Configure.NUMBER_NEW_RELEVANT_TWEETS == 0) {
				return true;
			}
		}
		return false;
	}

	/***
	 * update model once a new relevant tweet is identified
	 * 
	 * @param tweet
	 */
	public abstract void update(Tweet tweet);

	/***
	 * output a tweet
	 * 
	 * @param tweet
	 */
	public void outputTweet(Tweet tweet, String outputPath) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath, true));
			bw.write(String.format("%s\t%s\t%s\t%s\n", tweet.getTweetId(), tweet.getPublishedTime(), tweet.getUser(),
					tweet.getText().replace('\n', ' ').replace('\r', ' ')));
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
