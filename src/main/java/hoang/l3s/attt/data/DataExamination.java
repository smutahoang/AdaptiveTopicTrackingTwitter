package hoang.l3s.attt.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataExamination {

	static boolean simpleKeywordMatch(String tweet) {
		if (tweet.contains("travel ban"))
			return true;
		if (tweet.contains("muslim ban"))
			return true;
		if (tweet.contains("#travelban"))
			return true;
		if (tweet.contains("#muslimban"))
			return true;
		if (tweet.contains("trump") && tweet.contains(" ban "))
			return true;
		return false;
	}

	static void countTweets(String gzFile) {
		try {
			int nLines = 0;
			int nEnTweets = 0;
			int nTweets = 0;
			InputStream is = new FileInputStream(gzFile);
			GZIPInputStream gzReader = new GZIPInputStream(is);
			BufferedReader br = new BufferedReader(new InputStreamReader(gzReader, Charset.forName("UTF-8")));
			JsonParser parser = new JsonParser();
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				nLines++;
				try {
					JsonObject jsonTweet = (JsonObject) parser.parse(line);
					if (jsonTweet.has("delete"))
						continue;

					if (!jsonTweet.get("lang").toString().equals("\"en\""))
						continue;
					nEnTweets++;

					if (!simpleKeywordMatch(jsonTweet.get("text").toString().toLowerCase()))
						continue;
					nTweets++;
					/*
					 * System.out.printf("%s\t%s\t%s\t%s\n",
					 * jsonTweet.get("created_at").toString(),
					 * jsonTweet.get("id").toString(),
					 * jsonTweet.get("lang").toString(),
					 * jsonTweet.get("text").toString());
					 */

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

	static void checkTimeOrder(String gzFile) {
		try {
			SimpleDateFormat dateTimeFormater = new SimpleDateFormat("\"EEE MMM dd HH:mm:ss +0000 yyyy\"");
			long preTime, postTime;
			String strPreTime, strPostTime;
			String preId, postId;

			InputStream is = new FileInputStream(gzFile);
			GZIPInputStream gzReader = new GZIPInputStream(is);
			BufferedReader br = new BufferedReader(new InputStreamReader(gzReader, Charset.forName("UTF-8")));
			JsonParser parser = new JsonParser();

			preTime = Long.MIN_VALUE;
			strPreTime = null;
			preId = null;
			int nPairs = 0;
			String line = null;
			while ((line = br.readLine()) != null) {
				try {
					JsonObject jsonTweet = (JsonObject) parser.parse(line);
					postId = jsonTweet.get("id").toString();
					strPostTime = jsonTweet.get("created_at").toString();
					postTime = dateTimeFormater.parse(strPostTime).getTime();

					if (preTime > postTime) {
						System.out.printf("preTime = %s \t preId = %s \t postTime = %s \t postId = %s\n", strPreTime,
								preId, strPostTime, postId);
						nPairs++;
					}

					preTime = postTime;
					strPreTime = strPostTime;
					preId = postId;
				} catch (Exception e) {
					// System.out.println("line = " + line);
					// e.printStackTrace();
					// System.exit(-1);
				}
			}

			br.close();
			gzReader.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		/*
		 * try { SimpleDateFormat dateTimeFormater = new SimpleDateFormat(
		 * "\"EEE MMM dd HH:mm:ss +0000 yyyy\""); String text =
		 * "\"Thu Jan 26 22:59:59 +0000 2017\"";
		 * System.out.println(dateTimeFormater.parse(text));
		 * 
		 * } catch (Exception e) { e.printStackTrace(); System.exit(-1); }
		 */

		File dir = new File("/home/hoang/attt/data/travel_ban");
		File[] files = dir.listFiles();
		for (File file : files) {
			if (!file.getName().endsWith(".gz"))
				continue;
			// countTweets(file.getAbsolutePath());
			checkTimeOrder(file.getAbsolutePath());
		}
	}
}
