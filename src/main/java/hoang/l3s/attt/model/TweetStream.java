package hoang.l3s.attt.model;

public class TweetStream {
	private String path;

	public TweetStream(String _path) {
		path = _path;
	}

	public Tweet getTweet() {
		return new Tweet(null, null, -1);
	}

}
