package com.etester.data.domain.util.sms;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="usermessage")
public class Usermessage {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usermessage")
    private Long idUsermessage;
	
    @Column(name = "id_user")
	private Long idUser;

    @Column(name = "id_channel")
	private Long idChannel;

	@Column(name = "heading")
	private String heading;

	@Column(name = "content")
	private String content;

	@Column(name = "link")
	private String link;

	@Column(name = "is_channel_message")
	private Integer isChannelMessage;

	@Column(name = "message_priority")
	private Integer messagePriority;

	@Column(name = "message_acknowledged")
	private Integer messageAcknowledged;

	@Column(name = "message_expired")
	private Integer messageExpired;

	@Column(name = "message_creation_date")
	private Date messageCreationDate;

	@Column(name = "message_acknowledged_date")
	private Date messageAcknowledgedDate;

	@Column(name = "message_expiration_date")
	private Date messageExpirationDate;

	public Usermessage() {
	}

	/**
	 * @return the idUsermessage
	 */
	public Long getIdUsermessage() {
		return idUsermessage;
	}

	/**
	 * @param idUsermessage the idUsermessage to set
	 */
	public void setIdUsermessage(Long idUsermessage) {
		this.idUsermessage = idUsermessage;
	}

	/**
	 * @return the idUser
	 */
	public Long getIdUser() {
		return idUser;
	}

	/**
	 * @param idUser the idUser to set
	 */
	public void setIdUser(Long idUser) {
		this.idUser = idUser;
	}

	/**
	 * @return the idChannel
	 */
	public Long getIdChannel() {
		return idChannel;
	}

	/**
	 * @param idChannel the idChannel to set
	 */
	public void setIdChannel(Long idChannel) {
		this.idChannel = idChannel;
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
	 * @return the isChannelMessage
	 */
	public Integer getIsChannelMessage() {
		return isChannelMessage;
	}

	/**
	 * @param isChannelMessage the isChannelMessage to set
	 */
	public void setIsChannelMessage(Integer isChannelMessage) {
		this.isChannelMessage = isChannelMessage;
	}

	/**
	 * @return the messagePriority
	 */
	public Integer getMessagePriority() {
		return messagePriority;
	}

	/**
	 * @param messagePriority the messagePriority to set
	 */
	public void setMessagePriority(Integer messagePriority) {
		this.messagePriority = messagePriority;
	}

	/**
	 * @return the messageAcknowledged
	 */
	public Integer getMessageAcknowledged() {
		return messageAcknowledged;
	}

	/**
	 * @param messageAcknowledged the messageAcknowledged to set
	 */
	public void setMessageAcknowledged(Integer messageAcknowledged) {
		this.messageAcknowledged = messageAcknowledged;
	}

	/**
	 * @return the messageExpired
	 */
	public Integer getMessageExpired() {
		return messageExpired;
	}

	/**
	 * @param messageExpired the messageExpired to set
	 */
	public void setMessageExpired(Integer messageExpired) {
		this.messageExpired = messageExpired;
	}

	/**
	 * @return the messageCreationDate
	 */
	public Date getMessageCreationDate() {
		return messageCreationDate;
	}

	/**
	 * @param messageCreationDate the messageCreationDate to set
	 */
	public void setMessageCreationDate(Date messageCreationDate) {
		this.messageCreationDate = messageCreationDate;
	}

	/**
	 * @return the messageAcknowledgedDate
	 */
	public Date getMessageAcknowledgedDate() {
		return messageAcknowledgedDate;
	}

	/**
	 * @param messageAcknowledgedDate the messageAcknowledgedDate to set
	 */
	public void setMessageAcknowledgedDate(Date messageAcknowledgedDate) {
		this.messageAcknowledgedDate = messageAcknowledgedDate;
	}

	/**
	 * @return the messageExpirationDate
	 */
	public Date getMessageExpirationDate() {
		return messageExpirationDate;
	}

	/**
	 * @param messageExpirationDate the messageExpirationDate to set
	 */
	public void setMessageExpirationDate(Date messageExpirationDate) {
		this.messageExpirationDate = messageExpirationDate;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Usermessage [idUsermessage=" + idUsermessage + ", idUser="
				+ idUser + ", idChannel=" + idChannel + ", heading=" + heading
				+ ", content=" + content + ", link=" + link
				+ ", isChannelMessage=" + isChannelMessage
				+ ", messagePriority=" + messagePriority
				+ ", messageAcknowledged=" + messageAcknowledged
				+ ", messageExpired=" + messageExpired
				+ ", messageCreationDate=" + messageCreationDate
				+ ", messageAcknowledgedDate=" + messageAcknowledgedDate
				+ ", messageExpirationDate=" + messageExpirationDate + "]";
	}

}
