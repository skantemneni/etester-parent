package com.etester.data.domain.content.core;

import java.util.List;

import com.etester.data.domain.content.ChannelWithStats;
import com.etester.data.domain.util.RedumptionCodeWebRequest;

public interface ChannelDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.

	// *** Major Problem *** I am listing out columns from the Level table here.  Big Risk:  If I add/delete/update/modify columns from level table, I need to adjust this statement.
	// Note thatI am replacing this short sweet statement with this big stuff to make sure I grab topiccount and skillcount values from the referenced level.  
// 	public static final String findLevelsForChannelSQL = "SELECT l.*, s.description AS subject_description FROM level l LEFT JOIN subject s ON l.subject = s.subject_name WHERE l.id_system = :idChannel	ORDER BY l.id_level ASC ";
	public static final String findLevelsForChannelSQL = 
				" SELECT l.id_level, l.id_system, l.id_provider, l.subject, l.subject_header, l.name, "
				+ "	l.display_name, l.description, l.text, l.addl_info, l.published,l.derived, "
				+ "	IF(l.id_level_reference, l2.topiccount, l.topiccount) AS topiccount, "
				+ " IF(l.id_level_reference, l2.skillcount, l.skillcount) AS skillcount, "
				+ "	l.id_level_reference, s.description AS subject_description "
				+ " FROM level l LEFT JOIN level l2 ON l.id_level_reference = l2.id_level "
				+ " LEFT JOIN subject s ON l.subject = s.subject_name "
				+ " WHERE l.id_system = :idChannel "
				+ " ORDER BY l.id_level ASC; ";

	
	
	public static final String findPublishedChannelsByChannelSetSQL = 
			"SELECT s.*, cs.*, ct.channeltrack AS channeltrack, ct.channeltrack_description AS channeltrack_description "
			+ "	FROM system s LEFT JOIN channel_stats cs ON s.id_system = cs.id_channel "
			+ "		  LEFT JOIN channel_channeltrack cct ON  s.id_system = cct.id_channel "
			+ "		  LEFT JOIN channeltrack ct ON cct.channeltrack = ct.channeltrack "
			+ " WHERE s.published = true "
			+ " ORDER BY ct.display_seq ";
	
	public static final String getChannelRedumptionCodeSQL = 
			"SELECT crc.channel_redumption_code, crc.id_channel_redumption_code_type, crct.channel_redumption_code_type_description, crct.id_channel, crct.retail_price, "
			+ "	crc.sale_price, crc.purchaser, crc.redeemed, crc.redumption_date, crc.subscriber_username, crc.expired, crct.code_validity_start_date, crct.code_validity_end_date, crct.subscription_duration_days, "
			+ " crc.subscription_start_date, crc.subscription_end_date "
			+ " FROM channel_redumption_code crc LEFT JOIN channel_redumption_code_type crct ON crc.id_channel_redumption_code_type = crct.id_channel_redumption_code_type "
			+ "	WHERE crc.id_channel_redumption_code_type = :idChannelRedumptionCodeType AND crc.channel_redumption_code = :channelRedumptionCode ";
	

	public static final String updateChannelRedumptionCodeSQL = 
			" UPDATE channel_redumption_code SET redeemed = true, subscription_start_date = :subscriptionStartDate, subscription_end_date = :subscriptionEndDate, redumption_date = :redumptionDate, subscriber_username = :subscriberName "
			+ " WHERE channel_redumption_code = :redumptionCode ";

	//	 "SELECT * FROM level WHERE id_system = :idChannel ORDER BY id_level ASC";
	/**
	 * Find a channel by its ID
	 * @param idChannel
	 * @return
	 */
	public Channel findByChannelId(Long idChannel);
	/**
	 * Return all the channels in the system
	 * @return
	 */
	public List<Channel> findAllChannels();
	
	/**
	 * Checks to see if the logged in provider has Content Upload permission to the requested channel
	 * @param idUser
	 * @return
	 */
	public boolean providerHasChannelUploadPermission(Long idChannel, Long idLoggedInProvider);
	

	/**
	 * Find channel with its levels
	 * @param idChannel
	 * @return
	 */
	public Channel getChannelWithLevels(Long idChannel);
	
	/**
	 * Method that returns Published Channels in a particular Channel Set. channelSet=all returns all Published Channels.
	 * @param channelSet
	 * @return
	 */
	public List<ChannelWithStats> findPublishedChannelsByChannelSet(String channelSet);
	
	/**
	 * Activate the given Channel for the Logged in user.  Note that this method can fail in its business objective 
	 * depending on validity of the predefined redumptionCode.
	 * @param idChannel
	 * @param redumptionCode
	 * @return
	 */
	public String activatePublishedChannel(Long idChannel, String redumptionCode);
	
	
	/**
	 * Request that will assign RedumptionCodes t email addresses and email it to the user.
	 * @param redumptionCodeRequestList
	 * @return
	 */
	public String generateRedumptionCodes(
			List<RedumptionCodeWebRequest> redumptionCodeRequestList);
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Infrastructure Admin Functions Below.
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Update Skill and Topic Counts for all channels in the system
	 */
	public void updateAllSkillCounts();
	
	/**
	 * Update Skill and Topic Counts for a given list of channels in the system
	 */
	public void updateSkillCounts(List<Long> idChannels);
	
	/**
	 * Update Level Descriptions for all channels in the system
	 */
	public void updateAllLevelDescriptions();

	/**
	 * Update Level Descriptions for all channels in the system
	 */
	public void updateLevelDescriptions(List<Long> idChannels);

	/**
	 * Update Topic Descriptions for all channels in the system
	 */
	public void updateAllTopicDescriptions();

	/**
	 * Update Topic Descriptions for selected channels in the system
	 */
	public void updateTopicDescriptions(List<Long> idChannels);

}
