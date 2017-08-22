package hoang.l3s.attt.configure;

import hoang.l3s.attt.model.languagemodel.LMSmoothingUtils.SmoothingType;

public class Configure {

	public enum Author {
		hoang, ren, nguyen
	}

	public enum RetentionTechnique {
		Forget, Queue
	}

	public enum UpdatingScheme {
		Periodic, // update after every TIME_STEP_WIDTH
		TweetCount // update after every NUMBER_RECENT_RELEVANT_TWEETS
	}

	public final static Author author = Author.nguyen;
	public final static boolean runningOnLocal = false;

	// constants for updating models
	public final static UpdatingScheme updatingScheme = UpdatingScheme.TweetCount;
	public static long TIME_STEP_WIDTH = 30 * 60 * 1000;// 30 mins;
	public static int NUMBER_NEW_RELEVANT_TWEETS = 100;

	// constants for graph based model
	public static int TEMPORAL_WINDOW = 3;
	public static double AMPLIFY_FACTOR = 1.029;

	// constants for language model
	public static int nGram = 1;
	public static SmoothingType smoothingType = SmoothingType.AbsoluteDiscounting;
	public static RetentionTechnique updateType = RetentionTechnique.Forget;
	public static String startData = "2017-01-28";
	public static double perplexityThreshold;
	public static int QUEUE_CAPACITY = 1000;
	

	// constants for pseudo supervised model
	public static String rClassName = "RELEVANT";
	public static String nonrClassName = "NONRELEVANT";
	public static int nExclusionTerms = 200;
	public static int negativeSamplesRatio = 20;
	public static int removingRatio = 10;
	public static String missAttribute = "[[MISS_ATTRIBUTE]]";
	public static String classAttribute = "[[CLASS_ATTRIBUTE]]";
	public static String problemName = "TWEET_CLASSIFICATION";
	public static int nTweetsRemovedFromWindow = 1;

	public static int updatingTime = 360000; // update every hour
	
	public static String dirPath;
	public static String stopwordsPath;
	public static String firstTweetsPath;
	public static String streamPath;
	public static String outputPath;

	public Configure() {
		if (author == Author.hoang) {
			// dirPath = "/home/hoang/attt";
			dirPath = "E:/code/java/AdaptiveTopicTrackingTwitter";
		} else if (author == Author.nguyen) {
			dirPath = "/home/hoang/attt";
			// dirPath = "E:/code/java/AdaptiveTopicTrackingTwitter";
		}

		else {
			if (runningOnLocal) {
				dirPath = "/Users/renlipeng/Documents/topicTracking";
			} else {
				dirPath = "/home/ren";
			}
		}

		Configure.stopwordsPath = String.format("%s/data/stopwords", dirPath);
		Configure.firstTweetsPath = String.format("%s/data/firstTweets/travel_ban.txt", dirPath);
		Configure.streamPath = String.format("%s/data/travel_ban", dirPath);
		Configure.outputPath = String.format("%s/output", dirPath);

		if (Configure.nGram == 1) {
			Configure.perplexityThreshold = 200;
		} else {
			Configure.perplexityThreshold = 2;
		}
	}

	// private static class SingletonHolder {
	// private static final Configure conf = new Configure();
	// }
	//
	// public static final Configure getConf() {
	// return SingletonHolder.conf;
	// }
}