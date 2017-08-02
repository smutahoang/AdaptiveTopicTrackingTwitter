package hoang.l3s.attt.runner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
import hoang.l3s.attt.model.keywordbased.KeyWordMatchingFilter;
import hoang.l3s.attt.model.languagemodel.LanguageModelBasedFilter;

public class Runner {

	static List<Tweet> getFirstTweets(String file) {
		try {
			SimpleDateFormat dateTimeFormater = new SimpleDateFormat("EEE MMM dd HH:mm:ss +0000 yyyy",Locale.US);
			JsonParser jsonParser = new JsonParser();
			List<Tweet> firstTweets = new ArrayList<Tweet>();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				JsonObject jsonTweet = (JsonObject) jsonParser.parse(line);
				//to get english tweet without delete tweet, the count is 105
				{
					if (jsonTweet.has("delete"))
						continue;

					if (!jsonTweet.get("lang").toString().equals("\"en\""))
						continue;
				}
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

			if (model.equals("km")) {
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
		// filter(args);
		Configure.getConf();
		int nGram = 1;
		
		switch (Configure.author) {
		case hoang:
			filter(args);
			break;
		case ren:
//			long startTime = System.currentTimeMillis();
			
			try {
				List<Tweet> firstTweets = getFirstTweets(Configure.getFirstTweetsPath());
				LanguageModelBasedFilter filter = new LanguageModelBasedFilter(nGram);
				filter.init(firstTweets);
				
				String startDate = "2017-01-28";
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				TweetStream stream = new TweetStream(Configure.getStreamPath(), dateFormat.parse(startDate));
				filter.filter(stream, Configure.getOutputPath());
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			long endTime = System.currentTimeMillis();
//			System.out.println("running time： " + (endTime - startTime) + "ms");
			break;
		default:
			break;

		}

	}
}