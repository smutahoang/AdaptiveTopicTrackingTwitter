package hoang.l3s.attt.model.pseudosupervised;


import java.util.ArrayList;
import java.util.List;

import hoang.l3s.attt.model.Tweet;
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
	//private static int thresold = 10; // only terms appear more than 10 times as the features
	private TweetPreprocessingUtils preprocessingUtils;
	private ArrayList<Attribute> attributes;
	private ArrayList<Instance> instances;
	private SMO svm;
	public Classifier(List<Tweet> tweets) {
		// TODO Auto-generated constructor stub
	}
	
	public Classifier(List<Tweet> positiveSamples, List<Tweet> negativeSamples) {
		preprocessingUtils = new TweetPreprocessingUtils();
		attributes = featureSelection(positiveSamples);
		instances = getListOfInstances(positiveSamples, negativeSamples);
		trainingModel(attributes, instances);
	}
	
	
	// training model
	public void trainingModel(ArrayList<Attribute> attributes, ArrayList<Instance> instances) {

		Instances dataSet = new Instances("TrackingTweets", attributes, instances.size());
		dataSet.setClassIndex(attributes.size()-1);
		for(int i = 0; i< instances.size(); i++) {
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
		int nOfInstances =positiveSamples.size() + negativeSamples.size() ;
		int nOfAttributes = attributes.size();
		
		for(int i = 0; i<  nOfInstances; i++ )
			instances.add(new SparseInstance(nOfAttributes));
	
		//iterate all positive samples and add into dataset
		for(int i = 0; i<positiveSamples.size(); i++) {
						
			getInstance(positiveSamples.get(i), instances.get(i));
			instances.get(i).setValue(attributes.get(nOfAttributes-1), "relevant");
		}
		int nPositiveInstances = positiveSamples.size(); 
		
		//iterate all positive samples and add into dataset
		for(int i = 0; i<negativeSamples.size(); i++) {
			getInstance(negativeSamples.get(i), instances.get(nPositiveInstances + i));
			instances.get(nPositiveInstances+ i).setValue(attributes.get(attributes.size()-1), "nonrelevant");
		}
		
		return instances;
	}
	
	// get list of features 
	public ArrayList<Attribute> featureSelection(List<Tweet> positiveSamples) { 
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	//	HashMap<String, Integer> countTermMaps = new HashMap<String, Integer>();
		int nAttributes = 0;
		
		
//		for(Tweet tweet: positiveSamples) {
//			List<String> terms = tweet.getTerms(preprocessingUtils);
//			for(String term: terms) {
//				if(!countTermMaps.containsKey(term))
//					countTermMaps.put(term, 1);
//				else {
//					int count = countTermMaps.get(term);
//					
//					//only get 
//					if(count == thresold) {//should increase "count" before checking
//						attributes.add(new Attribute(term, nAttributes));
//						nAttributes++;
//					}
//					countTermMaps.put(term, count+1);
//				}
//			}
//		}
		
		for(Tweet tweet: positiveSamples) {
			List<String> terms = tweet.getTerms(preprocessingUtils);
			for(String term: terms) {
				if(!attributes.contains(new Attribute(term))) {
					attributes.add(new Attribute(term, nAttributes));
					nAttributes++;
				}
			}
		}
		attributes.add(new Attribute("MISS_ATT", nAttributes));// what if one of the above "term" is "miss"??
		//[[MISS_FEATURE]]
		ArrayList<String> classAtt = new ArrayList<String>();
		classAtt.add("relevant");
		classAtt.add("nonrelevant");
		attributes.add(new Attribute("CLASS_ATT", classAtt, nAttributes+1));
		return attributes;
	}

	// extract feature structure of a tweet
	public void getInstance (Tweet tweet, Instance instance) {
		List<String> terms = tweet.getTerms(preprocessingUtils);
		for(int j = 0; j < attributes.size()-1; j++) {
			Attribute att = attributes.get(j);
			if(!terms.contains(att.name())) 
				instance.setValue(att, 0);
			else
				instance.setValue(att, 1);	
		}
		
		
		int count = 0;
		
		
		for(String term: terms) {
			if(!attributes.contains(new Attribute(term))) 
				count++;
		}
	
		instance.setValue(attributes.get(attributes.size() - 2), ((double) count/terms.size()));
	}
	
	// get class of a new instance
	public String classify (Tweet tweet) {
		String result = "";
		Instances test = new Instances("TrackingTweets", attributes, 1);
		test.setClassIndex(attributes.size()-1);
		Instance ins = new SparseInstance(attributes.size());
		getInstance(tweet, ins);
		test.add(ins);
		try {
			result = test.classAttribute().value((int)svm.classifyInstance(test.get(0)));
			
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
