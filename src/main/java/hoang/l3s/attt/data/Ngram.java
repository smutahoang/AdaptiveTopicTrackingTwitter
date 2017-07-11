package hoang.l3s.attt.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Ngram {
	private int n;
	private List<String> centenceList;

	private HashMap<String, Double> existedPreWordCountsMap;
	private HashMap<String, HashMap<String, Double>> existedWordCountsMap;

	public Ngram(List<String> centenceList, int n) {
		this.centenceList = centenceList;
		this.n = n;
	}

	public void getCountsMap(String[] wordsArr, int count) {

		for (int j = 0; j < wordsArr.length - (count - 1); j++) {
			StringBuffer preWord = new StringBuffer("");

			int k = 0;
			for (k = 0; k < count - 1; k++) {
				preWord.append(wordsArr[j + k] + " ");
			}
			String term = wordsArr[j + k];

			if (existedPreWordCountsMap.containsKey(preWord.toString())) {
				double preCnt = existedPreWordCountsMap.get(preWord.toString());
				existedPreWordCountsMap.put(preWord.toString(), preCnt + 1.0);

				HashMap<String,Double> termCountMap = existedWordCountsMap.get(preWord.toString());
				if(termCountMap != null) {
					Set<String> keys = termCountMap.keySet();
					Iterator<String> iter = keys.iterator();
					boolean find = false;
					while (iter.hasNext()) {
						String aTerm = iter.next();
						if (aTerm.equals(term)) {
							double cnt = termCountMap.get(term);
							termCountMap.put(term, cnt + 1.0);
							find = true;
							break;
						}
					}
					if(!find) {
						termCountMap.put(term, 1.0);
					}
				}else {
					termCountMap = new HashMap<String,Double>();
					termCountMap.put(term, 1.0);
				}
				existedWordCountsMap.put(preWord.toString(), termCountMap);	
			} else {
				existedPreWordCountsMap.put(preWord.toString(), 1.0);
				
				HashMap<String,Double> termCountMap = new HashMap<String,Double>();
				termCountMap.put(term, 1.0);
				existedWordCountsMap.put(preWord.toString(), termCountMap);
			}
		}
	}

	public void getCount() {
		existedPreWordCountsMap = new HashMap<String, Double>();
		existedWordCountsMap = new HashMap<String, HashMap<String, Double>>();

		for (int i = 0; i < this.centenceList.size(); i++) {
			String centence = this.centenceList.get(i);
			String wordsArr[] = centence.split(" ");
			getCountsMap(wordsArr, this.n);
		}
	}

	public void getLM() {
		System.out.println("++++++++++++++++++++++++");
		List<LanguageModel> lmList = new ArrayList<LanguageModel>();

		Set<String> preKeys = existedPreWordCountsMap.keySet();
		Iterator<String> preIter = preKeys.iterator();
		while (preIter.hasNext()) {
			String preWord = preIter.next();
			double preCount = existedPreWordCountsMap.get(preWord);

			HashMap<String, Double> termCountMap = existedWordCountsMap.get(preWord);
			Set<String> keys = termCountMap.keySet();
			Iterator<String> iter = keys.iterator();
			while (iter.hasNext()) {
				String term = iter.next();
				double count = termCountMap.get(term);

				LanguageModel lm = new LanguageModel();
				lm.n = this.n;
				lm.preTerm = preWord;
				lm.term = term;
				lm.probility = count / preCount;
				lmList.add(lm);

				System.out.println("n:" + this.n + ",P(" + lm.term + "|" + lm.preTerm +")" + " = " + count + "/" + preCount + " = " + lm.probility);
			}
		}
	}

	public void train() {
		getCount();
		getLM();
	}
}
