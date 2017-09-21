package hoang.l3s.attt.runner;

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
import hoang.l3s.attt.model.graphbased.GraphBasedFilter;
import hoang.l3s.attt.model.languagemodel.LanguageModelBasedFilter;
import hoang.l3s.attt.model.pseudosupervised.PseudoSupervisedFilter;

public class Runner {

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
				if (jsonTweet.has("delete"))
					continue;
				if (!jsonTweet.get("lang").toString().equals("\"en\""))
					continue;
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

	static List<Tweet> getFirstTweets(String file, int K) {
		try {

			List<Tweet> firstTweets = new ArrayList<Tweet>();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				if (!tokens[1].equals("on-topic"))
					continue;
				String tweetId = tokens[0];
				String text = tokens[2];
				String userId = null;
				long publishedTime = -1;
				Tweet tweet = new Tweet(tweetId, text, userId, publishedTime);
				firstTweets.add(tweet);
				if (firstTweets.size() == K) {
					break;
				}
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

	static void graphFilter(String[] args) {
		try {
			// String model = args[1];
			// String firstTweetPath = args[2];
			// String streamPath = args[3];
			// String startDate = args[4];
			// String outputPath = args[5];

			String firstTweetPath = "/home/hoang/attt/data/firstTweets/travel_ban.txt";
			String streamPath = "/home/hoang/attt/data/travel_ban";
			String startDate = "2017-01-28";
			String outputPath = "/home/hoang/attt/output";

			List<Tweet> firstTweets = getFirstTweets(firstTweetPath);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));

			FilteringModel filteringModel = new GraphBasedFilter(null, 0.001);

			filteringModel.init(firstTweets);
			filteringModel.filter(stream, outputPath, "travel_ban");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void lmFilter(String[] args) {
		try {
			List<Tweet> firstTweets = getFirstTweets(Configure.FIRST_TWEET_PATH);
			int nGram = 1;
			LanguageModelBasedFilter filter = new LanguageModelBasedFilter(nGram);
			filter.init(firstTweets);

			String startDate = "2017-01-28";
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			TweetStream stream = new TweetStream(Configure.STREAM_PATH, dateFormat.parse(startDate));
			filter.filter(stream, Configure.OUTPUT_PATH, "travel_ban");

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void pseudoSupervisedFilter() {
		String startDate = "2017-01-28";
		String windowTweetsPath = "/home/hoang/attt/data/firstWindow/travel_ban.txt";
		String outputPath = "/home/hoang/attt/output";
		try {

			List<Tweet> recentRelevantTweets = getFirstTweets(Configure.FIRST_TWEET_PATH);

			LinkedList<Tweet> recentTweets = getTweetsInWindow(windowTweetsPath);

			FilteringModel filter = new PseudoSupervisedFilter(recentTweets);

			filter.init(recentRelevantTweets);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			TweetStream stream = new TweetStream(Configure.STREAM_PATH, dateFormat.parse(startDate));

			filter.filter(stream, outputPath, "travel_ban");

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void crisis() {
		String startDate = "2017-01-28";
		String windowTweetsPath = "/home/hoang/attt/data/firstWindow/travel_ban.txt";
		LinkedList<Tweet> recentTweets = getTweetsInWindow(windowTweetsPath);
		String rootOutputPath = "/home/hoang/attt/output";

		List<String> datasets = new ArrayList<String>();
		List<Integer> nFirstTweets = new ArrayList<Integer>();
		// 3-day event
		datasets.add("2012_Sandy_Hurricane");
		nFirstTweets.add(50);
		// 5-day event
		datasets.add("2013_Boston_Bombings");
		nFirstTweets.add(75);
		// 6-day event
		datasets.add("2013_Queensland_Floods");
		nFirstTweets.add(100);
		// 11-day event
		datasets.add("2013_Alberta_Floods");
		nFirstTweets.add(200);
		// 11-day event
		datasets.add("2013_Oklahoma_Tornado");
		nFirstTweets.add(200);
		// 11-day event
		datasets.add("2013_West_Texas_Explosion");
		nFirstTweets.add(200);

		try {

			for (int i = 0; i < datasets.size(); i++) {

				List<Tweet> recentRelevantTweets = getFirstTweets(
						String.format("/home/hoang/attt/data/crisis/odered/%s-ontopic_offtopic.csv", datasets.get(i)),
						nFirstTweets.get(i));

				System.out.println("initializing stream");
				String eventStreamPath = String.format("/home/hoang/attt/data/crisis/odered/%s-ontopic_offtopic.csv",
						datasets.get(i));
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				TweetStream stream = new TweetStream(Configure.STREAM_PATH, eventStreamPath,
						dateFormat.parse(startDate));

				System.out.println("initializing filter");

				FilteringModel filter = new PseudoSupervisedFilter(recentTweets);
				String outputPath = String.format("%s/pseudo_supervised", rootOutputPath, datasets.get(i));

				// int nGram = 1;
				// LanguageModelBasedFilter filter = new
				// LanguageModelBasedFilter(nGram);

				// FilteringModel filter = new GraphBasedFilter(recentTweets,
				// 1);
				// String outputPath = String.format("%s/graph",
				// rootOutputPath);

				filter.init(recentRelevantTweets);
				System.out.println("filtering");
				filter.filter(stream, outputPath, datasets.get(i));
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void cikmEventTweets() {
		hoang.l3s.attt.data.CIKM12EventTweets.main(null);
	}

	public static void main(String[] args) {
		// hoang.l3s.attt.data.DataExamination.main(null);
		// hoang.l3s.attt.data.NewsMediaTweets.main(null);
		// hoang.l3s.attt.data.LipengRen.main(null);
		// hoang.l3s.attt.data.GetFirstWindowTweet.main(null);
		// System.exit(-1);

		// filter(args);
		new Configure();

		switch (Configure.AUTHOR) {
		case HOANG:
			// graphFilter(args);
			// lmFilter(args);
			// pseudoSupervisedFilter();
			// cikmEventTweets();
			crisis();
			// hoang.l3s.attt.model.graphbased.ModelInspection.main(null);
			break;
		case REN:
			lmFilter(args);
			break;
		default:
			break;

		}

	}
}
