package com.gzachos.ir;

import java.util.Map;

public class Globals {
	public static final String[] DOCUMENT_FIELDS = {"title", "content", "multimedia", "quotes", "references"};
	public static final int HITS_PER_PAGE = 10;
	public static final Map<String, Float> QUERY_BOOSTS = Map.of(
			"title",      10.0f,
			"content",     1.0f,
			"multimedia",  0.5f,
			"quotes",      1.0f,
			"references",  0.1f
	);
}

