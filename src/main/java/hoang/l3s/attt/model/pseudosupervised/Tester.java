package hoang.l3s.attt.model.pseudosupervised;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
//import hoang.l3s.attt.model.graphbased.GraphBasedFilter;
import hoang.l3s.attt.model.languagemodel.LanguageModelBasedFilter;
import hoang.l3s.attt.model.pseudosupervised.PseudoSupervisedFilter;

public class Tester {
	private static String dateFormat = "EEE MMM dd HH:mm:ss +0000 yyyy";
	static List<Tweet> getFirstTweets(String file) {
		try {
			String dateformat = "EEE MMM dd HH:mm:ss +0000 yyyy";

			SimpleDateFormat dateTimeFormater = new SimpleDateFormat(dateformat, Locale.US);
			JsonParser jsonParser = new JsonParser();
			List<Tweet> firstTweets = new ArrayList<Tweet>();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				JsonObject jsonTweet = (JsonObject) jsonParser.parse(line);
				// to get english tweet without delete tweet, the count is 105
				{
					if (jsonTweet.has("delete"))
						continue;

					if (!jsonTweet.get("lang").toString().equals("\"en\""))
						continue;
				}
				System.out.printf("time = |%s|\n", jsonTweet.get("created_at").getAsString());
				long createdAt = dateTimeFormater.parse(jsonTweet.get("created_at").getAsString()).getTime();
				Tweet tweet = new Tweet(jsonTweet.get("id_str").getAsString(), jsonTweet.get("text").getAsString(),
						((JsonObject) jsonTweet.get("user")).get("id").getAsString(), createdAt);
				firstTweets.add(tweet);
			}

			br.close();
			return firstTweets;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	static LinkedList<Tweet> getTweetsInWindow(String file) {
		try {
			String dateformat = "EEE MMM dd HH:mm:ss +0000 yyyy";

			SimpleDateFormat dateTimeFormater = new SimpleDateFormat(dateformat, Locale.US);
			JsonParser jsonParser = new JsonParser();
			LinkedList<Tweet> tweetsInWindow = new LinkedList<Tweet>();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				JsonObject jsonTweet = (JsonObject) jsonParser.parse(line);
				// to get english tweet without delete tweet, the count is 105
				{
					if (jsonTweet.has("delete"))
						continue;

					if (!jsonTweet.get("lang").toString().equals("\"en\""))
						continue;
				}
				System.out.printf("time = |%s|\n", jsonTweet.get("created_at").getAsString());
				long createdAt = dateTimeFormater.parse(jsonTweet.get("created_at").getAsString()).getTime();
				Tweet tweet = new Tweet(jsonTweet.get("id_str").getAsString(), jsonTweet.get("text").getAsString(),
						((JsonObject) jsonTweet.get("user")).get("id").getAsString(), createdAt);
				tweetsInWindow.add(tweet);
			}

			br.close();
			return tweetsInWindow;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	public static void main(String[] args) {

		new Configure();
		int nGram = 1;

		switch (Configure.AUTHOR) {
		case HOANG:
			break;
		case REN:
			// long startTime = System.currentTimeMillis();

			try {
				List<Tweet> firstTweets = getFirstTweets(Configure.FIRST_TWEET_PATH);
				LanguageModelBasedFilter filter = new LanguageModelBasedFilter(nGram);
				filter.init(firstTweets);

				String startDate = "2017-01-28";
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				TweetStream stream = new TweetStream(Configure.STREAM_PATH, dateFormat.parse(startDate));
				filter.filter(stream, Configure.OUTPUT_PATH);

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;

		case NGUYEN:

			String startDate = "Fri Jan 28 00:31:10 +0000 2017";
			String windowTweetsPath = "/home/hnguyen/proj/data/firstWindow/travel_ban.txt";
			String output = "hnt.txt";

			try {

				List<Tweet> firstTweets = getFirstTweets(Configure.FIRST_TWEET_PATH);

				LinkedList<Tweet> windowTweets = getTweetsInWindow(windowTweetsPath);

				FilteringModel filter = new PseudoSupervisedFilter(windowTweets);

				filter.init(firstTweets);

				SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(dateFormat, Locale.US);
				TweetStream stream = new TweetStream(Configure.STREAM_PATH, dateTimeFormatter.parse(startDate));
				filter.filter(stream, output);

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			break;

		}

	}
}