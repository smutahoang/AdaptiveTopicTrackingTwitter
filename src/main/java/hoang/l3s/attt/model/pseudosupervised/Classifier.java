package hoang.l3s.attt.model.pseudosupervised;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.utils.KeyValue_Pair;
import hoang.l3s.attt.utils.RankingUtils;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

public class Classifier {

	/***
	 * to train a language model based on a set of tweets
	 * 
	 * @param tweets
	 */

	private TweetPreprocessingUtils preprocessingUtils;
	private ArrayList<Attribute> attributes;
	private ArrayList<Instance> instances;
	private SMO svm;

	private HashMap<String, Integer> attribute2Index;

	// utility variables
	private HashSet<Integer> indiceSet;

	public Classifier(List<Tweet> tweets) {
		// TODO Auto-generated constructor stub
	}

	public Classifier(List<Tweet> positiveSamples, List<Tweet> negativeSamples,
			TweetPreprocessingUtils _preprocessingUtils) {
		System.out.println("(Re-) training a classifer");
		// utility variables
		indiceSet = new HashSet<Integer>();

		preprocessingUtils = _preprocessingUtils;

		attributes = featureSelection(positiveSamples, negativeSamples);

		instances = getListOfInstances(positiveSamples, negativeSamples);

		trainingModel(attributes, instances);
	}

