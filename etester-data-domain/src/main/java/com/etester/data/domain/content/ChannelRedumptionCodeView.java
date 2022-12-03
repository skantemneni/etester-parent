package com.etester.data.domain.content;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

//@Entity
@Data
@Table(name="channel_redumption_code_view")
public class ChannelRedumptionCodeView {

	@Id
    @Column(name = "channel_redumption_code")
    private String channelRedumptionCode;
	
	@Id
    @Column(name = "id_channel_redumption_code_type")
    private Integer idChannelRedumptionCodeType;
	
	@NotNull
    @Column(name = "id_channel")
	private Long idChannel;

	@Column(name = "retail_price")
    private Integer retailPrice;
	    
	@Column(name = "sale_price")
    private Integer salePrice;
	    
	@Column(name = "purchaser")
	private String purchaser;

	@Column(name = "redeemed")
	private Integer redeemed;

	@Column(name = "redumption_date")
	private Date redumptionDate;
	
	@Column(name = "subscriber_username")
	private String subscriberUsername;

	@Column(name = "expired")
	private Integer expired;

	@Column(name = "code_validity_start_date")
	private Date codeValidityStartDate;
	
	@Column(name = "code_validity_end_date")
	private Date codeValidityEndDate;
	
	@Column(name = "subscription_start_date")
	private Date subscriptionStartDate;
	
	@Column(name = "subscription_end_date")
	private Date subscriptionEndDate;
	
	@Column(name = "subscription_duration_days")
	private Integer subscriptionDurationDays;

}
