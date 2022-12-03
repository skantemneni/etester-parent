package com.etester.data.domain.test;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name="printsettings")
public class Printsettings {

	@Id
    @Column(name = "id_test")
    private Long idTest;
	
	@NotNull
	@Column(name = "settings")
	private String settings;

	@Column(name = "date_saved")
	private Date dateSaved;

}
