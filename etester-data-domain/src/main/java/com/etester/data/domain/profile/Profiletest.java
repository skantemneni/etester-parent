package com.etester.data.domain.profile;

import java.util.Date;

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
@Table(name="profiletest")
public class Profiletest {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profiletest")
    private Long idProfiletest;
	
//	@NotNull
    @Column(name = "id_profile")
	private Long idProfile;

    @Column(name = "id_profilesegment")
	private Long idProfilesegment;

	@NotNull
    @Column(name = "id_test_ref")
	private Long idTestRef;

	@Column(name = "name")
    @Size(min=1, max=100)
	private String name;
    
	@Column(name = "description")
    @Size(min=0, max=200)
	private String description;

    @Column(name = "seq")
	private Integer seq;

	@Column(name = "test_provision_day")
	private Integer testProvisionDay;
	
	@Column(name = "test_removal_day")
	private Integer testRemovalDay;
	
	@Column(name = "test_provision_date")
	private Date testProvisionDate;

	@Column(name = "test_removal_date")
	private Date testRemovalDate;

	@Column(name = "initial_active")
	private Integer initialActive;

	// set on the Test that we are referring to
    private String testType;

}