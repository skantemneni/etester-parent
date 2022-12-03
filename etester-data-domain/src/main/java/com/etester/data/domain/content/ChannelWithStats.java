package com.etester.data.domain.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name="system")
public class ChannelWithStats {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_system")
	private Long idSystem;
	
    @NotNull
    @Size(min=1, max=100)
	private String name;
    
    @Size(min=0, max=200)
	private String description;

	@Column(name = "text")
	private String text;

	@Column(name = "addl_info")
	private String addlInfo;

	@Column(name = "editable")
	private Integer editable;

	@Column(name = "published")
    private Integer published;

    private String channeltrack ;
    private String channeltrackDescription;

    private Integer totalSubscribers;
    private Integer levelCount;
    private Integer topicCount;
    private Integer skillCount;
    private Integer comprehensiveTestCount;
    private Integer subjectTestCount;
    private Integer levelTestCount;
    private Integer topicTestCount;

}
