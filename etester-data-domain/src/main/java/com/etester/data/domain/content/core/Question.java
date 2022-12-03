package com.etester.data.domain.content.core;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name="question")
public class Question {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_question")
	private Long idQuestion;
	
	@Column(name = "id_section")
	private Long idSection;
	
    @Size(min=0, max=100)
	private String name;

    @Size(min=0, max=200)
	private String description;

    @Size(min=0, max=45)
	private String banner;

    @Size(min=0, max=200)
	private String heading;

    @Size(min=0, max=200)
	private String instructions;

    @Column(name = "question_type")
    @Size(min=0, max=45)
	private String questionType;

    @Column(name = "text")
	private String text;

    @Column(name = "addl_info")
	private String addlInfo;

    @Column(name = "text_precontext")
	private String textPrecontext;

    @Column(name = "text_postcontext")
	private String textPostcontext;

    @Column(name = "multiple_answers")
    private Integer multipleAnswers;

    @Column(name = "all_answers")
    private Integer allAnswers;

    @Column(name = "points")
    private Integer points;

    @Column(name = "id_questionset")
	private Long idQuestionset;

    @Column(name = "reference_skills")
	private String referenceSkills;

	// Relevant in special scenarios where the Questions Parent Skill, Topic and Level does not mean much since its 
	// a Test Question (Topic Test or Level Test or Mock Exam).  
    @Column(name = "id_reference_topic")
	private Long idReferenceTopic;

    @Column(name = "id_reference_level")
	private Long idReferenceLevel;

	private String questionsetText;

	private Integer questionOrder;
	
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name= "id_answer")
    private List<Answer> answers;

}
