package com.gzachos.ir.gui;

public class IfaceRangeQuery {
	private String field;
	private long lowerBound;
	private long upperBound;
	
	public IfaceRangeQuery(String field, long lowerBound, long upperBound) {
		this.field = field;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public String getField() {
		return field;
	}
	
	public long getLowerBound() {
		return lowerBound;
	}

	public long getUpperBound() {
		return upperBound;
	}
	
}
