package com.etester.data.domain.content.instance;

import com.etester.data.domain.content.core.Question;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="testinstance_detail")
public class TestinstanceDetail {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_testinstance_detail")
    private Long idTestinstanceDetail;
	
	@NotNull
    @Column(name = "id_testinstance")
    private Long idTestinstance;

	@NotNull
    @Column(name = "id_testinstance_section")
    private Long idTestinstanceSection;

	@NotNull
    @Column(name = "id_section")
    private Long idSection;

	@NotNull
    @Column(name = "id_question")
    private Long idQuestion;

	@NotNull
    @Column(name = "question_status")
	private String questionStatus;

	@NotNull
    @Column(name = "answer_text")
	private String answerText;

	@Column(name = "user_points")
    private Float userPoints;
	
	@Column(name = "time_in_seconds")
    private Integer timeInSeconds;

	@Column(name = "attempt_quality")
    private Integer attemptQuality;

	@Transient
	private Question question;

	// bunch of read only fields good on the front end
	private String referenceSkills;
	private Long idReferenceTopic;
	private String topicNameReference;
	private Long idReferenceLevel;
	private String levelNameReference;
		
	public TestinstanceDetail () {
	}

	public TestinstanceDetail (Long idTestinstance, Long idTestinstanceSection, Long idSection, Long idQuestion, String questionStatus, String answerText, float userPoints, int questionTimeInSeconds, int attemptQuality) {
		this.idTestinstance = idTestinstance;
		this.idTestinstanceSection = idTestinstanceSection;
		this.idSection = idSection;
		this.idQuestion = idQuestion; 
		// should be (C)ORRECT or (W)RONG or (N)OT_ANSWERED
		this.questionStatus = questionStatus;
		this.answerText = answerText;
		this.userPoints = userPoints;
		this.timeInSeconds = questionTimeInSeconds;
		this.attemptQuality = attemptQuality;
	}
	/**
	 * @return the idTestinstanceDetail
	 */
	public Long getIdTestinstanceDetail() {
		return idTestinstanceDetail;
	}

	/**
	 * @param idTestinstanceDetail the idTestinstanceDetail to set
	 */
	public void setIdTestinstanceDetail(Long idTestinstanceDetail) {
		this.idTestinstanceDetail = idTestinstanceDetail;
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
	 * @return the idQuestion
	 */
	public Long getIdQuestion() {
		return idQuestion;
	}

	/**
	 * @param idQuestion the idQuestion to set
	 */
	public void setIdQuestion(Long idQuestion) {
		this.idQuestion = idQuestion;
	}

	/**
	 * @return the questionStatus
	 */
	public String getQuestionStatus() {
		return questionStatus;
	}

	/**
	 * @param questionStatus the questionStatus to set
	 */
	public void setQuestionStatus(String questionStatus) {
		this.questionStatus = questionStatus;
	}

	/**
	 * @return the answerText
	 */
	public String getAnswerText() {
		return answerText;
	}

	/**
	 * @param answerText the answerText to set
	 */
	public void setAnswerText(String answerText) {
		this.answerText = answerText;
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
	 * @return the attemptQuality
	 */
	public Integer getAttemptQuality() {
		return attemptQuality;
	}

	/**
	 * @param attemptQuality the attemptQuality to set
	 */
	public void setAttemptQuality(Integer attemptQuality) {
		this.attemptQuality = attemptQuality;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TestinstanceDetail [idTestinstanceDetail="
				+ idTestinstanceDetail + ", idTestinstance=" + idTestinstance
				+ ", idSection=" + idSection + ", idQuestion=" + idQuestion
				+ ", questionStatus=" + questionStatus + ", answerText="
				+ answerText + "]";
	}

	/**
	 * @return the referenceSkills
	 */
	public String getReferenceSkills() {
		return referenceSkills;
	}

	/**
	 * @param referenceSkills the referenceSkills to set
	 */
	public void setReferenceSkills(String referenceSkills) {
		this.referenceSkills = referenceSkills;
	}

	/**
	 * @return the idReferenceTopic
	 */
	public Long getIdReferenceTopic() {
		return idReferenceTopic;
	}

	/**
	 * @param idReferenceTopic the idReferenceTopic to set
	 */
	public void setIdReferenceTopic(Long idReferenceTopic) {
		this.idReferenceTopic = idReferenceTopic;
	}

	/**
	 * @return the topicNameReference
	 */
	public String getTopicNameReference() {
		return topicNameReference;
	}

	/**
	 * @param topicNameReference the topicNameReference to set
	 */
	public void setTopicNameReference(String topicNameReference) {
		this.topicNameReference = topicNameReference;
	}

	/**
	 * @return the idReferenceLevel
	 */
	public Long getIdReferenceLevel() {
		return idReferenceLevel;
	}

	/**
	 * @param idReferenceLevel the idReferenceLevel to set
	 */
	public void setIdReferenceLevel(Long idReferenceLevel) {
		this.idReferenceLevel = idReferenceLevel;
	}

	/**
	 * @return the levelNameReference
	 */
	public String getLevelNameReference() {
		return levelNameReference;
	}

	/**
	 * @param levelNameReference the levelNameReference to set
	 */
	public void setLevelNameReference(String levelNameReference) {
		this.levelNameReference = levelNameReference;
	}

}
