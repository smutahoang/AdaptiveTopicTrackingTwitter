package hoang.l3s.attt.utils;

public class DescriptiveStats {
	private double mean;
	private double M2;
	private double variance;
	private int nSamples;

	public DescriptiveStats() {
		mean = 0;
		M2 = 0;
		nSamples = 0;
		variance = Double.NaN;
	}

	public double getMean() {
		return mean;
	}

	public double getVariance() {
		return variance;
	}

	public double getSqrtVariance() {
		return Math.sqrt(variance);
	}

	public int getNSamples() {
		return nSamples;
	}

	public void update(double x) {
		nSamples++;
		double delta = x - mean;
		mean += delta / nSamples;
		double delta2 = x - mean;
		M2 += delta * delta2;
		if (nSamples < 2) {
			variance = Double.NaN;
		} else {
			variance = M2 / (nSamples - 1);
		}
	}

	public static void main(String[] args) {
		DescriptiveStats dStat = new DescriptiveStats();
		for (int i = 1; i <= 10; i++) {
			dStat.update(i);
		}
		System.out.printf("mean = %f, var = %f sqrt(var) = %f\n", dStat.getMean(), dStat.getVariance(),
				dStat.getSqrtVariance());
	}
}
