package hoang.l3s.attt.model.graphbased;

import java.util.HashMap;

public class Term {
	private int nRelevantTweets;// #relavant tweets containing this term
	private int nAllTweets;// #(all) tweets containing this term
	private int lastUpdate;
	private boolean newFlag;
	private HashMap<Integer, AdjacentTerm> outTerms;
	private HashMap<Integer, AdjacentTerm> inTerms;
	private HashMap<Integer, AdjacentTerm> adjTerms;// undirected

	private double sumOutWeight;
	private double sumInWeight;
	private double sumAdjWeight;// undirected

	private double importance;

	public Term(int _lastUpdate) {
		nRelevantTweets = 0;
		nAllTweets = 0;
		lastUpdate = _lastUpdate;
		newFlag = true;
		outTerms = new HashMap<Integer, AdjacentTerm>();
		inTerms = new HashMap<Integer, AdjacentTerm>();
		adjTerms = new HashMap<Integer, AdjacentTerm>();
		sumOutWeight = 0;
		sumInWeight = 0;
		sumAdjWeight = 0;
		importance = -1;
	}

	public boolean isNew() {
		return newFlag;
	}

	public void age() {
		newFlag = false;
	}

	public void increaseNRelevantTweets(int delta) {
		nRelevantTweets += delta;
	}

	public void setNRelevantTweets(int n) {
		nRelevantTweets = n;
	}

	public int getNRelevantTweets() {
		return nRelevantTweets;
	}

	public void increaseNAllTweets(int delta) {
		nAllTweets += delta;
	}

	public void setNAllTweets(int n) {
		nAllTweets = n;
	}

	public int getNAllTweets() {
		return nAllTweets;
	}

	public void changeLastUpdate(int _lastUpdate) {
		lastUpdate = _lastUpdate;
	}

	public int getLastUpdate() {
		return lastUpdate;
	}

	public HashMap<Integer, AdjacentTerm> getOutTerms() {
		return outTerms;
	}

	public double getSumOutWeight() {
		return sumOutWeight;
	}

	public void addOutTerm(int desTerm, double w) {
		AdjacentTerm adjTerm = outTerms.get(desTerm);
		if (adjTerm == null) {
			outTerms.put(desTerm, new AdjacentTerm(w, -1));
		} else {
			adjTerm.addWeight(w);
		}
		sumOutWeight += w;
	}

	public void removeOutTerm(int desTerm) {
		double w = outTerms.remove(desTerm).getWeight();
		sumOutWeight -= w;
	}

	public double getOutTermWeight(int j) {
		AdjacentTerm adjacentTerm = outTerms.get(j);
		if (adjacentTerm == null) {
			return 0;
		} else {
			return adjacentTerm.getWeight();
		}
	}

	public HashMap<Integer, AdjacentTerm> getInTerms() {
		return inTerms;
	}

	public double getSumInWeight() {
		return sumInWeight;
	}

	public void addInTerm(int srcTerm, double w) {
		AdjacentTerm adjTerm = inTerms.get(srcTerm);
		if (adjTerm == null) {
			inTerms.put(srcTerm, new AdjacentTerm(w, -1));
		} else {
			adjTerm.addWeight(w);
		}
		sumInWeight += w;
	}

	public void removeInTerm(int srcTerm) {
		double w = inTerms.remove(srcTerm).getWeight();
		sumInWeight -= w;
	}

	public double getInTermWeight(int j) {
		AdjacentTerm adjacentTerm = inTerms.get(j);
		if (adjacentTerm == null) {
			return 0;
		} else {
			return adjacentTerm.getWeight();
		}
	}

	// undirected

	public HashMap<Integer, AdjacentTerm> getAdjTerms() {
		return adjTerms;
	}

	public double getSumAdjWeight() {
		return sumAdjWeight;
	}

	public void addAdjTerm(int term, double w) {
		AdjacentTerm adjTerm = adjTerms.get(term);
		if (adjTerm == null) {
			adjTerms.put(term, new AdjacentTerm(w, -1));
		} else {
			adjTerm.addWeight(w);
		}
		sumAdjWeight += w;
	}

	public void removeAdjTerm(int term) {
		double w = adjTerms.remove(term).getWeight();
		sumAdjWeight -= w;
	}

	public double getAdjTermWeight(int j) {
		AdjacentTerm adjTerm = adjTerms.get(j);
		if (adjTerm == null) {
			return 0;
		} else {
			return adjTerm.getWeight();
		}
	}

	public void setImportance(double _importance) {
		importance = _importance;
	}

	public double getImportance() {
		return importance;
	}
}
