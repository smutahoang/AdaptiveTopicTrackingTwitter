package hoang.l3s.attt.model.languagemodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;

public class LanguageModelBasedFilter extends FilteringModel {

	private LanguageModel filter;
	private int nGram;
	private TweetPreprocessingUtils preprocessingUtils;
	private HashMap<String, Double> unigramProMap;
	private HashMap<String, HashMap<String, Double>> ngramProMap;

	public LanguageModelBasedFilter(int ngram) {
		nGram = ngram;
	}

	public void init(List<Tweet> tweets) {
		// TODO Auto-generated method stub
		preprocessingUtils = new TweetPreprocessingUtils();
		filter = new LanguageModel(nGram, preprocessingUtils);
		trainingLM(tweets);
	}

	public void trainingLM(List<Tweet> tweets) {
		if (nGram == 1) {
			// unigramProMap.clear();
			unigramProMap = filter.trainUnigramLM(tweets);
		} else {
			// ngramProMap.clear();
			ngramProMap = filter.trainNgramLM(tweets);
		}
	}

	public double relevantScore(Tweet tweet) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void update(Tweet tweet) {
		// TODO Auto-generated method stub

	}

	public List<Double> getProbilities(Tweet tweet) {
		List<Double> probilitiesList = new ArrayList<Double>();
		List<String> terms = tweet.getTerms(preprocessingUtils);
		System.out.println("term size:" + terms.size());
		if (nGram == 1) {
			for (int i = 0; i < terms.size(); i++) {
				String term = terms.get(i);
				if (unigramProMap.containsKey(term)) {
					probilitiesList.add(unigramProMap.get(term));
					System.out.println("pro:" + unigramProMap.get(term));
				}
			}
		} else {
			for (int i = 0; i < terms.size() - nGram + 1; i++) {
				StringBuffer preTerm = new StringBuffer("");
				for (int j = 0; j < nGram - 1; j++) {
					preTerm.append(terms.get(i + j) + " ");
				}
				String term = terms.get(i + nGram - 1);
				System.out.println(term + "|" + preTerm);

				if (ngramProMap.containsKey(preTerm.toString())) {
					HashMap<String, Double> termProMap = ngramProMap.get(preTerm.toString());
					if (termProMap.containsKey(term)) {
						probilitiesList.add(termProMap.get(term));
						System.out.println("-----------pro:" + termProMap.get(term));
					}
				}
			}
		}

		return probilitiesList;
	}

	// public double getPerplexity(List<Double> proList) {
	// double perplexity = 0;
	// double product = 0;
	// int count = proList.size();
	// for (int i = 0; i < count; i++) {
	// product = product * proList.get(i);
	// System.out.println("pro:" + proList.get(i));
	// }
	// System.out.println("product:" + product);
	// if (count != 0) {
	// perplexity = Math.pow(product, -1.0 / count);
	// }
	//
	// return perplexity;
	//
	// }

	public double getPerplexity(List<Double> proList) {
		double perplexity = 0;
		double sum = 0;
		int count = proList.size();
		for (int i = 0; i < count; i++) {
			double pro = proList.get(i);
			sum += Math.log(pro) / Math.log(2);
			System.out.print("a pro:" + pro + " log:" + Math.log(pro) / Math.log(2) + " sum:" + sum + "\n");
		}
		if (count != 0) {
			sum = sum * (-1.0 / count);
			System.out.println("sum:" + sum);
			perplexity = Math.pow(2, sum);
		}

		return perplexity;
	}

	public void updateForget(Tweet tweet,List<Tweet> histories) {
		histories.add(tweet);

		if (histories.size() >= 100) {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~update");
			trainingLM(histories);
			histories.removeAll(histories);
		}
	}

	public void updataQueue(Tweet tweet,List<Tweet> histories) {
		histories.add(tweet);
		if (histories.size() >= 100) {
			histories.remove(0);
			histories.add(tweet);
			trainingLM(histories);
		}
	}

	public void filter(TweetStream stream, String ouputPath) {
		// TODO Auto-generated method stub

		BufferedWriter out = null;
		try {
			File file = new File(ouputPath);
			if (file.exists()) {
				file.delete();
			}
			out = new BufferedWriter(new FileWriter(file, true));
			List<Tweet> histories = new ArrayList<Tweet>();
			while (true) {
				Tweet tweet = stream.getTweet();

				double perplexity = getPerplexity(getProbilities(tweet));
				System.out.println("perplexity:" + perplexity);
				int maxPerplexity;
				if(nGram == 1) {
					maxPerplexity = 200;
				}else {
					maxPerplexity = 2;
				}
				if (perplexity > 0 && perplexity < maxPerplexity) {
					updateForget(tweet,histories);
					// updataQueue(tweet,histories);
					out.write(tweet.getText());
					out.write('\n');
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
