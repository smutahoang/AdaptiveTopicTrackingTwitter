package hoang.l3s.attt.model.languagemodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class LanguageModelBasedFilter extends FilteringModel {

	private LanguageModel bgLanguageModel;
	private int nGram;
	private TweetPreprocessingUtils preprocessingUtils;
	// private HashMap<String, HashMap<String, Double>> bgProbMap;
	private LMSmoothingUtils lmSmoothingUtils;
	private List<Tweet> buffer;
	private double threshold = 200;

	public LanguageModelBasedFilter(int _nGram) {
		nGram = _nGram;
	}

	public void init(List<Tweet> recentRelevantTweets) {
		preprocessingUtils = new TweetPreprocessingUtils();
		lmSmoothingUtils = new LMSmoothingUtils();
		bgLanguageModel = new LanguageModel(nGram, preprocessingUtils, lmSmoothingUtils);
		buffer = recentRelevantTweets;
		bgLanguageModel.train(buffer, Configure.SmoothingType.NO_SMOOTHING);

		String filename = String.format("%s/language_model/model_%d.csv", Configure.OUTPUT_PATH,
				bgLanguageModel.getNUpdates());
		bgLanguageModel.save(filename);
	}

	public double relevantScore(Tweet tweet) {
		return bgLanguageModel.getPerplexity(tweet);
	}

	public void update(Tweet tweet) {
		// TODO Auto-generated method stub
		switch (Configure.RETENTION_TECHNIQUE) {
		case FORGET:
			updateForget(tweet);
			break;
		case QUEUE:
			updateQueue(tweet);
			break;
		}
		String filename = String.format("%s/language_model/model_%d.csv", Configure.OUTPUT_PATH,
				bgLanguageModel.getNUpdates());
		bgLanguageModel.save(filename);
	}

	public void updateForget(Tweet tweet) {
		buffer.add(tweet);

		if (buffer.size() >= Configure.NUMBER_NEW_RELEVANT_TWEETS) {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~update");
			// bgLanguageModel.train(updateBuffer, Configure.smoothingType);
			LanguageModel fgLanguageModel = new LanguageModel(nGram, preprocessingUtils, lmSmoothingUtils);
			fgLanguageModel.countNGrams(buffer);
			lmSmoothingUtils.update(bgLanguageModel, fgLanguageModel, Configure.SMOOTHING_TYPE);
			bgLanguageModel.incNUpdates();
			buffer.removeAll(buffer);
		}
	}

	public void updateQueue(Tweet tweet) {
		buffer.add(tweet);
		if (buffer.size() >= Configure.QUEUE_CAPACITY) {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~update");
			// bgLanguageModel.train(updateBuffer, Configure.smoothingType);
			LanguageModel fgLanguageModel = new LanguageModel(nGram, preprocessingUtils, lmSmoothingUtils);
			fgLanguageModel.countNGrams(buffer);
			lmSmoothingUtils.update(bgLanguageModel, fgLanguageModel, Configure.SMOOTHING_TYPE);
			bgLanguageModel.incNUpdates();

			int n = Configure.QUEUE_CAPACITY - Configure.NUMBER_NEW_RELEVANT_TWEETS;
			for (int i = 0; i < n; i++) {
				buffer.set(i, buffer.get(i + Configure.NUMBER_NEW_RELEVANT_TWEETS));
			}
			for (int i = 0; i < Configure.NUMBER_NEW_RELEVANT_TWEETS; i++) {
				buffer.remove(buffer.size() - 1);
			}
		}
	}

	private void outputCandidateTweet(Tweet tweet, double score, String outputPath) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath, true));
			bw.write(String.format("%f,%s\n", score,
					tweet.getText().replace('\n', ' ').replace('\r', ' ').replace(',', ' ')));
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void filter(TweetStream stream, String outputPath, String dataset) {
		System.out.println("determining start time");
		super.setStartTime(stream, buffer.get(buffer.size() - 1));
		System.out.println("done!");

		File file = new File(outputPath);
		if (file.exists()) {
			file.delete();
		}
		String filteredTweetFile = String.format("%s/language_model/lmFilteredTweets.txt", outputPath);
		String candidateTweetFile = String.format("%s/language_model/candidateTweets.csv", outputPath);
		Tweet tweet = null;
		while ((tweet = stream.getTweet()) != null) {
			if (tweet.getTerms(preprocessingUtils).size() == 0)
				continue;
			double s = relevantScore(tweet);
			if (s <= threshold) {
				outputTweet(tweet, filteredTweetFile);
				update(tweet);
			}
			outputCandidateTweet(tweet, s, candidateTweetFile);
		}
	}
}