package hoang.l3s.attt.model;

import java.util.List;

import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class Tweet {

	private String tweetId;
	private String text;
	private String userId;
	private long createdAt;

	// for experimenting with crisis tweets
	private String alignedTweet;
	private boolean isRelevant;

	private boolean verbose = false;

	private List<String> terms;

	public Tweet(String _tweetId, String _text, String _userId, long _publishedTime) {
		tweetId = _tweetId;
		text = _text;
		userId = _userId;
		createdAt = _publishedTime;
		terms = null;
	}

	public String getTweetId() {
		return tweetId;
	}

	public String getText() {
		return text;
	}

	public String getUser() {
		return userId;
	}

	public long getPublishedTime() {
		return createdAt;
	}

	public void setPublishedTime(long _createdAt) {
		createdAt = _createdAt;
	}

	public List<String> getTerms(TweetPreprocessingUtils preprocessingUtils) {
		if (terms == null) {
			terms = preprocessingUtils.extractTermInTweet(text);
		}
		if (verbose) {
			System.out.println("*********************************");
			System.out.printf("tweet = [[%s]]\n", text);
			int nTerms = terms.size();
			for (int j = 0; j < nTerms; j++) {
				System.out.printf("term[%d] = [[%s]]\n", j, terms.get(j));
			}
		}
		return terms;
	}

	public void setAlignedTweet(String _tweetId) {
		alignedTweet = _tweetId;
	}

	public String getAlignedTweet() {
		return alignedTweet;
	}

	public void setIsRelevant(boolean _isRelevant) {
		isRelevant = _isRelevant;
	}

	public boolean getIsRelevant() {
		return isRelevant;
	}

	public boolean isRetweet() {
		// TODO: refactorizing this to make it more efficient
		return text.trim().startsWith("RT @");
	}

	public void print(String prefix) {
		System.out.printf("%s: time = %d user = %s id = %s text = %s\n", prefix, createdAt, userId, tweetId, text);
	}
}
