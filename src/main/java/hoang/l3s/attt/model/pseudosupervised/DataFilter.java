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
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;

public class DataFilter {
	private static int numOfFirstTweets = 1000;
	private static String dateFormat  = "yyyy-MM-dd";
	private static String startDate = "2017-01-28";
	private static String streamPath = "/home/hoang/attt/data/travel_ban";
	static List<Tweet> getFirstTweets(String path) {
		List<Tweet> tweets = new ArrayList<Tweet>();
		try {
			SimpleDateFormat dateTimeFormater = new SimpleDateFormat(dateFormat,Locale.US);
			TweetStream stream = new TweetStream(path, dateTimeFormater.parse(startDate));
			while(tweets.size() < numOfFirstTweets) {
				tweets.add(stream.getTweet());
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		
		return tweets;
	}
	public static void main(String[] args) {
		
		List<Tweet> firstTweets = getFirstTweets(streamPath);
		System.out.println("Number of first tweets: "+firstTweets.size());
	}
}
