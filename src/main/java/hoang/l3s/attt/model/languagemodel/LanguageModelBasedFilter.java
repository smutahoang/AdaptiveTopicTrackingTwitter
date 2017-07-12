package hoang.l3s.attt.model.languagemodel;

import java.util.List;

import hoang.l3s.attt.model.FilteringModel;
import hoang.l3s.attt.model.Tweet;
import hoang.l3s.attt.model.TweetStream;

public class LanguageModelBasedFilter extends FilteringModel {

	private LanguageModel filter;

	public void init(List<Tweet> tweets) {
		// TODO Auto-generated method stub
		filter = new LanguageModel(tweets);
	}

	public double relevantScore(Tweet tweet) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void update(Tweet tweet) {
		// TODO Auto-generated method stub

	}

	public void filter(TweetStream stream, String ouputPath) {
		// TODO Auto-generated method stub
		
	}

	

}
