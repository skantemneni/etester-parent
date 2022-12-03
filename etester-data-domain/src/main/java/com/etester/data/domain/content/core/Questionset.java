package com.etester.data.domain.content.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="questionset")
public class Questionset {

	@Id
    @Column(name = "id_questionset")
	private Long idQuestionset;
	
	@Column(name = "id_section")
	private Long idSection;
	
    @Column(name = "text")
	private String text;

}
