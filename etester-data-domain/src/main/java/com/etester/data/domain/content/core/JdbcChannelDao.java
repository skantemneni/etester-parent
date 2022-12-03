package com.etester.data.domain.content.core;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.etester.data.dao.JdbcDataDaoParent;
import com.etester.data.domain.content.ChannelRedumptionCodeView;
import com.etester.data.domain.content.ChannelSubscription;
import com.etester.data.domain.content.ChannelWithStats;
import com.etester.data.domain.test.JdbcDaoStaticHelper;
import com.etester.data.domain.util.RedumptionCodeWebRequest;
import com.etester.data.domain.util.cache.EtesterCacheController;
import com.etester.data.domain.util.email.EmailNotifierService;

@Repository
public class JdbcChannelDao extends JdbcDataDaoParent implements ChannelDao {

//	private DataSource dataSource;
//
	public JdbcChannelDao(DataSource dataSource) {
		super(dataSource);
	}

//	@PostConstruct
//	private void initialize() {
//		setDataSource(dataSource);
//	}
//
	private static final int CHANNEL_REDUMPTION_CODE_SIZE = 16;
	private static final long DAY_TIME_IN_MILLIS = 24 * 60 * 60 * 1000;
	
	private static final Long CONSOLIDATED_CHANNEL_CODE_TYPE_FOR_ENGINEERING = 130l;
	private static final Long CONSOLIDATED_CHANNEL_CODE_TYPE_FOR_MEDICAL = 133l;
	private static final Integer CHANNEL_REDUMPTION_CODE_TYPE_FOR_ENGINEERING = 201601;
	private static final Integer CHANNEL_REDUMPTION_CODE_TYPE_FOR_MEDICAL = 201602;
	
	private static Map<Long, List<Long>> CONSOLIDATED_CHANNEL_SET = new HashMap<Long, List<Long>>();
	static {
		CONSOLIDATED_CHANNEL_SET.put (CONSOLIDATED_CHANNEL_CODE_TYPE_FOR_ENGINEERING, Arrays.asList(131l, 132l, 133l));
		CONSOLIDATED_CHANNEL_SET.put (CONSOLIDATED_CHANNEL_CODE_TYPE_FOR_MEDICAL, Arrays.asList(133l));
	}
	
	private EmailNotifierService emailNotifierService;

