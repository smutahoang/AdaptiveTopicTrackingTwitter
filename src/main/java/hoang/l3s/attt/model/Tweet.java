package hoang.l3s.attt.model;

import java.util.Date;

public class Tweet {
	private String text;
	private String user;
	private long created_At;

	public Tweet(String _text, String _user, long _publishedTime) {
		text = _text;
		user = _user;
		created_At = _publishedTime;
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
}
