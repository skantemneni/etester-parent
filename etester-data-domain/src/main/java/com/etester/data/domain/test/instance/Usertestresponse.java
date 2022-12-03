package com.etester.data.domain.test.instance;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="usertestresponse")
public class Usertestresponse {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usertestresponse")
    private Long idUsertestresponse;
	
	@NotNull
    @Column(name = "id_usertest")
    private Long idUsertest;

	@NotNull
	@Column(name = "response")
	private String response;

	@Column(name = "date_saved")
	private Date dateSaved;

	private boolean completed = false;

	/**
	 * @return the idUsertestresponse
	 */
	public Long getIdUsertestresponse() {
		return idUsertestresponse;
	}

	/**
	 * @param idUsertestresponse the idUsertestresponse to set
	 */
	public void setIdUsertestresponse(Long idUsertestresponse) {
		this.idUsertestresponse = idUsertestresponse;
	}

	/**
	 * @return the idUsertest
	 */
	public Long getIdUsertest() {
		return idUsertest;
	}

	/**
	 * @param idUsertest the idUsertest to set
	 */
	public void setIdUsertest(Long idUsertest) {
		this.idUsertest = idUsertest;
	}

	/**
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(String response) {
		this.response = response;
	}

	/**
	 * @return the dateSaved
	 */
	public Date getDateSaved() {
		return dateSaved;
	}

	/**
	 * @param dateSaved the dateSaved to set
	 */
	public void setDateSaved(Date dateSaved) {
		this.dateSaved = dateSaved;
	}

	/**
	 * @return the completed
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * @param completed the completed to set
	 */
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Usertestresponse [idUsertestresponse=" + idUsertestresponse
				+ " \\n, idUsertest=" + idUsertest 
				+ " \\n, response=" + response 
				+ " \\n, dateSaved=" + dateSaved + "]";
	}

}