    public void setEmailNotifierService(EmailNotifierService emailNotifierService) {
        this.emailNotifierService = emailNotifierService;
    }


	
	@Override
	public Channel findByChannelId(Long idChannel) {
        String sql = "SELECT * FROM system WHERE id_system = :idChannel";
        BeanPropertyRowMapper<Channel> channelRowMapper = BeanPropertyRowMapper.newInstance(Channel.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idChannel", idChannel);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        Channel channel = null;
        try {
        	channel = getNamedParameterJdbcTemplate().queryForObject(sql, args, channelRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return channel;
	}

	@Override
	public List<Channel> findAllChannels() {
        String sql = "SELECT * FROM system ORDER BY id_system ASC";
        BeanPropertyRowMapper<Channel> channelRowMapper = BeanPropertyRowMapper.newInstance(Channel.class);
        List<Channel> channels = getNamedParameterJdbcTemplate().query(sql, channelRowMapper);
        return channels;
	}

	@Override
	public boolean providerHasChannelUploadPermission(Long idChannel,
			Long idLoggedInProvider) {
		String sql = "SELECT count(*) FROM channel_admin WHERE id_channel = :idChannel AND id_provider = :idProvider AND upload_content = 1";
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idChannel", idChannel);
		args.put("idProvider", idLoggedInProvider);
		int existsCount = getNamedParameterJdbcTemplate().queryForObject(sql, args, Integer.class);
		if (existsCount > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Channel getChannelWithLevels(Long idChannel) {
		if (EtesterCacheController.isCaching()) {
			// check to see if the information is available in the cache
	        // see if it already exists in cache
			Channel channel = EtesterCacheController.getChannelWithLevels(idChannel);
			if (channel != null) {
				return channel;
			} else {
				boolean withTopics = false;
				channel = findByChannelId(idChannel);
				if (channel == null) {
					return null;
				}
				channel.setLevels(findLevelsForChannel (idChannel, withTopics));
				EtesterCacheController.putChannelWithLevels(channel);
				return channel;
			}
		} else {
			boolean withTopics = false;
			Channel channel = findByChannelId(idChannel);
			if (channel == null) {
				return null;
			}
			channel.setLevels(findLevelsForChannel (idChannel, withTopics));
			return channel;
		}
	}

	private List<Level> findLevelsForChannel(Long idChannel, boolean withTopics) {
        BeanPropertyRowMapper<Level> levelRowMapper = BeanPropertyRowMapper.newInstance(Level.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idChannel", idChannel);
        List<Level> levels = getNamedParameterJdbcTemplate().query(findLevelsForChannelSQL, args, levelRowMapper);
        return levels;
	}

	@Override
	public List<ChannelWithStats> findPublishedChannelsByChannelSet(String channelSet) {
	      BeanPropertyRowMapper<ChannelWithStats> channelRowMapper = BeanPropertyRowMapper.newInstance(ChannelWithStats.class);
	      Map<String, Object> args = new HashMap<String, Object>();
//	      args.put("examtrack", examtrack);
	      List<ChannelWithStats> channels = getNamedParameterJdbcTemplate().query(findPublishedChannelsByChannelSetSQL, args, channelRowMapper);
	      return channels;
	}

	@Override
	public String activatePublishedChannel(Long idChannel, String channelRedumptionCode) {
		// null check (& validate size of) parameters
		if (idChannel == null) {
			return "Invalid Channel: NULL";
		}
		if (channelRedumptionCode == null || channelRedumptionCode.trim().length() != CHANNEL_REDUMPTION_CODE_SIZE) {
			return "Invalid Activation Code: '" + channelRedumptionCode + "'";
		}
		// first get the logged in user id and name
		Long loggedinStudentId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinStudentId == null || loggedinStudentId.equals(JdbcDaoStaticHelper.ANONYMOUS_USER_ID)) {
			return "Please Login before attempting to Activate a Channel";
		}
		String loggedinStudentName = JdbcDaoStaticHelper.getCurrentUserName();

        // validate the transaction/activation code for channel
		// get the code record
		Integer idChannelRedumptionCodeType = getChannelRedumptionCodeType(idChannel);
		ChannelRedumptionCodeView channelRedumptionCodeView = getChannelRedumptionCode(channelRedumptionCode, idChannelRedumptionCodeType);
        // complain if null
        if (channelRedumptionCodeView == null) {
        	return "Invalid Activation Code: '" + channelRedumptionCode + "'";
        }
        // see if the Redumption code is reserved....if it is, make sure it is assigned to the loggedinStudentName
        String existingSubscriberUsername = channelRedumptionCodeView.getSubscriberUsername();
        if (existingSubscriberUsername != null && !existingSubscriberUsername.trim().equalsIgnoreCase(loggedinStudentName.trim())) {
        	return "Activation Code: '" + channelRedumptionCode + "' is reserved for a different user/email address.  Please login as the authorized user.";
        }
        // validate Redumption code is good for channel
        if (channelRedumptionCodeView.getIdChannel() == null || !channelRedumptionCodeView.getIdChannel().equals(idChannel)) {
           	return "Invalid Activation Code for Channel: '" + channelRedumptionCode + "'";
        }
        // make sure the code validity period is already started
        if (channelRedumptionCodeView.getCodeValidityStartDate().after(new Date())) {
        	return "Activation Code is Only Valid after '" + channelRedumptionCodeView.getCodeValidityStartDate() + "'";
        }
        // make sure the code validity period is not expired yet
        if (channelRedumptionCodeView.getCodeValidityEndDate().before(new Date())) {
        	return "Activation Code is Expired";
        }
        // if we come this far, the code is valid and active
        // see what channels come subscribed for this code
//        String[] channelsIds = channelRedumptionCodeView.getChannelList().trim().split(",");
		
		// now insert any channel subscriptions
//        Date subscriptionStartDate = channelRedumptionCodeView.getSubscriptionStartDate() == null ? new Date() : channelRedumptionCodeView.getSubscriptionStartDate();
//        Date subscriptionEndDate = null;
//        if (channelRedumptionCodeView.getSubscriptionEndDate() != null) {
//        	subscriptionEndDate = channelRedumptionCodeView.getSubscriptionEndDate();
//        } else if (channelRedumptionCodeView.getSubscriptionDurationDays() != null) {
//        	subscriptionEndDate =  new Date(subscriptionStartDate.getTime() +  DAY_TIME_IN_MILLIS * channelRedumptionCodeView.getSubscriptionDurationDays());
//        } else {
//        	return "Activation Code cannot calculate the Subscription End Date";
//        }
        Date currentDate = new Date();
        Date subscriptionStartDate = currentDate;
        Date subscriptionEndDate = null;
        if (channelRedumptionCodeView.getSubscriptionDurationDays() != null) {
        	subscriptionEndDate =  new Date(subscriptionStartDate.getTime() +  DAY_TIME_IN_MILLIS * channelRedumptionCodeView.getSubscriptionDurationDays());
        } else {
        	return "Activation Code cannot calculate the Subscription End Date";
        }
        // if subscription end date is after the CodeValidityEndDate, set the subscription end date to CodeValidityEndDate
        if (subscriptionEndDate.after(channelRedumptionCodeView.getCodeValidityEndDate())) {
        	subscriptionEndDate = channelRedumptionCodeView.getCodeValidityEndDate();
        }
        
        List<Long> channelIds = CONSOLIDATED_CHANNEL_SET.get(idChannel);
        if (channelIds == null) {
        	channelIds = new ArrayList<Long>();
        	channelIds.add(idChannel);
        }
        
        List<ChannelSubscription> subscriptions = new ArrayList<ChannelSubscription>();
        for (int i = 0; i < channelIds.size(); i++) {
        	ChannelSubscription channelSubscription = new ChannelSubscription(channelIds.get(i), loggedinStudentId, subscriptionStartDate, subscriptionEndDate); 
        	subscriptions.add(channelSubscription);
        }
        // now add to the database
    	JdbcDaoStaticHelper.updateChannelSubscriptionsForUser (loggedinStudentId, subscriptions, getNamedParameterJdbcTemplate());
    	
    	// mark the redumption code as "redeemed"
		String updateSql = updateChannelRedumptionCodeSQL;
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("redumptionCode", channelRedumptionCode.trim());
        args.put("subscriptionStartDate", subscriptionStartDate);
        args.put("subscriptionEndDate", subscriptionEndDate);
        args.put("redumptionDate", currentDate);
        args.put("subscriberName", JdbcDaoStaticHelper.getCurrentUserName());
        getNamedParameterJdbcTemplate().update(updateSql, args);

        return null;
	}
	
	// TODO: SESI - REVAMP THIS FUNCTIONALITY
	private Integer getChannelRedumptionCodeType(Long idChannel) {
		if (CONSOLIDATED_CHANNEL_CODE_TYPE_FOR_ENGINEERING.equals(idChannel)) {
			return CHANNEL_REDUMPTION_CODE_TYPE_FOR_ENGINEERING;
		} else if (CONSOLIDATED_CHANNEL_CODE_TYPE_FOR_MEDICAL.equals(idChannel)) {
			return CHANNEL_REDUMPTION_CODE_TYPE_FOR_MEDICAL;
		} else {
			return idChannel.intValue();
		}
	}



	private ChannelRedumptionCodeView getChannelRedumptionCode(String channelRedumptionCode, Integer idChannelRedumptionCodeType) {
		String sql = getChannelRedumptionCodeSQL;
		
        BeanPropertyRowMapper<ChannelRedumptionCodeView> channelRedumptionCodeRowMapper = BeanPropertyRowMapper.newInstance(ChannelRedumptionCodeView.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("channelRedumptionCode", channelRedumptionCode.trim());
		args.put("idChannelRedumptionCodeType", idChannelRedumptionCodeType);
        ChannelRedumptionCodeView channelRedumptionCodeView = null;
        try {
        	channelRedumptionCodeView = getNamedParameterJdbcTemplate().queryForObject(sql, args, channelRedumptionCodeRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
		return channelRedumptionCodeView;
	}

	/*********************************************************************************************************************************************
	 * Logic to auto-generate and assign channel_redumption_codes.  Note that call may come from Web or Webservices
	 *********************************************************************************************************************************************/
	private static final String newCRCCodeSql = " SELECT crc.channel_redumption_code FROM channel_redumption_code crc LEFT JOIN channel_redumption_code_type crct ON crc.id_channel_redumption_code_type = crct.id_channel_redumption_code_type "
			+ "	WHERE crc.crc_type_sequence = :crcTypeSequence AND crct.id_channel_redumption_code_type = :idCrcTypeCode ";
	private static final String reserveCRCCodeSql = " UPDATE channel_redumption_code SET subscriber_username = :emailAddress WHERE channel_redumption_code = :newCRCCode AND id_channel_redumption_code_type = :idCrcTypeCode  ";
	private static final String existingCRCCodeSql = " SELECT crc.channel_redumption_code FROM channel_redumption_code crc LEFT JOIN channel_redumption_code_type crct ON crc.id_channel_redumption_code_type = crct.id_channel_redumption_code_type "
			+ "	WHERE crc.subscriber_username = :emailAddress AND crct.id_channel_redumption_code_type = :idCrcTypeCode ";
	@Override
	public String generateRedumptionCodes(List<RedumptionCodeWebRequest> redumptionCodeRequestList) {
		if (redumptionCodeRequestList != null && redumptionCodeRequestList.size() > 0) {
			for (RedumptionCodeWebRequest redumptionCodeWebRequest : redumptionCodeRequestList) {
				// see if the CRC already exists for this user id 
				String existingCRCCode = getExistingCRCCode (redumptionCodeWebRequest.getIdCrcTypeCode(), redumptionCodeWebRequest.getEmailAddress());
				if (existingCRCCode != null && existingCRCCode.trim().length() > 0) {
					redumptionCodeWebRequest.setRedumptionCode(existingCRCCode);
				} else {
					String newCRCCode = reserveAndGetNewCRCCode(redumptionCodeWebRequest.getIdCrcTypeCode(), redumptionCodeWebRequest.getEmailAddress());
					// set the code on the RedumptionCodeWebRequest
					redumptionCodeWebRequest.setRedumptionCode(newCRCCode);
				}
			}
	        // compose and send an email...
			new Thread(new EmailSenderRunnable().emailNotifierService(redumptionCodeRequestList)).start();
		}
		// TODO Auto-generated method stub
		return null;
	}

	private String reserveAndGetNewCRCCode(Integer idCrcTypeCode, String emailAddress) {
		Integer assignSequence = getNewCRCAssignSequence(idCrcTypeCode);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("crcTypeSequence", assignSequence);
		args.put("idCrcTypeCode", idCrcTypeCode);
		String newCRCCode = getNamedParameterJdbcTemplate().queryForObject(newCRCCodeSql, args, String.class);
		if (newCRCCode == null || newCRCCode.trim().length() == 0) {
			//complain and throw an exception
		} else {
			// reserve the crc code...
			Map<String, Object> updateArgs = new HashMap<String, Object>();
			updateArgs.put("emailAddress", emailAddress);
			updateArgs.put("newCRCCode", newCRCCode);
			updateArgs.put("idCrcTypeCode", idCrcTypeCode);
			getNamedParameterJdbcTemplate().update(reserveCRCCodeSql, updateArgs);
		}
		return newCRCCode;
	}



	private String getExistingCRCCode (Integer idCrcTypeCode, String emailAddress) {
		String existingCRCCode = null;
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idCrcTypeCode", idCrcTypeCode);
		args.put("emailAddress", emailAddress);
		try {
			existingCRCCode = getNamedParameterJdbcTemplate().queryForObject(existingCRCCodeSql, args, String.class);
		} catch (IncorrectResultSizeDataAccessException e) {}
		return existingCRCCode;
	}
	
	private Integer getNewCRCAssignSequence (Integer idCrcTypeCode) {
		Integer sequence = null;
		String sql = "SELECT get_new_crc_assign_sequence(:idCrcTypeCode)";
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idCrcTypeCode", idCrcTypeCode);
		try {
			sequence = getNamedParameterJdbcTemplate().queryForObject(sql, args, Integer.class);
		} catch (IncorrectResultSizeDataAccessException e) {}
		return sequence;
	}
	
	
	
	
	
	class EmailSenderRunnable implements Runnable {
		List<RedumptionCodeWebRequest> redumptionCodeWebRequestList;
		EmailSenderRunnable emailNotifierService (List<RedumptionCodeWebRequest> redumptionCodeWebRequestList) {
			this.redumptionCodeWebRequestList = redumptionCodeWebRequestList;
			return this;
		}
		
		public void run() {
			if (redumptionCodeWebRequestList != null && redumptionCodeWebRequestList.size() > 0) {
				for (RedumptionCodeWebRequest redumptionCodeWebRequest : redumptionCodeWebRequestList) {
					if (redumptionCodeWebRequest.getRedumptionCode() != null) {
						emailNotifierService.notifyChannelRedumptionCode(
								redumptionCodeWebRequest.getEmailAddress(), redumptionCodeWebRequest.getRedumptionCode(), redumptionCodeWebRequest.getFirstName(), redumptionCodeWebRequest.getLastName(), redumptionCodeWebRequest.getPurchaserMessage());
					}
				}
			}
		}
	}




	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Infrastructure Admin Functions Below.
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Update Skill and Topic Counts for all channels in the system
	 */
	@Override
	public void updateAllSkillCounts() {
		List<Long> idChannels = getAllChannelIdsInSystem();
		updateSkillCounts(idChannels);
	}


	/**
	 * Update Skill and Topic Counts for a given list of channels in the system
	 */
	@Override
	public void updateSkillCounts(List<Long> idChannels) {
		if (idChannels != null && idChannels.size() > 0) {
			for (Long idChannel : idChannels) {
				updateSkillCountsForAChannel(idChannel);
			}
		}
	}

	/**
	 * Update Skill and Topic Counts For A given Channel
	 * @param idChannel
	 */
	private void updateSkillCountsForAChannel(Long idChannel) {
		// Null check first
		if (idChannel == null || idChannel.equals(0l)) {
			return;
		}
		getNamedParameterJdbcTemplate().update("call rulefree.update_topic_skill_counts_for_channel(:idChannel)", new MapSqlParameterSource().addValue("idChannel", idChannel, Types.NUMERIC));
	}

	/**
	 * Update Level Descriptions for all channels in the system
	 */
	@Override
	public void updateAllLevelDescriptions() {
		List<Long> idChannels = getAllChannelIdsInSystem();
		updateLevelDescriptions(idChannels);
	}

	/**
	 * Update Level Descriptions for all channels in the system
	 */
	@Override
	public void updateLevelDescriptions(List<Long> idChannels) {
		if (idChannels != null && idChannels.size() > 0) {
			for (Long idChannel : idChannels) {
				updateLevelDescriptionsForAChannel(idChannel);
			}
		}
	}

	/**
	 * Update Level Descriptions For A given Channel
	 * @param idChannel
	 */
	private void updateLevelDescriptionsForAChannel(Long idChannel) {
		// Null check first
		if (idChannel == null || idChannel.equals(0l)) {
			return;
		}
		// get a list of all LevelId's in a channel
		List<Long> idLevels = getAllLevelIdsInAChannel(idChannel);
		if (idLevels != null && idLevels.size() > 0) {
			for (Long idLevel : idLevels) {
				// perform the actual function
				updateLevelDescriptionsForALevel(idLevel);
			}			
		}		
	}



	/**
	 *  Update Level Descriptions For A given level
	 * @param idLevel
	 */
	private void updateLevelDescriptionsForALevel(Long idLevel) {
		// May want to make these strings static
        String sqlTopicQuery = "SELECT * FROM topic WHERE id_level = :idLevel ORDER BY id_topic ASC";
    	String sqlLevelUpdate = "UPDATE level SET description = :levelDescription WHERE id_level = :idLevel";
		// null check...
		if (idLevel == null || idLevel.equals(0l)) {
			return;
		}
		// get all topics and concatenate their names with a comma seperating the names (skip any Topic name that has a "Test" in the name
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        Map<String, Object> argsQuery = new HashMap<String, Object>();
        argsQuery.put("idLevel", idLevel);
        List<Topic> topics = getNamedParameterJdbcTemplate().query(sqlTopicQuery, argsQuery, topicRowMapper);
        if (topics != null && topics.size() > 0) {
        	StringBuffer levelDescriptionBuffer = new StringBuffer();
//        	levelDescriptionBuffer.append("Learn about... ");
        	boolean isFirstTopicName = true;
        	for (Topic topic : topics) {
        		if (topic.getName() != null && topic.getName().trim().length() > 0 && !topic.getName().toUpperCase().contains("TEST")) {
        			if (isFirstTopicName) {
            			levelDescriptionBuffer.append(topic.getName().trim());
            			isFirstTopicName = false;
        			} else {
            			levelDescriptionBuffer.append(", ").append(topic.getName().trim());
        			}
        		}
        	}
        	// now update the level with the newly created level description
        	
        	// note that the column size for level description is 500 chars...so truncate to 500 
        	String levelDescription = levelDescriptionBuffer.toString();
        	if (levelDescription.length() > 500) {
        		levelDescription = levelDescription.substring(0, 497) + "...";
        	}
        	
        	if (levelDescription != null && levelDescription.trim().length() > 0) {
	        	Map<String, Object> argsUpdate = new HashMap<String, Object>();
	        	argsUpdate.put("levelDescription", levelDescription);
	        	argsUpdate.put("idLevel", idLevel);
	        	getNamedParameterJdbcTemplate().update(sqlLevelUpdate, argsUpdate);
        	}
        }
	}


	/**
	 * Update Level Descriptions for all channels in the system
	 */
	@Override
	public void updateAllTopicDescriptions() {
		List<Long> idChannels = getAllChannelIdsInSystem();
		updateTopicDescriptions(idChannels);
	}

	/**
	 * Update Level Descriptions for all channels in the system
	 */
	@Override
	public void updateTopicDescriptions(List<Long> idChannels) {
		if (idChannels != null && idChannels.size() > 0) {
			for (Long idChannel : idChannels) {
				updateTopicDescriptionsForAChannel(idChannel);
			}
		}
	}

	/**
	 * Update Level Descriptions For A given Channel
	 * @param idChannel
	 */
	private void updateTopicDescriptionsForAChannel(Long idChannel) {
		// Null check first
		if (idChannel == null || idChannel.equals(0l)) {
			return;
		}
		// get a list of all LevelId's in a channel
		List<Long> idLevels = getAllLevelIdsInAChannel(idChannel);
		if (idLevels != null && idLevels.size() > 0) {
			for (Long idLevel : idLevels) {
				// perform the actual function
				updateTopicDescriptionsForALevel(idLevel);
			}			
		}		
	}

	/**
	 *  Update Level Descriptions For A given level
	 * @param idLevel
	 */
	private void updateTopicDescriptionsForALevel(Long idLevel) {
		// Null check first
		if (idLevel == null || idLevel.equals(0l)) {
			return;
		}
		// get a list of all Topics in a level
		// first use an existing function to get all levels in a channel
        String sql = "SELECT * FROM topic WHERE id_level = :idLevel ORDER BY id_topic ASC";
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idLevel", idLevel);
        List<Topic> topics = getNamedParameterJdbcTemplate().query(sql, args, topicRowMapper);
		if (topics != null && topics.size() > 0) {
			for (Topic topic : topics) {
				updateTopicDescriptionForATopic(topic.getIdTopic(), topic.getName());
			}
		}
	}

	/**
	 *  Update Level Descriptions For A given level
	 * @param idLevel
	 */
	private void updateTopicDescriptionForATopic(Long idTopic, String descriptionPrefix) {
		// May want to make these strings static
        String sqlSkillQuery = "SELECT * FROM skill WHERE id_topic = :idTopic ORDER BY id_skill ASC";
    	String sqlTopicUpdate = "UPDATE topic SET description = :topicDescription WHERE id_topic = :idTopic";
		// null check...
		if (idTopic == null || idTopic.equals(0l)) {
			return;
		}
		// get all topics and concatenate their names with a comma seperating the names (skip any Topic name that has a "Test" in the name
        BeanPropertyRowMapper<Skill> skillRowMapper = BeanPropertyRowMapper.newInstance(Skill.class);
        Map<String, Object> argsQuery = new HashMap<String, Object>();
        argsQuery.put("idTopic", idTopic);
        List<Skill> skills = getNamedParameterJdbcTemplate().query(sqlSkillQuery, argsQuery, skillRowMapper);
        if (skills != null && skills.size() > 0) {
        	StringBuffer topicDescriptionBuffer = new StringBuffer();
        	if (descriptionPrefix != null && descriptionPrefix.trim().length() > 0) {
        		topicDescriptionBuffer.append(descriptionPrefix.trim()).append(": ");
        	}
//        	levelDescriptionBuffer.append("Learn about... ");
        	boolean isFirstSkillName = true;
        	for (Skill skill : skills) {
        		if (skill.getName() != null && skill.getName().trim().length() > 0 && !skill.getName().toUpperCase().contains("TEST")) {
        			if (isFirstSkillName) {
        				topicDescriptionBuffer.append(skill.getName().trim());
            			isFirstSkillName = false;
        			} else {
        				topicDescriptionBuffer.append(", ").append(skill.getName().trim());
        			}
        		}
        	}
        	// now update the level with the newly created level description
        	
        	// note that the column size for level description is 500 chars...so truncate to 500 
        	String topicDescription = topicDescriptionBuffer.toString();
        	if (topicDescription.length() > 500) {
        		topicDescription = topicDescription.substring(0, 497) + "...";
        	}
        	
        	if (topicDescription != null && topicDescription.trim().length() > 0) {
	        	Map<String, Object> argsUpdate = new HashMap<String, Object>();
	        	argsUpdate.put("topicDescription", topicDescription);
	        	argsUpdate.put("idTopic", idTopic);
	        	getNamedParameterJdbcTemplate().update(sqlTopicUpdate, argsUpdate);
        	}
        }
	}


	/**
	 * Returns a list of Channel Id's in the system
	 * @return
	 */
	private List<Long> getAllChannelIdsInSystem() {
		List<Long> idChannels = null;
		// first use an existing function to get all channels in the system
		List<Channel> channels = findAllChannels();
		// now create and return the list of Channel ID's
		if (channels != null && channels.size() > 0) {
			idChannels = new ArrayList<Long>();
			for (Channel channel : channels) {
				idChannels.add(channel.getIdSystem());
			}
		}
		return idChannels;
	}

	/**
	 * Returns a list of Level Id's in the system
	 * @return
	 */
	private List<Long> getAllLevelIdsInAChannel(Long idChannel) {
		List<Long> idLevels = null;
		// first use an existing function to get all levels in a channel
        String sql = "SELECT * FROM level WHERE id_system = :idChannel ORDER BY id_level ASC";
        BeanPropertyRowMapper<Level> levelRowMapper = BeanPropertyRowMapper.newInstance(Level.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idChannel", idChannel);
        List<Level> levels = getNamedParameterJdbcTemplate().query(sql, args, levelRowMapper);
		// now create and return the list of Channel ID's
		if (levels != null && levels.size() > 0) {
			idLevels = new ArrayList<Long>();
			for (Level level : levels) {
				idLevels.add(level.getIdLevel());
			}
		}
		return idLevels;
	}

	/**
	 * Returns a list of Level Id's in the system
	 * @return
	 */
	private List<Long> getAllTopicIdsInALevel(Long idLevel) {
		List<Long> idTopics = null;
		// first use an existing function to get all levels in a channel
        String sql = "SELECT * FROM topic WHERE id_level = :idLevel ORDER BY id_topic ASC";
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idLevel", idLevel);
        List<Topic> topics = getNamedParameterJdbcTemplate().query(sql, args, topicRowMapper);
		// now create and return the list of Topic ID's
		if (topics != null && topics.size() > 0) {
			idTopics = new ArrayList<Long>();
			for (Topic topic : topics) {
				idTopics.add(topic.getIdTopic());
			}
		}
		return idTopics;
	}





}

