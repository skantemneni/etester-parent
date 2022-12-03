package com.etester.data.domain.content;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name="printsectionsettings")
public class Printsectionsettings {

	@Id
    @Column(name = "id_section")
    private Long idSection;
	
	@NotNull
	@Column(name = "settings")
	private String settings;

	@Column(name = "date_saved")
	private Date dateSaved;

}
