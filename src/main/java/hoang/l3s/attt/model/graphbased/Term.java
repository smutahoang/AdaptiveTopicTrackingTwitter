package hoang.l3s.attt.model.graphbased;

import java.util.HashMap;

public class Term {
	private int nRelevantTweets;// #relavant tweets containing this term
	private int nAllTweets;// #(all) tweets containing this term
	private int lastUpdate;
	private boolean newFlag;
	private HashMap<Integer, AdjacentTerm> outTerms;
	private HashMap<Integer, AdjacentTerm> inTerms;
	private double sumOutWeight;
	private double sumInWeight;
	private double rank;

	public Term(int _lastUpdate) {
		nRelevantTweets = 0;
		nAllTweets = 0;
		lastUpdate = _lastUpdate;
		newFlag = true;
		outTerms = new HashMap<Integer, AdjacentTerm>();
		inTerms = new HashMap<Integer, AdjacentTerm>();
		sumOutWeight = 0;
		sumInWeight = 0;
		rank = -1;
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

	public int getNRelevantTweets() {
		return nRelevantTweets;
	}

	public void increaseNAllTweets(int delta) {
		nAllTweets += delta;
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

	public void setRank(double _rank) {
		rank = _rank;
	}

	public double getRank() {
		return rank;
	}
}
