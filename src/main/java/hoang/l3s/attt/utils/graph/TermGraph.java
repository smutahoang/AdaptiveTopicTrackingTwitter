package hoang.l3s.attt.utils.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.PriorityBlockingQueue;

import hoang.l3s.attt.configure.Configure;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.utils.TweetPreprocessingUtils;
import hoang.l3s.attt.utils.KeyValue_Pair;
import hoang.l3s.attt.utils.RankingUtils;
import hoang.l3s.attt.utils.SimilarityUtils;

public class TermGraph {
	String[] strTerms;
	HashMap<String, Integer> term2Index;
	List<Term> adjList;
	Stack<Integer> blankIndex;

	private int nActiveTerms;
	private int nNewTerms;
	private int sumNewTermFrequent;

	private final int TERM_WINDOW_SIZE = 4;
	private final double DAMPING_FACTOR = 0.15;
	private final double ADAPTIVE_FACTOR = 0.05;
	private final int MAX_ITERATIONS = 20;
	private final int MAX_NUMBER_TERMS = 1000000;

	private boolean verbose = false;
	private boolean predefineKeywordMatch = false;
	private boolean simpleTermImportance = true;
	private boolean keytermFlag = true;

	private int maxNKeyTerms;

	// utility variables
	private HashSet<Integer> termIndexSubSet;

	private int[] termIndexes;// temp array of index a tweet' terms in
								// term2Index map

	private HashSet<Integer> topImportantTerms;// pagerank
	private HashSet<Integer> keyTerms;// pagerank - idf

	/***
	 * initialize variables
	 */
	private void init() {
		strTerms = new String[MAX_NUMBER_TERMS];
		term2Index = new HashMap<String, Integer>();
		adjList = new ArrayList<Term>();
		blankIndex = new Stack<Integer>();
		nActiveTerms = 0;
		nNewTerms = 0;
		sumNewTermFrequent = 0;

		for (int i = MAX_NUMBER_TERMS - 1; i >= 0; i--) {
			blankIndex.push(i);
		}

		termIndexes = new int[100];
		keyTerms = null;

		termIndexSubSet = new HashSet<Integer>();
	}

	public int getNActiveTerms() {
		return nActiveTerms;
	}

	/***
	 * return index of a term if the term exists in term2Index map, or -1
	 * otherwise
	 * 
	 * @param term
	 * @return
	 */
	private int getTermIndex(String term) {
		try {
			return term2Index.get(term);
		} catch (Exception e) {
			return -1;
		}
	}

	/***
	 * add a term with time label "time", if term already exists, update its
	 * lastUpdate
	 * 
	 * @param term
	 * @param time
	 * @return index of the term in adjList
	 */
	private int addTerm(String term, int time) {
		if (term2Index.containsKey(term)) {
			int index = term2Index.get(term);
			Term termInfo = adjList.get(index);
			termInfo.changeLastUpdate(time);
			if (termInfo.isNew()) {
				sumNewTermFrequent++;
			}
			return index;
		}
		// new term
		int index = blankIndex.pop();
		strTerms[index] = term;
		term2Index.put(term, index);
		if (index < adjList.size()) {
			adjList.set(index, new Term(time));
		} else {
			adjList.add(index, new Term(time));
		}
		nActiveTerms++;
		nNewTerms++;
		sumNewTermFrequent++;
		return index;
	}

	/***
	 * add an edge from srcTerm to desTerm
	 * 
	 * @param srcTerm
	 * @param desTerm
	 * @param weight
	 */
	private void addEdge(int srcTerm, int desTerm, double weight) {
		// out-going edge
		Term term = adjList.get(srcTerm);
		if (term == null) {
			System.out.printf("index = %d, term = %s\n", srcTerm, strTerms[srcTerm]);
		}
		term.addOutTerm(desTerm, weight);

		// in-going edge
		term = adjList.get(desTerm);
		if (term == null) {
			System.out.printf("index = %d, term = %s\n", desTerm, strTerms[desTerm]);
		}
		term.addInTerm(srcTerm, weight);

	}

