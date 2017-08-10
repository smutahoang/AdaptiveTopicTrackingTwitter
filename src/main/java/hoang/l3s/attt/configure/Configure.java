package hoang.l3s.attt.configure;

import hoang.l3s.attt.model.languagemodel.LanguageModelSmoothing.SmoothingType;

public class Configure {

	public enum Author {
		hoang, ren, nguyen
	}

	public enum UpdateType {
		Forget, Queue
	}

	public final static Author author = Author.hoang;
	public final static boolean runningOnLocal = false;

	public static int nGram = 1;
	public static String startData = "2017-01-28";
	public static double perplexityThreshold;
	public static int queueCapacity = 1000;
	public static int updateBufferCapacity = 100;
	public static SmoothingType smoothingType = SmoothingType.AbsoluteDiscounting;
	public static UpdateType updateType = UpdateType.Forget;

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
			// dirPath = "/home/hoang/attt";
			dirPath = "E:/code/java/AdaptiveTopicTrackingTwitter";
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