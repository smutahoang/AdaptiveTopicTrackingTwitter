package hoang.l3s.attt.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.validator.routines.UrlValidator;

import hoang.l3s.attt.configure.Configure;

public class TweetPreprocessingUtils {

	static HashSet<Character> punctuations;
	static HashSet<Character> braces;
	static HashSet<Character> quoteSymbols;
	static HashSet<Character> validPrefixSymbols;
	static HashSet<Character> validSuffixSymbols;

	// static String stopwordPath = "/home/hoang/attt/data/stopwords";
	// static String stopwordPath =
	// "E:/code/java/AdaptiveTopicTrackingTwitter/data/stopwords";
	static HashSet<String> stopWords;

	static void getStopWords() {
		try {
			new Configure();

			stopWords = new HashSet<String>();
			BufferedReader br;
			String line = null;

			br = new BufferedReader(
					new FileReader(String.format("%s/common-english-adverbs.txt", Configure.stopwordsPath)));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.toLowerCase().split(",");
				for (int i = 0; i < tokens.length; i++) {
					stopWords.add(tokens[i]);
				}
			}
			br.close();

			br = new BufferedReader(
					new FileReader(String.format("%s/common-english-prep-conj.txt", Configure.stopwordsPath)));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.toLowerCase().split(",");
				for (int i = 0; i < tokens.length; i++) {
					stopWords.add(tokens[i]);
				}
			}
			br.close();

