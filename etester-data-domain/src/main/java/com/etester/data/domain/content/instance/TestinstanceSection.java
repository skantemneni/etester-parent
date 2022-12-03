package com.etester.data.domain.content.instance;

import java.util.List;

import com.etester.data.domain.test.TestsectionProfile;

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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="testinstance")
public class TestinstanceSection {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_testinstance_section")
    private Long idTestinstanceSection;
	
	@NotNull
    @Column(name = "id_testinstance")
	private Long idTestinstance;

	@NotNull
    @Column(name = "id_section")
	private Long idSection;

	@NotNull
    @Column(name = "id_testsection")
	private Long idTestsection;

    @Size(min=1, max=100)
    @Column(name = "testsection_name")
	private String testsectionName;
    
    @Size(min=0, max=200)
    @Column(name = "testsection_description")
	private String testsectionDescription;

	@Column(name = "report_subject")
    @Size(min=0, max=100)
	private String reportSubject;

	@Column(name = "question_count")
	private Integer questionCount;
	
	@Column(name = "point_count")
	private Float pointCount;
	
	@Column(name = "points_per_question")
	private Float pointsPerQuestion;
	
	@Column(name = "negative_points_per_question")
	private Float negativePointsPerQuestion;
	
	@Column(name = "unanswered_points_per_question")
	private Float unansweredPointsPerQuestion;
	
	@Column(name = "time_to_answer")
	private Integer timeToAnswer;

    @Column(name = "seq")
	private Integer seq;

	@Column(name = "question_start_index")
	private Integer questionStartIndex;
	
