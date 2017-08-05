package hoang.l3s.attt.model.pseudosupervised;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.berkeley.nlp.lm.collections.LongToIntHashMap;
import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;

public class Tester {
	private static int numOfWindowTweets = 1000;
	private static String dateFormat  = "EEE MMM dd HH:mm:ss +0000 yyyy";
	
	//get positive instance
	static List<Tweet> getFirstTweets(String file) {
		try {
			SimpleDateFormat dateTimeFormater = new SimpleDateFormat(dateFormat,Locale.US);
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
				//System.out.printf("time = |%s|\n", jsonTweet.get("created_at").getAsString());
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

	// get all tweets in W_window
	static List<Tweet> getWindowTweets(String file, String startDate) {
		List<Tweet> tweets = new ArrayList<Tweet>();
		try {
			SimpleDateFormat dateTimeFormater = new SimpleDateFormat(dateFormat,Locale.US);
			TweetStream stream = new TweetStream(file, dateTimeFormater.parse(startDate));
			Tweet t;
			while(tweets.size() < numOfWindowTweets) {
				// check null or not before adding 
				t = stream.getTweet();
				if(t != null)
					tweets.add(t);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		
		return tweets;
	}
	public static void main(String[] args) {
		Configure.getConf();
		String startDate = "Fri Jan 27 00:31:10 +0000 2017";
		int nExclusionTerms = 50;
		int percentOfNegativeSamples = 80;
		
		//System.out.println(Configure.getFirstTweetsPath());
		List<Tweet> firstTweets = getFirstTweets(Configure.getFirstTweetsPath());
		List<Tweet> lastTweets = getWindowTweets(Configure.getStreamPath(), startDate);
		PseudoSupervisedFilter filter = new PseudoSupervisedFilter(nExclusionTerms, percentOfNegativeSamples);
		filter.init(firstTweets, lastTweets);
		
		
	}
}
