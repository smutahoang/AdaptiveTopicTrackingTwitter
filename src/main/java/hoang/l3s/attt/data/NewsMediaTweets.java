/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hoang.l3s.attt.data;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.7
 */
public class NewsMediaTweets {
	public static int MAX_API_CALLS = 180;
	public static int TWITTER_WINDOW = 15 * 60 * 1000;
	public static int MAX_PAGE = 16;

	public static class TwitterAPIKeyInfo {
		public String oAuthConsumerKey;
		public String oAuthConsumerSecret;
		public String oAuthAccessToken;
		public String oAuthAccessTokenSecret;

		public TwitterAPIKeyInfo(String _oAuthConsumerKey, String _oAuthConsumerSecret, String _oAuthAccessToken,
				String _oAuthAccessTokenSecret) {
			oAuthConsumerKey = _oAuthConsumerKey;
			oAuthConsumerSecret = _oAuthConsumerSecret;
			oAuthAccessToken = _oAuthAccessToken;
			oAuthAccessTokenSecret = _oAuthAccessTokenSecret;
		}

		public void printInfo() {
			System.out.println("API KEY:");
			System.out.printf("\toAuthConsumerKey = %s\n", oAuthConsumerKey);
			System.out.printf("\toAuthConsumerSecret = %s\n", oAuthConsumerSecret);
			System.out.printf("\toAuthAccessToken = %s\n", oAuthAccessToken);
			System.out.printf("\toAuthAccessTokenSecret = %s\n", oAuthAccessTokenSecret);
		}
	}

	public static List<TwitterAPIKeyInfo> getAPIKeys() {
		List<TwitterAPIKeyInfo> apiKeys = new ArrayList<TwitterAPIKeyInfo>();
		try {

			String _oAuthConsumerKey;
			String _oAuthConsumerSecret;
			String _oAuthAccessToken;
			String _oAuthAccessTokenSecret;

			TwitterAPIKeyInfo apiKey;

			BufferedReader br = new BufferedReader(new FileReader("/home/hoang/attt/apiKeys.txt"));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#"))
					continue;
				_oAuthConsumerKey = line.split("=")[1];

				line = br.readLine();
				_oAuthConsumerSecret = line.split("=")[1];

				line = br.readLine();
				_oAuthAccessToken = line.split("=")[1];

				line = br.readLine();
				_oAuthAccessTokenSecret = line.split("=")[1];

				apiKey = new TwitterAPIKeyInfo(_oAuthConsumerKey, _oAuthConsumerSecret, _oAuthAccessToken,
						_oAuthAccessTokenSecret);
				apiKeys.add(apiKey);
			}

			_oAuthConsumerKey = "DJKwjC0vadmfxyWgGmd5Aw";
			_oAuthConsumerSecret = "lz7RNetup4Lv2O6i72x4nU5ywHF5tLROkDDDkHSSbW0";
			_oAuthAccessToken = "187504434-mQQsnJxKlogrQhYiVLqcjS1lIX9ngYly2rBhWj60";
			_oAuthAccessTokenSecret = "krJuqVKELM1KRbyZujivlh7WVX4Sg7PzqHkvICtXs5lyH";

			apiKey = new TwitterAPIKeyInfo(_oAuthConsumerKey, _oAuthConsumerSecret, _oAuthAccessToken,
					_oAuthAccessTokenSecret);

			apiKeys.add(apiKey);
			br.close();
			return apiKeys;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	public static List<String> getUsers() {
		try {
			List<String> users = new ArrayList<String>();
			BufferedReader br = new BufferedReader(new FileReader("/home/hoang/attt/news twitter accounts.txt"));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#"))
					continue;
				users.add(line.split("\t")[0]);
			}
			br.close();
			return users;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	public static void crawl() {
		try {
			List<TwitterAPIKeyInfo> apiKeys = getAPIKeys();
			List<String> users = getUsers();
			int nAPICalls = 0;
			int keyIndex = 0;
			long[] starts = new long[apiKeys.size()];
			for (int k = 0; k < apiKeys.size(); k++) {
				starts[k] = System.currentTimeMillis();
			}
			for (int u = 0; u < users.size(); u++) {
				String user = users.get(u);

				String output_file = String.format("/home/hoang/attt/data/news_media_tweets/%s.txt", user);
				BufferedWriter bw = new BufferedWriter(new FileWriter(output_file));

				Paging paging = new Paging();
				paging.setCount(200);
				System.out.printf("Getting tweets of @%s\n", user);
				for (int p = 1; p <= MAX_PAGE; p++) {
					paging.setPage(p);
					if (nAPICalls == MAX_API_CALLS) {
						nAPICalls = 0;
						keyIndex++;
						if (keyIndex == apiKeys.size())
							keyIndex = 0;
						long duration = System.currentTimeMillis() - starts[keyIndex];
						if (duration < TWITTER_WINDOW)
							try {
								System.out.println("API rate exceeded. Waitting for the next window");
								Thread.sleep(duration + 5000);
							} catch (InterruptedException e) {
								e.printStackTrace();
								System.exit(-1);
							}
						starts[keyIndex] = System.currentTimeMillis();
					}
					nAPICalls++;
					ConfigurationBuilder cb = new ConfigurationBuilder();
					cb.setDebugEnabled(true).setOAuthConsumerKey(apiKeys.get(keyIndex).oAuthConsumerKey)
							.setOAuthConsumerSecret(apiKeys.get(keyIndex).oAuthConsumerSecret)
							.setOAuthAccessToken(apiKeys.get(keyIndex).oAuthAccessToken)
							.setOAuthAccessTokenSecret(apiKeys.get(keyIndex).oAuthAccessTokenSecret);
					TwitterFactory tf = new TwitterFactory(cb.build());
					Twitter twitter = tf.getInstance();

					try {
						List<Status> statuses = twitter.getUserTimeline(user, paging);
						for (Status status : statuses) {
							/*
							 * System.out.println("@" +
							 * status.getUser().getScreenName() + " - " +
							 * status.getCreatedAt() + ":" + status.toString());
							 */
							bw.write(String.format("%s\n", status.toString().replace("\n", "").replace("\r", "")));
						}

					} catch (TwitterException te) {
						te.printStackTrace();
						System.out.println("Failed to get timeline: " + te.getMessage());
						apiKeys.get(keyIndex).printInfo();
						System.exit(-1);
					}
				}
				bw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void getEarliestTime() {
		try {
			BufferedWriter bw = new BufferedWriter(
					new FileWriter("/home/hoang/attt/results/empirical/earliestTime.csv"));
			File dir = new File("/home/hoang/attt/data/news_media_tweets");
			File[] files = dir.listFiles();
			for (File file : files) {
				BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()));
				String lastLine = null, line = null;
				while ((line = br.readLine()) != null) {
					lastLine = line;
				}
				br.close();
				System.out.printf("file = %s \t lastLine = %s\n", file.getName(), lastLine);
				String time = lastLine.split(",")[0].split("=")[1];
				bw.write(String.format("%s,%s\n", file.getName(), time));
			}

			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		// crawl();
		getEarliestTime();
	}
}
