package hoang.l3s.attt.model.languagemodel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
import hoang.l3s.attt.model.languagemodel.LMSmoothingUtils.SmoothingType;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class LanguageModelBasedFilter extends FilteringModel {

	private LanguageModel bgLanguageModel;
	private int nGram;
	private TweetPreprocessingUtils preprocessingUtils;
	// private HashMap<String, HashMap<String, Double>> bgProbMap;
	private LMSmoothingUtils lmSmoothingUtils;
	private List<Tweet> updateBuffer;

	public LanguageModelBasedFilter(int _nGram) {
		nGram = _nGram;
	}

	public void init(List<Tweet> tweets) {
		preprocessingUtils = new TweetPreprocessingUtils();
		lmSmoothingUtils = new LMSmoothingUtils();
		// bgProbMap = new HashMap<String, HashMap<String, Double>>();
		bgLanguageModel = new LanguageModel(nGram, preprocessingUtils, lmSmoothingUtils);
		updateBuffer = new ArrayList<Tweet>();
		bgLanguageModel.train(tweets, SmoothingType.NoSmoothing);
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
		String filename = String.format("%s/language_model/model_%d.csv", Configure.outputPath,
				bgLanguageModel.getNUpdates());
		bgLanguageModel.save(filename);
	}

	public void updateForget(Tweet tweet) {
		updateBuffer.add(tweet);

		if (updateBuffer.size() >= Configure.updateBufferCapacity) {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~update");
			// bgLanguageModel.train(updateBuffer, Configure.smoothingType);
			LanguageModel fgLanguageModel = new LanguageModel(nGram, preprocessingUtils, lmSmoothingUtils);
			fgLanguageModel.countNGrams(updateBuffer);
			lmSmoothingUtils.update(bgLanguageModel, fgLanguageModel, Configure.smoothingType);
			bgLanguageModel.incNUpdates();
			updateBuffer.removeAll(updateBuffer);
		}
	}

	public void updataQueue(Tweet tweet) {
		updateBuffer.add(tweet);
		if (updateBuffer.size() >= Configure.queueCapacity) {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~update");
			// bgLanguageModel.train(updateBuffer, Configure.smoothingType);
			LanguageModel fgLanguageModel = new LanguageModel(nGram, preprocessingUtils, lmSmoothingUtils);
			fgLanguageModel.countNGrams(updateBuffer);
			lmSmoothingUtils.update(bgLanguageModel, fgLanguageModel, Configure.smoothingType);
			bgLanguageModel.incNUpdates();

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
		String filteredTweetFile = String.format("%s/language_model/lmFilteredTweets.txt", outputPath);
		Tweet tweet = null;
		while ((tweet = stream.getTweet()) != null) {

			double perplexity = bgLanguageModel.getPerplexity(tweet);

			if (perplexity > 0 && perplexity <= Configure.perplexityThreshold) {
				outputTweet(tweet, filteredTweetFile);
				update(tweet);
			}
		}
	}
}