	/***
	 * construct a term graph from a set of tweets
	 * 
	 * @param tweets
	 */
	public TermGraph(List<Tweet> tweets, TweetPreprocessingUtils preprocessingUtils) {
		init();
		for (Tweet tweet : tweets) {
			List<String> terms = tweet.getTerms(preprocessingUtils);
			int nTerms = terms.size();
			for (int j = 0; j < nTerms; j++) {
				termIndexes[j] = addTerm(terms.get(j), 0);
			}

			// add edge
			for (int j = 0; j < nTerms; j++) {
				int srcTerm = termIndexes[j];
				for (int k = 1; k < TERM_WINDOW_SIZE; k++) {
					if (j + k >= nTerms)
						break;
					int desTerm = termIndexes[j + k];
					if (desTerm == srcTerm) {// no self-loop
						continue;
					}
					addEdge(srcTerm, desTerm, 1.0);
					if (verbose) {
						System.out.printf("edge: (%d - %d) (%s - %s)\n", srcTerm, desTerm, strTerms[srcTerm],
								strTerms[desTerm]);
					}
				}
			}
			if (verbose) {
				System.out.println("*********************************");
				System.out.printf("tweet = [[%s]]\n", tweet.getText());
				for (int j = 0; j < nTerms; j++) {
					System.out.printf("term[%d] = [[%s]]\n", j, terms.get(j));
				}
			}
		}
		//
		for (Tweet tweet : tweets) {
			List<String> terms = tweet.getTerms(preprocessingUtils);
			int nTerms = terms.size();
			termIndexSubSet.clear();
			for (int j = 0; j < nTerms; j++) {
				termIndexSubSet.add(getTermIndex(terms.get(j)));
			}
			for (int j : termIndexSubSet) {
				Term term = adjList.get(j);
				term.increaseNAllTweets(1);
				term.increaseNRelevantTweets(1);
			}
		}
	}

	/***
	 * update terms' number of tweets containing the term when a new tweet
	 * arrives
	 * 
	 * @param tweet
	 * @param preprocessingUtils
	 */
	public void updateTermNAllTweets(Tweet tweet, TweetPreprocessingUtils preprocessingUtils) {
		List<String> terms = tweet.getTerms(preprocessingUtils);
		int nTerms = terms.size();
		termIndexSubSet.clear();
		for (int j = 0; j < nTerms; j++) {
			termIndexSubSet.add(getTermIndex(terms.get(j)));
		}
		// update terms' number of all tweets
		for (int j : termIndexSubSet) {
			if (j == -1)
				continue;
			Term term = adjList.get(j);
			term.increaseNAllTweets(1);
		}
	}

	/***
	 * update terms' number of tweets containing the term when a new relevant
	 * tweet arrives
	 * 
	 * @param tweet
	 * @param preprocessingUtils
	 */
	public void updateTermNRelevantTweets(Tweet tweet, TweetPreprocessingUtils preprocessingUtils) {
		List<String> terms = tweet.getTerms(preprocessingUtils);
		int nTerms = terms.size();
		termIndexSubSet.clear();
		for (int j = 0; j < nTerms; j++) {
			termIndexSubSet.add(getTermIndex(terms.get(j)));
		}
		// update terms' number of all tweets
		for (int j : termIndexSubSet) {
			if (j == -1)
				continue;
			Term term = adjList.get(j);
			term.increaseNRelevantTweets(1);
		}
	}

	/***
	 * check if the term is outdate
	 * 
	 * @param term
	 * @return
	 */
	private boolean isOutdate(Term term, int time) {
		// TODO: add the code here
		if (term.getLastUpdate() < time) {
			return true;
		}
		return false;
	}

	/***
	 * remove outdate term
	 */
	private void removeOutdateTerms(int lastUpdateTime) {
		List<Integer> outdateTerms = new ArrayList<Integer>();
		int nTerms = adjList.size();
		for (int i = 0; i < nTerms; i++) {
			Term term = adjList.get(i);
			if (term == null) {
				continue;
			}
			if (isOutdate(term, lastUpdateTime)) {
				outdateTerms.add(i);
			}

			// System.out.printf("index = %d \t lastUpdateTime = %d \t time = %d
			// isOutDate = %s\n", i,
			// term.getLastUpdate(), lastUpdateTime, isOutdate(term,
			// lastUpdateTime));
		}

		for (int i : outdateTerms) {
			Iterator<Map.Entry<Integer, AdjacentTerm>> iter = adjList.get(i).getInTerms().entrySet().iterator();
			while (iter.hasNext()) {
				int j = iter.next().getKey();
				if (outdateTerms.contains(j)) {
					continue;
				}
				Term term = adjList.get(j);
				if (term == null)
					continue;
				if (verbose) {
					System.out.printf("remove: (%d - %d) (%s - %s)\n", j, i, strTerms[j], strTerms[i]);
				}
				term.removeOutTerm(i);

			}
			iter = adjList.get(i).getOutTerms().entrySet().iterator();
			while (iter.hasNext()) {
				int j = iter.next().getKey();
				if (outdateTerms.contains(j)) {
					continue;
				}
				Term term = adjList.get(j);
				if (term == null)
					continue;
				if (verbose) {
					System.out.printf("remove: (%d - %d) (%s - %s)\n", i, j, strTerms[i], strTerms[j]);
				}
				term.removeInTerm(i);
			}

			// System.out.printf("term[%d] = %s is REMOVED\n", i, strTerms[i]);
			adjList.set(i, null);
			blankIndex.push(i);
			term2Index.remove(strTerms[i]);
			strTerms[i] = null;
		}

		nActiveTerms -= outdateTerms.size();

	}

