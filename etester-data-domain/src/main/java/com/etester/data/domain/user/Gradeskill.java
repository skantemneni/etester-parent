package com.etester.data.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="gradeskill")
public class Gradeskill {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gradeskill")
	private Long idGradeskill;
	
	@Column(name = "grade_name")
	private String gradeName;
	
	@Column(name = "id_skill")
	private Long idSkill;
	
	@Column(name = "alt_name")
    @Size(min=0, max=100)
	private String altName;

	@Column(name = "alt_description")
    @Size(min=0, max=200)
	private String altDescription;


	public Gradeskill() {
	}

	public Gradeskill(String gradeName, Long idSkill, String altName, String altDescription) {
		this.gradeName = gradeName;
		this.idSkill = idSkill;
		this.altName = altName;
		this.altDescription = altDescription;
	}


	/**
	 * @return the idGradeskill
	 */
	public Long getIdGradeskill() {
		return idGradeskill;
	}


	/**
	 * @param idGradeskill the idGradeskill to set
	 */
	public void setIdGradeskill(Long idGradeskill) {
		this.idGradeskill = idGradeskill;
	}


	/**
	 * @return the gradeName
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
	 * @return the idSkill
	 */
	public Long getIdSkill() {
		return idSkill;
	}


	/**
	 * @param idSkill the idSkill to set
	 */
	public void setIdSkill(Long idSkill) {
		this.idSkill = idSkill;
	}


	/**
	 * @return the altName
	 */
	public String getAltName() {
		return altName;
	}


	/**
	 * @param altName the altName to set
	 */
	public void setAltName(String altName) {
		this.altName = altName;
	}


	/**
	 * @return the altDescription
	 */
	public String getAltDescription() {
		return altDescription;
	}


	/**
	 * @param altDescription the altDescription to set
	 */
	public void setAltDescription(String altDescription) {
		this.altDescription = altDescription;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\tGradeskill [idGradeskill=" + idGradeskill + " \n\t, gradeName=" + gradeName + " \n\t, idSkill=" + idSkill 
				+ " \n\t, alt_name=" + altName + " \n\t, alt_description=" + altDescription + "]\n";
	}

}
