package com.etester.data.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="mysection")
public class Mysection {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mysection")
    private Long idMysection;
	
    @Column(name = "id_section")
    private Long idSection;

    @Column(name = "id_provider")
	private Long idProvider;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private Integer description;

}
