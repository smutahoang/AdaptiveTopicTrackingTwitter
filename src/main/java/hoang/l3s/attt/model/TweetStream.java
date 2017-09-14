package hoang.l3s.attt.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TweetStream {
	private String mainStreamPath;
	private String eventStreamPath;
	private Date date;
	private SimpleDateFormat fileDateFormat;
	private String filename;
	private InputStream is;
	private GZIPInputStream gzReader;
	private BufferedReader mainStreamReader;
	private BufferedReader eventStreamReader;
	private JsonParser parser;
	private SimpleDateFormat tweetDateTimeFormater;

	private HashSet<String> originalTweets;

	private long shift;
	private Tweet currentMainStreamTweet;
	private Tweet currentEventStreamTweet;
	private String preEventStreamTweetId;

	private void openNextMainStreamFile() {
		try {
			filename = String.format("%s/statuses.log.%s.gz", mainStreamPath, fileDateFormat.format(date));
			is = new FileInputStream(filename);
			gzReader = new GZIPInputStream(is);
			mainStreamReader = new BufferedReader(new InputStreamReader(gzReader, Charset.forName("UTF-8")));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public TweetStream(String _mainStreamPath, Date _startDate) {
		mainStreamPath = _mainStreamPath;
		eventStreamPath = null;
		date = _startDate;
		fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		tweetDateTimeFormater = new SimpleDateFormat("EEE MMM dd HH:mm:ss +0000 yyyy");
		parser = new JsonParser();
		openNextMainStreamFile();
		originalTweets = new HashSet<String>();
		preEventStreamTweetId = null;
	}

	public TweetStream(String _mainStreamPath, String _eventStreamPath, Date _startDate) {
		try {
			mainStreamPath = _mainStreamPath;
			eventStreamPath = _eventStreamPath;
			date = _startDate;
			fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			tweetDateTimeFormater = new SimpleDateFormat("EEE MMM dd HH:mm:ss +0000 yyyy");
			parser = new JsonParser();
			openNextMainStreamFile();
			if (eventStreamPath != null) {
				eventStreamReader = new BufferedReader(new FileReader(eventStreamPath));
			}
			originalTweets = new HashSet<String>();
			System.out.println("read first tweet in main stream");
			currentMainStreamTweet = readMainStream();
			System.out.println("read first tweet in event stream");
			currentEventStreamTweet = readEventStream();
			System.out.println("determining shift");
			shift = Long.parseLong(currentMainStreamTweet.getTweetId())
					- Long.parseLong(currentEventStreamTweet.getTweetId());
			preEventStreamTweetId = null;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/***
	 * check if tweet is valid by manually identified rules
	 * 
	 * @param text
	 * @return
	 */
	private boolean isValidTweet(String text) {
		if (text.startsWith("I liked a @YouTube video")) {
			return false;
		}
		return true;
	}

	private Tweet readMainStream() {
		try {
			String line = mainStreamReader.readLine();
			Tweet tweet = null;
			while (true) {
				if (line == null) {
					// close current file
					mainStreamReader.close();
					gzReader.close();
					is.close();
					// open a new file
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);
					calendar.add(Calendar.DATE, 1);
					date = calendar.getTime();
					openNextMainStreamFile();
				}
				// System.out.println(line);
				try {
					JsonObject jsonTweet = (JsonObject) parser.parse(line);
					if (jsonTweet.has("delete")) {
						line = mainStreamReader.readLine();
						continue;
					}
					if (jsonTweet.get("lang").getAsString().equals("en")) {
						if (jsonTweet.has("retweeted_status")) {
							JsonObject jsonOriginalTweet = (JsonObject) jsonTweet.get("retweeted_status");
							String tweetId = jsonOriginalTweet.get("id_str").getAsString();
							if (originalTweets.contains(tweetId)) {
								line = mainStreamReader.readLine();
								continue;
							} else {
								originalTweets.add(tweetId);
							}
						}
						String tweetId = jsonTweet.get("id_str").getAsString();
						String user = ((JsonObject) jsonTweet.get("user")).get("id").getAsString();
						String text = jsonTweet.get("text").getAsString();
						if (!isValidTweet(text)) {
							line = mainStreamReader.readLine();
							continue;
						}
						long createdAt = tweetDateTimeFormater.parse(jsonTweet.get("created_at").getAsString())
								.getTime();
						tweet = new Tweet(tweetId, text, user, createdAt);
					}
				} catch (Exception e) {
					// System.out.println("line = " + line);
					// e.printStackTrace();
					// System.exit(-1);
				}
				if (tweet != null) {
					return tweet;
				} else {
					line = mainStreamReader.readLine();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	private Tweet readEventStream() {
		try {
			while (true) {
				String line = eventStreamReader.readLine();
				if (line == null) {
					// close current file
					return null;
				}

				String tokens[] = line.split("\t");
				String tweetId = tokens[0];
				String user = null;
				String text = tokens[2];
				if (!isValidTweet(text)) {
					line = eventStreamReader.readLine();
					continue;
				}
				long createdAt = -1;
				Tweet tweet = new Tweet(tweetId, text, user, createdAt);
				if (tokens[1].equals("on-topic")) {
					tweet.setIsRelevant(true);
				} else {
					tweet.setIsRelevant(false);
				}
				return tweet;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	public Tweet getTweet() {
		Tweet tweet = null;
		if (eventStreamPath == null) {
			tweet = currentMainStreamTweet;
			currentMainStreamTweet = readMainStream();
			tweet.setAlignedTweet(preEventStreamTweetId);
			return tweet;
		}
		if (currentEventStreamTweet == null) {
			tweet = currentMainStreamTweet;
			currentMainStreamTweet = readMainStream();
			tweet.setAlignedTweet(preEventStreamTweetId);
			return tweet;
		}
		long gap = Long.parseLong(currentMainStreamTweet.getTweetId())
				- Long.parseLong(currentEventStreamTweet.getTweetId());
		if (gap < shift) {
			tweet = currentMainStreamTweet;
			currentMainStreamTweet = readMainStream();
			tweet.setAlignedTweet(preEventStreamTweetId);
			return tweet;
		} else {
			preEventStreamTweetId = currentEventStreamTweet.getTweetId();
			tweet = currentEventStreamTweet;
			currentEventStreamTweet = readEventStream();
			tweet.setAlignedTweet(preEventStreamTweetId);
			tweet.setPublishedTime(currentMainStreamTweet.getPublishedTime());
			return tweet;
		}
	}

}