	@Column(name = "distributed_scoring")
	private Integer distributedScoring;
	
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
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_testinstance_section")
	private List<TestinstanceDetail> testquestions;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name= "id_testsection", insertable =  false, updatable = false)
    private TestsectionProfile testsectionProfile;

	/**
	 * @return the idTestinstanceSection
	 */
	public Long getIdTestinstanceSection() {
		return idTestinstanceSection;
	}

	/**
	 * @param idTestinstanceSection the idTestinstanceSection to set
	 */
	public void setIdTestinstanceSection(Long idTestinstanceSection) {
		this.idTestinstanceSection = idTestinstanceSection;
	}

	/**
	 * @return the idTestinstance
	 */
	public Long getIdTestinstance() {
		return idTestinstance;
	}

	/**
	 * @param idTestinstance the idTestinstance to set
	 */
	public void setIdTestinstance(Long idTestinstance) {
		this.idTestinstance = idTestinstance;
	}

	/**
	 * @return the idSection
	 */
	public Long getIdSection() {
		return idSection;
	}

	/**
	 * @param idSection the idSection to set
	 */
	public void setIdSection(Long idSection) {
		this.idSection = idSection;
	}

	/**
	 * @return the idTestsection
	 */
	public Long getIdTestsection() {
		return idTestsection;
	}

	/**
	 * @param idTestsection the idTestsection to set
	 */
	public void setIdTestsection(Long idTestsection) {
		this.idTestsection = idTestsection;
	}

	/**
	 * @return the testsectionName
	 */
	public String getTestsectionName() {
		return testsectionName;
	}

	/**
	 * @param testsectionName the testsectionName to set
	 */
	public void setTestsectionName(String testsectionName) {
		this.testsectionName = testsectionName;
	}

	/**
	 * @return the testsectionDescription
	 */
	public String getTestsectionDescription() {
		return testsectionDescription;
	}

	/**
	 * @param testsectionDescription the testsectionDescription to set
	 */
	public void setTestsectionDescription(String testsectionDescription) {
		this.testsectionDescription = testsectionDescription;
	}

	/**
	 * @return the reportSubject
	 */
	public String getReportSubject() {
		return reportSubject;
	}

	/**
	 * @param reportSubject the reportSubject to set
	 */
	public void setReportSubject(String reportSubject) {
		this.reportSubject = reportSubject;
	}

	/**
	 * @return the questionCount
	 */
	public Integer getQuestionCount() {
		return questionCount;
	}

	/**
	 * @param questionCount the questionCount to set
	 */
	public void setQuestionCount(Integer questionCount) {
		this.questionCount = questionCount;
	}

	/**
	 * @return the pointCount
	 */
	public Float getPointCount() {
		return pointCount;
	}

	/**
	 * @param pointCount the pointCount to set
	 */
	public void setPointCount(Float pointCount) {
		this.pointCount = pointCount;
	}

	/**
	 * @return the pointsPerQuestion
	 */
	public Float getPointsPerQuestion() {
		return pointsPerQuestion;
	}

	/**
	 * @param pointsPerQuestion the pointsPerQuestion to set
	 */
	public void setPointsPerQuestion(Float pointsPerQuestion) {
		this.pointsPerQuestion = pointsPerQuestion;
	}

	/**
	 * @return the negativePointsPerQuestion
	 */
	public Float getNegativePointsPerQuestion() {
		return negativePointsPerQuestion;
	}

	/**
	 * @param negativePointsPerQuestion the negativePointsPerQuestion to set
	 */
	public void setNegativePointsPerQuestion(Float negativePointsPerQuestion) {
		this.negativePointsPerQuestion = negativePointsPerQuestion;
	}

	/**
	 * @return the unansweredPointsPerQuestion
	 */
	public Float getUnansweredPointsPerQuestion() {
		return unansweredPointsPerQuestion;
	}

	/**
	 * @param unansweredPointsPerQuestion the unansweredPointsPerQuestion to set
	 */
	public void setUnansweredPointsPerQuestion(Float unansweredPointsPerQuestion) {
		this.unansweredPointsPerQuestion = unansweredPointsPerQuestion;
	}

	/**
	 * @return the timeToAnswer
	 */
	public Integer getTimeToAnswer() {
		return timeToAnswer;
	}

	/**
	 * @param timeToAnswer the timeToAnswer to set
	 */
	public void setTimeToAnswer(Integer timeToAnswer) {
		this.timeToAnswer = timeToAnswer;
	}

	/**
	 * @return the seq
	 */
	public Integer getSeq() {
		return seq;
	}

	/**
	 * @param seq the seq to set
	 */
	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	/**
	 * @return the questionStartIndex
	 */
	public Integer getQuestionStartIndex() {
		return questionStartIndex;
	}

	/**
	 * @param questionStartIndex the questionStartIndex to set
	 */
	public void setQuestionStartIndex(Integer questionStartIndex) {
		this.questionStartIndex = questionStartIndex;
	}

	/**
	 * @return the distributedScoring
	 */
	public Integer getDistributedScoring() {
		return distributedScoring;
	}

	/**
	 * @param distributedScoring the distributedScoring to set
	 */
	public void setDistributedScoring(Integer distributedScoring) {
		this.distributedScoring = distributedScoring;
	}

	/**
	 * @return the correctCount
	 */
	public Integer getCorrectCount() {
		return correctCount;
	}

	/**
	 * @param correctCount the correctCount to set
	 */
	public void setCorrectCount(Integer correctCount) {
		this.correctCount = correctCount;
	}

	/**
	 * @return the wrongCount
	 */
	public Integer getWrongCount() {
		return wrongCount;
	}

	/**
	 * @param wrongCount the wrongCount to set
	 */
	public void setWrongCount(Integer wrongCount) {
		this.wrongCount = wrongCount;
	}

	/**
	 * @return the unansweredCount
	 */
	public Integer getUnansweredCount() {
		return unansweredCount;
	}

	/**
	 * @param unansweredCount the unansweredCount to set
	 */
	public void setUnansweredCount(Integer unansweredCount) {
		this.unansweredCount = unansweredCount;
	}

	/**
	 * @return the userPoints
	 */
	public Float getUserPoints() {
		return userPoints;
	}

	/**
	 * @param userPoints the userPoints to set
	 */
	public void setUserPoints(Float userPoints) {
		this.userPoints = userPoints;
	}

	/**
	 * @return the timeInSeconds
	 */
	public Integer getTimeInSeconds() {
		return timeInSeconds;
	}

	/**
	 * @param timeInSeconds the timeInSeconds to set
	 */
	public void setTimeInSeconds(Integer timeInSeconds) {
		this.timeInSeconds = timeInSeconds;
	}

	/**
	 * @return the perfectAttempts
	 */
	public Integer getPerfectAttempts() {
		return perfectAttempts;
	}

	/**
	 * @param perfectAttempts the perfectAttempts to set
	 */
	public void setPerfectAttempts(Integer perfectAttempts) {
		this.perfectAttempts = perfectAttempts;
	}

	/**
	 * @return the inefficientAttempts
	 */
	public Integer getInefficientAttempts() {
		return inefficientAttempts;
	}

	/**
	 * @param inefficientAttempts the inefficientAttempts to set
	 */
	public void setInefficientAttempts(Integer inefficientAttempts) {
		this.inefficientAttempts = inefficientAttempts;
	}

	/**
	 * @return the badAttempts
	 */
	public Integer getBadAttempts() {
		return badAttempts;
	}

	/**
	 * @param badAttempts the badAttempts to set
	 */
	public void setBadAttempts(Integer badAttempts) {
		this.badAttempts = badAttempts;
	}

	/**
	 * @return the wastedAttempts
	 */
	public Integer getWastedAttempts() {
		return wastedAttempts;
	}

	/**
	 * @param wastedAttempts the wastedAttempts to set
	 */
	public void setWastedAttempts(Integer wastedAttempts) {
		this.wastedAttempts = wastedAttempts;
	}

	/**
	 * @return the attemptQuality
	 */
	public Float getAttemptQuality() {
		return attemptQuality;
	}

	/**
	 * @param attemptQuality the attemptQuality to set
	 */
	public void setAttemptQuality(Float attemptQuality) {
		this.attemptQuality = attemptQuality;
	}

	/**
	 * @return the percentile
	 */
	public Integer getPercentile() {
		return percentile;
	}

	/**
	 * @param percentile the percentile to set
	 */
	public void setPercentile(Integer percentile) {
		this.percentile = percentile;
	}

	/**
	 * @return the testquestions
	 */
	public List<TestinstanceDetail> getTestquestions() {
		return testquestions;
	}

	/**
	 * @param testquestions the testquestions to set
	 */
	public void setTestquestions(List<TestinstanceDetail> testquestions) {
		this.testquestions = testquestions;
	}

	/**
	 * @return the testsectionProfile
	 */
	public TestsectionProfile getTestsectionProfile() {
		return testsectionProfile;
	}

	/**
	 * @param testsectionProfile the testsectionProfile to set
	 */
	public void setTestsectionProfile(TestsectionProfile testsectionProfile) {
		this.testsectionProfile = testsectionProfile;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TestinstanceSection [idTestinstanceSection="
				+ idTestinstanceSection + ", idTestinstance=" + idTestinstance
				+ ", idSection=" + idSection + ", testsectionName="
				+ testsectionName + ", testsectionDescription="
				+ testsectionDescription + ", questionCount=" + questionCount
				+ ", pointCount=" + pointCount + ", timeToAnswer="
				+ timeToAnswer + ", correctCount=" + correctCount
				+ ", wrongCount=" + wrongCount + ", unansweredCount="
				+ unansweredCount + ", userPoints=" + userPoints
				+ ", timeInSeconds=" + timeInSeconds + ", testquestions="
				+ testquestions + "]";
	}
	
}
