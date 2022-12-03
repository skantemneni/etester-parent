package com.etester.data.domain.user;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="user_alert")
public class UserAlert {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user_alert")
    private Long idUserAlert;

	@NotNull
    @Column(name = "id_provider")
	private Long idProvider;

	@NotNull
    @Size(min=1, max=100)
	private String name;
    
    @Size(min=0, max=200)
	private String description;

    @Column(name = "alert_target_criteria")
	private String alertTargetCriteria;

	@Column(name = "heading")
	private String heading;

	@Column(name = "content")
	private String content;

	@Column(name = "link")
	private String link;

	@Column(name = "alert_type")
    private Integer alertType;

	@Column(name = "alert_priority")
	private Integer alertPriority;

	@Column(name = "alert_creation_date")
	private Date alertCreationDate;

	@Column(name = "alert_publish_date")
	private Date alertPublichDate;

	@Column(name = "alert_expiry_date")
	private Date alertExpiryDate;

	@Column(name = "alert_mode_online")
    private Integer alertModeOnline;

	@Column(name = "alert_mode_email")
    private Integer alertModeEmail;

	@Column(name = "alert_mode_sms")
    private Integer alertModeSms;

	@Column(name = "published")
    private Integer published;

	/**
	 * @return the idUserAlert
	 */
	public Long getIdUserAlert() {
		return idUserAlert;
	}

	/**
	 * @param idUserAlert the idUserAlert to set
	 */
	public void setIdUserAlert(Long idUserAlert) {
		this.idUserAlert = idUserAlert;
	}

	/**
	 * @return the idProvider
	 */
	public Long getIdProvider() {
		return idProvider;
	}

	/**
	 * @param idProvider the idProvider to set
	 */
	public void setIdProvider(Long idProvider) {
		this.idProvider = idProvider;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the alertTargetCriteria
	 */
	public String getAlertTargetCriteria() {
		return alertTargetCriteria;
	}

	/**
	 * @param alertTargetCriteria the alertTargetCriteria to set
	 */
	public void setAlertTargetCriteria(String alertTargetCriteria) {
		this.alertTargetCriteria = alertTargetCriteria;
	}

	/**
	 * @return the heading
	 */
	public String getHeading() {
		return heading;
	}

	/**
	 * @param heading the heading to set
	 */
	public void setHeading(String heading) {
		this.heading = heading;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the link
	 */
	public String getLink() {
		return link;
	}

	/**
	 * @param link the link to set
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * @return the alertType
	 */
	public Integer getAlertType() {
		return alertType;
	}

	/**
	 * @param alertType the alertType to set
	 */
	public void setAlertType(Integer alertType) {
		this.alertType = alertType;
	}

	/**
	 * @return the alertPriority
	 */
	public Integer getAlertPriority() {
		return alertPriority;
	}

	/**
	 * @param alertPriority the alertPriority to set
	 */
	public void setAlertPriority(Integer alertPriority) {
		this.alertPriority = alertPriority;
	}

	/**
	 * @return the alertCreationDate
	 */
	public Date getAlertCreationDate() {
		return alertCreationDate;
	}

	/**
	 * @param alertCreationDate the alertCreationDate to set
	 */
	public void setAlertCreationDate(Date alertCreationDate) {
		this.alertCreationDate = alertCreationDate;
	}

	/**
	 * @return the alertPublichDate
	 */
	public Date getAlertPublichDate() {
		return alertPublichDate;
	}

	/**
	 * @param alertPublichDate the alertPublichDate to set
	 */
	public void setAlertPublichDate(Date alertPublichDate) {
		this.alertPublichDate = alertPublichDate;
	}

	/**
	 * @return the alertExpiryDate
	 */
	public Date getAlertExpiryDate() {
		return alertExpiryDate;
	}

	/**
	 * @param alertExpiryDate the alertExpiryDate to set
	 */
	public void setAlertExpiryDate(Date alertExpiryDate) {
		this.alertExpiryDate = alertExpiryDate;
	}

	/**
	 * @return the alertModeOnline
	 */
	public Integer getAlertModeOnline() {
		return alertModeOnline;
	}

	/**
	 * @param alertModeOnline the alertModeOnline to set
	 */
	public void setAlertModeOnline(Integer alertModeOnline) {
		this.alertModeOnline = alertModeOnline;
	}

	/**
	 * @return the alertModeEmail
	 */
	public Integer getAlertModeEmail() {
		return alertModeEmail;
	}

	/**
	 * @param alertModeEmail the alertModeEmail to set
	 */
	public void setAlertModeEmail(Integer alertModeEmail) {
		this.alertModeEmail = alertModeEmail;
	}

	/**
	 * @return the alertModeSms
	 */
	public Integer getAlertModeSms() {
		return alertModeSms;
	}

	/**
	 * @param alertModeSms the alertModeSms to set
	 */
	public void setAlertModeSms(Integer alertModeSms) {
		this.alertModeSms = alertModeSms;
	}

	/**
	 * @return the published
	 */
	public Integer getPublished() {
		return published;
	}

	/**
	 * @param published the published to set
	 */
	public void setPublished(Integer published) {
		this.published = published;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UserAlert [idUserAlert=" + idUserAlert + ", idProvider="
				+ idProvider + ", name=" + name + ", description="
				+ description + ", alertTargetCriteria=" + alertTargetCriteria
				+ ", heading=" + heading + ", content=" + content + ", link="
				+ link + ", alertType=" + alertType + ", alertPriority="
				+ alertPriority + ", alertCreationDate=" + alertCreationDate
				+ ", alertPublichDate=" + alertPublichDate
				+ ", alertExpiryDate=" + alertExpiryDate + ", alertModeOnline="
				+ alertModeOnline + ", alertModeEmail=" + alertModeEmail
				+ ", alertModeSms=" + alertModeSms + ", published=" + published
				+ "]";
	}

}