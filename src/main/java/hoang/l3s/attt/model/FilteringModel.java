package hoang.l3s.attt.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

public abstract class FilteringModel {

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
			bw.write(String.format("%s\t%s\t%s\n", tweet.getPublishedTime(), tweet.getUser(), tweet.getText()));
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
	public abstract void filter(TweetStream stream, String ouputPath);

}
