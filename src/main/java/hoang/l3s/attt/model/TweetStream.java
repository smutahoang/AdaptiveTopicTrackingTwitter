package hoang.l3s.attt.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
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
	private String path;
	private Date date;
	private SimpleDateFormat fileDateFormat;
	private String filename;
	private InputStream is;
	private GZIPInputStream gzReader;
	private BufferedReader br;
	private JsonParser parser;
	private SimpleDateFormat tweetDateTimeFormater;

	private HashSet<String> originalTweets;

	private void openFile() {
		try {
			filename = String.format("%s/statuses.log.%s.gz", path, fileDateFormat.format(date));
			is = new FileInputStream(filename);
			gzReader = new GZIPInputStream(is);
			br = new BufferedReader(new InputStreamReader(gzReader, Charset.forName("UTF-8")));
			parser = new JsonParser();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public TweetStream(String _path, Date startDate) {
		path = _path;
		date = startDate;
		fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		tweetDateTimeFormater = new SimpleDateFormat("EEE MMM dd HH:mm:ss +0000 yyyy");
		openFile();
		originalTweets = new HashSet<String>();
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

	public Tweet getTweet() {
		try {
			String line = br.readLine();
			Tweet tweet = null;
			while (true) {
				if (line == null) {
					// close current file
					br.close();
					gzReader.close();
					is.close();
					// open a new file
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);
					calendar.add(Calendar.DATE, 1);
					date = calendar.getTime();
					openFile();
				}
				// System.out.println(line);
				try {
					JsonObject jsonTweet = (JsonObject) parser.parse(line);
					if (jsonTweet.get("lang").getAsString().equals("en")) {
						if (jsonTweet.has("retweeted_status")) {
							JsonObject jsonOriginalTweet = (JsonObject) jsonTweet.get("retweeted_status");
							String tweetId = jsonOriginalTweet.get("id_str").getAsString();
							if (originalTweets.contains(tweetId)) {
								line = br.readLine();
								continue;
							} else {
								originalTweets.add(tweetId);
							}
						}
						String tweetId = jsonTweet.get("id_str").getAsString();
						String user = ((JsonObject) jsonTweet.get("user")).get("id").getAsString();
						String text = jsonTweet.get("text").getAsString();
						if (!isValidTweet(text)) {
							line = br.readLine();
							continue;
						}
						long createdAt = tweetDateTimeFormater.parse(jsonTweet.get("created_at").getAsString())
								.getTime();
						tweet = new Tweet(tweetId, text, user, createdAt);
					}
				} catch (Exception e) {
					// System.out.println("line = " + line);
					// System.exit(-1);
				}
				if (tweet != null) {
					return tweet;
				} else {
					line = br.readLine();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

}
