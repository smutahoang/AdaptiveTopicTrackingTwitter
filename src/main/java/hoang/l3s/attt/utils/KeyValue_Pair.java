package hoang.l3s.attt.utils;

public class KeyValue_Pair implements Comparable<KeyValue_Pair> {
	private int intKey;
	private String strKey;
	private double doubleValue;
	private String strValue;
	private int intValue;

	private enum ValueType {
		intType, doubleType
	};

	private ValueType valueType;

	public KeyValue_Pair(int _intKey, double _doubleValue) {
		intKey = _intKey;
		doubleValue = _doubleValue;
		valueType = ValueType.doubleType;
	}

	public KeyValue_Pair(String _strKey, double _doubleValue) {
		strKey = _strKey;
		doubleValue = _doubleValue;
		valueType = ValueType.doubleType;
	}

	public KeyValue_Pair(String _strKey, int _intValue) {
		strKey = _strKey;
		intValue = _intValue;
		valueType = ValueType.intType;
	}

	public int getIntKey() {
		return intKey;
	}

	public String getStrKey() {
		return strKey;
	}

	public double getDoubleValue() {
		return doubleValue;
	}

	public String getStrValue() {
		return strValue;
	}

	public int getIntValue() {
		return intValue;
	}

	public int compareTo(KeyValue_Pair o) {
		if (valueType == ValueType.doubleType) {
			if (o.getDoubleValue() > doubleValue)
				return -1;
			if (o.getDoubleValue() < doubleValue)
				return 1;
			return 0;
		} else if (valueType == ValueType.intType) {
			if (o.getIntValue() > intValue)
				return -1;
			if (o.getIntValue() < intValue)
				return 1;
			return 0;
		} else {
			return strValue.compareTo(o.getStrValue());
		}
	}

}
