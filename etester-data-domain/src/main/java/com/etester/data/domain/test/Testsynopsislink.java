package com.etester.data.domain.test;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="testsynopsislink")
public class Testsynopsislink {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_testsynopsislink")
    private Long idTestsynopsislink;
	
	@NotNull
    @Column(name = "id_testsegment")
	private Long idTestsegment;

	@NotNull
    @Column(name = "id_synopsis_link_ref")
	private Long idSynopsisLinkRef;

	@NotNull
	@Column(name = "name")
    @Size(min=1, max=100)
	private String name;
    
	@Column(name = "description")
    @Size(min=0, max=200)
	private String description;

	@NotNull
	@Column(name = "link")
    @Size(min=0, max=400)
	private String link;

    @Column(name = "link_type")
	private Integer linkType;

    @Column(name = "seq")
	private Integer seq;

	/**
	 * @return the idTestsynopsislink
	 */
	public Long getIdTestsynopsislink() {
		return idTestsynopsislink;
	}

	/**
	 * @param idTestsynopsislink the idTestsynopsislink to set
	 */
	public void setIdTestsynopsislink(Long idTestsynopsislink) {
		this.idTestsynopsislink = idTestsynopsislink;
	}

	/**
	 * @return the idTestsegment
	 */
	public Long getIdTestsegment() {
		return idTestsegment;
	}

	/**
	 * @param idTestsegment the idTestsegment to set
	 */
	public void setIdTestsegment(Long idTestsegment) {
		this.idTestsegment = idTestsegment;
	}

	/**
	 * @return the idSynopsisLinkRef
	 */
	public Long getIdSynopsisLinkRef() {
		return idSynopsisLinkRef;
	}

	/**
	 * @param idSynopsisLinkRef the idSynopsisLinkRef to set
	 */
	public void setIdSynopsisLinkRef(Long idSynopsisLinkRef) {
		this.idSynopsisLinkRef = idSynopsisLinkRef;
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
	 * @return the linkType
	 */
	public Integer getLinkType() {
		return linkType;
	}

	/**
	 * @param linkType the linkType to set
	 */
	public void setLinkType(Integer linkType) {
		this.linkType = linkType;
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

	@Override
	public String toString() {
		return "Testsynopsislink [idTestsynopsislink=" + idTestsynopsislink
				+ ", idTestsegment=" + idTestsegment + ", idSynopsisLinkRef="
				+ idSynopsisLinkRef + ", name=" + name + ", description="
				+ description + ", link=" + link + ", linkType=" + linkType
				+ ", seq=" + seq + "]";
	}
}
