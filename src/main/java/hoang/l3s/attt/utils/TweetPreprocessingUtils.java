package hoang.l3s.attt.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.validator.routines.UrlValidator;

public class TweetPreprocessingUtils {

	static HashSet<Character> punct;
	static HashSet<Character> quoteSymbols;
	static HashSet<Character> validPrefixSymbols;
	static HashSet<Character> validSuffixSymbols;

	static String stopwordPath = "/home/hoang/attt/data/stopwords";
	// static String stopwordPath =
	// "E:/code/java/AdaptiveTopicTrackingTwitter/data/stopwords";
	static HashSet<String> stopWords;

	static void getStopWords() {
		try {
			stopWords = new HashSet<String>();
			BufferedReader br;
			String line = null;

			br = new BufferedReader(new FileReader(String.format("%s/common-english-adverbs.txt", stopwordPath)));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.toLowerCase().split(",");
				for (int i = 0; i < tokens.length; i++) {
					stopWords.add(tokens[i]);
				}
			}
			br.close();

			br = new BufferedReader(new FileReader(String.format("%s/common-english-prep-conj.txt", stopwordPath)));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.toLowerCase().split(",");
				for (int i = 0; i < tokens.length; i++) {
					stopWords.add(tokens[i]);
				}
			}
			br.close();