	// training model
	public void trainingModel(ArrayList<Attribute> attributes, ArrayList<Instance> instances) {

		Instances dataSet = new Instances(Configure.PROBLEM_NAME, attributes, instances.size());
		dataSet.setClassIndex(attributes.size() - 1);
		for (int i = 0; i < instances.size(); i++) {
			dataSet.add(instances.get(i));
		}
		System.out.println("=================Starting Training Model=========================");
		svm = new SMO();

		try {
			svm.buildClassifier(dataSet);
			System.out.println("=================Finishing Training Model=========================");
			// to print out attributes' weight

			/*double[] weights = svm.sparseWeights()[0][1];
			int[] indices = svm.sparseIndices()[0][1];
			for (int i = 0; i < indices.length; i++) {
				int j = indices[i];
				System.out.printf("attribute[%d]: name = %s weight = %f\n", j, attributes.get(j).name(), weights[i]);
			}

			System.exit(-1);*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}

	}

	// get list of instances
	public ArrayList<Instance> getListOfInstances(List<Tweet> positiveSamples, List<Tweet> negativeSamples) {
		ArrayList<Instance> instances = new ArrayList<Instance>();

		int nOfAttributes = attributes.size();

		// iterate all positive samples and add into dataset
		for (int i = 0; i < positiveSamples.size(); i++) {
			Instance instance = getSparseInstance(positiveSamples.get(i));
			instance.setValue(attributes.get(nOfAttributes - 1), Configure.RELEVANT_CLASS);
			instances.add(instance);
		}

		// iterate all positive samples and add into dataset
		for (int i = 0; i < negativeSamples.size(); i++) {
			Instance instance = getSparseInstance(negativeSamples.get(i));
			instance.setValue(attributes.get(nOfAttributes - 1), Configure.NONRELEVANT_CLASS);
			instances.add(instance);
		}
		return instances;
	}
	
	
	//get top k terms in negative samples
	public List<String> getTopKNegativeTerms(List<Tweet> negativeSamples, int k) {
		HashMap<String, Integer> negativeTermMap = new HashMap<String, Integer>();
		for(Tweet tweet: negativeSamples) {
			List<String> terms = tweet.getTerms(preprocessingUtils);
			for(String term: terms) {
				if(!negativeTermMap.containsKey(term))
					negativeTermMap.put(term, 1);
				else {
					int count = negativeTermMap.get(term);
					negativeTermMap.put(term, count+1);
				}
			}
			
		}
		PriorityBlockingQueue<KeyValue_Pair> queue = new PriorityBlockingQueue<KeyValue_Pair>();
		for (Map.Entry<String, Integer> negativeTerms : negativeTermMap.entrySet()) {
			String term = negativeTerms.getKey();
			Integer negativeTerm = negativeTerms.getValue();
			if (queue.size() < k) {
				queue.add(new KeyValue_Pair(term, negativeTerm));
			} else {
				KeyValue_Pair head = queue.peek();
				if (head.getIntKey() < negativeTerm) {
					queue.poll();
					queue.add(new KeyValue_Pair(term, negativeTerm));
				}
			}
		}

		List<String> topNegativeTerms = new ArrayList<String>();
		while (!queue.isEmpty()) {
			topNegativeTerms.add(queue.poll().getStrKey());
		}
		return topNegativeTerms;
	}
	// get list of features
	public ArrayList<Attribute> featureSelection(List<Tweet> positiveSamples, List<Tweet> negativeSamples) {

		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attribute2Index = new HashMap<String, Integer>();

		int attributeIndex = 0;
		for (Tweet tweet : positiveSamples) {
			List<String> terms = tweet.getTerms(preprocessingUtils);
			for (String term : terms) {
				if (!attribute2Index.containsKey(term)) {
					attributes.add(new Attribute(term, attributeIndex));
					attribute2Index.put(term, attributeIndex);
					attributeIndex++;
				}
			}
		}
		int nPositiveTerms = attributeIndex;
		if (Configure.USE_NEGATIVE_TWEET_FEATURE_SELECTION) {
//			for (Tweet tweet : negativeSamples) {
//				List<String> terms = tweet.getTerms(preprocessingUtils);
//				for (String term : terms) {
//					if (!attribute2Index.containsKey(term)) {
//						attributes.add(new Attribute(term, attributeIndex));
//						attribute2Index.put(term, attributeIndex);
//						attributeIndex++;
//					}
//				}
//			}
			List<String> topNegativeTerms = getTopKNegativeTerms(negativeSamples, nPositiveTerms*10);
			for (String term : topNegativeTerms) {
				if (!attribute2Index.containsKey(term)) {
					attributes.add(new Attribute(term, attributeIndex));
					attribute2Index.put(term, attributeIndex);
					attributeIndex++;
				}
			}
		}
//		System.out.printf(">>>>>>>>>>>>>>>>>>>Feature Selection:\n +\tNumber of positive features %d\n+\tNumber of negative features: %d\n+number of features: %d\n", nPositiveTerms, attributeIndex + 2 - nPositiveTerms, attributeIndex);
		attributes.add(new Attribute(Configure.MISSING_ATTRIBUTE, attributeIndex));
		attribute2Index.put(Configure.MISSING_ATTRIBUTE, attributeIndex);
		attributeIndex++;

		ArrayList<String> classAtt = new ArrayList<String>();

		classAtt.add(Configure.NONRELEVANT_CLASS);
		classAtt.add(Configure.RELEVANT_CLASS);
		

		attributes.add(new Attribute(Configure.CLASS_ATTRIBUTE, classAtt, attributeIndex));

		attribute2Index.put(Configure.CLASS_ATTRIBUTE, attributeIndex);

		return attributes;
	}

	public Instance getSparseInstance(Tweet tweet) {
		List<String> termsofTweet = tweet.getTerms(preprocessingUtils);


		indiceSet.clear();
		int count = 0;
		for (String term : termsofTweet) {
			if (attribute2Index.containsKey(term)) {
				indiceSet.add(attribute2Index.get(term));
			} else {
				count++;
			}
		}

		double[] attValues = new double[indiceSet.size() + 1];
		int[] indices = new int[indiceSet.size() + 1];
		int i = 0;
		for (int j : indiceSet) {
			attValues[i] = 1;
			indices[i] = j;
			i++;
		}

		// [[MISSING_ATTRIBUTE]]
		attValues[i] = ((double) count / termsofTweet.size());
		indices[i] = attributes.size() - 2;

		Instance ins = new SparseInstance(1.0, attValues, indices, attribute2Index.size());
		return ins;
	}

	// get class of a new instance
	public String classify(Tweet tweet) {

		System.out.printf("\n[Classification] tweet = %s\n", tweet.getText().replace('\n', ' '));
		String result = "";
		Instances test = new Instances(Configure.PROBLEM_NAME, attributes, 1);
		test.setClassIndex(attributes.size() - 1);

		Instance ins = getSparseInstance(tweet);

		test.add(ins);

		try {
			int v = (int) svm.classifyInstance(test.get(0));

			// TODO

			result = test.classAttribute().value(v);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);// this means v is not 0 or 1
		}
		return result;
	}

	public boolean predict(Tweet tweet) {
		return false;
	}
}