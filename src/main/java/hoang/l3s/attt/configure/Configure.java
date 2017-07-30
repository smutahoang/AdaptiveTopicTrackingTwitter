package hoang.l3s.attt.configure;

public class Configure {

	public enum Author {
		hoang, ren
	}

	public final static Author author = Author.ren;
	public final static boolean runningOnLocal = false;
	

	private static String stopwordsPath;
	private static String firstTweetsPath;
	private static String streamPath;
	private static String outputPath;

	private static String dirPath;
	
	public static String getStopwordsPath() {
		return stopwordsPath;
	}

	public static void setStopwordsPath(String stopwordsPath) {
		Configure.stopwordsPath = stopwordsPath;
	}

	public static String getFirstTweetsPath() {
		return firstTweetsPath;
	}

	public static void setFirstTweetsPath(String firstTweetsPath) {
		Configure.firstTweetsPath = firstTweetsPath;
	}

	public static String getStreamPath() {
		return streamPath;
	}

	public static void setStreamPath(String streamPath) {
		Configure.streamPath = streamPath;
	}

	public static String getOutputPath() {
		return outputPath;
	}

	public static void setOutputPath(String outputPath) {
		Configure.outputPath = outputPath;
	}

	private Configure() {
		if (author == Author.hoang) {
			dirPath = "/home/hoang/attt";
		}else {
			if(runningOnLocal) {
				dirPath = "/Users/renlipeng/Documents/topicTracking";
			}else {
				dirPath = "/home/ren";
			}
		}
		
		Configure.setStopwordsPath(String.format("%s/data/stopwords",dirPath));
		Configure.setFirstTweetsPath(String.format("%s/data/firstTweets/travel_ban.txt",dirPath));
		Configure.setStreamPath(String.format("%s/data/travel_ban",dirPath));
		Configure.setOutputPath(String.format("%s/output",dirPath));
		
	}

	private static class SingletonHolder {
		private static final Configure conf = new Configure();
	}

	public static final Configure getConf() {
		return SingletonHolder.conf;
	}
}
