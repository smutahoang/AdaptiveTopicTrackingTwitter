package hoang.l3s.attt.model.languagemodel;

import java.util.List;

import hoang.l3s.attt.model.Tweet;

public class LanguageModel {
	/***
	 * to train a language model based on a set of tweets
	 * 
	 * @param tweets
	 */
	public LanguageModel(List<Tweet> tweets) {

	}

	/***
	 * compute likelihood of a tweet
	 * 
	 * @param tweet
	 * @return
	 */
	public double getLikelihood(Tweet tweet) {
		return 0;
	}

	/***
	 * update the language model given a new tweet
	 * 
	 * @param tweet
	 */
	public void update(Tweet tweet) {

	}
}
