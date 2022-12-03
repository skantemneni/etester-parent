package com.etester.data.domain.content.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name="answer")
public class Answer {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_answer")
	private Long idAnswer;
	
	@Column(name = "id_question")
	private Long idQuestion;
	
    @Column(name = "seq")
	private Integer seq;

    @Column(name = "correct")
    private Integer correct;

	@NotNull
    @Column(name = "text")
	private String text;

    @Column(name = "addl_info")
	private String addlInfo;

    // answer_compare_type can be one of 3 values: 1=text, 2=integer, 3=numeric.
    // Default is text compare (1).'
    @Column(name = "answer_compare_type")
	private Integer answerCompareType;

    // Value here depends on what's entered on the answer_compare_type values.
    // 1.) For Text compare (1) this value is a 3 char string for, the characters representing the following:
    //		a.) 1st char: 0=case insensitive, 1= case sensitive
    //		b.) 2nd char: 0=do not trim surrounding white space, 1 = trim surrounding white space
    //		c.) 3rd char: 0= trim interior white spaces to one white space, 1 = leave interior white spaces alone
    // 2.) For Integer Compare (2) this value has no significance
    // 3.) For Decimal Compare (3) this value is a number indicating the number of digits of precision to compare
    @Column(name = "answer_compare_addl_info")
	private String answerCompareAddlInfo;
    
    private Integer precisionDigits = 2;
    private boolean caseSensitive = false;
    private boolean trimOuterSpaces = true;
    private boolean trimExtraInnerSpaces = false;
    
}
