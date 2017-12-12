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
import hoang.l3s.attt.model.keywordbased.KeywordBasedFilter;
import hoang.l3s.attt.model.languagemodel.LanguageModelBasedFilter;
import hoang.l3s.attt.model.pseudosupervised.PseudoSupervisedFilter;

public class Runner {

	static List<Tweet> getEventFirstTweets(String file, int K) {
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

	static List<Tweet> getCrisisTweets(String file, int K) {
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
				if (jsonTweet.has("delete"))
					continue;
				if (!jsonTweet.get("lang").toString().equals("\"en\""))
					continue;
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

	static LinkedList<Tweet> getTweetsInWindow(TweetStream stream, long windowWidth) {
		LinkedList<Tweet> tweetsInWindow = new LinkedList<Tweet>();
		Tweet tweet = stream.getTweet();
		long endTime = tweet.getPublishedTime() + windowWidth;
		while (tweet.getPublishedTime() <= endTime) {
			tweetsInWindow.add(tweet);
			tweet = stream.getTweet();
		}
		return tweetsInWindow;
	}

	static LinkedList<Tweet> getTweetsInWindow(TweetStream stream, long windowWidth, long lastTime) {
		LinkedList<Tweet> tweetsInWindow = new LinkedList<Tweet>();
		long startTime = lastTime - windowWidth;
		Tweet tweet = null;
		while ((tweet = stream.getTweet()) != null) {
			if (tweet.getPublishedTime() >= lastTime)
				break;
			if (tweet.getPublishedTime() >= startTime) {
				tweetsInWindow.add(tweet);
			}
		}
		return tweetsInWindow;
	}

	static void graphFilter(String[] args) {
		try {
			String dataset = "travel_ban";
			String firstTweetPath = "/home/hoang/attt/data/firstTweets/travel_ban.txt";
			String streamPath = "/home/hoang/attt/data/travel_ban";
			String startDate = "2017-01-28";
			String outputPath = "/home/hoang/attt/output/graph";

			String windowTweetsPath = "/home/hoang/attt/data/firstWindow/travel_ban.txt";
			LinkedList<Tweet> recentTweets = getTweetsInWindow(windowTweetsPath);

			List<Tweet> recentRelevantTweets = getEventFirstTweets(firstTweetPath, 100);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));
			long endTime = dateFormat.parse(startDate).getTime() + 7 * 24 * 60 * 60 * 1000;

			FilteringModel filteringModel = new GraphBasedFilter(dataset, recentRelevantTweets, recentTweets, stream,
					endTime, outputPath, "");
			filteringModel.filter();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void lmFilter(String[] args) {
		try {
			String firstTweetPath = "/home/hoang/attt/data/firstTweets/travel_ban.txt";
			String streamPath = "/home/hoang/attt/data/travel_ban";
			String outputPath = "/home/hoang/attt/output/language_model";

			List<Tweet> firstTweets = getEventFirstTweets(firstTweetPath, 100);
			int nGram = 1;
			LanguageModelBasedFilter filter = new LanguageModelBasedFilter(nGram);
			filter.init(firstTweets);

			String startDate = "2017-01-28";
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));
			filter.filter(stream, outputPath, "travel_ban");

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void pseudoSupervisedFilter() {
		String dataset = "travel_ban";
		String startDate = "2017-01-28";
		String windowTweetsPath = "/home/hoang/attt/data/firstWindow/travel_ban.txt";
		String firstTweetPath = "/home/hoang/attt/data/firstTweets/travel_ban.txt";
		String streamPath = "/home/hoang/attt/data/travel_ban";
		String outputPath = "/home/hoang/attt/output/pseudo_supervised";
		try {
			List<Tweet> recentRelevantTweets = getEventFirstTweets(firstTweetPath, 100);
			LinkedList<Tweet> recentTweets = getTweetsInWindow(windowTweetsPath);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));
			FilteringModel filter = new PseudoSupervisedFilter(dataset, recentRelevantTweets, recentTweets, stream, 0,
					outputPath, null);
			filter.filter();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void travelban_ps() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = "2017-01-27";
		String streamPath = "/home/hoang/attt/data/travel_ban";
		String dataset = "travelBan";
		String rootOutputPath = "/home/hoang/attt/output";
		try {

			System.out.println("getting first relevant tweets");
			List<Tweet> recentRelevantTweets = getEventFirstTweets("/home/hoang/attt/data/firstTweets/travel_ban.txt",
					50);

			System.out.println("initializing carrier stream");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));

			System.out.println("getting recent tweets");
			long lastTime = recentRelevantTweets.get(recentRelevantTweets.size() - 1).getPublishedTime();
			long windowWidth = 30 * 60 * 1000;// 30mins
			LinkedList<Tweet> recentTweets = getTweetsInWindow(stream, windowWidth, lastTime);
			System.out.printf(" --- #recent tweets = %d\n", recentTweets.size());

			System.out.println("initializing filter");

			long endTime = dateFormat.parse(startDate).getTime() + 8 * 24 * 60 * 60 * 1000 + windowWidth;
			String outputPrefix = startDate;

			String outputPath = String.format("%s/pseudo_supervised/travel_ban", rootOutputPath, dataset);
			FilteringModel filter = new PseudoSupervisedFilter(dataset, recentRelevantTweets, recentTweets, stream,
					endTime, outputPath, outputPrefix);

			System.out.println("filtering");
			filter.filter();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void travelban_kw() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = "2017-01-27";
		String streamPath = "/home/hoang/attt/data/travel_ban";
		String dataset = "travelBan";
		String rootOutputPath = "/home/hoang/attt/output";
		try {

			System.out.println("getting first relevant tweets");
			List<Tweet> recentRelevantTweets = getEventFirstTweets("/home/hoang/attt/data/firstTweets/travel_ban.txt",
					50);

			System.out.println("initializing carrier stream");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));