	public void setMaxNKeyTerms(int _maxNKeyTerms) {
		maxNKeyTerms = _maxNKeyTerms;
	}

	public void updateKeyTerms() {
		// System.out.printf("nActiveTerms = %d\n", nActiveTerms);
		int k = (int) (Configure.PROPORTION_OF_KEYTERMS * nActiveTerms);
		// System.out.printf("nKeyterms = %d\n", k);
		if (k > maxNKeyTerms) {
			k = maxNKeyTerms;
		}
		if (k < Configure.MIN_NUMBER_KEY_TERMS) {
			k = Configure.MIN_NUMBER_KEY_TERMS;
		}

		// System.out.printf("nKeyterms = %d", k);

		PriorityBlockingQueue<KeyValue_Pair> queue = new PriorityBlockingQueue<KeyValue_Pair>();
		int nTerms = adjList.size();
		for (int i = 0; i < nTerms; i++) {
			Term term = adjList.get(i);
			if (term == null) {
				continue;
			}
			double s = term.getImportance();
			if (term.getNRelevantTweets() >= Configure.MIN_KEYTERM_NUMBER_TWEETS) {
				double r = ((double) term.getNRelevantTweets()) / term.getNAllTweets();
				s *= Math.log(1 + r);
			} else {
				s = 0;
			}
			if (queue.size() < k) {
				queue.add(new KeyValue_Pair(i, s));
			} else {
				KeyValue_Pair head = queue.peek();
				if (head.getDoubleValue() < s) {
					queue.poll();
					queue.add(new KeyValue_Pair(i, s));
				}
			}
		}

		keyTerms = new HashSet<Integer>();
		while (!queue.isEmpty()) {
			keyTerms.add(queue.poll().getIntKey());
		}

	}

	public void updateImportantTerms() {
		PriorityBlockingQueue<KeyValue_Pair> queue = new PriorityBlockingQueue<KeyValue_Pair>();
		int nTerms = adjList.size();
		for (int i = 0; i < nTerms; i++) {
			Term term = adjList.get(i);
			if (term == null) {
				continue;
			}
			double s = term.getImportance();
			if (queue.size() < Configure.NUMBER_IMPORTANT_TERMS) {
				queue.add(new KeyValue_Pair(i, s));
			} else {
				KeyValue_Pair head = queue.peek();
				if (head.getDoubleValue() < s) {
					queue.poll();
					queue.add(new KeyValue_Pair(i, s));
				}
			}
		}

		topImportantTerms = new HashSet<Integer>();
		while (!queue.isEmpty()) {
			topImportantTerms.add(queue.poll().getIntKey());
		}

	}

	/***
	 * compute importance of terms
	 */
	public void updateTermImportance(int lastUpdateTime) {
		removeOutdateTerms(lastUpdateTime);
		int nTerms = adjList.size();
		double[] tempImportance = new double[nTerms];
		for (int i = 0; i < nTerms; i++) {
			Term term = adjList.get(i);
			if (term != null) {
				term.setImportance(1.0 / nActiveTerms);
				term.age();
			}
		}
		for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
			for (int i = 0; i < nTerms; i++) {
				tempImportance[i] = DAMPING_FACTOR / nActiveTerms;
			}
			for (int i = 0; i < nTerms; i++) {
				Term term = adjList.get(i);
				if (term == null) {
					continue;
				}
				double importance = term.getImportance();
				double sum = term.getSumOutWeight() + term.getSumInWeight();

				for (Map.Entry<Integer, AdjacentTerm> adjTermIter : term.getOutTerms().entrySet()) {
					int j = adjTermIter.getKey();
					AdjacentTerm adjTerm = adjTermIter.getValue();
					tempImportance[j] += (1 - DAMPING_FACTOR) * importance * adjTerm.getWeight() / sum;
				}

				for (Map.Entry<Integer, AdjacentTerm> adjTermIter : term.getInTerms().entrySet()) {
					int j = adjTermIter.getKey();
					AdjacentTerm adjTerm = adjTermIter.getValue();
					tempImportance[j] += (1 - DAMPING_FACTOR) * importance * adjTerm.getWeight() / sum;
				}

			}

			for (int i = 0; i < nTerms; i++) {
				Term term = adjList.get(i);
				if (term != null) {
					term.setImportance(tempImportance[i]);
				}
			}
		}
		// TODO: diversified pagerank