			br = new BufferedReader(
					new FileReader(String.format("%s/common-english-words.txt", Configure.stopwordsPath)));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.toLowerCase().split(",");
				for (int i = 0; i < tokens.length; i++) {
					stopWords.add(tokens[i]);
				}
			}
			br.close();

			br = new BufferedReader(
					new FileReader(String.format("%s/smart-common-words.txt", Configure.stopwordsPath)));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.toLowerCase().split(",");
				for (int i = 0; i < tokens.length; i++) {
					stopWords.add(tokens[i]);
				}
			}
			br.close();

			br = new BufferedReader(new FileReader(String.format("%s/mysql-stopwords.txt", Configure.stopwordsPath)));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.toLowerCase().split(",");
				for (int i = 0; i < tokens.length; i++) {
					stopWords.add(tokens[i]);
				}
			}
			br.close();

			br = new BufferedReader(new FileReader(String.format("%s/twitter-slang.txt", Configure.stopwordsPath)));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.toLowerCase().split(",");
				for (int i = 0; i < tokens.length; i++) {
					stopWords.add(tokens[i]);
				}
			}
			br.close();

			br = new BufferedReader(new FileReader(String.format("%s/shorthen.txt", Configure.stopwordsPath)));
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

	private void initPunctuations() {
		punctuations = new HashSet<Character>();
		punctuations.add('~');
		punctuations.add('^');
		punctuations.add('(');
		punctuations.add(')');
		punctuations.add('{');
		punctuations.add('}');
		punctuations.add('[');
		punctuations.add(']');
		punctuations.add('<');
		punctuations.add('>');
		punctuations.add(':');
		punctuations.add(';');
		punctuations.add(',');
		punctuations.add('.');
		punctuations.add('?');
		punctuations.add('!');
	}

	private void initBraces() {
		braces = new HashSet<Character>();
		braces.add('~');
		braces.add('^');
		braces.add('(');
		braces.add(')');
		braces.add('{');
		braces.add('}');
		braces.add('[');
		braces.add(']');
		braces.add('<');
		braces.add('>');
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
		// quoteSymbols.add('\u00ab'); // left-pointing // double-angle //

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
		initPunctuations();
		initBraces();
		initQouteSymbols();
		initPrefixSuffixSymbols();

		getStopWords();

		urlValidator = new UrlValidator();
	}

	public TweetPreprocessingUtils() {
		init();
	}

	private void removeOriginalAuthors(char[] chars) {
		for (int i = 0; i < chars.length; i++) {
			if (i > chars.length - 4) {
				return;
			}
			if (Character.toLowerCase(chars[i]) != 'r') {
				continue;
			}
			if (Character.toLowerCase(chars[i + 1]) != 't') {
				continue;
			}
			if (chars[i + 2] != ' ') {
				continue;
			}
			if (chars[i + 3] != '@') {
				continue;
			}
			if (i > 0) {
				if (chars[i - 1] != ' ')
					continue;
			}
			int j = i + 4;
			while (j < chars.length) {
				if (chars[j] == ' ')
					break;
				j++;
			}
			for (int p = i; p < j; p++) {
				chars[p] = ' ';
			}
		}
	}

	private void removeNewLineAndTabCharacter(char[] chars) {
		// System.out.printf("Before removeNewLineAndTabCharacter:\t");
		// ptScreen(chars);
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '\n') {
				chars[i] = ' ';
				continue;
			} else if (chars[i] == '\r') {
				chars[i] = ' ';
				continue;
			} else if (chars[i] == '\t') {
				chars[i] = ' ';
				continue;
			} else {
				continue;
			}
		}
		// System.out.printf("After removeNewLineAndTabCharacter:\t");
		// ptScreen(chars);
	}

	private boolean isURLStart(char[] chars, int i) {
		if (i >= chars.length - 4)
			return false;
		if (chars[i] != 'h')
			return false;
		if (chars[i + 1] != 't')
			return false;
		if (chars[i + 2] != 't')
			return false;
		if (chars[i + 3] != 'p')
			return false;
		return true;
	}

	private void removePunct(char[] chars) {
		for (int i = 0; i < chars.length; i++) {
			if (punctuations.contains(chars[i])) {
				if (i == 0) {// first character
					chars[i] = ' ';
					continue;
				} else if (i == chars.length - 1) {// last character
					chars[i] = ' ';
					continue;
				} else if (chars[i - 1] == ' ') {// first character of the word
					chars[i] = ' ';
					continue;
				} else if (chars[i + 1] == ' ') {// last character of the word
					chars[i] = ' ';
					continue;
				} else if (isURLStart(chars, i + 1)) {// right before url
					chars[i] = ' ';
					continue;
				} else if (chars[i] == '.') {// do nothing, in case of url
					continue;
				} else if (chars[i] == ':') {// do nothing, in case of url
					continue;
				} else if (!Character.isDigit(chars[i - 1])) {
					chars[i] = ' ';
					continue;
				} else if (!Character.isDigit(chars[i + 1])) {
					chars[i] = ' ';
					continue;
				} else {
					// do nothing
					continue;
				}
			}
		}
	}

	private boolean isShorten(char[] chars, int i) {
		if (!quoteSymbols.contains(chars[i]))
			return false;
		if (i <= chars.length - 3) {
			if (chars[i + 1] == 'r' && chars[i + 2] == 'e') // e.g, "they're"
				return true;
			if (chars[i + 1] == 'v' && chars[i + 2] == 'e') // e.g, "they've"
				return true;
			if (chars[i + 1] == 'l' && chars[i + 2] == 'l') // e.g, "she'll"
				return true;
		} else if (i <= chars.length - 2) {
			if (chars[i + 1] == 'm') // e.g, "I'm"
				return true;
			if (chars[i + 1] == 'd') // e.g, "it'd"
				return true;
			if (chars[i + 1] == 't') // e.g, "it'd"
				return true;
		}
		return false;
	}

	private void removeQuotationSymbols(char[] chars) {
		for (int i = 0; i < chars.length; i++) {
			if (quoteSymbols.contains(chars[i])) {
				if (i == 0) {// first character
					chars[i] = ' ';
					continue;
				} else if (i == chars.length - 1) {// last character
					chars[i] = ' ';
					continue;
				} else if (chars[i - 1] == ' ') {// first character of the word
					chars[i] = ' ';
					continue;
				} else if (chars[i + 1] == ' ') {// last character of the word
					chars[i] = ' ';
					if (chars[i - 1] == 's') { // for the case, e.g., "mothers'"
						chars[i - 1] = ' ';
					}
					continue;
				} else if (chars[i + 1] == 's') {// for the case, e.g.,
													// "mother's"
					chars[i] = ' ';
					chars[i + 1] = ' ';
				} else if (i < chars.length - 2) {
					if (chars[i + 2] == ' ') // for the case, e.g., "don't"
						continue;
				} else if (isShorten(chars, i)) {
					continue;
				} else {
					chars[i] = ' ';
				}
			}
		}
	}

	private void removeHTMLsymbols(char[] chars) {
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '&') {// html character
				int j = i;
				while (j < chars.length) {
					if (chars[j] == ' ')
						break;
					else if (punctuations.contains(chars[j]))
						break;
					else if (validPrefixSymbols.contains(chars[j]))
						break;
					else {
						chars[j] = ' ';
						j++;
					}
				}
				i = j;
			}
		}
	}

	private int getWord(char[] chars, int i) {
		int j = i;
		while (true) {
			if (j >= chars.length)
				break;
			else if (chars[j] == ' ') {
				break;
			} else {
				j++;
			}
		}
		return j;
	}

	private boolean isNumber(char[] chars, int start, int end) {
		for (int i = start; i < end; i++) {
			if (Character.isDigit(chars[i])) {
				continue;
			}
			if (chars[i] == '.') {
				continue;
			}
			if (chars[i] == ',') {
				continue;
			}
			return false;
		}
		return true;
	}

	private boolean isHour(char[] chars, int start, int end) {
		for (int i = start; i < end; i++) {
			if (Character.isDigit(chars[i])) {
				continue;
			}
			if (chars[i] == ':') {
				continue;
			}
			return false;
		}
		return true;
	}

	private boolean isURL(String text, int i, int j) {
		return urlValidator.isValid(text.substring(i, j));
	}

	private boolean isEnglish(char[] chars, int start, int end) {
		for (int i = start; i < end; i++)
			if ((int) chars[i] > 128)
				return false;
		return true;
	}

	private void removeSymbolInWord(char[] chars, int start, int end) {
		int i = end - 1;
		while (!Character.isLetterOrDigit(chars[i])) {
			if (validSuffixSymbols.contains(chars[i])) {
				break;
			}
			chars[i] = ' ';
			i--;
			if (i == start)
				return;
		}

		i = start;
		while (!Character.isLetterOrDigit(chars[i])) {
			if (validPrefixSymbols.contains(chars[i])) {
				break;
			}
			chars[i] = ' ';
			i++;
			if (i == end)
				return;
		}
	}

	private void ptScreen(char[] chars) {
		System.out.printf("array = [");
		for (int i = 0; i < chars.length; i++) {
			System.out.printf("%c", chars[i]);
		}
		System.out.println("]");
	}

	public List<String> newTermExtraction(String text) {
		//System.out.printf("text = %s\n", text);
		char[] chars = text.trim().toCharArray();

		removeNewLineAndTabCharacter(chars);
		removeOriginalAuthors(chars);

		removeHTMLsymbols(chars);
		//System.out.printf("After remove symbol:\t");
		//ptScreen(chars);

		removeQuotationSymbols(chars);
		//System.out.printf("After remove quotation:\t");
		//ptScreen(chars);

		removePunct(chars);
		//System.out.printf("After remove punctuations and tab:\t");
		//ptScreen(chars);

		int i = 0;
		while (i < chars.length) {
			int j = getWord(chars, i);
			if (j == i) {
				i++;
				continue;
			}
			// System.out.printf("\t isNumber = %s", isNumber(chars, i, j));
			// System.out.printf("\t isHours = %s", isHour(chars, i, j));
			// System.out.printf("\t isURL = %s", isURL(text, i, j));
			if (isNumber(chars, i, j)) {
				i = j;
			} else if (isHour(chars, i, j)) {
				i = j;
			} else if (isURL(text, i, j)) {
				i = j;
			} else if (!isEnglish(chars, i, j)) {
				for (int p = i; p < j; p++) {
					chars[p] = ' ';
				}
				i = j;
			} else {
				for (int p = i; p < j; p++) {
					if (punctuations.contains(chars[p])) {
						chars[p] = ' ';
					} else {
						chars[p] = Character.toLowerCase(chars[p]);
					}
				}
				removeSymbolInWord(chars, i, j);
				i = j;
			}
		}

		List<String> terms = new ArrayList<String>();
		i = 0;
		while (i < chars.length) {
			int j = getWord(chars, i);
			if (j == i) {
				i++;
				continue;
			}

			String term = new String(chars, i, j - i);
			if (!stopWords.contains(term)) {
				//System.out.printf("\nnew-word: %s", term);
				terms.add(terms.size(), term);
			}

			i = j;
		}

		return terms;
	}

	private String removeSymbolInText(String text) {
		try {
			char[] chars = text.trim().toCharArray();
			int i = 0;
			while (i < chars.length) {
				// System.out.printf("i = %d char = %c\n", i, chars[i]);
				if (isURLStart(chars, i)) {// check url
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
						j++;
					}
					if (j == i + 1) {
						i++;
						continue;
					}
					if (urlValidator.isValid(text.substring(i, j))) {
						i = j;
						// System.out.println("\t\tVALID");
					} else {
						// System.out.println("\t\tINVALID");
						for (int p = i; p < j; p++) {
							if (punctuations.contains(chars[p])) {
								chars[p] = ' ';
							}
						}
						i = j;
					}
				} else if (punctuations.contains(chars[i])) {
					chars[i] = ' ';
					i++;
				} else if (chars[i] == '\n') {// newline
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
						else if (punctuations.contains(chars[j]))
							break;
						else if (validPrefixSymbols.contains(chars[j]))
							break;
						else {
							chars[j] = ' ';
							j++;
						}
					}
					i = j;
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

	private boolean isEnglish(String word) {
		for (int i = 0; i < word.length(); i++)
			if ((int) word.charAt(i) > 128)
				return false;
		return true;
	}

	private String removeSymbolInWord(String word) {
		if (!isEnglish(word)) {
			return null;
		}
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
		// message = removeSymbolInText(message);
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
			if (term == null)
				continue;
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
		// System.out.printf("tweet = %s\n", tweet);
		// tweet = getTweetContent(tweet);
		// List<String> terms = termExtraction(tweet);
		List<String> terms = newTermExtraction(tweet);
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

		// char c = '\u2026'; // System.out.println("c = " + (int) c);

		TweetPreprocessingUtils nlpUtils = new TweetPreprocessingUtils();
		// nlpUtils.checkQuoteSymbols();

		// nlpUtils.checkStopWordList();

		String message = "RT @MattAsherS: Sessions vote may occur early. Pls call senators. We need more hearings 2 address #MuslimBan &amp; Sanc Cities XOsâ€¦ ";
		System.out.printf("message = %s\n", message);
		List<String> terms = nlpUtils.extractTermInTweet(message);

		for (int i = 0; i < terms.size(); i++) {
			System.out.printf("\ni = %d term = |%s|", i, terms.get(i));
		}

	}

}