			System.out.println("getting recent tweets");
			long lastTime = recentRelevantTweets.get(recentRelevantTweets.size() - 1).getPublishedTime();
			long windowWidth = 30 * 60 * 1000;// 30mins
			LinkedList<Tweet> recentTweets = getTweetsInWindow(stream, windowWidth, lastTime);
			System.out.printf(" --- #recent tweets = %d\n", recentTweets.size());

			System.out.println("initializing filter");

			long endTime = dateFormat.parse(startDate).getTime() + 8 * 24 * 60 * 60 * 1000 + windowWidth;
			String outputPrefix = startDate;

			String outputPath = String.format("%s/keyword_topImportant/travel_ban", rootOutputPath, dataset);
			Configure.KEYWORD_ADAPTATION kwAdaptation = Configure.KEYWORD_ADAPTATION.TOP_IMPORTANT;
			KeywordBasedFilter filter = new KeywordBasedFilter(dataset, recentRelevantTweets, recentTweets,
					kwAdaptation, stream, endTime, outputPath, outputPrefix);

			System.out.println("filtering");
			filter.filter();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void travelban_graph() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = "2017-01-27";
		String streamPath = "/home/hoang/attt/data/travel_ban";
		String dataset = "travelBan";
		String rootOutputPath = "/home/hoang/attt/output";
		try {

			System.out.println("getting first relevant tweets");
			List<Tweet> recentRelevantTweets = getEventFirstTweets("/home/hoang/attt/data/firstTweets/travel_ban.txt",
					100);

			System.out.println("initializing carrier stream");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));

			System.out.println("getting recent tweets");
			long lastTime = recentRelevantTweets.get(recentRelevantTweets.size() - 1).getPublishedTime();
			long windowWidth = 30 * 60 * 1000;// 30mins
			LinkedList<Tweet> recentTweets = getTweetsInWindow(stream, windowWidth, lastTime);
			System.out.printf(" --- #recent tweets = %d\n", recentTweets.size());

			System.out.println("initializing filter");

			long endTime = dateFormat.parse(startDate).getTime() + 8 * 24 * 60 * 60 * 1000 + windowWidth;
			String outputPrefix = startDate;

			String outputPath = String.format("%s/graph/travel_ban", rootOutputPath);
			FilteringModel filter = new GraphBasedFilter(dataset, recentRelevantTweets, recentTweets, stream, endTime,
					outputPath, outputPrefix);

			System.out.println("filtering");
			filter.filter();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void travelban() {
		// travelban_kw();
		// travelban_ps();
		travelban_graph();
	}

	static void londonAttack_kw() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = "2017-03-22";
		String streamPath = "/home/hoang/attt/data/london_attack";
		String dataset = "londonAttack";
		String rootOutputPath = "/home/hoang/attt/output";
		try {

			System.out.println("getting first relevant tweets");
			List<Tweet> recentRelevantTweets = getEventFirstTweets(
					"/home/hoang/attt/data/firstTweets/london_attack_50.txt", 50);

			System.out.println("initializing carrier stream");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));

			System.out.println("getting recent tweets");
			long lastTime = recentRelevantTweets.get(recentRelevantTweets.size() - 1).getPublishedTime();
			long windowWidth = 30 * 60 * 1000;// 30mins
			LinkedList<Tweet> recentTweets = getTweetsInWindow(stream, windowWidth, lastTime);
			System.out.printf(" --- #recent tweets = %d\n", recentTweets.size());

			System.out.println("initializing filter");

			long endTime = dateFormat.parse(startDate).getTime() + 3 * 24 * 60 * 60 * 1000 + windowWidth;
			String outputPrefix = startDate;

			String outputPath = String.format("%s/keyword_topImportant/london_attack", rootOutputPath, dataset);
			Configure.KEYWORD_ADAPTATION kwAdaptation = Configure.KEYWORD_ADAPTATION.TOP_IMPORTANT;
			KeywordBasedFilter filter = new KeywordBasedFilter(dataset, recentRelevantTweets, recentTweets,
					kwAdaptation, stream, endTime, outputPath, outputPrefix);

			System.out.println("filtering");
			filter.filter();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void londonAttack_ps() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = "2017-03-22";
		String streamPath = "/home/hoang/attt/data/london_attack";
		String dataset = "londonAttack";
		String rootOutputPath = "/home/hoang/attt/output";
		try {

			System.out.println("getting first relevant tweets");
			List<Tweet> recentRelevantTweets = getEventFirstTweets(
					"/home/hoang/attt/data/firstTweets/london_attack_50.txt", 50);

			System.out.println("initializing carrier stream");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));

			System.out.println("getting recent tweets");
			long lastTime = recentRelevantTweets.get(recentRelevantTweets.size() - 1).getPublishedTime();
			long windowWidth = 30 * 60 * 1000;// 30mins
			LinkedList<Tweet> recentTweets = getTweetsInWindow(stream, windowWidth, lastTime);
			System.out.printf(" --- #recent tweets = %d\n", recentTweets.size());

			System.out.println("initializing filter");

			long endTime = dateFormat.parse(startDate).getTime() + 3 * 24 * 60 * 60 * 1000 + windowWidth;
			String outputPrefix = startDate;

			String outputPath = String.format("%s/pseudo_supervised/london_attack", rootOutputPath, dataset);
			FilteringModel filter = new PseudoSupervisedFilter(dataset, recentRelevantTweets, recentTweets, stream,
					endTime, outputPath, outputPrefix);

			System.out.println("filtering");
			filter.filter();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void londonAttack_graph() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = "2017-03-22";
		String streamPath = "/home/hoang/attt/data/london_attack";
		String dataset = "londonAttack";
		String rootOutputPath = "/home/hoang/attt/output";
		try {

			System.out.println("getting first relevant tweets");
			List<Tweet> recentRelevantTweets = getEventFirstTweets(
					"/home/hoang/attt/data/firstTweets/london_attack_50.txt", 50);

			System.out.println("initializing carrier stream");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));

			System.out.println("getting recent tweets");
			long lastTime = recentRelevantTweets.get(recentRelevantTweets.size() - 1).getPublishedTime();
			long windowWidth = 30 * 60 * 1000;// 30mins
			LinkedList<Tweet> recentTweets = getTweetsInWindow(stream, windowWidth, lastTime);
			System.out.printf(" --- #recent tweets = %d\n", recentTweets.size());

			System.out.println("initializing filter");

			long endTime = dateFormat.parse(startDate).getTime() + 3 * 24 * 60 * 60 * 1000 + windowWidth;
			String outputPrefix = startDate;

			String outputPath = String.format("%s/graph/london_attack", rootOutputPath);
			FilteringModel filter = new GraphBasedFilter(dataset, recentRelevantTweets, recentTweets, stream, endTime,
					outputPath, outputPrefix);

			System.out.println("filtering");
			filter.filter();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void londonAttack(String model) {
		if (model.equals("kw")) {
			londonAttack_kw();
		} else if (model.equals("ps")) {
			londonAttack_ps();
		} else if (model.equals("graph")) {
			londonAttack_graph();
		} else {
			System.out.printf("There is no model %s", model);
		}
	}

	static void uaScandal_kw() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = "2017-04-10";
		String streamPath = "/home/hoang/attt/data/2017apr";
		String dataset = "usScandal";
		String rootOutputPath = "/home/hoang/attt/output";
		try {

			System.out.println("getting first relevant tweets");
			List<Tweet> recentRelevantTweets = getEventFirstTweets("/home/hoang/attt/data/firstTweets/ua_62.txt", 50);

			System.out.println("initializing carrier stream");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));

			System.out.println("getting recent tweets");
			long lastTime = recentRelevantTweets.get(recentRelevantTweets.size() - 1).getPublishedTime();
			long windowWidth = 30 * 60 * 1000;// 30mins
			LinkedList<Tweet> recentTweets = getTweetsInWindow(stream, windowWidth, lastTime);
			System.out.printf(" --- #recent tweets = %d\n", recentTweets.size());

			System.out.println("initializing filter");

			long endTime = dateFormat.parse(startDate).getTime() + 3 * 24 * 60 * 60 * 1000 + windowWidth;
			String outputPrefix = startDate;

			String outputPath = String.format("%s/keyword_topImportant/ua", rootOutputPath, dataset);
			Configure.KEYWORD_ADAPTATION kwAdaptation = Configure.KEYWORD_ADAPTATION.TOP_IMPORTANT;
			KeywordBasedFilter filter = new KeywordBasedFilter(dataset, recentRelevantTweets, recentTweets,
					kwAdaptation, stream, endTime, outputPath, outputPrefix);

			System.out.println("filtering");
			filter.filter();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void uaScandal_ps() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = "2017-04-10";
		String streamPath = "/home/hoang/attt/data/2017apr";
		String dataset = "usScandal";
		String rootOutputPath = "/home/hoang/attt/output";
		try {

			System.out.println("getting first relevant tweets");
			List<Tweet> recentRelevantTweets = getEventFirstTweets("/home/hoang/attt/data/firstTweets/ua_62.txt", 50);

			System.out.println("initializing carrier stream");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));

			System.out.println("getting recent tweets");
			long lastTime = recentRelevantTweets.get(recentRelevantTweets.size() - 1).getPublishedTime();
			long windowWidth = 30 * 60 * 1000;// 30mins
			LinkedList<Tweet> recentTweets = getTweetsInWindow(stream, windowWidth, lastTime);
			System.out.printf(" --- #recent tweets = %d\n", recentTweets.size());

			System.out.println("initializing filter");

			long endTime = dateFormat.parse(startDate).getTime() + 3 * 24 * 60 * 60 * 1000 + windowWidth;
			String outputPrefix = startDate;

			String outputPath = String.format("%s/pseudo_supervised/ua", rootOutputPath, dataset);
			FilteringModel filter = new PseudoSupervisedFilter(dataset, recentRelevantTweets, recentTweets, stream,
					endTime, outputPath, outputPrefix);

			System.out.println("filtering");
			filter.filter();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void uaScandal_graph() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = "2017-04-10";
		String streamPath = "/home/hoang/attt/data/2017apr";
		String dataset = "usScandal";
		String rootOutputPath = "/home/hoang/attt/output";
		try {

			System.out.println("getting first relevant tweets");
			List<Tweet> recentRelevantTweets = getEventFirstTweets("/home/hoang/attt/data/firstTweets/ua_62.txt", 50);

			System.out.println("initializing carrier stream");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));

			System.out.println("getting recent tweets");
			long lastTime = recentRelevantTweets.get(recentRelevantTweets.size() - 1).getPublishedTime();
			long windowWidth = 30 * 60 * 1000;// 30mins
			LinkedList<Tweet> recentTweets = getTweetsInWindow(stream, windowWidth, lastTime);
			System.out.printf(" --- #recent tweets = %d\n", recentTweets.size());

			System.out.println("initializing filter");

			long endTime = dateFormat.parse(startDate).getTime() + 3 * 24 * 60 * 60 * 1000 + windowWidth;
			String outputPrefix = startDate;

			String outputPath = String.format("%s/graph/ua", rootOutputPath);
			FilteringModel filter = new GraphBasedFilter(dataset, recentRelevantTweets, recentTweets, stream, endTime,
					outputPath, outputPrefix);

			System.out.println("filtering");
			filter.filter();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void uaScandal(String model) {
		if (model.equals("kw")) {
			uaScandal_kw();
		} else if (model.equals("ps")) {
			uaScandal_ps();
		} else if (model.equals("graph")) {
			uaScandal_graph();
		} else {
			System.out.printf("There is no model %s", model);
		}
	}

	static void crisis() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		List<String> startDate = new ArrayList<String>();
		List<String> streamPath = new ArrayList<String>();

		// *******
		startDate.add("2017-01-27");
		streamPath.add("/home/hoang/attt/data/travel_ban");
		startDate.add("2017-03-01");
		streamPath.add("/home/hoang/attt/data/2017mar");
		startDate.add("2017-04-01");
		streamPath.add("/home/hoang/attt/data/2017apr");
		// *******
		startDate.add("2017-01-28");
		streamPath.add("/home/hoang/attt/data/travel_ban");
		startDate.add("2017-03-02");
		streamPath.add("/home/hoang/attt/data/2017mar");
		startDate.add("2017-04-02");
		streamPath.add("/home/hoang/attt/data/2017apr");
		// *******
		startDate.add("2017-01-29");
		streamPath.add("/home/hoang/attt/data/travel_ban");
		startDate.add("2017-03-03");
		streamPath.add("/home/hoang/attt/data/2017mar");
		startDate.add("2017-04-03");
		streamPath.add("/home/hoang/attt/data/2017apr");
		// *******
		startDate.add("2017-01-30");
		streamPath.add("/home/hoang/attt/data/travel_ban");
		startDate.add("2017-03-04");
		streamPath.add("/home/hoang/attt/data/2017mar");
		startDate.add("2017-04-04");
		streamPath.add("/home/hoang/attt/data/2017apr");
		// *******
		startDate.add("2017-01-31");
		streamPath.add("/home/hoang/attt/data/travel_ban");
		startDate.add("2017-03-05");
		streamPath.add("/home/hoang/attt/data/2017mar");
		startDate.add("2017-04-05");
		streamPath.add("/home/hoang/attt/data/2017apr");

		String rootOutputPath = "/home/hoang/attt/output";

		List<String> datasets = new ArrayList<String>();
		List<Integer> nFirstTweets = new ArrayList<Integer>();
		List<Integer> nTrackingDays = new ArrayList<Integer>();
		// 3-day event
		datasets.add("2012_Sandy_Hurricane");
		nFirstTweets.add(50);
		nTrackingDays.add(3);
		// 5-day event
		datasets.add("2013_Boston_Bombings");
		nFirstTweets.add(75);
		nTrackingDays.add(5);
		// 6-day event
		datasets.add("2013_Queensland_Floods");
		nFirstTweets.add(100);
		nTrackingDays.add(6);
		// 11-day event
		datasets.add("2013_Alberta_Floods");
		nFirstTweets.add(200);
		nTrackingDays.add(11);
		// 11-day event
		datasets.add("2013_Oklahoma_Tornado");
		nFirstTweets.add(200);
		nTrackingDays.add(11);
		// 11-day event
		datasets.add("2013_West_Texas_Explosion");
		nFirstTweets.add(200);
		nTrackingDays.add(11);

		try {
			for (int d = 0; d < startDate.size(); d++) {
				// for (int i = 0; i < datasets.size(); i++) {
				for (int i = 0; i < 2; i++) {
					System.out.println("getting first relevant tweets");
					List<Tweet> recentRelevantTweets = getCrisisTweets(String
							.format("/home/hoang/attt/data/crisis/odered/%s-ontopic_offtopic.csv", datasets.get(i)),
							nFirstTweets.get(i));

					System.out.println("initializing carrier stream");
					TweetStream stream = new TweetStream(streamPath.get(d), dateFormat.parse(startDate.get(d)));

					System.out.println("getting recent tweets");
					long windowWidth = 60 * 60 * 1000;// 30mins
					LinkedList<Tweet> recentTweets = getTweetsInWindow(stream, windowWidth);
					System.out.printf(" --- #recent tweets = %d\n", recentTweets.size());

					System.out.println("mixing with event stream");
					String eventStreamPath = String
							.format("/home/hoang/attt/data/crisis/odered/%s-ontopic_offtopic.csv", datasets.get(i));
					stream.mixEventStream(eventStreamPath, nFirstTweets.get(i));

					System.out.println("initializing filter");

					long endTime = dateFormat.parse(startDate.get(d)).getTime()
							+ nTrackingDays.get(i) * 24 * 60 * 60 * 1000 + windowWidth;
					String outputPrefix = startDate.get(d);

					/*
					 * long endTime =
					 * dateFormat.parse(startDate.get(d)).getTime() +
					 * nTrackingDays.get(i) * 60 * 60 * 1000 + windowWidth;
					 */

					/*
					 * String outputPath = String.format("%s/pseudo_supervised",
					 * rootOutputPath, datasets.get(i)); FilteringModel filter =
					 * new PseudoSupervisedFilter(datasets.get(i),
					 * recentRelevantTweets, recentTweets, stream, endTime,
					 * outputPath, outputPrefix);
					 */

					/*
					 * String outputPath = String.format("%s/keyword_expansion",
					 * rootOutputPath, datasets.get(i));
					 * Configure.KEYWORD_ADAPTATION kwAdaptation =
					 * Configure.KEYWORD_ADAPTATION.EXPANSION;
					 * KeywordBasedFilter filter = new
					 * KeywordBasedFilter(datasets.get(i), recentRelevantTweets,
					 * recentTweets, kwAdaptation, stream, endTime, outputPath,
					 * outputPrefix);
					 */

					// int nGram = 1;
					// LanguageModelBasedFilter filter = new
					// LanguageModelBasedFilter(nGram);

					String outputPath = String.format("%s/graph", rootOutputPath);
					FilteringModel filter = new GraphBasedFilter(datasets.get(i), recentRelevantTweets, recentTweets,
							stream, endTime, outputPath, outputPrefix);
					System.out.println("filtering");
					filter.filter();
				}
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void testStreamMixing() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = "2017-01-28";

		String streamPath = "/home/hoang/attt/data/travel_ban";

		List<String> datasets = new ArrayList<String>();
		List<Integer> nFirstTweets = new ArrayList<Integer>();
		List<Integer> nTrackingDays = new ArrayList<Integer>();
		// 3-day event
		datasets.add("2012_Sandy_Hurricane");
		nFirstTweets.add(50);
		nTrackingDays.add(3);
		// 5-day event
		datasets.add("2013_Boston_Bombings");
		nFirstTweets.add(75);
		nTrackingDays.add(5);
		// 6-day event
		datasets.add("2013_Queensland_Floods");
		nFirstTweets.add(100);
		nTrackingDays.add(6);
		// 11-day event
		datasets.add("2013_Alberta_Floods");
		nFirstTweets.add(200);
		nTrackingDays.add(11);
		// 11-day event
		datasets.add("2013_Oklahoma_Tornado");
		nFirstTweets.add(200);
		nTrackingDays.add(11);
		// 11-day event
		datasets.add("2013_West_Texas_Explosion");
		nFirstTweets.add(200);
		nTrackingDays.add(11);

		try {
			for (int i = 0; i < 1; i++) {

				System.out.println("initializing stream");
				String eventStreamPath = String.format("/home/hoang/attt/data/crisis/odered/%s-ontopic_offtopic.csv",
						datasets.get(i));

				TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));
				for (int j = 0; j < 10; j++) {
					Tweet tweet = stream.getTweet();
					System.out.println(String.format("%s\t%s\t%d\t%s\t%s\t%s\n", tweet.getTweetId(),
							tweet.getAlignedTweet(), stream.getShift(), tweet.getPublishedTime(), tweet.getUser(),
							tweet.getText().replace('\n', ' ').replace('\r', ' ')));
				}
				stream.mixEventStream(eventStreamPath, nFirstTweets.get(i));
				long endTime = dateFormat.parse(startDate).getTime() + 1 * 1 * 60 * 60 * 1000;
				Tweet tweet = stream.getTweet();
				while (tweet.getPublishedTime() < endTime) {
					System.out.println(String.format("%s\t%s\t%d\t%s\t%s\t%s\n", tweet.getTweetId(),
							tweet.getAlignedTweet(), stream.getShift(), tweet.getPublishedTime(), tweet.getUser(),
							tweet.getText().replace('\n', ' ').replace('\r', ' ')));
					tweet = stream.getTweet();
				}
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

		// hoang.l3s.attt.data.GetFirstTweets.main(null);
		// hoang.l3s.attt.data.DataExamination.main(null);
		// hoang.l3s.attt.data.NewsMediaTweets.main(null);
		// hoang.l3s.attt.data.LipengRen.main(null);
		// hoang.l3s.attt.data.GetFirstWindowTweet.main(null);
		// System.exit(-1);

		// filter(args);
		new Configure();

		switch (Configure.AUTHOR) {
		case HOANG:
			// testStreamMixing();
			// graphFilter(args);
			// lmFilter(args);
			// pseudoSupervisedFilter();
			// cikmEventTweets();
			// crisis();
			// travelban();
			// hoang.l3s.attt.model.graphbased.ModelInspection.main(null);
			// hoang.l3s.attt.data.GetFirstTweets.main(null);
			// londonAttack(args[0]);
			// uaScandal(args[0]);
			hoang.l3s.attt.evaluation.TwitterLDA.main(null);
			break;
		case REN:
			lmFilter(args);
			break;
		default:
			break;

		}

	}
}