		nNewTerms = 0;
		sumNewTermFrequent = 0;

	}

	/***
	 * return importance of term j
	 * 
	 * @param j
	 * @return
	 */
	private double getTermImportance(int j) {
		Term term = adjList.get(j);
		try {
			double importance = term.getImportance();
			if (importance > 0) {
				return importance * (1 - ADAPTIVE_FACTOR);
			}
			if (simpleTermImportance)
				return 0;
			int frequent = term.getNRelevantTweets();
			return (ADAPTIVE_FACTOR * frequent / sumNewTermFrequent);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.printf("in getTermImportance: Term %d is NULL!!!!\n", j);
			System.exit(-1);
		}

		return 0;
	}

	/***
	 * A simple keyword-based filter, for testing purposes
	 * 
	 * @param tweet
	 * @return
	 */
	private boolean predefineKeywordMatch(String tweet) {
		if (tweet.contains("travel ban"))
			return true;
		if (tweet.contains("muslim ban"))
			return true;
		if (tweet.contains("#travelban"))
			return true;
		if (tweet.contains("#muslimban"))
			return true;
		return false;
	}

	/***
	 * compute likelihood of a tweet
	 * 
	 * @param tweet
	 * @return
	 */
	public double getScore(Tweet tweet, TweetPreprocessingUtils preprocessingUtils) {
		if (predefineKeywordMatch) {
			if (predefineKeywordMatch(tweet.getText().toLowerCase()))
				return 1;
			return 0;
		}
		// if (verbose) {
		// System.out.printf("scoring: %s\n", tweet.getText().replace('\n', '
		// '));
		// }

		double score = 0;
		List<String> terms = tweet.getTerms(preprocessingUtils);
		int nTerms = terms.size();
		boolean containKeyTerm = false;
		for (int i = 0; i < nTerms; i++) {
			termIndexes[i] = getTermIndex(terms.get(i));
			if (keyTerms.contains(termIndexes[i])) {
				containKeyTerm = true;
			}
		}

		if (keytermFlag) {// must contain some keyterms
			if (!containKeyTerm)
				return 0;
		}

		int u, v;
		double pu, pv;
		double Wuv, Wudot, Wdotv;
		double s = 0;
		Term uTerm = null, vTerm = null;
		for (int j = 0; j < nTerms; j++) {
			u = termIndexes[j];
			if (u == -1)
				continue;
			uTerm = adjList.get(u);
			pu = getTermImportance(u);
			Wudot = uTerm.getSumOutWeight();
			if (Wudot <= 0)
				continue;
			for (int k = 1; k < TERM_WINDOW_SIZE; k++) {
				if (j + k >= nTerms)
					break;
				v = termIndexes[j + k];
				if (v == -1)
					continue;
				vTerm = adjList.get(v);
				pv = getTermImportance(v);
				Wdotv = vTerm.getSumInWeight();
				if (Wdotv <= 0)
					continue;
				Wuv = uTerm.getOutTermWeight(v);
				s = Wuv * (pu / Wudot + pv / Wdotv);
				score += s;
				if (Double.isInfinite(score) || Double.isNaN(score)) {
					System.out.printf("u = %d pu = %f Wudot = %f\n", u, pu, Wudot);
					System.out.printf("v = %d pv = %f Wdotv = %f\n", v, pv, Wdotv);
					System.out.printf("Wuv = %f\n", Wuv);
					System.err.printf("isInfinite = %s \t isNAN = %s\n", Double.isFinite(score), Double.isNaN(score));
					System.out.printf("tweet = %s\n", tweet.getText());
					System.exit(-1);
				}
				if (verbose) {
					System.out.printf("------score: %s[%f-%f] %f %s[%f-%f] s = %f score = %f\n", strTerms[u], pu, Wudot,
							Wuv, strTerms[v], pv, Wdotv, s, score);
				}
			}
		}
		return score;

	}

	/***
	 * check if the tweet contain some important term
	 * 
	 * @param tweet
	 * @return
	 */
	public boolean containImportantTerm(Tweet tweet, TweetPreprocessingUtils preprocessingUtils) {

		List<String> terms = tweet.getTerms(preprocessingUtils);
		int nTerms = terms.size();
		for (int i = 0; i < nTerms; i++) {
			termIndexes[i] = getTermIndex(terms.get(i));
			if (topImportantTerms.contains(termIndexes[i])) {
				return true;
			}
		}
		return false;
	}

	/***
	 * update the term graph given a new tweet
	 * 
	 * @param abc
	 */
	public void updateTermEdges(List<String> terms, int time, double weight) {
		int nTerms = terms.size();
		for (int j = 0; j < nTerms; j++) {
			termIndexes[j] = addTerm(terms.get(j), time);
		}
		// add/update edges
		for (int j = 0; j < nTerms; j++) {
			int srcTerm = termIndexes[j];
			for (int k = 1; k < TERM_WINDOW_SIZE; k++) {
				if (j + k >= nTerms)
					break;
				int desTerm = termIndexes[j + k];
				if (desTerm == srcTerm) {// no self-loop
					continue;
				}
				addEdge(srcTerm, desTerm, weight);
				if (verbose) {
					System.out.printf("edge: (%d - %d) (%s - %s)\n", srcTerm, desTerm, strTerms[srcTerm],
							strTerms[desTerm]);
				}
			}
		}
	}

	/***
	 * save list of term to a file: each line is a term, its status, and its top
	 * 5 following terms
	 * 
	 * @param filename
	 */
	public void saveTermInfo(String filename) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			int nTerms = adjList.size();
			for (int i = 0; i < nTerms; i++) {
				bw.write(String.format("%d,[[%s]]", i, strTerms[i]));
				Term term = adjList.get(i);
				if (term == null) {
					bw.write(",DELETED,0,-1,-1,-1\n");
					continue;
				}
				bw.write(String.format(",ACTIVE,%d,%d,%d,%d", term.getLastUpdate(), term.getNRelevantTweets(),
						term.getNAllTweets(), term.getInTerms().size()));
				bw.write(String.format(",%f", term.getImportance()));
				List<Integer> topAdjTerms = RankingUtils.getTopAdjTerms(5, term.getInTerms());
				for (int j = 0; j < topAdjTerms.size(); j++) {
					int k = topAdjTerms.get(j);
					bw.write(String.format(",%s(%d-%f)", strTerms[k], k, term.getInTerms().get(k).getWeight()));
				}
				bw.write("\n");
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void saveGraphToFile(String filename) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			int nTerms = adjList.size();
			for (int i = 0; i < nTerms; i++) {
				Term term = adjList.get(i);
				if (term == null) {
					continue;
				}
				Iterator<Map.Entry<Integer, AdjacentTerm>> iter = adjList.get(i).getOutTerms().entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<Integer, AdjacentTerm> pair = iter.next();
					int j = pair.getKey();
					double w = pair.getValue().getWeight();
					bw.write(String.format("%s\t%s\t%f\n", strTerms[i], strTerms[j], w));
				}
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void saveKeyTermToFile(String filename) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (int i : keyTerms) {
				Term term = adjList.get(i);
				double s = term.getImportance();
				double r = ((double) term.getNRelevantTweets()) / term.getNAllTweets();
				s *= Math.log(1 + r);
				bw.write(String.format("[[%s]],%f,%f\n", strTerms[i], term.getImportance(), s));
			}
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public void saveImportantTermToFile(String filename) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (int i : topImportantTerms) {
				Term term = adjList.get(i);
				if (term == null) {
					continue;
				}
				double s = term.getImportance();
				double r = ((double) term.getNRelevantTweets()) / term.getNAllTweets();
				s *= Math.log(1 + r);
				bw.write(String.format("[[%s]],%f,%f\n", strTerms[i], term.getImportance(), s));
			}
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	/***
	 * load graph from files
	 * 
	 * @param termfile
	 * @param graphfile
	 * @param time
	 */
	public TermGraph(String termfile, String graphfile, int time) {
		try {
			init();

			BufferedReader br = new BufferedReader(new FileReader(termfile));
			String line = null;
			int maxIndex = 0;
			while ((line = br.readLine()) != null) {
				int index = Integer.parseInt(line.split(",")[0]);
				if (index > maxIndex) {
					maxIndex = index;
				}
			}

			for (int i = 0; i <= maxIndex; i++) {
				adjList.add(null);
			}
			br.close();

			br = new BufferedReader(new FileReader(termfile));
			line = null;
			while ((line = br.readLine()) != null) {
				if (line.contains("[[null]]")) {
					continue;
				}
				nActiveTerms++;
				String[] tokens = line.split("\\]\\],");
				String[] subTokens = tokens[0].split(",\\[\\[");
				int index = Integer.parseInt(subTokens[0]);
				String name = subTokens[1];
				term2Index.put(name, index);
				strTerms[index] = name;

				subTokens = tokens[1].split(",");
				int lastUpdate = Integer.parseInt(subTokens[1]);
				int nRelevantTweets = Integer.parseInt(subTokens[2]);
				int nAllTweets = Integer.parseInt(subTokens[3]);
				Term term = new Term(lastUpdate);
				term.setNRelevantTweets(nRelevantTweets);
				term.setNAllTweets(nAllTweets);
				adjList.set(index, term);
			}
			br.close();

			br = new BufferedReader(new FileReader(graphfile));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				int srcTerm = getTermIndex(tokens[0]);
				int desTerm = getTermIndex(tokens[1]);
				addEdge(srcTerm, desTerm, Double.parseDouble(tokens[2]));
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private HashMap<Integer, Double> getTFIDF(int p) {
		Term term = adjList.get(p);
		HashSet<Integer> q = new HashSet<Integer>();
		HashMap<Integer, Double> tfIdf = new HashMap<Integer, Double>();
		for (Map.Entry<Integer, AdjacentTerm> adjTermIter : term.getOutTerms().entrySet()) {
			int j = adjTermIter.getKey();
			AdjacentTerm adjTerm = adjTermIter.getValue();
			tfIdf.put(j, adjTerm.getWeight());
			q.add(j);
		}

		for (Map.Entry<Integer, AdjacentTerm> adjTermIter : term.getInTerms().entrySet()) {
			int j = adjTermIter.getKey();
			AdjacentTerm adjTerm = adjTermIter.getValue();
			if (tfIdf.containsKey(j)) {
				tfIdf.put(j, adjTerm.getWeight() + tfIdf.get(j));
			} else {
				tfIdf.put(j, adjTerm.getWeight());
			}
			q.add(j);
		}
		for (int j : q) {
			double w = tfIdf.get(j) * Math.log(((double) nActiveTerms) / adjList.get(j).nAdjTerms());
			tfIdf.put(j, w);
		}
		return tfIdf;
	}

	public void expandImportantTerms() {
		HashSet<Integer> newImportantTerms = new HashSet<Integer>();
		HashMap<Integer, HashMap<Integer, Double>> tfIdfVector = new HashMap<Integer, HashMap<Integer, Double>>();
		for (int p : topImportantTerms) {
			if (adjList.get(p) == null)
				continue;
			if (!tfIdfVector.containsKey(p)) {
				tfIdfVector.put(p, getTFIDF(p));
			}
			// common terms
			Term term = adjList.get(p);
			HashSet<Integer> commonAdjTerms = new HashSet<Integer>();
			for (Map.Entry<Integer, AdjacentTerm> adjTermIter : term.getOutTerms().entrySet()) {
				int j = adjTermIter.getKey();
				commonAdjTerms.add(j);
			}
			for (Map.Entry<Integer, AdjacentTerm> adjTermIter : term.getInTerms().entrySet()) {
				int j = adjTermIter.getKey();
				commonAdjTerms.add(j);
			}
			// candidates
			HashSet<Integer> candidates = new HashSet<Integer>();
			for (int q : commonAdjTerms) {
				term = adjList.get(q);
				for (Map.Entry<Integer, AdjacentTerm> adjTermIter : term.getOutTerms().entrySet()) {
					int j = adjTermIter.getKey();
					candidates.add(j);
				}
				for (Map.Entry<Integer, AdjacentTerm> adjTermIter : term.getInTerms().entrySet()) {
					int j = adjTermIter.getKey();
					candidates.add(j);
				}
			}
			// expand
			for (int q : candidates) {
				if (!tfIdfVector.containsKey(q)) {
					tfIdfVector.put(q, getTFIDF(q));
				}
				if (SimilarityUtils.cosineSimilarity(tfIdfVector.get(p),
						tfIdfVector.get(q)) >= Configure.SIMILARITY_THRESHOLD) {
					newImportantTerms.add(q);
				}
			}
		}
		topImportantTerms.addAll(newImportantTerms);
	}
}
