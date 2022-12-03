package com.etester.data.domain.util;

import lombok.Data;

/**
 * Class used to convey statuses of update database operations.  Usually used in database update 
 * operations that occur via the RfService interface or the Command line tool.
 * @author sesi
 */
@Data
public class UpdateStatusBean {
	
	private static final int SUCCESS_STATUS = 0;
	private static final int FAIL_STATUS = -1;

	// Artifact that is being updated
	private Long idArtifact =  null;
	
	// Status code to indicate weather the update(/upsert/insert/delete) operation was successful
	private int statusCode = FAIL_STATUS;

	// Reason why an operation might have failed. May be Null or "success" otherwise.
	private String statusText = null;
	
    public UpdateStatusBean() {
	}

    /**
     * Constructor that takes all parameters.  
     * @param idArtifact
     * @param statusCode
     * @param statusText
     */
    public UpdateStatusBean(Long idArtifact, int statusCode, String statusText) {
    	this.idArtifact = idArtifact;
    	this.statusCode = statusCode;
    	this.statusText = statusText;
	}

	/**
	 * @return the idArtifact
	 */
	public Long getIdArtifact() {
		return idArtifact;
	}

	/**
	 * @param idArtifact the idArtifact to set
	 */
	public void setIdArtifact(Long idArtifact) {
		this.idArtifact = idArtifact;
	}

	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode the statusCode to set
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * @return the statusText
	 */
	public String getStatusText() {
		return statusText;
	}

	/**
	 * @param statusText the statusText to set
	 */
	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "idArtifact=" + idArtifact + ", statusCode="
				+ statusCode + ", statusText=" + statusText + "\n";
	}

	/**
	 * @param failed the failed to set
	 */
	public void setPassed(boolean passed) {
		this.setStatusCode(SUCCESS_STATUS);
	}

	/**
	 * @return the failed
	 */
	public boolean hasPassed() {
		return this.getStatusCode() == SUCCESS_STATUS;
	}

	/**
	 * @param failed the failed to set
	 */
	public void setFailed(boolean failed) {
		this.setStatusCode(FAIL_STATUS);
	}

	/**
	 * @return the failed
	 */
	public boolean hasFailed() {
		return !hasPassed();
	}

}