			br = new BufferedReader(new FileReader(String.format("%s/common-english-words.txt", stopwordPath)));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.toLowerCase().split(",");
				for (int i = 0; i < tokens.length; i++) {
					stopWords.add(tokens[i]);
				}
			}
			br.close();

			br = new BufferedReader(new FileReader(String.format("%s/smart-common-words.txt", stopwordPath)));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.toLowerCase().split(",");
				for (int i = 0; i < tokens.length; i++) {
					stopWords.add(tokens[i]);
				}
			}
			br.close();

			br = new BufferedReader(new FileReader(String.format("%s/mysql-stopwords.txt", stopwordPath)));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.toLowerCase().split(",");
				for (int i = 0; i < tokens.length; i++) {
					stopWords.add(tokens[i]);
				}
			}
			br.close();

			br = new BufferedReader(new FileReader(String.format("%s/twitter-slang.txt", stopwordPath)));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.toLowerCase().split(",");
				for (int i = 0; i < tokens.length; i++) {
					stopWords.add(tokens[i]);
				}
			}
			br.close();

			br = new BufferedReader(new FileReader(String.format("%s/shorthen.txt", stopwordPath)));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.toLowerCase().split(",");
				for (int i = 0; i < tokens.length; i++) {
					stopWords.add(tokens[i]);
				}
			}
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private UrlValidator urlValidator;

	private void initPunct() {
		punct = new HashSet<Character>();
		punct.add('~');
		punct.add('^');
		punct.add('(');
		punct.add(')');
		punct.add('{');
		punct.add('}');
		punct.add('[');
		punct.add(']');
		punct.add('<');
		punct.add('>');
		punct.add(':');
		punct.add(';');
		punct.add(',');
		punct.add('.');
		punct.add('?');
		punct.add('!');
	}

	private void initQouteSymbols() {

		quoteSymbols = new HashSet<Character>();
		quoteSymbols.add('\"');
		quoteSymbols.add('\'');
		quoteSymbols.add('`');
		quoteSymbols.add('\u2014');// long dash
		quoteSymbols.add('\u0022');
		// quotation mark (")
		// quoteSymbols.add('\u0027'); // apostrophe (')
		// quoteSymbols.add('\u00ab'); // left-pointing // double-angle // // //
		// // // // //
			// //
			// //
			// //
			// //
			// //
			// //
			// //
			// //
			// //
			// //
			// //
			// //
			// //
			// //
			// //
			// //
			// //
			// //
			// //
			// quotation
			// //
			// //
			// //
			// //
			// mark
		quoteSymbols.add('\u00bb'); // right-pointing double-angle quotation
									// mark
		quoteSymbols.add('\u2018'); // left single quotation mark
		quoteSymbols.add('\u2019'); // right single quotation mark
		quoteSymbols.add('\u201a'); // single low-9 quotation mark
		quoteSymbols.add('\u201b'); // single high-reversed-9 quotation mark
		quoteSymbols.add('\u201c'); // left double quotation mark
		quoteSymbols.add('\u201d'); // right double quotation mark
		quoteSymbols.add('\u201e'); // double low-9 quotation mark
		quoteSymbols.add('\u201f'); // double high-reversed-9 quotation mark
		quoteSymbols.add('\u2039'); // single left-pointing angle quotation mark
		quoteSymbols.add('\u203a'); // single right-pointing angle quotation
									// mark
		quoteSymbols.add('\u300c'); // left corner bracket
		quoteSymbols.add('\u300d'); // right corner bracket
		quoteSymbols.add('\u300e'); // left white corner bracket
		quoteSymbols.add('\u300f'); // right white corner bracket
		quoteSymbols.add('\u301d'); // reversed double prime quotation mark
		quoteSymbols.add('\u301e'); // double prime quotation mark
		quoteSymbols.add('\u301f'); // low double prime quotation mark
		quoteSymbols.add('\ufe41'); // presentation form for vertical left
									// corner bracket
		quoteSymbols.add('\ufe42'); // presentation form for vertical right
									// corner bracket
		quoteSymbols.add('\ufe43'); // presentation form for vertical left
									// corner white bracket
		quoteSymbols.add('\ufe44'); // presentation form for vertical right
									// corner white bracket
		quoteSymbols.add('\uff02'); // fullwidth quotation mark
		quoteSymbols.add('\uff07'); // fullwidth apostrophe
		quoteSymbols.add('\uff62'); // halfwidth left corner bracket
		quoteSymbols.add('\uff63'); // halfwidth right corner bracket

	}

	private void initPrefixSuffixSymbols() {
		validPrefixSymbols = new HashSet<Character>();
		validPrefixSymbols.add('@');
		validPrefixSymbols.add('#');
		validPrefixSymbols.add('%');
		validPrefixSymbols.add('$');

		validSuffixSymbols = new HashSet<Character>();
		validSuffixSymbols.add('%');
		validSuffixSymbols.add('$');
	}

	private void init() {
		initPunct();
		initQouteSymbols();
		initPrefixSuffixSymbols();

		getStopWords();

		urlValidator = new UrlValidator();
	}

	public TweetPreprocessingUtils() {
		init();
	}

	private String removeSymbolInText(String text) {
		try {
			char[] chars = text.toCharArray();
			int i = 0;
			while (i < chars.length) {
				// System.out.printf("i = %d char = %c\n", i, chars[i]);
				if (chars[i] == '\n') {// newline
					chars[i] = ' ';
					i++;
				} else if (chars[i] == '\r') {// newline
					chars[i] = ' ';
					i++;
				} else if (chars[i] == '\t') {// tab
					chars[i] = ' ';
					i++;
				} else if (chars[i] == '\\') {// \n or \r or \t
					chars[i] = ' ';
					i++;
					if (i < chars.length) {
						if (chars[i] == 'n') {
							chars[i] = ' ';
							i++;
						} else if (chars[i] == 'r') {
							chars[i] = ' ';
							i++;
						} else if (chars[i] == 't') {
							chars[i] = ' ';
							i++;
						} else {
							// do nothing
						}
					}
				} else if (quoteSymbols.contains(chars[i])) {// quote
					chars[i] = ' ';
					i++;
					if (i < chars.length) {
						if (chars[i] == 's') {// 's
							if (i < chars.length - 1) {
								if (!Character.isLetterOrDigit(chars[i + 1])) {
									chars[i] = ' ';
									i++;
								}
							} else {
								chars[i] = ' ';
								i++;
							}
						}
					}

				} else if (chars[i] == '&') {// html character
					int j = i;
					while (j < chars.length) {
						if (chars[j] == ' ')
							break;
						else if (chars[j] == '\\')
							break;
						else if (punct.contains(chars[j]))
							break;
						else if (validPrefixSymbols.contains(chars[j]))
							break;
						else {
							chars[j] = ' ';
							j++;
						}
					}
					i = j;
				} else if (i == 0) {// punctuation - first word
					while (text.charAt(i) == ' ') {
						i++;
						if (i == chars.length)
							break;
					}
					int j = i + 1;
					while (j < chars.length) {
						if (chars[j] == ' ')
							break;
						if (chars[j] == '\t')
							break;
						if (chars[j] == '\n')
							break;
						if (chars[j] == '\r')
							break;
						if (punct.contains(chars[j])) {
							if (chars[j] != '.') {// in case of url
								break;
							}
						}
						j++;
					}
					if (j == i + 1) {
						i++;
						continue;
					}
					// System.out.printf("sub-string = [[%s]]",
					// text.substring(i,
					// j));
					if (urlValidator.isValid(text.substring(i, j))) {
						i = j;
						// System.out.println("\t\tVALID");
					} else {
						// System.out.println("\t\tINVALID");
						for (int p = i; p < j; p++) {
							if (punct.contains(chars[p])) {
								chars[p] = ' ';
							}
						}
						i++;
					}

				} else if (chars[i] == ' ') {// punctuation
					int j = i + 1;
					while (j < chars.length) {
						if (chars[j] == ' ')
							break;
						if (chars[j] == '\t')
							break;
						if (chars[j] == '\n')
							break;
						if (chars[j] == '\r')
							break;
						if (punct.contains(chars[j])) {
							if (chars[j] != '.') {// in case of url
								break;
							}
						}
						j++;
					}
					if (j == i + 1) {
						i++;
						continue;
					}
					// System.out.printf("sub-string = [[%s]]", text.substring(i
					// +
					// 1, j));
					if (urlValidator.isValid(text.substring(i + 1, j))) {
						i = j;
						// System.out.println("\t\tVALID");
					} else {
						// System.out.println("\t\tINVALID");
						for (int p = i; p < j; p++) {
							if (punct.contains(chars[p])) {
								chars[p] = ' ';
							}
						}
						i++;
					}

				} else {
					i++;
				}

			}
			return (new String(chars));
		} catch (Exception e) {
			System.out.printf("tweet = [[%s]]\n", text);
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	private String removeSymbolInWord(String word) {
		int r = word.length();
		if (r == 0)
			return null;
		if (word.endsWith("\u2026")) // three-dots symbol - chunked tweet
			return null;
		if (urlValidator.isValid(word))
			return word;
		char c = word.charAt(r - 1);
		while (!Character.isLetterOrDigit(c)) {
			if (validSuffixSymbols.contains(c)) {
				break;
			}
			r--;
			if (r == 0)
				return null;
			c = word.charAt(r - 1);
		}
		int l = 0;
		c = word.charAt(l);
		while (!Character.isLetterOrDigit(c)) {
			if (validPrefixSymbols.contains(c)) {
				break;
			}
			l++;
			c = word.charAt(l);
		}

		word = word.substring(l, r);
		if (word.startsWith("http")) {
			if (!urlValidator.isValid(word)) {
				return null;// broken url
			}
		}
		return word;
	}

	private String getTweetContent(String message) {
		message = removeSymbolInText(message);
		// System.out.printf("after reomve symbol [[%s]]\n", message);
		// TODO optimize this function using more efficient string utilities
		int l = message.length();
		int p = message.indexOf("rt @");
		if (p < 0)
			p = message.indexOf("RT @");
		if (p >= 0) {
			int j = p + 4;
			while (message.charAt(j) != ' ') {
				j++;
				if (j >= l) {
					break;
				}
			}
			message = message.replace(message.substring(p, j), "");
		}
		return message;
	}

	private List<String> termExtraction(String text) {
		List<String> terms = new ArrayList<String>();
		String[] tokens = text.toLowerCase().split(" ");
		for (int i = 0; i < tokens.length; i++) {
			String term = removeSymbolInWord(tokens[i]);
			if (stopWords.contains(term)) {
				continue;
			}
			if (term != null) {
				terms.add(terms.size(), term);
			}
		}
		return terms;
	}

	public List<String> extractTermInTweet(String tweet) {
		tweet = getTweetContent(tweet);
		List<String> terms = termExtraction(tweet);
		return terms;
	}

	public void checkStopWordList() {
		System.out.println("more: " + stopWords.contains("more"));
		System.out.println("this: " + stopWords.contains("this"));
	}

	public void checkQuoteSymbols() {
		for (char s : quoteSymbols) {
			System.out.printf("c= %c\n", s);
		}
	}

	public static void main(String[] args) {
		// String text = "trump’s";
		// char a = '\u2019';
		// System.out.println("code = " + a);

		// char c = '\u2014'; // System.out.println("c = " + c);

		TweetPreprocessingUtils nlpUtils = new TweetPreprocessingUtils();
		// nlpUtils.checkQuoteSymbols();

		nlpUtils.checkStopWordList();

		String message = "Interesting,Trump's country travel ban on majority Muslim countries doesn't include the no. 1 Islamic radicalization offender:Saudi Arabia!!";

		List<String> terms = nlpUtils.extractTermInTweet(message);
		for (int i = 0; i < terms.size(); i++) {
			System.out.printf("i = %d term = |%s|\n", i, terms.get(i));
		}

		String link = "https://t.co/vh1VSAUwxQ";
		UrlValidator urlValidator = new UrlValidator();
		System.out.printf("link = %s valid = %s\n", link, urlValidator.isValid(link));
	}

}
