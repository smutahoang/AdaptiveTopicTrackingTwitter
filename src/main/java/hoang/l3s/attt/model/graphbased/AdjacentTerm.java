package hoang.l3s.attt.model.graphbased;

public class AdjacentTerm {
	private double weight;
	private double probability;

	public AdjacentTerm(double _weight, double _probability) {
		weight = _weight;
		probability = _probability;
	}

	public double getWeight() {
		return weight;
	}

	public void addWeight(double w) {
		weight += w;
	}

	public double getProbability() {
		return probability;
	}
}
