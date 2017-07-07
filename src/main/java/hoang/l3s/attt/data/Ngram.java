package hoang.l3s.attt.data;

import java.util.ArrayList;
import java.util.List;

public class Ngram {
	private int n;
	private List<String> centenceList;

	public List<CountModel> preCountsModelList;
	public List<CountModel> countsModelList;

	public Ngram(List<String> centenceList, int n) {
		this.centenceList = centenceList;
		this.n = n;
	}

	public int containsTerm(List<CountModel> countsList, List<String> list) {
		boolean contain = false;
		int index = 0;

		for (int i = 0; i < countsList.size(); i++) {
			CountModel count = countsList.get(i);
			int j = 0;
			for (j = 0; j < list.size(); j++) {
				if (!count.words.get(j).equals(list.get(j))) {
					break;
				}
			}
			if (j == list.size()) {
				contain = true;
				index = i;
			}
		}

		if (contain) {
			return index;
		} else {
			return -1;
		}
	}

	public void getCountModelList(String[] wordsArr, int count, List<CountModel> modelList) {

		for (int j = 0; j < wordsArr.length - (count - 1); j++) {
			List<String> wordList = new ArrayList<String>();
			for (int k = 0; k < count; k++) {
				wordList.add(wordsArr[j + k]);
			}

			int index = containsTerm(modelList, wordList);
			if (index != -1) {
				CountModel countModel = modelList.get(index);
				countModel.count++;
			} else {
				CountModel countModel = new CountModel();
				countModel.words = wordList;
				countModel.count = 1;
				modelList.add(countModel);
			}
		}
	}

	public void getCount() {
		preCountsModelList = new ArrayList<CountModel>();
		countsModelList = new ArrayList<CountModel>();

		for (int i = 0; i < this.centenceList.size(); i++) {
			String centence = this.centenceList.get(i);
			String wordsArr[] = centence.split(" ");

			getCountModelList(wordsArr, this.n - 1, preCountsModelList);
			getCountModelList(wordsArr, this.n, countsModelList);
		}

		/*
		System.out.println("================");
		for (int i = 0; i < preCountsModelList.size(); i++) {
			CountModel countModel = preCountsModelList.get(i);
			List<String> list = countModel.words;
			for (int j = 0; j < list.size(); j++) {
				System.out.print(list.get(j) + ",");
			}
			System.out.println("count:" + countModel.count);
		}
		System.out.println("***************");
		for (int i = 0; i < countsModelList.size(); i++) {
			CountModel countModel = countsModelList.get(i);
			List<String> list = countModel.words;
			for (int j = 0; j < list.size(); j++) {
				System.out.print(list.get(j) + ",");
			}
			System.out.println("count:" + countModel.count);
		}
		*/
	}

	public String getTerm(List<String> preWordList,List<String> wordList) {
		int i = 0;
		for(i = 0; i < preWordList.size(); i ++) {
			if (!preWordList.get(i).equals(wordList.get(i))) {
				break;
			}	
		}
		if (i == preWordList.size()) {
			return wordList.get(i);
		}else {
			return null;
		}
	}

	public void getLM() {
		System.out.println("++++++++++++++++++++++++");
		List<LanguageModel> lmList = new ArrayList<LanguageModel>();
		for(int i = 0; i < this.preCountsModelList.size(); i ++) {
			for(int j = 0; j < this.countsModelList.size(); j ++) {
				CountModel preCountModel = this.preCountsModelList.get(i);
				CountModel countModel = this.countsModelList.get(j);
				String term = getTerm(preCountModel.words,countModel.words);
				if(term != null) {
					LanguageModel lm = new LanguageModel();
					lm.n = this.n;
					lm.preTerm = preCountModel.words;
					lm.term = term;
					lm.probility = ((double)countModel.count)/preCountModel.count;
					lmList.add(lm);
					
					System.out.print("n:"+this.n + "," + lm.term + "/");
					for(int k = 0; k < preCountModel.words.size(); k ++) {
						System.out.print(preCountModel.words.get(k)+ " ");
					}
					System.out.println("------pro:"+lm.probility);
				}
			}
		}
	}

	public void train() {
		getCount();
		getLM();
	}
}
