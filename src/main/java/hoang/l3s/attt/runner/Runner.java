package hoang.l3s.attt.runner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
import hoang.l3s.attt.model.graphbased.GraphBasedFilter;
import hoang.l3s.attt.model.keywordbased.KeyWordMatchingFilter;

public class Runner {

	static List<Tweet> getFirstTweets(String file) {
		try {

			SimpleDateFormat dateTimeFormater = new SimpleDateFormat("EEE MMM dd HH:mm:ss +0000 yyyy");
			JsonParser jsonParser = new JsonParser();
			List<Tweet> firstTweets = new ArrayList<Tweet>();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				JsonObject jsonTweet = (JsonObject) jsonParser.parse(line);
				System.out.printf("time = |%s|\n", jsonTweet.get("created_at").getAsString());
				long createdAt = dateTimeFormater.parse(jsonTweet.get("created_at").getAsString()).getTime();
				Tweet tweet = new Tweet(jsonTweet.get("text").getAsString(),
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

	static void filter(String[] args) {
		try {
			// String model = args[1];
			// String firstTweetPath = args[2];
			// String streamPath = args[3];
			// String startDate = args[4];
			// String outputPath = args[5];

			FilteringModel filteringModel = null;

			// test
			String model = "graph";
			String firstTweetPath = "/home/hoang/attt/data/firstTweets/travel_ban.txt";
			String streamPath = "/home/hoang/attt/data/travel_ban";
			String startDate = "2017-01-28";
			String outputPath = "/home/hoang/attt/output";

			if (model.equals("graph")) {
				filteringModel = new GraphBasedFilter();
			} else if (model.equals("km")) {
				filteringModel = new KeyWordMatchingFilter();
			} else {
				System.out.printf("%s is not an option for filtering model\n", model);
				System.exit(-1);
			}

			List<Tweet> firstTweets = getFirstTweets(firstTweetPath);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			TweetStream stream = new TweetStream(streamPath, dateFormat.parse(startDate));

			filteringModel.init(firstTweets);
			filteringModel.filter(stream, outputPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// hoang.l3s.attt.data.DataExamination.main(null);
		// hoang.l3s.attt.data.NewsMediaTweets.main(null);
		// hoang.l3s.attt.data.LipengRen.main(null);
		// hoang.l3s.attt.data.GetFirstTweets.main(null);
		filter(args);
		// hoang.l3s.attt.data.LipengRen ren = new
		// hoang.l3s.attt.data.LipengRen();
		// long startTime = System.currentTimeMillis();
		// ren.test();
		// ren.main(null);
		// long endTime = System.currentTimeMillis();
		// System.out.println("running time： " + (endTime - startTime) + "ms");
	}
}
