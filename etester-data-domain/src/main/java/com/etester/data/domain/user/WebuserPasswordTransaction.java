package com.etester.data.domain.user;

import java.util.Calendar;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="webuser_password_transaction")
public class WebuserPasswordTransaction {

	public static final String NEW_PASSWORD_TRANSACTION_TYPE = "N"; 
	public static final String RESET_PASSWORD_TRANSACTION_TYPE = "R"; 
	public static final String UPDATE_PASSWORD_TRANSACTION_TYPE = "U"; 

	@Id
    @Column(name = "username", length = 50, nullable = false)
    private String username;
    @Column(name = "transaction_type", length = 1, nullable = false)
    private String transactionType;
    @Column(name = "transaction_key", length = 200, nullable = false)
    private String transactionKey;
    @Column(name = "new_password", length = 200, nullable = false)
    private String newPassword;
	@Column(name = "transaction_expiry_date")
	private Date transactionExpiryDate;

	public WebuserPasswordTransaction() {
    }

    public WebuserPasswordTransaction(String username, String transactionType, String transactionKey, String newPassword) {
        this(username, transactionType, transactionKey, newPassword, Calendar.getInstance().getTime());
    }

    public WebuserPasswordTransaction(String username, String transactionType, String transactionKey, String newPassword, Date transactionExpiryDate) {
        this.username = username;
        this.transactionType = transactionType;
        this.transactionKey = transactionKey;
        this.newPassword = newPassword;
        this.transactionExpiryDate = transactionExpiryDate;
    }

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the transactionType
	 */
	public String getTransactionType() {
		return transactionType;
	}

	/**
	 * @param transactionType the transactionType to set
	 */
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	/**
	 * @return the transactionKey
	 */
	public String getTransactionKey() {
		return transactionKey;
	}

	/**
	 * @param transactionKey the transactionKey to set
	 */
	public void setTransactionKey(String transactionKey) {
		this.transactionKey = transactionKey;
	}

	/**
	 * @return the newPassword
	 */
	public String getNewPassword() {
		return newPassword;
	}

	/**
	 * @param newPassword the newPassword to set
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	/**
	 * @return the transactionExpiryDate
	 */
	public Date getTransactionExpiryDate() {
		return transactionExpiryDate;
	}

	/**
	 * @param transactionExpiryDate the transactionExpiryDate to set
	 */
	public void setTransactionExpiryDate(Date transactionExpiryDate) {
		this.transactionExpiryDate = transactionExpiryDate;
	}


}
