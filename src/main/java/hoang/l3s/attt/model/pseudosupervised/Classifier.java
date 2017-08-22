package hoang.l3s.attt.model.pseudosupervised;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.BinarySparseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

public class Classifier {

	/***
	 * to train a language model based on a set of tweets
	 * 
	 * @param tweets
	 */

	// private static int thresold = 10; // only terms appear more than 10 times
	// as the features
	private TweetPreprocessingUtils preprocessingUtils;
	private ArrayList<Attribute> attributes;
	private ArrayList<Instance> instances;
	private SMO svm;

	private HashMap<String, Integer> attribute2Index;

	public Classifier(List<Tweet> tweets) {
		// TODO Auto-generated constructor stub
	}

	public Classifier(List<Tweet> positiveSamples, List<Tweet> negativeSamples,
			TweetPreprocessingUtils _preprocessingUtils) {
                                   
		preprocessingUtils = _preprocessingUtils;

		attributes = featureSelection(positiveSamples, negativeSamples);

		instances = getListOfInstances(positiveSamples, negativeSamples);

		trainingModel(attributes, instances);
	}

	// training model
	public void trainingModel(ArrayList<Attribute> attributes, ArrayList<Instance> instances) {

		Instances dataSet = new Instances(Configure.problemName, attributes, instances.size());
		dataSet.setClassIndex(attributes.size() - 1);
		for (int i = 0; i < instances.size(); i++) {
			dataSet.add(instances.get(i));
		}
		svm = new SMO();
		try {
			svm.buildClassifier(dataSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// get list of instances
	public ArrayList<Instance> getListOfInstances(List<Tweet> positiveSamples, List<Tweet> negativeSamples) {
		ArrayList<Instance> instances = new ArrayList<Instance>();

		int nOfInstances = positiveSamples.size() + negativeSamples.size();
		int nOfAttributes = attributes.size();


		//for (int i = 0; i < nOfInstances; i++)
		//	instances.add(new SparseInstance(nOfAttributes));

		// iterate all positive samples and add into dataset
		for (int i = 0; i < positiveSamples.size(); i++) {
			
			instances.add(getInstance(positiveSamples.get(i)));
			instances.get(i).setValue(attributes.get(nOfAttributes - 1), Configure.rClassName);
		}
		int nPositiveInstances = positiveSamples.size();

		// iterate all positive samples and add into dataset
		for (int i = 0; i < negativeSamples.size(); i++) {
			instances.add(getInstance(negativeSamples.get(i)));
			instances.get(nPositiveInstances + i).setValue(attributes.get(attributes.size() - 1),
					Configure.nonrClassName);
		}
		return instances;
	}

	// get list of features
	public ArrayList<Attribute> featureSelection(List<Tweet> positiveSamples, List<Tweet> negativeSamples) {
		
		ArrayList<Tweet> samples = new ArrayList<Tweet>();
		samples.addAll(positiveSamples);
		//samples.addAll(negativeSamples);
		
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attribute2Index = new HashMap<String, Integer>();
		
		int attributeIndex = 0;
		for (Tweet tweet : samples) {
			List<String> terms = tweet.getTerms(preprocessingUtils);
			for (String term : terms) {
				if (!attribute2Index.containsKey(term)) {
					attributes.add(new Attribute(term, attributeIndex));
					attribute2Index.put(term, attributeIndex);
					attributeIndex++;
				}
			}
		}

		attributes.add(new Attribute(Configure.missAttribute, attributeIndex));
		attribute2Index.put(Configure.missAttribute, attributeIndex);
		attributeIndex++;

		ArrayList<String> classAtt = new ArrayList<String>();
		classAtt.add(Configure.rClassName);
		classAtt.add(Configure.nonrClassName);
		attributes.add(new Attribute(Configure.classAttribute, classAtt, attributeIndex));
		attribute2Index.put(Configure.classAttribute, attributeIndex);

		return attributes;
	}

	// -- extract feature structure of a tweet
	public Instance getInstance(Tweet tweet) {
		List<String> termsofTweet = tweet.getTerms(preprocessingUtils);
		Instance ins = new BinarySparseInstance(attributes.size());
		int count = 0;
		for (String term : termsofTweet) {
			if (attribute2Index.containsKey(term)) {
				ins.setValue(attribute2Index.get(term), 0);
			} else {
				count++;
			}
		}

		// [[MISS]] attribute
		ins.setValue(attributes.get(attributes.size() - 2), ((double) count / termsofTweet.size()));
		return ins;

	}

	// get class of a new instance
	public String classify(Tweet tweet) {
		String result = "";
		Instances test = new Instances(Configure.problemName, attributes, 1);
		test.setClassIndex(attributes.size() - 1);
		
		Instance ins = getInstance(tweet);
		test.add(ins);
		try {
			result = test.classAttribute().value((int) svm.classifyInstance(test.get(0)));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public boolean predict(Tweet tweet) {
		return false;
	}
}