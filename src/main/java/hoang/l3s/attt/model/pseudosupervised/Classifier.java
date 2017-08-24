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
		System.out.println("(Re-) training a classifer");

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
			// weight[i] is now weight of attribute indices[i]

			for (int i = 0; i < indices.length; i++) {
				int j = indices[i];
				System.out.printf("attribute[%d]: name = %s weight = %f\n", j, attributes.get(j).name(), weights[i]);
			}
*/
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

		if (Configure.USE_NEGATIVE_TWEET_FEATURE_SELECTION) {
			for (Tweet tweet : negativeSamples) {
				List<String> terms = tweet.getTerms(preprocessingUtils);
				for (String term : terms) {
					if (!attribute2Index.containsKey(term)) {
						attributes.add(new Attribute(term, attributeIndex));
						attribute2Index.put(term, attributeIndex);
						attributeIndex++;
					}
				}
			}
		}

		attributes.add(new Attribute(Configure.MISSING_ATTRIBUTE, attributeIndex));
		attribute2Index.put(Configure.MISSING_ATTRIBUTE, attributeIndex);
		attributeIndex++;

		ArrayList<String> classAtt = new ArrayList<String>();

		classAtt.add(Configure.RELEVANT_CLASS);
		classAtt.add(Configure.NONRELEVANT_CLASS);

		attributes.add(new Attribute(Configure.CLASS_ATTRIBUTE, classAtt, attributeIndex));

		attribute2Index.put(Configure.CLASS_ATTRIBUTE, attributeIndex);

		return attributes;
	}

	public Instance getSparseInstance(Tweet tweet) {
		List<String> termsofTweet = tweet.getTerms(preprocessingUtils);

		// Tuan-Anh: use other constructor for constructing sparse instance
		// SparseInstance(double weight, double[] attValues, int[] indices, int
		// maxNumValues)
		
		double[] attValues = new double[attributes.size()];
		Instance ins = new SparseInstance(1, attValues);
		//Instance ins = new SparseInstance(attributes.size());

		int count = 0;
		for (String term : termsofTweet) {
			if (attribute2Index.containsKey(term)) {
				ins.setValue(attribute2Index.get(term), 1);
			} else {
				count++;
			}
		}

		// [[MISSING_ATTRIBUTE]]
		ins.setValue(attributes.size()-2, ((double) count / termsofTweet.size()));
		
		return ins;

	}

	// get class of a new instance
	public String classify(Tweet tweet) {
		
		System.out.printf("\n\n[Classification] tweet = %s\n", tweet.getText().replace('\n', ' '));

		if (tweet.getTerms(preprocessingUtils).size() == 0)
			return Configure.NONRELEVANT_CLASS;

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