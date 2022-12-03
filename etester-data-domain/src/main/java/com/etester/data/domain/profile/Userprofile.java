package com.etester.data.domain.profile;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="userprofile")
public class Userprofile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_userprofile")
    private Long idUserprofile;

    @Column(name = "id_provider")
	private Long idProvider;

    @Column(name = "id_profile")
	private Long idProfile;

    @Column(name = "id_student")
	private Long idStudent;

	private String name;
    
	private String description;

	@Column(name = "profile_assignment_date")
	private Date profileAssignmentDate;

	@Column(name = "profile_start_date")
	private Date profileStartDate;

	public Userprofile() {
    }
}
