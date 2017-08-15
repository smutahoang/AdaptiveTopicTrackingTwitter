package hoang.l3s.attt.model.languagemodel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
import hoang.l3s.attt.model.languagemodel.LanguageModelSmoothing.SmoothingType;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class LanguageModelBasedFilter extends FilteringModel {

	private LanguageModel filter;
	private int nGram;
	private TweetPreprocessingUtils preprocessingUtils;
	// private HashMap<String, HashMap<String, Double>> bgProbMap;
	private LanguageModelSmoothing smoothing;
	private List<Tweet> updateBuffer;

	public LanguageModelBasedFilter(int _nGram) {
		nGram = _nGram;
	}

	public void init(List<Tweet> tweets) {
		preprocessingUtils = new TweetPreprocessingUtils();
		smoothing = new LanguageModelSmoothing();
		// bgProbMap = new HashMap<String, HashMap<String, Double>>();
		filter = new LanguageModel(nGram, preprocessingUtils, smoothing);
		updateBuffer = new ArrayList<Tweet>();
		filter.trainLM(tweets, SmoothingType.NoSmoothing);
	}

	public double relevantScore(Tweet tweet) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void update(Tweet tweet) {
		// TODO Auto-generated method stub
		switch (Configure.updateType) {
		case Forget:
			updateForget(tweet);
			break;
		case Queue:
			updataQueue(tweet);
			break;
		}

	}

	public void updateForget(Tweet tweet) {
		updateBuffer.add(tweet);

		if (updateBuffer.size() >= Configure.updateBufferCapacity) {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~update");
			filter.trainLM(updateBuffer, Configure.smoothingType);
			updateBuffer.removeAll(updateBuffer);
		}
	}

	public void updataQueue(Tweet tweet) {
		updateBuffer.add(tweet);
		if (updateBuffer.size() >= Configure.queueCapacity) {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~update");
			filter.trainLM(updateBuffer, Configure.smoothingType);
			// for (int i = 0; i < Configure.updateBufferCapacity; i++) {
			// updateBuffer.remove(0);
			// }
			int n = Configure.queueCapacity - Configure.updateBufferCapacity;
			for (int i = 0; i < n; i++) {
				updateBuffer.set(i, updateBuffer.get(i + Configure.updateBufferCapacity));
			}
			for (int i = 0; i < Configure.updateBufferCapacity; i++) {
				updateBuffer.remove(Configure.queueCapacity - i - 1);
			}
		}
	}

	public void filter(TweetStream stream, String outputPath) {
		// TODO Auto-generated method stub

		File file = new File(outputPath);
		if (file.exists()) {
			file.delete();
		}
		String filteredTweetFile = String.format("%s/language_model/filteredTweets.txt", outputPath);
		Tweet tweet = null;
		while ((tweet = stream.getTweet()) != null) {

			double perplexity = filter.getPerplexity(tweet);

			if (perplexity > 0 && perplexity <= Configure.perplexityThreshold) {
				outputTweet(tweet, filteredTweetFile);
				update(tweet);
			}
		}
	}
}