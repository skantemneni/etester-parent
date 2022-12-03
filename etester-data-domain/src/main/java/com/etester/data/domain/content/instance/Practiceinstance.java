package com.etester.data.domain.content.instance;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Table(name = "practiceinstance")
public class Practiceinstance {

	@NotNull
    @Column(name = "id_user")
	private Long idUser;

	@NotNull
    @Column(name = "id_artifact")
	private Long idArtifact;

	// Channel, Subject, Level, Topic, Skill
    @Size(min = 1, max = 45)
    @Column(name = "artifact_type")
	private String artifactType;

    // Same as test_status but... 
	// only assigned,started,corrections,completed are used
    @Size(min = 1, max = 45)
    @Column(name = "practice_status")
	private String practiceStatus;

	// adaptive, all
    @Size(min = 0, max = 45)
    @Column(name = "practice_method")
	private String practiceMethod;

	@Column(name = "date_saved")
	private Date dateSaved;

	// Carries information about the status of individual sections for the artifact.
	// We expect that the "practiceStatus" field on the Practiceinstance to carry the summary status for the Artifact. This field will carry the additional supporting evidence.
    @Column(name = "practice_additional_info")
	private String practiceAdditionalInfo;

}
