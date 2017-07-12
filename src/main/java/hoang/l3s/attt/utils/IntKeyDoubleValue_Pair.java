package hoang.l3s.attt.utils;

public class IntKeyDoubleValue_Pair implements Comparable<IntKeyDoubleValue_Pair> {
	private int key;
	private double value;

	public IntKeyDoubleValue_Pair(int _key, double _value) {
		key = _key;
		value = _value;
	}

	public int getKey() {
		return key;
	}

	public double getValue() {
		return value;
	}

	public int compareTo(IntKeyDoubleValue_Pair o) {
		if (o.getValue() > value)
			return -1;
		if (o.getValue() < value)
			return 1;
		return 0;
	}

}
