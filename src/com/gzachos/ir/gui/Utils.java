package com.gzachos.ir.gui;

import java.util.ArrayList;

import javafx.scene.text.Text;

public class Utils {
	public final static int MAX_HIGHLIGHT_LENGTH = 180;
	public final static int MIN_SENTENCE_LENGTH = 25;

	public static ArrayList<Text> parseHighlightText(String highlightStr) {
		ArrayList<Text> texts = new ArrayList<Text>();
		if (highlightStr == null)
			return texts;
		if (!highlightStr.contains("<b>")) {
			// texts.add(new Text(highlightStr));
			return texts;
		}

		String highlightString = shortenHighlightStr(highlightStr);

		while (true) {
			int boldStart = highlightString.indexOf("<b>");
			int boldEnd = highlightString.indexOf("</b>");
			if (boldStart == -1 || boldEnd == -1) {
				texts.add(getText(highlightString, false));
				break;
			}
			if (boldStart > 0) {
				String plainText = highlightString.substring(0, boldStart);
				texts.add(getText(plainText, false));
			}
			String boldText = highlightString.substring(boldStart + "<b>".length(), boldEnd);
			texts.add(getText(boldText, true));

			highlightString = highlightString.substring(boldEnd + "</b>".length());
		}

		return texts;
	}

	private static Text getText(String textStr, boolean bold) {
		Text text = new Text(textStr);
		text.setStyle(((bold) ? "-fx-font-weight: bold; " : "") + "-fx-font-size: 12");
		return text;
	}

	private static String shortenHighlightStr(String highlightStr) {
		String str = "";
		String lines[] = highlightStr.split("\n");
		for (int li = 0; li < lines.length; li++) {
			String line = lines[li];
			if (!line.contains("<b>"))
				continue;
			String sentences[] = line.split("\\.\\s");
			for (int si = 0; si < sentences.length; si++) {
				String sentence = sentences[si];
				if (!sentence.contains("<b>"))
					continue;
				str += getSentencePart(sentence, str.length());
			}
		}
		return str;
	}

	private static String getSentencePart(String sentence, int previousLength) {
		String str = "";
		// The whole sentence fits.
		if (previousLength + sentence.length() + 2 <= MAX_HIGHLIGHT_LENGTH)
			return " " + sentence + ".";
		// We have to tokenize the sentence.
		String[] words = sentence.split("\\s");
		for (String word : words) {
			if (previousLength + str.length() + word.length() > MAX_HIGHLIGHT_LENGTH)
				break;
			str += " " + word;
		}
		if (str.length() < MIN_SENTENCE_LENGTH)
			return "";
		return str + "...";
	}

	public static boolean alreadySuggested(ArrayList<QuerySpellSuggestion> suggestions,
			QuerySpellSuggestion newSuggestion) {
		for (QuerySpellSuggestion suggestion : suggestions)
			if (suggestion.equals(newSuggestion))
				return true;
		return false;
	}

	public static QuerySpellSuggestion getSuggestionByTerm(ArrayList<QuerySpellSuggestion> suggestions,
			String newTerm) {
		for (QuerySpellSuggestion suggestion : suggestions)
			if (suggestion.getNewTerm().equals(newTerm))
				return suggestion;
		return null;
	}

}
