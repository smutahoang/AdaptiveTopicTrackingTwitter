package hoang.l3s.attt.configure;

public class Configure {

	public final static int TWEET_MAX_NUMBER_CHARACTER = 150;

	public enum Author {
		HOANG, REN, NGUYEN
	}

	public enum RetentionTechnique {
		FORGET, QUEUE
	}

	public enum UpdatingScheme {
		PERIODIC, // update after every TIME_STEP_WIDTH
		TWEET_COUNT // update after every NUMBER_RECENT_RELEVANT_TWEETS
	}

	public enum SmoothingType {
		STUPID_BACKOFF, JELINEK_MERCER, BAYESIAN, ABSOLUTE_DISCOUNTING, NO_SMOOTHING
	}

	public final static Author AUTHOR = Author.HOANG;
	public final static boolean runningOnLocal = false;

	// constants for updating models
	public final static UpdatingScheme updatingScheme = UpdatingScheme.PERIODIC;
	public final static long TIME_STEP_WIDTH = 30 * 60 * 1000;// 30 mins;
	public final static int NUMBER_NEW_RELEVANT_TWEETS = 100;

	// constants for graph based model
	public final static int TEMPORAL_WINDOW = 12;
	// public final static double AMPLIFY_FACTOR = 1.029;// decay by a half per
	// timestep
	public final static double AMPLIFY_FACTOR = 1.0;// not decay
	public final static double BACKGROUND_TWEET_SAMPLING_RATIO = 0.2;
	public final static double PROPORTION_OF_KEYTERMS = 0.01;
	public final static double MAX_DEVIATION_FROM_MEAN_RELEVANT_SCORE = 6;
	public final static int MAX_NUMBER_KEY_TERMS = 10;
	public final static int MIN_NUMBER_KEY_TERMS = 2;

	// constants for language model
	// public static int nGram = 1;
	public final static SmoothingType SMOOTHING_TYPE = SmoothingType.ABSOLUTE_DISCOUNTING;
	public final static RetentionTechnique RETENTION_TECHNIQUE = RetentionTechnique.FORGET;

	public final static int QUEUE_CAPACITY = 1000;

	// constants for pseudo supervised model
	public static String RELEVANT_CLASS = "RELEVANT";
	public static String NONRELEVANT_CLASS = "NONRELEVANT";
	public static int NUMBER_EXCLUSION_TERMS = 200;
	public static int NONRELEVANT_TWEET_SAMPLING_RATIO = 10;
	public static double OLD_RELEVANT_TWEET_REMOVING_RATIO = 0.05;
	public static String MISSING_ATTRIBUTE = "[[MISS_ATTRIBUTE]]";
	public static String CLASS_ATTRIBUTE = "[[CLASS_ATTRIBUTE]]";
	public static String PROBLEM_NAME = "TWEET_CLASSIFICATION";
	public static int NUMBER_OLD_TWEET_REMOVING_WINDOW = 1;
	public static boolean USE_NEGATIVE_TWEET_FEATURE_SELECTION = false;

	public static String WORKING_DIRECTORY;
	public static String STOPWORD_PATH;
	public static String FIRST_TWEET_PATH;
	public static String STREAM_PATH;
	public static String OUTPUT_PATH;

	public Configure() {
		if (AUTHOR == Author.HOANG) {
			WORKING_DIRECTORY = "/home/hoang/attt";
			// WORKING_DIRECTORY = "E:/code/java/AdaptiveTopicTrackingTwitter";
		} else if (AUTHOR == Author.NGUYEN) {
			WORKING_DIRECTORY = "/home/hoang/attt";
			// WORKING_DIRECTORY = "E:/code/java/AdaptiveTopicTrackingTwitter";
		} else {
			if (runningOnLocal) {
				WORKING_DIRECTORY = "/Users/renlipeng/Documents/topicTracking";
			} else {
				WORKING_DIRECTORY = "/home/ren";
			}
		}

		Configure.STOPWORD_PATH = String.format("%s/data/stopwords", WORKING_DIRECTORY);
		Configure.FIRST_TWEET_PATH = String.format("%s/data/firstTweets/travel_ban.txt", WORKING_DIRECTORY);
		Configure.STREAM_PATH = String.format("%s/data/travel_ban", WORKING_DIRECTORY);
		Configure.OUTPUT_PATH = String.format("%s/output", WORKING_DIRECTORY);

	}

	// private static class SingletonHolder {
	// private static final Configure conf = new Configure();
	// }
	//
	// public static final Configure getConf() {
	// return SingletonHolder.conf;
	// }
}