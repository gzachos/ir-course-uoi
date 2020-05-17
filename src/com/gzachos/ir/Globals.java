package com.gzachos.ir;

import java.util.Map;

public class Globals {
	
	public static final String TITLE_FIELD_NAME = "title", CONTENT_FIELD_NAME = "content",
			MULTIMEDIA_FIELD_NAME = "multimedia", QUOTES_FIELD_NAME = "quotes",
			REFERENCES_FIELD_NAME = "references", URL_FIELD_NAME = "url", SUMMARY_FIELD_NAME = "summary",
			PUBLICATION_TIME_FIELD_NAME = "published", UPDATE_TIME_FIELD_NAME = "updated";
	public static final String[] DOCUMENT_FIELDS = {TITLE_FIELD_NAME, CONTENT_FIELD_NAME, MULTIMEDIA_FIELD_NAME,
			QUOTES_FIELD_NAME, REFERENCES_FIELD_NAME};
	public static final int HITS_PER_PAGE = 10;
	public static final Map<String, Float> QUERY_BOOSTS = Map.of(
			"title",      10.0f,
			"content",     1.0f,
			"multimedia",  0.5f,
			"quotes",      1.0f,
			"references",  0.1f
	);
	public static final Map<String, Float> DEFAULT_QUERY_BOOSTS = Map.of(
			"title",      10.0f,
			"content",     1.0f,
			"multimedia",  1.0f,
			"quotes",      1.0f,
			"references",  1.0f
	);
	public static final Map<String, Float> IDENTITY_QUERY_BOOSTS = Map.of(
			"title",       1.0f,
			"content",     1.0f,
			"multimedia",  1.0f,
			"quotes",      1.0f,
			"references",  1.0f
	);
	public static final String QUERY_PARSE_ERROR = "Error parsing query!",
			QUERY_EXEC_ERROR = "Error executing query!";
}

