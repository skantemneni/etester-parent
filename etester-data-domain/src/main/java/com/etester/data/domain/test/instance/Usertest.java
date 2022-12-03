package com.etester.data.domain.test.instance;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name="usertest")
public class Usertest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usertest")
    private Long idUsertest;

    @Column(name = "id_provider")
	private Long idProvider;

    @Column(name = "id_profile")
	private Long idProfile;

    @Column(name = "profile_name")
	private String profileName;
    
    @Column(name = "profilesegment_name")
	private String profilesegmentName;
    
	@Column(name = "id_test")
	private Long idTest;

	// Assignment,Test,Quiz,Challenge
    @Size(min = 1, max = 45)
    @Column(name = "test_type")
	private String testType;

    @Column(name = "id_user")
	private Long idUser;

	@Column(name = "auto_grade")
    private Integer autoGrade;

	@Column(name = "auto_publish_results")
    private Integer autoPublishResults;

    // 1=user, 2=usergroup, null=1
    @Column(name = "user_type")
	private Integer userType;

    @Size(min = 0, max = 100)
	private String name;

	@Size(min = 0, max = 200)
	private String description;

	@Column(name = "test_assignment_date")
	private Date testAssignmentDate;

	@Column(name = "test_completion_date")
	private Date testCompletionDate;

	@Column(name = "test_reassignment_date")
	private Date testReassignmentDate;

	// assigned,started,submitted,corrections,completed,archived
    @Size(min = 1, max = 45)
    @Column(name = "test_status")
	private String testStatus;

	// 0=true,1=false,null=1
    @Column(name = "test_archived")
	private Integer testArchived;

	// 0=true, 1=false, null=0
    @Column(name = "active")
	private Integer active;

	// 0=false,1=true,null=0
    @Column(name = "administered_offline")
	private Integer administeredOffline;
    
	@Column(name = "is_report_available_to_view_by_student")
    private Integer isReportAvailableToViewByStudent;
    
    
    private String associatedUserName;
    
	private String associatedFirstName;
    
	private String associatedLastName;
    
	private Integer timeInMinutes;

	private Integer questionCount;

	private String examtrackDescription;

	private String organizationName;

	public Usertest() {
    }

}
