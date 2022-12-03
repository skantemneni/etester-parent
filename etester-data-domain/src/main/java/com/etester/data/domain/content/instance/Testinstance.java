package com.etester.data.domain.content.instance;

import java.util.Date;
import java.util.List;

import com.etester.data.domain.test.TestProfile;
import com.etester.data.domain.user.Webuser;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name="testinstance")
public class Testinstance {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_testinstance")
    private Long idTestinstance;
	
	@NotNull
    @Column(name = "id_test")
	private Long idTest;

	@NotNull
    @Column(name = "id_user")
	private Long idUser;

	@NotNull
    @Column(name = "id_provider")
	private Long idProvider;

	@NotNull
    @Column(name = "id_usertest")
	private Long idUsertest;

	@NotNull
    @Size(min=1, max=100)
	private String name;
    
    @Size(min=0, max=200)
	private String description;

	// Assignment,Test,Quiz,Challenge
    @Size(min = 1, max = 45)
    @Column(name = "test_type")
	private String testType;

	@Column(name = "question_count")
	private Integer questionCount;
	
	@Column(name = "point_count")
	private Float pointCount;
	
	@Column(name = "time_to_answer")
	private Integer timeToAnswer;

	@Column(name = "report_by_subject")
	private Integer reportBySubject;

	@Column(name = "correct_count")
    private Integer correctCount;
	
	@Column(name = "wrong_count")
    private Integer wrongCount;
	
	@Column(name = "unanswered_count")
    private Integer unansweredCount;
	
	@Column(name = "user_points")
    private Float userPoints;
	
	@Column(name = "time_in_seconds")
    private Integer timeInSeconds;
	
	@Column(name = "archived")
    private Integer archived;
	
	@Column(name = "test_completion_date")
	private Date testCompletionDate;
	
	@Column(name = "perfect_attempts")
    private Integer perfectAttempts;
	
	@Column(name = "inefficient_attempts")
    private Integer inefficientAttempts;
	
	@Column(name = "bad_attempts")
    private Integer badAttempts;
	
	@Column(name = "wasted_attempts")
    private Integer wastedAttempts;
	
	@Column(name = "attempt_quality")
    private Float attemptQuality;
	
	@Column(name = "percentile")
    private Integer percentile;
	
	@Column(name = "is_report_available_to_view_by_student")
    private Integer isReportAvailableToViewByStudent;
	
	// 0=false,1=true,null=0
	private Integer administeredOffline;
    
	private String studentDisplayName;

	@Transient
	private Webuser studentDetails;

	// organizationName corresponding to id_organization
	private String organizationName;
    
	// providerName corresponding to id_provider
	private String providerName;

	// organizationName corresponding to id_organization
	private String channelName;
	
	// examtrack field carried over from a test
	private String examtrack;

	// String version of a rank, if one exists for teh test category
	private String estimatedRankString;
    
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_testinstance")
	private List<TestinstanceSection> testsections;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name= "id_test", insertable=false, updatable=false)
    private TestProfile testProfile;

}
