package com.etester.data.domain.test;

import com.etester.data.domain.content.core.Section;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name="testsection")
public class Testsection {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_testsection")
    private Long idTestsection;
	
	@NotNull
    @Column(name = "id_testsegment")
	private Long idTestsegment;

	@NotNull
    @Column(name = "id_section_ref")
	private Long idSectionRef;

	@Column(name = "name")
    @Size(min=1, max=100)
	private String name;
    
	@Column(name = "description")
    @Size(min=0, max=200)
	private String description;

	@Column(name = "instructions_name")
    @Size(min=0, max=100)
	private String instructionsName;

	@Column(name = "report_subject")
    @Size(min=0, max=100)
	private String reportSubject;

	@Column(name = "time_to_answer")
	private Integer timeToAnswer;

    @Column(name = "seq")
	private Integer seq;

	@Column(name = "question_count")
	private Integer questionCount;
	
	@Column(name = "point_count")
	private Float pointCount;
	
	@Column(name = "points_per_question")
	private Float pointsPerQuestion;
	
	@Column(name = "negative_points_per_question")
	private Float negativePointsPerQuestion;
	
	@Column(name = "unanswered_points_per_question")
	private Float unansweredPointsPerQuestion;
	
	@Column(name = "question_start_index")
	private Integer questionStartIndex;
	
	@Column(name = "distributed_scoring")
	private Integer distributedScoring;
	
	// Variable that may contain the actual section - with questions and answers - used for creating and caching 
	// serialized tests 
	@Transient
	private Section section;
}
