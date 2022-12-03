package com.etester.data.domain.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="derived_section_question")
public class DerivedSectionQuestion {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_derived_section_question")
	private Long idDerivedSectionQuestion;
	
	@Column(name = "id_section")
	private Long idSection;
	
	@Column(name = "id_question")
	private Long idQuestion;
	
    @Column(name = "question_order")
    private Integer questionOrder;

}
