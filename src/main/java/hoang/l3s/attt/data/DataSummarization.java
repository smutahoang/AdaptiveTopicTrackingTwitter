package hoang.l3s.attt.configure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hoang.l3s.attt.model.Tweet;

public class DataSummarization {

	public static String dateTimeFormat = "\"EEE MMM dd HH:mm:ss +0000 yyyy\"";
	static SimpleDateFormat dateTimeFormater = new SimpleDateFormat(dateTimeFormat);
	static HashMap<String, Integer> eventPublishedTimeCount = new HashMap<String, Integer>();
	// get hashMap of event id and list of tweet ids of each event
	static HashMap<String, HashSet<String>> getEventTweetIdMap(String path) {
		HashMap<String, HashSet<String>> eventTweetIdMap = new HashMap<String, HashSet<String>>();
		try {
			FileReader in;
			in = new FileReader(new File(path));
			BufferedReader buff = new BufferedReader(in);

			String line = null;
			String[] newTweetId;
			String tweetId, eventId;
			while ((line = buff.readLine()) != null) {
				newTweetId = line.split("\t");
				eventId = newTweetId[0].trim();
				tweetId = newTweetId[1].trim();
				if (eventTweetIdMap.containsKey(eventId)) {
					eventTweetIdMap.get(eventId).add(tweetId);

				} else {
					HashSet<String> tweetIdList = new HashSet<String>();
					tweetIdList.add(tweetId);
					eventTweetIdMap.put(eventId, tweetIdList);
				}
			}

			in.close();
			buff.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		return eventTweetIdMap;
	}

	// get HashMap of events and descriptions
	public static HashMap<String, String> getEventDescriptionMap(String path) {
		HashMap<String, String> eventDescriptionMap = new HashMap<String, String>();
		try {
			BufferedReader buff = new BufferedReader(new FileReader(new File(path)));
			String line = null;
			String[] eventDescription;
			String eventId;
			String description;
			while ((line = buff.readLine()) != null) {
				eventDescription = line.split("\t");
				eventId = eventDescription[0].trim();
				description = eventDescription[1].trim();
				eventDescriptionMap.put(eventId, description);
			}
			buff.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}

		return eventDescriptionMap;

	}

	// get HashMap of event ids and earliest/lasted tweets
	public static HashMap<String, Tweet[]> readTweetStream(String path,
			HashMap<String, HashSet<String>> eventTweetIdMap) {
		HashMap<String, Tweet[]> eventTimePointsMap = new HashMap<String, Tweet[]>();
		
		
		// get list of file in streaam folder
		File dir = new File(path);
		File[] fileList = dir.listFiles();

		BufferedReader buff;
		JsonParser parser = new JsonParser();
		String postId, strPostTime, text, userId;
		long postTime;
		HashSet<String> tweetIds;


		for (File file : fileList) {
			if (file.isDirectory())
				continue;
			try {
				System.out.println();
				buff = new BufferedReader(new FileReader(file));
				String line = null;
				System.out.println("\n???????????????????????"+ file.getName()+"????????????????????????????????\n");
				while ((line = buff.readLine()) != null) {
				
					JsonObject jsonTweet = (JsonObject) parser.parse(line);
					postId = jsonTweet.get("id").toString();
					strPostTime = jsonTweet.get("created_at").toString();
					postTime = dateTimeFormater.parse(strPostTime).getTime();
					userId = ((JsonObject) jsonTweet.get("user")).get("id").getAsString();
					text = jsonTweet.get("text").getAsString();
					
					for (Map.Entry<String, HashSet<String>> event : eventTweetIdMap.entrySet()) {
						if (event.getValue().contains(postId)) {
							if (!eventTimePointsMap.containsKey(event.getKey())) {
								Tweet[] tweets = new Tweet[2];
								tweets[0] = new Tweet(postId, text, userId, postTime);
								
								tweets[1] = new Tweet(postId, text, userId, postTime);
								
								eventTimePointsMap.put(event.getKey(), tweets);
								System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>put:" + event.getKey());
								eventPublishedTimeCount.put(event.getKey(), 1);
							//	System.out.println("...."+eventPublishedTimeCount.get(event.getKey()));
							
							} else {
								Tweet[] tweets = eventTimePointsMap.get(event.getKey());
								if (postTime < tweets[0].getPublishedTime()) {
									tweets[0] = new Tweet(postId, text,
											((JsonObject) jsonTweet.get("user")).get("id").getAsString(), postTime);
									
								} else if (postTime > tweets[1].getPublishedTime()) {
									tweets[1] = new Tweet(postId, text,
											((JsonObject) jsonTweet.get("user")).get("id").getAsString(), postTime);

								}
								System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>update:" + event.getKey());
								eventPublishedTimeCount.put(event.getKey(), eventPublishedTimeCount.get(event.getKey()) + 1);
							//	System.out.println("...."+eventPublishedTimeCount.get(event.getKey()));

							}
						}
					}

				}
				buff.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
		return eventTimePointsMap;
	}
	

	public static void main(String[] args) {

		String eventIdPath = "/home/hnguyen/proj/data/extracted_tweets/Events2012/relevant_tweets.tsv";
		String tweetStreamPath = "/home/hnguyen/proj/data/extracted_tweets";
		String eventDescriptionPath = "/home/hnguyen/proj/data/extracted_tweets/Events2012/event_descriptions.tsv";

		HashMap<String, HashSet<String>> eventTweetIdMap = getEventTweetIdMap(eventIdPath);
		HashMap<String, String> eventTweetDescriptionMap = getEventDescriptionMap(eventDescriptionPath);

		HashMap<String, Tweet[]> eventTimePointsMap = readTweetStream(tweetStreamPath, eventTweetIdMap);
		System.out.println(eventTimePointsMap.size());
		System.out.printf("%-10s%10s\t%35s\t%35s\t%s\n", "Event Id", "Count", "Start time", "End time", "Event Description");
		for (Map.Entry<String, Tweet[]> eventId : eventTimePointsMap.entrySet()) {
			String key = eventId.getKey();

			System.out.printf("%-10s%5d,%4d\t%35s\t%35s\t%s\n", key,
					eventTweetIdMap.get(key).size(), eventPublishedTimeCount.get(key), dateTimeFormater.format(new Date((eventId.getValue()[0]).getPublishedTime())),
					dateTimeFormater.format(new Date((eventId.getValue()[1]).getPublishedTime())), eventTweetDescriptionMap.get(key));
		}
		
	
	}
}
