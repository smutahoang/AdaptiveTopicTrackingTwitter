package hoang.l3s.attt.model;

import java.util.List;

import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class Tweet {

	private String text;
	private String user;
	private long created_At;

	private boolean verbose = true;

	private List<String> terms;

	public Tweet(String _text, String _user, long _publishedTime) {
		text = _text;
		user = _user;
		created_At = _publishedTime;
		terms = null;
	}

	public String getText() {
		return text;
	}

	public String getUser() {
		return user;
	}

	public long getPublishedTime() {
		return created_At;
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
}
