package com.etester.data.domain.content.instance;

import java.util.Date;

import com.etester.data.domain.content.core.Section;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Sesi Kantemneni
 *
 * Commenting out the Entity Annotation
 * Throws Error: org.hibernate.AnnotationException: No identifier specified for entity: com.etester.data.domain.content.instance.Usersectionresponse 
 * You are missing a field annotated with @Id. Each @Entity needs an @Id - this is the primary key in the database.
 * If you don't want your entity to be persisted in a separate table, but rather be a part of other entities, you can use @Embeddable instead of @Entity.
 * If you want simply a data transfer object to hold some data from the hibernate entity, use no annotations on it whatsoever - leave it a simple pojo.
 */
@Data
// @Entity
@Table(name="usersectionresponse")
public class Usersectionresponse {

    @Column(name = "id_user")
	private Long idUser;

	@NotNull
    @Column(name = "id_section")
    private Long idSection;

    @Column(name = "id_artifact")
    private Long idArtifact;

	@NotNull
	@Column(name = "response")
	private String response;

	@Column(name = "date_saved")
	private Date dateSaved;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_section")
    private Section section;
	
	// may contain an override value for the section name
	private String sectionName = null;

	// indicates if the task is complete 
	private boolean completed = false;

	// indicates if the task is complete 
	private String completionStatus;

	// indicates the practice mode of this test 
	private String practiceMethod;
	
	// indicates what type of artifact we are testing 
	private String artifactType;

	private String practiceAdditionalInfo;

}
