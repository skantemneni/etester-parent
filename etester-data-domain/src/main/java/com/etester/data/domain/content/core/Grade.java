package com.etester.data.domain.content.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="grade")
public class Grade {

	@Id
	@Column(name = "grade_name")
    @Size(min=1, max=45)
	private String gradeName;

    @Size(min=0, max=200)
	private String description;


	public Grade() {
	}

	/**
	 * @return the grade_name
	 */
	public String getGradeName() {
		return gradeName;
	}

	/**
	 * @param gradeName the gradeName to set
	 */
	public void setGradeName(String gradeName) {
		this.gradeName = gradeName;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\tGrade [grade_name=" + gradeName + " \n\t, description=" + description + "]\n";
	}

}
