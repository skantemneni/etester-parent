package com.etester.data.domain.content.additional.wordlist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name="cwordusage")
public class Cwordusage {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cwordusage")
	private Long idCwordusage;
	
	@NotNull
    @Column(name = "id_cword")
	private Long idCword;
	
	@Column(name = "text")
	private String text;

    @Size(min=0, max=200)
	@Column(name = "source")
	private String source;

}
