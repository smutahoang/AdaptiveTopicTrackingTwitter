package hoang.l3s.attt.model.languagemodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class LipengRen {

	List<Tweet> tweets;

	static boolean simpleKeywordMatch(String tweet) {
		if (tweet.contains("travel ban"))
			return true;
		if (tweet.contains("muslim ban"))
			return true;
		if (tweet.contains("#travelban")) {
			return true;
		}
		if (tweet.contains("#muslimban"))
			return true;
		return false;
	}

	void countTweets(String gzFile) {
		try {
			int nLines = 0;
			int nEnTweets = 0;
			int nTweets = 0;
			this.tweets = new ArrayList<Tweet>();
			InputStream is = new FileInputStream(gzFile);
			GZIPInputStream gzReader = new GZIPInputStream(is);
			BufferedReader br = new BufferedReader(new InputStreamReader(gzReader, Charset.forName("UTF-8")));
			JsonParser parser = new JsonParser();
			String line = null;
			while ((line = br.readLine()) != null) {
				nLines++;
				try {
					JsonObject jsonTweet = (JsonObject) parser.parse(line);
					if (jsonTweet.has("delete"))
						continue;

					if (!jsonTweet.get("lang").toString().equals("\"en\""))
						continue;
					nEnTweets++;

					String text = jsonTweet.get("text").getAsString().toLowerCase();
					if (!simpleKeywordMatch(text))
						continue;
					nTweets++;
					this.tweets.add(new Tweet(text, jsonTweet.get("text").toString(), 0));

				} catch (Exception e) {
					// System.out.println("line = " + line);
					// System.exit(-1);
				}
			}

			br.close();
			gzReader.close();
			is.close();
			System.out.printf("%s\tnLines =%d\tnEnTweets = %d\nTweets = %d\n", gzFile, nLines, nEnTweets, nTweets);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public void getData() {
		// String s1 = "I liked a @YouTube video from @lubatv
		// https://t.co/C9skEpPESr GANHEI UMA FANTASIA ESTRANHA | Favoritos do
		// Mês #16";
		// String s2 = "I liked a @YouTube video Favoritos do aaa ";
		// this.tweetTextList = new ArrayList<String>();
		// this.tweetTextList.add(s1);
		// this.tweetTextList.add(s2);

		String s1 = "<s> I am Sam </s>";
		String s2 = "<s> Sam I am </s>";
		String s3 = "<s> I do not like green eggs and ham </s>";
		this.tweets = new ArrayList<Tweet>();
		this.tweets.add(new Tweet(s1, "user1", 0));
		this.tweets.add(new Tweet(s2, "user2", 0));
		this.tweets.add(new Tweet(s3, "user3", 0));
	}

	public void main(String[] args) {
		// File dir = new File("/home/hoang/attt/data/travel_ban");
		File dir = new File("/home/ren/data");
		File[] files = dir.listFiles();
		for (File file : files) {
			if (!file.getName().endsWith(".gz"))
				continue;
			this.countTweets(file.getAbsolutePath());
		}

		int nGram = 2;
		TweetPreprocessingUtils preprocessingUtils = new TweetPreprocessingUtils();
		LanguageModel ngram = new LanguageModel(nGram, preprocessingUtils);
		ngram.train(tweets);
	}

	public void test() {
		getData();
		TweetPreprocessingUtils preprocessingUtils = new TweetPreprocessingUtils();
		LanguageModel trainingLM = new LanguageModel(1, preprocessingUtils);
		trainingLM.train(tweets);
	}
}
