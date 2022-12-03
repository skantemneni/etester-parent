package com.etester.data.domain.util;

public class QuestionCompareConstants {

	public static enum QuestionCompareTypes {
		TEXT_COMPARE (1),
		INTEGER_COMPARE (2),
		DECIMAL_COMPARE (3);
		
		private final Integer compare_type;
		QuestionCompareTypes(Integer compare_type) {
	        this.compare_type = compare_type;
	    }
		public Integer compareType() { return compare_type; }
	}
	
}
