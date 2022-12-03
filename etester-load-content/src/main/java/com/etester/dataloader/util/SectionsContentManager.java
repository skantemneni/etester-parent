package com.etester.dataloader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.etester.data.domain.content.core.ChannelDao;
import com.etester.data.domain.content.core.Level;
import com.etester.data.domain.content.core.LevelDao;
import com.etester.data.domain.content.core.Section;
import com.etester.data.domain.content.core.SectionDao;
import com.etester.data.domain.content.core.Skill;
import com.etester.data.domain.content.core.SkillDao;
import com.etester.data.domain.content.core.Topic;
import com.etester.data.domain.content.core.TopicDao;
import com.etester.data.domain.test.Test;
import com.etester.data.domain.test.TestConstants;
import com.etester.data.domain.test.TestDao;
import com.rulefree.rfloaddataschema.GetSectionsRequest;
import com.rulefree.rfloaddataschema.GetTestsRequest;
import com.rulefree.rfloaddataschema.LoadSectionsResponse;
import com.rulefree.rfloaddataschema.SaveTestsResponse;
import com.rulefree.rfloaddataschema.TestIdArrayType;
import com.rulefree.rfloaddataschema.TestRequestChoiceType;
import com.rulefree.rfloaddataschema.TestsByIdArrayType;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;

/**
 * For now this class does not care about Namespaces.  Will make it so when I get a chance.
 * 
 * @author sesi
 *
 */
@Slf4j
public class SectionsContentManager {
	
	private static final String DOWNLOAD_ARTIFACT_TYPE_CHANNEL = "channel";
	private static final String DOWNLOAD_ARTIFACT_TYPE_LEVEL = "level";
	private static final String DOWNLOAD_ARTIFACT_TYPE_TOPIC = "topic";
	private static final String DOWNLOAD_ARTIFACT_TYPE_SKILL = "skill";
	private static final String DOWNLOAD_ARTIFACT_TYPE_SECTION = "section";
	
	private static final String DOWNLOAD_TESTS_FOR_TESTLIST = "tests";
	private static final String DOWNLOAD_TESTS_FOR_CHANNEL = "channel";
	private static final String DOWNLOAD_TESTS_FOR_CHANNEL_TESTTYPE = "channel_testtype";
	private static final String DOWNLOAD_TESTS_FOR_PROVIDER = "provider";
	private static final String DOWNLOAD_TESTS_FOR_PROVIDER_TESTTYPE = "provider_testtype";

	// Default Spring setup file
	private static final String DEFAULT_APP_CONFIG_FILE = "beans-jdbc.xml";

	// Default Properties file
	private static final String SETUP_PROPERTIES_FILE_NAME = "sections_content_manager_setup.properties";

	// Override Properties file variable name
	private static final String OVERRIDE_PROPERTIES_VARIABLE_NAME = "override_properties";


	// Property names
	private static final String FORCED_LOGIN_USERNAME_VARIABLE_NAME = "FORCED_LOGIN_USERNAME";
	private static final String OVERRIDE_APP_CONFIG_FILE_VARIABLE_NAME = "OVERRIDE_APP_CONFIG_FILE";
	
	private static final String TEST_DOWNLOAD_PATH_VARIABLE_NAME = "TEST_DOWNLOAD_PATH";
	private static final String TEST_FILE_PATH_VARIABLE_NAME = "TEST_FILE_PATH";
	private static final String PROCESSED_TEST_FILE_PATH_VARIABLE_NAME = "PROCESSED_TEST_FILE_PATH";
	private static final String FAILED_TEST_FILE_PATH_VARIABLE_NAME = "FAILED_TEST_FILE_PATH";

	private static final String CONTENT_DOWNLOAD_PATH_VARIABLE_NAME = "CONTENT_DOWNLOAD_PATH";
	private static final String CONTENT_DOWNLOAD_FILE_LEVEL_VARIABLE_NAME = "CONTENT_DOWNLOAD_FILE_LEVEL";
	private static final String CONTENT_FILE_PATH_VARIABLE_NAME = "CONTENT_FILE_PATH";
	private static final String CONTENT_FILE_NAME_PREFIX_VARIABLE_NAME = "CONTENT_FILE_NAME_PREFIX";
	private static final String PROCESSED_CONTENT_FILE_PATH_VARIABLE_NAME = "PROCESSED_CONTENT_FILE_PATH";
	private static final String FAILED_CONTENT_FILE_PATH_VARIABLE_NAME = "FAILED_CONTENT_FILE_PATH";
	
	private static final String MAX_SECONDS_TO_WAIT_BEFORE_PROCESSING_VARIABLE_NAME = "MAX_SECONDS_TO_WAIT_BEFORE_PROCESSING";
	private static final String MAX_FILES_TO_PROCESS_VARIABLE_NAME = "MAX_FILES_TO_PROCESS";

			
	private String forcedLoginUsername = "mary";
	private String overrideAppConfigFile = "beans-jdbc.xml";
	private String contentFileNamePrefix = "";

	private String testsDownloadPath = "C:/rffiles/output/testscontent/";
	private File testsDownloadPathDirectory = null;
	private String testFilesPath = "C:/rffiles/input/testscontent/";
	private File testFilesPathDirectory = null;
	private String processedTestFilesPath = testFilesPath + "processed/";
	private File processedTestFilesPathDirectory = null;
	private String failedTestFilesPath = testFilesPath + "failed/";
	private File failedTestFilesPathDirectory = null;

	private String contentDownloadPath = "C:/rffiles/output/sectionscontent/";
	private File contentDownloadPathDirectory = null;
	private String contentDownloadFileLevel = "section";
	private boolean downloadSkillFiles = false;
	private String contentFilesPath = "C:/rffiles/input/sectionscontent/";
	private File contentFilesPathDirectory = null;
	private String processedContentFilesPath = contentFilesPath + "processed/";
	private File processedContentFilesPathDirectory = null;
	private String failedContentFilesPath = contentFilesPath + "failed/";
	private File failedContentFilesPathDirectory = null;
	private Integer maxSecondsToWaitBeforeProcessing = 0;
	private Integer maxFilesToProcess = -1;

//	private boolean process_files_in_service_class = true;
	Properties setup_properties = null;
	Properties override_properties = null;

	// Now load up spring conf files...
	ApplicationContext applicationContext = null; 
	DataloaderService dataloaderService = null;
	
	// used to download content
	private boolean isDownloader = false;
	private boolean isTestDownloader = false;
	private boolean isUploader = false;
	private boolean isTestUploader = false;
	private boolean isMiscAdminProcessor = false;
	private String download_artifact_type = null;
	private Long download_artifact_id = null;


//	public static void main(String[] args) {
//		String properties_file = System.getProperty(OVERRIDE_PROPERTIES_VARIABLE_NAME);
//		
//		if (properties_file != null && properties_file.trim().length() != 0 && !properties_file.trim().endsWith(".properties")) {
//			// Complain and quit...
//			System.out.println("Incorrect Usage. Use the following Commans Line Expression: ");
//			System.out.println("java -D" + OVERRIDE_PROPERTIES_VARIABLE_NAME +"=<override.properties> -jar <jar_file_path>  OR");
//			System.out.println("java -Dlog4j.configuration=<log_file_full_path> -D" + OVERRIDE_PROPERTIES_VARIABLE_NAME +"=<setup.properties> -jar <jar_file_path>");
//			System.out.println("For Example: ");
//			System.out.println("java -D" + OVERRIDE_PROPERTIES_VARIABLE_NAME +"=load_sections_setup.properties -jar target/rfloadcontent.jar");
//			System.exit(-1);
//		}
//		
//		// Create and run the Processor
//		new GetSectionsContent(args).run(args);
//	}


	public static void main(String[] args) {
		String properties_file = System.getProperty(OVERRIDE_PROPERTIES_VARIABLE_NAME);
		
		if (properties_file != null && properties_file.trim().length() != 0 && !properties_file.trim().endsWith(".properties")) {
			// Complain and quit...
			System.out.println("Incorrect Usage. Use the following Commans Line Expression: ");
			System.out.println("java -D" + OVERRIDE_PROPERTIES_VARIABLE_NAME +"=<override_setup.properties> -jar <jar_file_path>  OR");
			System.out.println("java -Dlog4j.configuration=<log_file_full_path> -D" + OVERRIDE_PROPERTIES_VARIABLE_NAME +"=<setup.properties> -jar <jar_file_path>");
			System.out.println("For Example: ");
			System.out.println("java -D" + OVERRIDE_PROPERTIES_VARIABLE_NAME +"=<override_setup.properties> -jar target/rfloadcontent.jar");
			System.exit(-1);
		}
		
		// Create and run the Processor
		new SectionsContentManager(args).run(args);
	}

	
	
	
	/**
	 * Constructor that dumps out any Settings to the Log file...
	 */
	public SectionsContentManager(String[] args) {
		this();
		if (log.isInfoEnabled()) {
			log.info("This program was invoked with the following Parameters: ");
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					log.info(args[i]);
				}
			} else {
				log.info("No Additional Arguments");
			}
			if (args != null && args.length >= 1) {
				if (args[0] != null && args[0].trim().equalsIgnoreCase("get")) {
					this.isDownloader = true;
					this.isTestDownloader = false;
					this.isUploader = false;
					this.isTestUploader = false;
					this.isMiscAdminProcessor = false;
				} else if (args[0] != null && args[0].trim().equalsIgnoreCase("gettest")) {
					this.isDownloader = false;
					this.isTestDownloader = true;
					this.isUploader = false;
					this.isTestUploader = false;
					this.isMiscAdminProcessor = false;
				} else if (args[0] != null && args[0].trim().equalsIgnoreCase("load")) {
					this.isDownloader = false;
					this.isTestDownloader = false;
					this.isUploader = true;
					this.isTestUploader = false;
					this.isMiscAdminProcessor = false;
				} else if (args[0] != null && args[0].trim().equalsIgnoreCase("savetest")) {
					this.isDownloader = false;
					this.isTestDownloader = false;
					this.isUploader = false;
					this.isTestUploader = true;
					this.isMiscAdminProcessor = false;
				} else if (args[0] != null && args[0].trim().equalsIgnoreCase("misc")) {
					this.isDownloader = false;
					this.isTestDownloader = false;
					this.isUploader = false;
					this.isTestUploader = false;
					this.isMiscAdminProcessor = true;
				} else {
					this.isDownloader = false;
					this.isTestDownloader = false;
					this.isUploader = false;
					this.isTestUploader = false;
					this.isMiscAdminProcessor = false;
				}
			}
		}
	}

	/**
	 * Constructor that dumps out any Settings to the Log file...
	 */
	public SectionsContentManager() {
		super();
		this.forcedLoginUsername = getProperty(FORCED_LOGIN_USERNAME_VARIABLE_NAME);
		this.overrideAppConfigFile = getProperty(OVERRIDE_APP_CONFIG_FILE_VARIABLE_NAME);
		
		this.testsDownloadPath = getProperty(TEST_DOWNLOAD_PATH_VARIABLE_NAME);
		this.testFilesPath = getProperty(TEST_FILE_PATH_VARIABLE_NAME);
		this.processedTestFilesPath = getProperty(PROCESSED_TEST_FILE_PATH_VARIABLE_NAME);
		this.failedTestFilesPath = getProperty(FAILED_TEST_FILE_PATH_VARIABLE_NAME);

		this.contentDownloadPath = getProperty(CONTENT_DOWNLOAD_PATH_VARIABLE_NAME);
		this.contentDownloadFileLevel = getProperty(CONTENT_DOWNLOAD_FILE_LEVEL_VARIABLE_NAME);
		this.contentFilesPath = getProperty(CONTENT_FILE_PATH_VARIABLE_NAME);
		this.contentFileNamePrefix = getProperty(CONTENT_FILE_NAME_PREFIX_VARIABLE_NAME);
		this.processedContentFilesPath = getProperty(PROCESSED_CONTENT_FILE_PATH_VARIABLE_NAME);
		this.failedContentFilesPath = getProperty(FAILED_CONTENT_FILE_PATH_VARIABLE_NAME);
		this.maxSecondsToWaitBeforeProcessing = getIntProperty(MAX_SECONDS_TO_WAIT_BEFORE_PROCESSING_VARIABLE_NAME);
		this.maxFilesToProcess = getIntProperty(MAX_FILES_TO_PROCESS_VARIABLE_NAME);
		
		// Now load up spring conf files...
		this.applicationContext = getApplicationContext(); 
		this.dataloaderService = this.applicationContext.getBean("dataloaderService", DataloaderService.class);
	}
	
	public boolean validateForLoadContent(String[] args) {
		
//		// Now validate all directory paths
//		// Source Content file path 
//		if (this.contentDownloadPath == null) {
//			log.error("CONTENT_DOWNLOAD_PATH is NOT SET.");
//		}
//		
		// Now validate all directory paths
		// Source Content file path 
		if (this.contentFilesPath == null) {
			log.error("CONTENT_FILE_PATH cannot be NULL.");
			return false;
		} else {
			this.contentFilesPathDirectory = new File(this.contentFilesPath);
			if (this.contentFilesPathDirectory == null || !this.contentFilesPathDirectory.exists() || !this.contentFilesPathDirectory.isDirectory() || !this.contentFilesPathDirectory.canWrite()) {
				log.error("CONTENT_FILE_PATH Has to be a Valid Directory and has to be Writable");
				return false;
			}
		}
		
		// Processed Content file path 
		if (this.processedContentFilesPath == null) {
			log.warn("PROCESSED_CONTENT_FILE_PATH is NULL.  Will default to: '" + this.contentFilesPath + "/processed'");
			this.processedContentFilesPath = this.contentFilesPath + "/processed";
		}
		this.processedContentFilesPathDirectory = new File(this.processedContentFilesPath);
		if (!this.processedContentFilesPathDirectory.exists()) {
			this.processedContentFilesPathDirectory.mkdir();
		}
		if (this.processedContentFilesPathDirectory == null || !this.processedContentFilesPathDirectory.exists() || !this.processedContentFilesPathDirectory.isDirectory() || !this.processedContentFilesPathDirectory.canWrite()) {
			log.error("PROCESSED_CONTENT_FILE_PATH Has to be a Valid Directory and has to be Writable");
			return false;
		}
		
		// Failed Content file path 
		if (this.failedContentFilesPath == null) {
			log.warn("FAILED_CONTENT_FILE_PATH is NULL.  Will default to: '" + this.contentFilesPath + "/failed'");
			this.failedContentFilesPath = this.contentFilesPath + "/failed";
		}
		this.failedContentFilesPathDirectory = new File(this.failedContentFilesPath);
		if (!this.failedContentFilesPathDirectory.exists()) {
			this.failedContentFilesPathDirectory.mkdir();
		}
		if (this.failedContentFilesPathDirectory == null || !this.failedContentFilesPathDirectory.exists() || !this.failedContentFilesPathDirectory.isDirectory() || !this.failedContentFilesPathDirectory.canWrite()) {
			log.error("FAILED_CONTENT_FILE_PATH Has to be a Valid Directory and has to be Writable");
			return false;
		}
		
		// Log at info level all the startup parameters
		if (log.isInfoEnabled()) {
			log.info("The following LOGIN_USERNAME will be used to Manage Content: '" + this.forcedLoginUsername + "'");
			log.info("The following OVERRIDE_APP_CONFIG_FILE will be used to Manage Content: '" + this.overrideAppConfigFile + "'");
//			log.info("The following CONTENT_DOWNLOAD_PATH will be used to Manage Content: '" + this.contentDownloadPath + "'");
			log.info("The following CONTENT_FILE_PATH will be used to Manage Content: '" + this.contentFilesPath + "'");
			log.info("The following CONTENT_FILE_NAME_PREFIX will be used to Manage Content: '" + this.contentFileNamePrefix + "'");
			log.info("The following PROCESSED_CONTENT_FILE_PATH will be used to Manage Content: '" + this.processedContentFilesPath + "'");
			log.info("The following FAILED_CONTENT_FILE_PATH will be used to Manage Content: '" + this.failedContentFilesPath + "'");
			log.info("The following MAX_SECONDS_TO_WAIT_BEFORE_PROCESSING will be used to Load Content: '" + this.maxSecondsToWaitBeforeProcessing + "'");
			log.info("The following MAX_FILES_TO_PROCESS will be used to Load Content: '" + this.maxFilesToProcess + "'");
//			log.info("File Processing occurs within the Service Class: '" + this.process_files_in_service_class + "'");
		}
		
		return true;
	}

	/**
	 * Returns an Unmarshaller object corresponding to the class.
	 * @param classObject
	 * @return
	 * @throws JAXBException
	 */
	@SuppressWarnings("rawtypes")
	static public Unmarshaller getPayloadUnmarshaller(Class classObject) throws JAXBException {
		JAXBContext context = JAXBContext
				.newInstance(classObject);
		Unmarshaller um = context.createUnmarshaller();
		return um;
	}

	/**
	 * Returns a Marshaller object corresponding to the class.
	 * @param classObject
	 * @return
	 * @throws JAXBException
	 */
	@SuppressWarnings("rawtypes")
	static public Marshaller getPayloadMarshaller(Class classObject) throws JAXBException {
		JAXBContext context = JAXBContext
				.newInstance(classObject);
		Marshaller m = context.createMarshaller();
		return m;
	}

	
	// Run runs the main functionality of the class
	private void run(String[] args) {
		// log the Start message
		log.info("Application Started...");

		if (this.isDownloader) {
			if (validateForGetContent(args)) {
				this.executeDownloadOperation();
			}
		} else if (this.isTestDownloader) { 
			this.executeDownloadTestsOperation(args);
		} else if (this.isUploader) { 
			if (validateForLoadContent(args)) {
				this.executeUploadOperation(args);
			}
		} else if (this.isTestUploader) { 
			this.executeTestUploadOperation(args);
		} else if (this.isMiscAdminProcessor) { 
			this.executeMiscAdminOperation(args);
		} else {
			log.error("Nothing to run.  Use command line parameter 'load', 'get' or 'misc'...");
		}
		// log the Completion message
		log.info("Application Finished Processing...");
		System.exit(0);
	}


	/**
	 * Executes a Miscellaneous Admin Function in the system.  
	 * Supported Miscellaneous Operations are:
	 * 
	 * "update_leveldescriptions" - Update Level Descriptions for a given set of channels in the system.  Keyword "all" is taken to mean All Channels
	 * "update_skillcounts" - Update Skill and Topic Counts for a given list of channels in the system.  Keyword "all" is taken to mean All Channels
	 * 
	 * @param args
	 * @return
	 */
	private void executeMiscAdminOperation(String[] args) {
		if (args != null && args.length < 2) {
			log.error("Unknown Misc Admin Operation: Available Misc Operations are: \"update_leveldescriptions\", \"update_topicdescriptions\", \"update_skillcounts\"");
		} else {
			if (args[1].trim().equalsIgnoreCase("update_leveldescriptions")) {
				this.updateLevelDescriptionsForChannels(args);
			} else if (args[1].trim().equalsIgnoreCase("update_topicdescriptions")) {
				this.updateTopicDescriptionsForChannels(args);
			} else if (args[1].trim().equalsIgnoreCase("update_skillcounts")) {
				this.updateSkillCountsForChannels(args);
			} else {
				log.error("Unknown Misc Admin Operation: '" + args[1] + "'.  Available Misc Operations are: \"update_leveldescriptions\", \"update_skillcounts\"");
			}
		}
	}



	/**
	 * Update Level Descriptions for a given list of channels (its levels) in the system.  Note that the word 
	 * "all" is allowed for a list of channels.  In which case all channels will be updated.
	 * 
	 * @param args
	 */
	public void updateLevelDescriptionsForChannels(String[] args) {
		log.info("Will Update LevelDescriptions for Channels...");
		if (args.length <= 2) {
			// we should have never been here...
			log.error("Insufficient Arguments....Exiting.");
			return;
		}
		// Get the Dao to update the skill counts on
		ChannelDao channelDao = this.applicationContext.getBean("channelDao", ChannelDao.class);
		if (channelDao == null) {
			log.error("Cannot find the ChannelDAO...Exiting");
			return;
		}
		if ((args != null && args.length >= 3) && args[2].trim().equalsIgnoreCase("all")) {
			// update for all
			channelDao.updateAllLevelDescriptions();
		} else {
			List<Long> idChannels = getChannelIdsOnCommandLine(args);
			if (idChannels != null && idChannels.size() > 0) {
				// find a list of levels for channel....
				channelDao.updateLevelDescriptions(idChannels);
			}
		}
		log.info("Completed Update of SkillCounts for Channels...");
	}


	/**
	 * Update Level Descriptions for a given list of channels (its levels) in the system.  Note that the word 
	 * "all" is allowed for a list of channels.  In which case all channels will be updated.
	 * 
	 * @param args
	 */
	public void updateTopicDescriptionsForChannels(String[] args) {
		log.info("Will Update TopicDescriptions for Channels...");
		if (args.length <= 2) {
			// we should have never been here...
			log.error("Insufficient Arguments....Exiting.");
			return;
		}
		// Get the Dao to update the skill counts on
		ChannelDao channelDao = this.applicationContext.getBean("channelDao", ChannelDao.class);
		if (channelDao == null) {
			log.error("Cannot find the ChannelDAO...Exiting");
			return;
		}
		if ((args != null && args.length >= 3) && args[2].trim().equalsIgnoreCase("all")) {
			// update for all
			channelDao.updateAllTopicDescriptions();
		} else {
			List<Long> idChannels = getChannelIdsOnCommandLine(args);
			if (idChannels != null && idChannels.size() > 0) {
				// find a list of levels for channel....
				channelDao.updateTopicDescriptions(idChannels);
			}
		}
		log.info("Completed Update of Topic Descriptions for Channels...");
	}




	/**
	 * Update Skill and Topic Counts for a given list of channels in the system.  Note that the word 
	 * "all" is allowed for a list of channels.  In which case all channels will be updated.
	 * 
	 * @param args
	 */
	private void updateSkillCountsForChannels(String[] args) {
		log.info("Will Update SkillCounts for Channels...");
		if (args.length <= 2) {
			// we should have never been here...
			log.error("Insufficient Arguments....Exiting.");
			return;
		}
		// Get the Dao to update the skill counts on
		ChannelDao channelDao = this.applicationContext.getBean("channelDao", ChannelDao.class);
		if (channelDao == null) {
			log.error("Cannot find the ChannelDAO...Exiting");
			return;
		}
		if ((args != null && args.length >= 3) && args[2].trim().equalsIgnoreCase("all")) {
			// update for all
			channelDao.updateAllSkillCounts();
		} else {
			List<Long> idChannels = getChannelIdsOnCommandLine(args);
			if (idChannels != null && idChannels.size() > 0) {
				// find a list of levels for channel....
				channelDao.updateSkillCounts(idChannels);
			}
		}
		log.info("Completed Update of SkillCounts for Channels...");
	}

	/**
	 * Return a list of the Channel ID's listed on the command line.  Note that all the channel ID's have to 
	 * be numbers.  Also, this function returns all or nothing.  Meaning, if any one of the channel ID's is not 
	 * a number,  this function logs an error statement and returns a null.
	 * 
	 * @param args  The original command line arguments
	 * @return
	 */
	private List<Long> getChannelIdsOnCommandLine(String[] args) {
		List<Long> idChannels = new ArrayList<Long>();
		for (int i = 2; i < args.length; i++) {
			String channelId = args[i];
			long idChannel = 0l;
			try {
				idChannel = Long.parseLong(channelId);
			} catch (NumberFormatException nfe) {
				log.error("Cannot Convert Channel Id to Long: '" + channelId + "'");
				log.error("No Channels will be updated: '" + channelId + "'");
				return null;
			}
			if (idChannel == 0l) {
				continue;
			} else {
				idChannels.add(idChannel);
			}
		}
		return idChannels;
	}



	/**
	 * Runs the main download tests functionality of the class.
	 * Interprets the request parameters (command line arguments) and invokes the download operation.
	 * The Expected command line arguments are of the form:
	 * Argument 1 : 'get'
	 * Argument 2 : '<artifact_type>'
	 * Argument 3 : '<artifact_id>'
	 * For example: get section 103101002020001
	 */
	private boolean executeDownloadTestsOperation (String[] args) {
		// Make sure the testsDownloadPath is set
		if (this.testsDownloadPath == null) {
			log.error("TEST DOWNLOAD PATH NOT SET");
			return false;
		}
		// create the testsDownloadPathDirectory object
		this.testsDownloadPathDirectory = new File(this.testsDownloadPath);
		// create the physical testsDownloadPathDirectory if necessary
		if (!this.testsDownloadPathDirectory.exists()) {
			this.testsDownloadPathDirectory.mkdirs();
		}
		// Make sure the testsDownloadPathDirectory exists and is writable
		if (this.testsDownloadPathDirectory == null || !this.testsDownloadPathDirectory.exists() || !this.testsDownloadPathDirectory.isDirectory() || !this.testsDownloadPathDirectory.canWrite()) {
			log.error("TEST_DOWNLOAD_PATH Has an Invalid Directory Path or it cannot be writtent to:");
			return false;
		}
		// Log at info level all the startup parameters
		if (log.isInfoEnabled()) {
			log.info("The following LOGIN_USERNAME will be used to Manage Content: '" + this.forcedLoginUsername + "'");
			log.info("The following OVERRIDE_APP_CONFIG_FILE will be used to Manage Content: '" + this.overrideAppConfigFile + "'");
			log.info("The following TEST_DOWNLOAD_PATH will be used to Manage Content: '" + this.testsDownloadPath + "'");
		}

		// now do the regular processing...
		if (args == null || args.length < 2) {
			log.error("Information Not Sufficient for a any Operation");
			return false;
		}
		
		// create a status variable
		boolean isValid = true;
		
		if (args[1].trim().equalsIgnoreCase(DOWNLOAD_TESTS_FOR_TESTLIST)) {
			if (args.length < 3) {
				log.error("Information Not Sufficient for a 'gettest " + DOWNLOAD_TESTS_FOR_TESTLIST + "' Operation.  Should be <program-name> gettest " + DOWNLOAD_TESTS_FOR_TESTLIST + " <testid> <testid> <testid> ...>");
				return false;
			} else {
				List<Long> idTestList = new ArrayList<Long>();
				for (int i = 2; i < args.length; i++) {
					try {
						idTestList.add(Long.parseLong(args[i]));
					} catch (NumberFormatException nfe) {
						log.error("Invalid TestId: '" + args[i] + "'");
						return false;
					}
				}
				if (idTestList.isEmpty()) {
					log.error("Information Not Sufficient for a 'gettest " + DOWNLOAD_TESTS_FOR_TESTLIST + "' Operation.  Should be <program-name> gettest " + DOWNLOAD_TESTS_FOR_TESTLIST + " <testid> <testid> <testid> ...>");
				} else {
					// perform the operation....get tests from database...
					downloadTestsForTestList(idTestList);
					return true;
				}
			}
			
		} else if (args[1].trim().equalsIgnoreCase(DOWNLOAD_TESTS_FOR_CHANNEL)) {
			if (args.length < 3) {
				log.error("Information Not Sufficient for a 'gettest " + DOWNLOAD_TESTS_FOR_CHANNEL + "' Operation.  Should be <program-name> gettest " + DOWNLOAD_TESTS_FOR_CHANNEL + " <idChannel>");
				return false;
			} else {
				Long idChannel = null;
				try {
					idChannel = Long.parseLong(args[2]);
				} catch (NumberFormatException nfe) {
					log.error("Invalid ChannelId: '" + args[2] + "'");
					return false;
				}
				if (idChannel == null) {
					log.error("Information Not Sufficient for a 'gettest " + DOWNLOAD_TESTS_FOR_CHANNEL + "' Operation.  Should be <program-name> gettest " + DOWNLOAD_TESTS_FOR_CHANNEL + " <idChannel>");
				} else {
					// perform the operation....get tests from database...
					downloadAllTestsForChannel(idChannel);
					return true;
				}
			}
			
		} else if (args[1].trim().equalsIgnoreCase(DOWNLOAD_TESTS_FOR_PROVIDER)) {
			if (args.length < 3) {
				log.error("Information Not Sufficient for a 'gettest " + DOWNLOAD_TESTS_FOR_PROVIDER + "' Operation.  Should be <program-name> gettest " + DOWNLOAD_TESTS_FOR_PROVIDER + " <idProvider>");
				return false;
			} else {
				Long idProvider = null;
				try {
					idProvider = Long.parseLong(args[2]);
				} catch (NumberFormatException nfe) {
					log.error("Invalid ProviderId: '" + args[2] + "'");
					return false;
				}
				if (idProvider == null) {
					log.error("Information Not Sufficient for a 'gettest " + DOWNLOAD_TESTS_FOR_PROVIDER + "' Operation.  Should be <program-name> gettest " + DOWNLOAD_TESTS_FOR_PROVIDER + " <idProvider>");
				} else {
					// perform the operation....get tests from database...
					downloadAllTestsForProvider(idProvider);
					return true;
				}
			}
		} else if (args[1].trim().equalsIgnoreCase(DOWNLOAD_TESTS_FOR_CHANNEL_TESTTYPE)) {
			if (args.length < 4) {
				log.error("Information Not Sufficient for a 'gettest " + DOWNLOAD_TESTS_FOR_CHANNEL_TESTTYPE + "' Operation.  Should be <program-name> gettest " + DOWNLOAD_TESTS_FOR_CHANNEL_TESTTYPE + " <idChannel> <testType>");
				return false;
			} else {
				Long idChannel = null;
				String testType = args[3];
				try {
					idChannel = Long.parseLong(args[2]);
				} catch (NumberFormatException nfe) {
					log.error("Invalid ChannelId: '" + args[2] + "'");
					return false;
				}
				if (testType == null || 
						!(testType.equalsIgnoreCase(TestConstants.TEST) || 
							testType.equalsIgnoreCase(TestConstants.ASSIGNMENT) || 
							testType.equalsIgnoreCase(TestConstants.CHALLENGE) || 
							testType.equalsIgnoreCase(TestConstants.QUIZ) || 
							testType.equalsIgnoreCase(TestConstants.ALL_TEST_TYPES)
						)
					) {
					log.error("Invalid TestType: '" + testType + "'");
					return false;
				}
				
				if (idChannel == null || testType == null) {
					log.error("Information Not Sufficient for a 'gettest " + DOWNLOAD_TESTS_FOR_CHANNEL_TESTTYPE + "' Operation.  Should be <program-name> gettest " + DOWNLOAD_TESTS_FOR_CHANNEL_TESTTYPE + " <idChannel> <testType>");
				} else {
					// perform the operation....get tests from database...
					downloadAllTestsForChannelAndTestType(idChannel, testType);
					return true;
				}
			}

		} else if (args[1].trim().equalsIgnoreCase(DOWNLOAD_TESTS_FOR_PROVIDER_TESTTYPE)) {
			if (args.length < 4) {
				log.error("Information Not Sufficient for a 'gettest " + DOWNLOAD_TESTS_FOR_PROVIDER_TESTTYPE + "' Operation.  Should be <program-name> gettest " + DOWNLOAD_TESTS_FOR_PROVIDER_TESTTYPE + " <idProvider> <testType>");
				return false;
			} else {
				Long idProvider = null;
				String testType = args[3];
				try {
					idProvider = Long.parseLong(args[2]);
				} catch (NumberFormatException nfe) {
					log.error("Invalid ProviderId: '" + args[2] + "'");
					return false;
				}
				if (testType == null || 
						!(testType.equalsIgnoreCase(TestConstants.TEST) || 
							testType.equalsIgnoreCase(TestConstants.ASSIGNMENT) || 
							testType.equalsIgnoreCase(TestConstants.CHALLENGE) || 
							testType.equalsIgnoreCase(TestConstants.QUIZ) || 
							testType.equalsIgnoreCase(TestConstants.ALL_TEST_TYPES)
						)
					) {
					log.error("Invalid TestType: '" + testType + "'");
					return false;
				}
				
				if (idProvider == null || testType == null) {
					log.error("Information Not Sufficient for a 'gettest " + DOWNLOAD_TESTS_FOR_PROVIDER_TESTTYPE + "' Operation.  Should be <program-name> gettest " + DOWNLOAD_TESTS_FOR_PROVIDER_TESTTYPE + " <idProvider> <testType>");
				} else {
					// perform the operation....get tests from database...
					downloadAllTestsForProviderAndTestType(idProvider, testType);
					return true;
				}
			}
		} else {
			log.error("Information OperationType '" + args[1] + "' Operation.");
			return false;
		}
		// all passed and action completed
		return true;
	}


	private void downloadTest(Long idTest) {
		TestIdArrayType testIdArrayType = new TestIdArrayType();
		testIdArrayType.getIdTest().add(idTest);
		TestsByIdArrayType testsByIdArrayType = new TestsByIdArrayType();
		testsByIdArrayType.setTestIdArray(testIdArrayType);
		TestRequestChoiceType testRequestChoiceType = new TestRequestChoiceType();
		testRequestChoiceType.setTestsByIdArray(testsByIdArrayType);
		GetTestsRequest getTestsRequest = new GetTestsRequest();
		getTestsRequest.setTestRequestChoice(testRequestChoiceType);
		try {
			String serializedTestResponse = this.dataloaderService.getTestsSerialized(getTestsRequest);
			if (serializedTestResponse != null) {
				String testsFileName = "test_" + idTest.toString() + ".xml";
				Files.write(Paths.get(this.testsDownloadPathDirectory.getAbsolutePath(), testsFileName), serializedTestResponse.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
				// log result;
				log.info("File: '" + testsFileName + "' Written!");
			} else {
				log.info("No Test with ID: '" + idTest + "'");
			}
		} catch (JAXBException jaxbe) {
			log.error("JAXBException: " + jaxbe.getMessage());
			jaxbe.printStackTrace();
			return;
		} catch (IOException ioe) {
			log.error("IOException: " + ioe.getMessage());
			ioe.printStackTrace();
			return;
		}
	}

	/**
	 * downloadTestsForTestList
	 * The Expected command line arguments are of the form:
	 * Argument 1 : 'gettest'
	 * Argument 2 : 'tests'
	 * Argument 3 : '<testid>...'
	 * For example: gettest tests 1331000012007 9990000000030 ...
	 * @param args
	 * @return
	 */
	private void downloadTestsForTestList(List<Long> idTestList) {
		if (idTestList == null || idTestList.size() == 0) {
			return;
		} else {
			for (Long idTest : idTestList) {
				downloadTest(idTest);
			}
			
		}
	}
	/**
	 * downloadAllTestsForChannel
	 * The Expected command line arguments are of the form:
	 * Argument 1 : 'gettest'
	 * Argument 2 : 'channel'
	 * Argument 3 : '<idChannel>'
	 * For example: gettest channel 114
	 * @param args
	 * @return
	 */
	private void downloadAllTestsForChannel(Long idChannel) {
		if (idChannel == null) {
			return;
		} else {
			// get all the tests from the testlist
			TestDao testDao = this.applicationContext.getBean("testDao", TestDao.class);
			List<Test> testsInChannel = testDao.findAllTestsInChannel(idChannel);
			if (testsInChannel != null && testsInChannel.size() > 0) {
				for (Test test : testsInChannel) {
					downloadTest(test.getIdTest());
				}
			}
		}
	}
	/**
	 * downloadAllTestsForProvider
	 * The Expected command line arguments are of the form:
	 * Argument 1 : 'gettest'
	 * Argument 2 : 'provider'
	 * Argument 3 : '<idProvider>'
	 * For example: gettest provider 5
	 * @param args
	 * @return
	 */
	private void downloadAllTestsForProvider(Long idProvider) {
		if (idProvider == null) {
			return;
		} else {
			// get all the tests from the testlist
			TestDao testDao = this.applicationContext.getBean("testDao", TestDao.class);
			List<Test> testsOwnedByProvider = testDao.findAllEditableTestsOwnedByProvider(idProvider);
			if (testsOwnedByProvider != null && testsOwnedByProvider.size() > 0) {
				for (Test test : testsOwnedByProvider) {
					downloadTest(test.getIdTest());
				}
			}
		}
	}
	/**
	 * downloadAllTestsForChannelAndTestType
	 * The Expected command line arguments are of the form:
	 * Argument 1 : 'gettest'
	 * Argument 2 : 'channel_testtype'
	 * Argument 3 : '<idChannel>'
	 * Argument 4 : '<testtype>'
	 * For example: gettest channel_testtype 114 assignment
	 * @param args
	 * @return
	 */
	private void downloadAllTestsForChannelAndTestType(Long idChannel, String testType) {
		if (idChannel == null || testType == null) {
			return;
		} else {
			String realTestType = null;
			if (testType.trim().equalsIgnoreCase(TestConstants.TEST)) { 
				realTestType = TestConstants.TEST;
			} else if (testType.equalsIgnoreCase(TestConstants.ASSIGNMENT)) {
				realTestType = TestConstants.ASSIGNMENT;
			} else if (testType.equalsIgnoreCase(TestConstants.CHALLENGE)) {
				realTestType = TestConstants.CHALLENGE;
			} else if (testType.equalsIgnoreCase(TestConstants.QUIZ)) {
				realTestType = TestConstants.QUIZ;
			} else if (testType.equalsIgnoreCase(TestConstants.ALL_TEST_TYPES)) {
				realTestType = TestConstants.ALL_TEST_TYPES;
			} 
			// get all the tests from the testlist
			TestDao testDao = this.applicationContext.getBean("testDao", TestDao.class);
			List<Test> testTypesInChannel = testDao.findAllTestsInChannelByType(idChannel, realTestType);
			if (testTypesInChannel != null && testTypesInChannel.size() > 0) {
				for (Test test : testTypesInChannel) {
					downloadTest(test.getIdTest());
				}
			}
		}
	}
	/**
	 * downloadAllTestsForProviderAndTestType
	 * The Expected command line arguments are of the form:
	 * Argument 1 : 'gettest'
	 * Argument 2 : 'provider_testtype'
	 * Argument 3 : '<idProvider>'
	 * Argument 4 : '<testtype>'
	 * For example: gettest provider_testtype 5 test
	 * @param args
	 * @return
	 */
	private void downloadAllTestsForProviderAndTestType(Long idProvider, String testType) {
		if (idProvider == null || testType == null) {
			return;
		} else {
			String realTestType = null;
			if (testType.trim().equalsIgnoreCase(TestConstants.TEST)) { 
				realTestType = TestConstants.TEST;
			} else if (testType.equalsIgnoreCase(TestConstants.ASSIGNMENT)) {
				realTestType = TestConstants.ASSIGNMENT;
			} else if (testType.equalsIgnoreCase(TestConstants.CHALLENGE)) {
				realTestType = TestConstants.CHALLENGE;
			} else if (testType.equalsIgnoreCase(TestConstants.QUIZ)) {
				realTestType = TestConstants.QUIZ;
			} else if (testType.equalsIgnoreCase(TestConstants.ALL_TEST_TYPES)) {
				realTestType = TestConstants.ALL_TEST_TYPES;
			} 
			// get all the tests from the testlist
			TestDao testDao = this.applicationContext.getBean("testDao", TestDao.class);
			List<Test> testTypesOwnedByProvider = testDao.findEditableTestsOwnedByProviderByType(idProvider, realTestType);
			if (testTypesOwnedByProvider != null && testTypesOwnedByProvider.size() > 0) {
				for (Test test : testTypesOwnedByProvider) {
					downloadTest(test.getIdTest());
				}
			}
		}
	}


	// Run runs the main upload functionality of the class
	private void executeTestUploadOperation(String[] args) {
		// Now validate all directory paths
		// Source Content file path 
		if (this.testFilesPath == null) {
			log.error("TEST_FILE_PATH cannot be NULL.");
			return;
		} else {
			this.testFilesPathDirectory = new File(this.testFilesPath);
			if (this.testFilesPathDirectory == null || !this.testFilesPathDirectory.exists() || !this.testFilesPathDirectory.isDirectory() || !this.testFilesPathDirectory.canWrite()) {
				log.error("TEST_FILE_PATH Has to be a Valid Directory and has to be Writable");
				return;
			}
		}
		
		// Processed Content file path 
		if (this.processedTestFilesPath == null) {
			log.warn("PROCESSED_TEST_FILE_PATH is NULL.  Will default to: '" + this.testFilesPath + "/processed'");
			this.processedTestFilesPath = this.testFilesPath + "/processed";
		}
		this.processedTestFilesPathDirectory = new File(this.processedTestFilesPath);
		if (!this.processedTestFilesPathDirectory.exists()) {
			this.processedTestFilesPathDirectory.mkdir();
		}
		if (this.processedTestFilesPathDirectory == null || !this.processedTestFilesPathDirectory.exists() || !this.processedTestFilesPathDirectory.isDirectory() || !this.processedTestFilesPathDirectory.canWrite()) {
			log.error("PROCESSED_TEST_FILE_PATH Has to be a Valid Directory and has to be Writable");
			return;
		}
		
		// Failed Content file path 
		if (this.failedTestFilesPath == null) {
			log.warn("FAILED_TEST_FILE_PATH is NULL.  Will default to: '" + this.testFilesPath + "/failed'");
			this.failedTestFilesPath = this.testFilesPath + "/failed";
		}
		this.failedTestFilesPathDirectory = new File(this.failedTestFilesPath);
		if (!this.failedTestFilesPathDirectory.exists()) {
			this.failedTestFilesPathDirectory.mkdir();
		}
		if (this.failedTestFilesPathDirectory == null || !this.failedTestFilesPathDirectory.exists() || !this.failedTestFilesPathDirectory.isDirectory() || !this.failedTestFilesPathDirectory.canWrite()) {
			log.error("FAILED_TEST_FILE_PATH Has to be a Valid Directory and has to be Writable");
			return;
		}

		// Start processing away...
		int processedFileCount = 0; 
		File inputFile = null;
		try {
			List<File> inputFilesToBeProcessed = this.getInputFiles(this.testFilesPath);
			if (inputFilesToBeProcessed != null && inputFilesToBeProcessed.size() > 0) {
				for (File inputFileToBeProcessed : inputFilesToBeProcessed) {
					processedFileCount++;
					// parse the XML and return an instance of the AppConfig class
					inputFile = inputFileToBeProcessed;
					String fileString = FileUtils.readFileToString(inputFile, Charset.forName("UTF-8"));
					
					// log the file name
					log.info("Processing File: " + inputFile.getName());

					// Log the file itself at Debug level
					if (log.isDebugEnabled()) {
						log.debug("Read in: " + fileString);
					}
					
					// Process the file
					SaveTestsResponse saveTestsResponse = null;
//					if (process_files_in_service_class) {
					saveTestsResponse = dataloaderService.saveTests(fileString, this.forcedLoginUsername, true, true);

					// marshall to create the payload response object -
					Writer writer = new StringWriter();
					getPayloadMarshaller(SaveTestsResponse.class).marshal(saveTestsResponse, writer);
					// log the response message
					log.info("Processed: " + processedFileCount + ".) '" + inputFile.getName() + "'.  Response: " + writer.toString());

					// Check to see if we have exceeded the max files to process 
					if (this.maxFilesToProcess > 0 && processedFileCount > this.maxFilesToProcess) {
						log.info("Max Count of Files Processed.  Exiting.");
						break;
					}
					
					// now archive the file...
					if (saveTestsResponse.getStatus() == DataloaderService.UPLOAD_SUCCESS_STATUS_CODE) {
						// move the file to Processed folder
						try {
							Path from = inputFileToBeProcessed.toPath(); //convert from File to Path
							Path to = Paths.get(this.processedTestFilesPathDirectory.getAbsolutePath(),inputFileToBeProcessed.getName()); //convert from String to Path
							Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
//							FileUtils.moveFileToDirectory(inputFileToBeProcessed, this.processedContentFilesPathDirectory, true);
						} catch (NullPointerException npe) {
							log.error("File Move Failed: " + inputFileToBeProcessed.getName() + " or " + this.processedTestFilesPath + " is null.", npe);
							System.exit(1);
						} catch (FileExistsException fee) {
							log.warn("Destination file '" + inputFileToBeProcessed.getName() + "' exists in Directory: '" + this.processedTestFilesPath + "'");
						} catch (IOException ioe) {
							log.warn(" IO error occurs moving the file '" + inputFileToBeProcessed.getName() + "' to Directory: '" + this.processedTestFilesPath + "'", ioe);
						}
					} else {
						// move the file to Failed folder
						try {
							Path from = inputFileToBeProcessed.toPath(); //convert from File to Path
							Path to = Paths.get(this.failedTestFilesPathDirectory.getAbsolutePath(), inputFileToBeProcessed.getName()); //convert from String to Path
							Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
//							FileUtils.moveFileToDirectory(inputFileToBeProcessed, this.failedContentFilesPathDirectory, true);
						} catch (NullPointerException npe) {
							log.error("File Move Failed: " + inputFileToBeProcessed.getName() + " or " + this.failedTestFilesPath + " is null.", npe);
							System.exit(1);
						} catch (FileExistsException fee) {
							log.warn("Destination file '" + inputFileToBeProcessed.getName() + "' exists in Directory: '" + this.failedTestFilesPath + "'");
						} catch (IOException ioe) {
							log.warn(" IO error occurs moving the file '" + inputFileToBeProcessed.getName() + "' to Directory: '" + this.failedTestFilesPath + "'", ioe);
						}
					}
				}
			
			}

		
		} catch (IOException ioe) {
			// if things went wrong...
			log.error("error Reading input file: " + inputFile);
			ioe.printStackTrace();
			// force quit
			System.exit(1);
		} catch (JAXBException jaxbe) {
			// if things went wrong...
			log.error("error parsing xml: " + inputFile);
			jaxbe.printStackTrace();
			// force quit
			System.exit(1);
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Runs the main download functionality of the class.
	 * Interprets the request parameters (command line arguments) and invokes the download operation.
	 * The Expected command line arguments are of the form:
	 * Argument 1 : 'get'
	 * Argument 2 : '<artifact_type>'
	 * Argument 3 : '<artifact_id>'
	 * For example: get section 103101002020001
	 */
	private boolean validateForGetContent(String[] args) {
		if (args != null && args.length >= 3) {
			if (args[1] != null) {
				if (args[1].trim().equalsIgnoreCase(DOWNLOAD_ARTIFACT_TYPE_CHANNEL)) {
					this.download_artifact_type = DOWNLOAD_ARTIFACT_TYPE_CHANNEL;
				} else if (args[1].trim().equalsIgnoreCase(DOWNLOAD_ARTIFACT_TYPE_LEVEL)) {
					this.download_artifact_type = DOWNLOAD_ARTIFACT_TYPE_LEVEL;
				} else if (args[1].trim().equalsIgnoreCase(DOWNLOAD_ARTIFACT_TYPE_TOPIC)) {
					this.download_artifact_type = DOWNLOAD_ARTIFACT_TYPE_TOPIC;
				} else if (args[1].trim().equalsIgnoreCase(DOWNLOAD_ARTIFACT_TYPE_SKILL)) {
					this.download_artifact_type = DOWNLOAD_ARTIFACT_TYPE_SKILL;
				} else if (args[1].trim().equalsIgnoreCase(DOWNLOAD_ARTIFACT_TYPE_SECTION)) {
					this.download_artifact_type = DOWNLOAD_ARTIFACT_TYPE_SECTION;
				}
			}
			if (args[2] != null) {
				try {
					this.download_artifact_id = Long.parseLong(args[2]);
				} catch (NumberFormatException nfe) {}
			}
			log.info("Operation is set to Download...for....");
			log.info("Artifact: " + this.download_artifact_type);
			log.info("ID: " + this.download_artifact_id == null ? "NOT SET!" : this.download_artifact_id.toString());
		}
		
		if (this.download_artifact_type == null || this.download_artifact_id == null) {
			log.error("Information Not Sufficient for a Download Operation");
			return false;
		}
		if (this.contentDownloadPath == null) {
			log.error("CONTENT DOWNLOAD PATH NOT SET");
			return false;
		}
		this.contentDownloadPathDirectory = new File(this.contentDownloadPath);
		if (!this.contentDownloadPathDirectory.exists()) {
			this.contentDownloadPathDirectory.mkdirs();
		}
		if (this.contentDownloadPathDirectory == null || !this.contentDownloadPathDirectory.exists() || !this.contentDownloadPathDirectory.isDirectory() || !this.contentDownloadPathDirectory.canWrite()) {
			log.error("CONTENT_DOWNLOAD_PATH Has an Invalid Directory Path or it cannot be writtent to:");
			return false;
		}
		// see if we need to write skill files....the default is section files (1 per section)
		this.downloadSkillFiles = this.contentDownloadFileLevel != null && this.contentDownloadFileLevel.equalsIgnoreCase("skill");
		// Log at info level all the startup parameters
		if (log.isInfoEnabled()) {
			log.info("The following LOGIN_USERNAME will be used to Manage Content: '" + this.forcedLoginUsername + "'");
			log.info("The following OVERRIDE_APP_CONFIG_FILE will be used to Manage Content: '" + this.overrideAppConfigFile + "'");
			log.info("The following CONTENT_DOWNLOAD_PATH will be used to Manage Content: '" + this.contentDownloadPath + "'");
//			log.info("The following CONTENT_FILE_PATH will be used to Manage Content: '" + this.contentFilesPath + "'");
//			log.info("The following CONTENT_FILE_NAME_PREFIX will be used to Manage Content: '" + this.contentFileNamePrefix + "'");
//			log.info("The following PROCESSED_CONTENT_FILE_PATH will be used to Manage Content: '" + this.processedContentFilesPath + "'");
//			log.info("The following FAILED_CONTENT_FILE_PATH will be used to Manage Content: '" + this.failedContentFilesPath + "'");
//			log.info("The following MAX_SECONDS_TO_WAIT_BEFORE_PROCESSING will be used to Load Content: '" + this.maxSecondsToWaitBeforeProcessing + "'");
//			log.info("The following MAX_FILES_TO_PROCESS will be used to Load Content: '" + this.maxFilesToProcess + "'");
//			log.info("File Processing occurs within the Service Class: '" + this.process_files_in_service_class + "'");
		}
		return true;
	}

	/**
	 * Interprets the request (as stored in class variables), parses the same and invokes the required operation.
	 * For the purposes of this function, the arguments are of the form:
	 * artifact_type = DOWNLOAD_ARTIFACT_TYPE_CHANNEL or DOWNLOAD_ARTIFACT_TYPE_LEVEL or DOWNLOAD_ARTIFACT_TYPE_TOPIC or DOWNLOAD_ARTIFACT_TYPE_SKILL or DOWNLOAD_ARTIFACT_TYPE_SECTION
	 * artifact_id = <full artifact id>
	 * For example: get section 103101002020001
	 */
	private void executeDownloadOperation() {
		Long idChannel = null;
		Long idLevel = null;
		Long idTopic = null;
		Long idSkill = null;
		Long idSection = null;
		
		if (this.download_artifact_type.equals(DOWNLOAD_ARTIFACT_TYPE_CHANNEL)) {
			idChannel = this.download_artifact_id;
			performDownloadChannel(idChannel);
		} else if (this.download_artifact_type.equals(DOWNLOAD_ARTIFACT_TYPE_LEVEL)) {
			idChannel = this.download_artifact_id/1000;
			idLevel = this.download_artifact_id % 1000;
			performDownloadLevel(idChannel, idLevel);
		} else if (this.download_artifact_type.equals(DOWNLOAD_ARTIFACT_TYPE_TOPIC)) {
			idChannel = this.download_artifact_id/1000000;
			idLevel = this.download_artifact_id / 1000 % 1000;
			idTopic = this.download_artifact_id % 1000;
			performDownloadTopic(idChannel, idLevel, idTopic);
		} else if (this.download_artifact_type.equals(DOWNLOAD_ARTIFACT_TYPE_SKILL)) {
			idChannel = this.download_artifact_id/100000000;
			idLevel = this.download_artifact_id / 100000 % 1000;
			idTopic = this.download_artifact_id / 100 % 1000;
			idSkill = this.download_artifact_id % 100;
			performDownloadSkill(idChannel, idLevel, idTopic, idSkill);
		} else if (this.download_artifact_type.equals(DOWNLOAD_ARTIFACT_TYPE_SECTION)) {
			idChannel = this.download_artifact_id/1000000000000l;
			idLevel = this.download_artifact_id / 1000000000 % 1000;
			idTopic = this.download_artifact_id / 1000000 % 1000;
			idSkill = this.download_artifact_id / 10000 % 100;
			idSection = this.download_artifact_id % 10000;
			performDownloadSection(idChannel, idLevel, idTopic, idSkill, idSection);
		}
	}



	/**
	 * Perform download for the given Section.  Note that all the ID's in this call are the short versions (not Full versions)
	 * The output file will be of the form: section-<idSectionFull>.xml. 
	 * @param idChannel
	 * @param idLevel
	 * @param idTopic
	 * @param idSkill
	 * @param idSection
	 */
	private void performDownloadSection(Long idChannel, Long idLevel,
			Long idTopic, Long idSkill, Long idSection) {
		// null check first
		if (idChannel == null || idLevel == null || idTopic == null) {
			log.error("performDownloadSection called with Null Values: idChannel = '" + 
						idChannel == null ? "NULL" : idChannel + "', idLevel = '" + 
						idLevel == null ? "NULL" : idLevel + "', idTopic = '" + 
						idTopic == null ? "NULL" : idTopic + "', idSkill = '" + 
						idSkill == null ? "NULL" : idSkill + "', idSection = '" + 
						idSection == null ? "NULL" : idSection + "'");
			return;
		}
		String responseString = null;
		GetSectionsRequest getSectionsRequest = new GetSectionsRequest();
		getSectionsRequest.setIdSystem(new BigInteger(idChannel.toString()));
		getSectionsRequest.setIdLevel(idLevel.intValue());
		getSectionsRequest.setIdTopic(idTopic.intValue());
		getSectionsRequest.setIdSkill(idSkill.intValue());
		if (idSection != null) {
			getSectionsRequest.setIdSection(idSection.intValue());
		}
		try {
			responseString = this.dataloaderService.getSectionsSerialized (getSectionsRequest);
			// now write the file to the output location...
			Long fullSectionId = idChannel * 1000000000000l + idLevel * 1000000000l + idTopic * 1000000 + idSkill * 10000 + idSection;
			String sectionFileName = "section-" + fullSectionId.toString() + ".xml";
		    Files.write(Paths.get(this.contentDownloadPathDirectory.getAbsolutePath(), sectionFileName), responseString.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			// log result;
			log.info("File: '" + sectionFileName + "' Written!");
		} catch (JAXBException jaxbe) {
			log.error("JAXBException: " + jaxbe.getMessage());
			jaxbe.printStackTrace();
			return;
		} catch (IOException ioe) {
			log.error("IOException: " + ioe.getMessage());
			ioe.printStackTrace();
			return;
		}
	}




	/**
	 * Perform all downloads at a Skill level.  Note that all the ID's in this call are the short versions (not Full versions)
	 * Also note that all sections for a Skill are written in a single file names skill-<idSkillFull>.xml. 
	 * @param idChannel
	 * @param idLevel
	 * @param idTopic
	 * @param idSkill
	 */
	private void performDownloadSkill(Long idChannel, Long idLevel,
			Long idTopic, Long idSkill) {
		// null check first
		if (idChannel == null || idLevel == null || idTopic == null) {
			log.error("performDownloadSkill called with Null Values: idChannel = '" + 
						idChannel == null ? "NULL" : idChannel + "', idLevel = '" + 
						idLevel == null ? "NULL" : idLevel + "', idTopic = '" + 
						idTopic == null ? "NULL" : idTopic + "', idSkill = '" + 
						idSkill == null ? "NULL" : idSkill + "'");
			return;
		}
		if (this.downloadSkillFiles) {
			String responseString = null;
			GetSectionsRequest getSectionsRequest = new GetSectionsRequest();
			getSectionsRequest.setIdSystem(new BigInteger(idChannel.toString()));
			getSectionsRequest.setIdLevel(idLevel.intValue());
			getSectionsRequest.setIdTopic(idTopic.intValue());
			getSectionsRequest.setIdSkill(idSkill.intValue());
			try {
				responseString = this.dataloaderService.getSectionsSerialized (getSectionsRequest);
				// now write the file to the output location...
				Long fullSkillId = idChannel * 100000000l + idLevel * 100000l + idTopic * 100 + idSkill;
				String skillFileName = "skill-" + fullSkillId.toString() + ".xml";
			    Files.write(Paths.get(this.contentDownloadPathDirectory.getAbsolutePath(), skillFileName), responseString.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
				// log result;
				log.info("File: '" + skillFileName + "' Written!");
			} catch (JAXBException jaxbe) {
				log.error("JAXBException: " + jaxbe.getMessage());
				jaxbe.printStackTrace();
				return;
			} catch (IOException ioe) {
				log.error("IOException: " + ioe.getMessage());
				ioe.printStackTrace();
				return;
			}
		} else {
			Long idSkillFull = idChannel * 100000000 + idLevel * 100000 + idTopic * 100 + idSkill;
			// get all the skills in the topic
			SectionDao sectionDao = this.applicationContext.getBean("sectionDao", SectionDao.class);
			List<Section> sectionsInSkillList = sectionDao.findSectionsForSkill(idSkillFull);
			if (sectionsInSkillList != null && sectionsInSkillList.size() > 0) {
				for (Section section : sectionsInSkillList) {
					// call the download operation at a skill level
					performDownloadSection(idChannel, idLevel, idTopic, idSkill, section.getIdSection() == null ? null : section.getIdSection() % 10000);
				}
			}
		}
	}

	/**
	 * Perform all downloads at a Topic level.  Note that all the ID's in this call are the short versions (not Full versions) 
	 * @param idChannel
	 * @param idLevel
	 * @param idTopic
	 */
	private void performDownloadTopic(Long idChannel, Long idLevel, Long idTopic) {
		// null check first
		if (idChannel == null || idLevel == null || idTopic == null) {
			log.error("performDownloadTopic called with Null Values: idChannel = '" + 
						idChannel == null ? "NULL" : idChannel + "', idLevel = '" + 
						idLevel == null ? "NULL" : idLevel + "', idTopic = '" + 
						idTopic == null ? "NULL" : idTopic + "'");
			return;
		}
		Long idTopicFull = idChannel * 1000000 + idLevel * 1000 + idTopic;
		// get all the skills in the topic
		SkillDao skillDao = this.applicationContext.getBean("skillDao", SkillDao.class);
		List<Skill> skillsInTopicList = skillDao.findSkillsForTopic(idTopicFull);
		if (skillsInTopicList != null && skillsInTopicList.size() > 0) {
			for (Skill skill : skillsInTopicList) {
				// call the download operation at a skill level
				performDownloadSkill(idChannel, idLevel, idTopic, skill.getIdSkill() == null ? null : skill.getIdSkill() % 100);
			}
		}
		return;
	}

	/**
	 * Perform all downloads at a Level level.  Note that all the ID's in this call are the short versions (not Full versions) 
	 * @param idChannel
	 * @param idLevel
	 */
	private void performDownloadLevel(Long idChannel, Long idLevel) {
		// null check first
		if (idChannel == null || idLevel == null) {
			log.error("performDownloadLevel called with Null Values: idChannel = '" + 
						idChannel == null ? "NULL" : idChannel + "', idLevel = '" + 
						idLevel == null ? "NULL" : idLevel + "'");
			return;
		}
		Long idLevelFull = idChannel * 1000 + idLevel;
		// get all the skills in the topic
		TopicDao topicDao = this.applicationContext.getBean("topicDao", TopicDao.class);
		List<Topic> topicsInLevelList = topicDao.findTopicsForLevel(idLevelFull);
		if (topicsInLevelList != null && topicsInLevelList.size() > 0) {
			for (Topic topic : topicsInLevelList) {
				// call the download operation at a Topic level
				performDownloadTopic(idChannel, idLevel, topic.getIdTopic() == null ? null : topic.getIdTopic() % 1000);
			}
		}
		return;
	}

	/**
	 * Perform all downloads at a Channel level. 
	 * @param idChannel
	 */
	private void performDownloadChannel(Long idChannel) {
		// null check first
		if (idChannel == null) {
			log.error("performDownloadChannel called with Null Values: idChannel = '" + 
						idChannel == null ? "NULL" : idChannel + "'");
			return;
		}
		// get all the skills in the topic
		LevelDao levelDao = this.applicationContext.getBean("levelDao", LevelDao.class);
		List<Level> levelsInChannelList = levelDao.findLevelsForChannel(idChannel);
		if (levelsInChannelList != null && levelsInChannelList.size() > 0) {
			for (Level level : levelsInChannelList) {
				// call the download operation at a Level level
				performDownloadLevel(idChannel, level.getIdLevel() == null ? null : level.getIdLevel() % 1000);
			}
		}
		return;
	}




	// Run runs the main upload functionality of the class
	private void executeUploadOperation(String[] args) {
		int processedFileCount = 0; 
		File inputFile = null;
		try {
			List<File> inputFilesToBeProcessed = this.getInputFiles(this.contentFilesPath);
			if (inputFilesToBeProcessed != null && inputFilesToBeProcessed.size() > 0) {
				for (File inputFileToBeProcessed : inputFilesToBeProcessed) {
					processedFileCount++;
					// parse the XML and return an instance of the AppConfig class
					inputFile = inputFileToBeProcessed;
					String fileString = FileUtils.readFileToString(inputFile, Charset.forName("UTF-8"));
					
					// log the file name
					log.info("Processing File: " + inputFile.getName());

					// Log the file itself at Debug level
					if (log.isDebugEnabled()) {
						log.debug("Read in: " + fileString);
					}
					
					// Process the file
					LoadSectionsResponse loadSectionsResponse = null;
//					if (process_files_in_service_class) {
						loadSectionsResponse = dataloaderService.saveSections(fileString, this.forcedLoginUsername, true, true);
//					} else {
//						String sectionsPayloadStringTrimmed = trimSoapHeadersWithSAX(fileString);
//						// LoadPracticesectionsRequest
//						if (sectionsPayloadStringTrimmed != null) {
//							// log the "trimmed" XML message
//							if (log.isDebugEnabled()) {
//								log.debug("sectionsPayload:");
//								Writer writer = new StringWriter();
//								getPayloadMarshaller(LoadSectionsRequest.class).marshal(sectionsPayloadStringTrimmed, writer);
//								log.debug("Response: " + writer.toString());
//							}
//							
//							// unmarshall to get the payload request object -
//							LoadSectionsRequest sectionsPayload = ((JAXBElement<LoadSectionsRequest>) getPayloadUnmarshaller(LoadSectionsRequest.class)
//									.unmarshal(new StringReader(sectionsPayloadStringTrimmed))).getValue();
//
//							// call the method to process the payload
//							loadSectionsResponse = dataloaderService.saveSections(sectionsPayload, this.forcedLoginUsername);
//						} else {
//							// create a blank response - indicating Another kind of error
//							loadSectionsResponse = new LoadSectionsResponse();
//							loadSectionsResponse.setStatus(-2);
//							loadSectionsResponse.setMessage("LoadSections Node not found in XML");
//						}
//						
//						if (loadSectionsResponse == null) {
//							// create a blank response
//							loadSectionsResponse = new LoadSectionsResponse();
//							loadSectionsResponse.setStatus(-1);
//							loadSectionsResponse.setMessage(DataloaderService.UPLOAD_UNKNOWN_FAILURE_MESSAGE);
//						}
//						
//					}

					// marshall to create the payload response object -
					Writer writer = new StringWriter();
					getPayloadMarshaller(LoadSectionsResponse.class).marshal(loadSectionsResponse, writer);
					// log the response message
					log.info("Processed: " + processedFileCount + ".) '" + inputFile.getName() + "'.  Response: " + writer.toString());

					// Check to see if we have exceeded the max files to process 
					if (this.maxFilesToProcess > 0 && processedFileCount > this.maxFilesToProcess) {
						log.info("Max Count of Files Processed.  Exiting.");
						break;
					}
					
					// now archive the file...
					if (loadSectionsResponse.getStatus() == DataloaderService.UPLOAD_SUCCESS_STATUS_CODE) {
						// move the file to Processed folder
						try {
							Path from = inputFileToBeProcessed.toPath(); //convert from File to Path
							Path to = Paths.get(this.processedContentFilesPathDirectory.getAbsolutePath(),inputFileToBeProcessed.getName()); //convert from String to Path
							Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
//							FileUtils.moveFileToDirectory(inputFileToBeProcessed, this.processedContentFilesPathDirectory, true);
						} catch (NullPointerException npe) {
							log.error("File Move Failed: " + inputFileToBeProcessed.getName() + " or " + this.processedContentFilesPath + " is null.", npe);
							System.exit(1);
						} catch (FileExistsException fee) {
							log.warn("Destination file '" + inputFileToBeProcessed.getName() + "' exists in Directory: '" + this.processedContentFilesPath + "'");
						} catch (IOException ioe) {
							log.warn(" IO error occurs moving the file '" + inputFileToBeProcessed.getName() + "' to Directory: '" + this.processedContentFilesPath + "'", ioe);
						}
					} else {
						// move the file to Failed folder
						try {
							Path from = inputFileToBeProcessed.toPath(); //convert from File to Path
							Path to = Paths.get(this.failedContentFilesPathDirectory.getAbsolutePath(), inputFileToBeProcessed.getName()); //convert from String to Path
							Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
//							FileUtils.moveFileToDirectory(inputFileToBeProcessed, this.failedContentFilesPathDirectory, true);
						} catch (NullPointerException npe) {
							log.error("File Move Failed: " + inputFileToBeProcessed.getName() + " or " + this.failedContentFilesPath + " is null.", npe);
							System.exit(1);
						} catch (FileExistsException fee) {
							log.warn("Destination file '" + inputFileToBeProcessed.getName() + "' exists in Directory: '" + this.failedContentFilesPath + "'");
						} catch (IOException ioe) {
							log.warn(" IO error occurs moving the file '" + inputFileToBeProcessed.getName() + "' to Directory: '" + this.failedContentFilesPath + "'", ioe);
						}
					}
				}
			
			}

		
		} catch (IOException ioe) {
			// if things went wrong...
			log.error("error Reading input file: " + inputFile);
			ioe.printStackTrace();
			// force quit
			System.exit(1);
		} catch (JAXBException jaxbe) {
			// if things went wrong...
			log.error("error parsing xml: " + inputFile);
			jaxbe.printStackTrace();
			// force quit
			System.exit(1);
		}
	}




	/**
	 * Create and return an Spring Application Context file.  Note that this honors any Overridden Spring config file.  
	 * Barring any overrides, it creates a context from the config file packaged in the JAR (and indicated by the 
	 * DEFAULT_APP_CONFIG_FILE) - "beans-jdbc.xml"
	 * @return
	 */
	private ApplicationContext getApplicationContext() {
		ApplicationContext applicationContext = null;
		if (this.overrideAppConfigFile != null && this.overrideAppConfigFile.trim().length() > 0) {
			applicationContext = new FileSystemXmlApplicationContext(this.overrideAppConfigFile);
		} else {
			applicationContext = new ClassPathXmlApplicationContext(DEFAULT_APP_CONFIG_FILE);
		}
		return applicationContext;
	}

	/**
	 * Returns a list of Input files to Process.  Note that this honors any maxSecondsToWaitBeforeProcessing and contentFileNamePrefix settings.
	 * @return
	 */
	private List<File> getInputFiles(String directoryName) {
		if (directoryName == null) {
			directoryName = this.contentFilesPath;
		}
		if (directoryName != null) {
			File[] files = null;
			AgeFileFilter ageFileFilter = null;
			if (this.maxSecondsToWaitBeforeProcessing != null) {
				long cutoff = System.currentTimeMillis() - (this.maxSecondsToWaitBeforeProcessing * 1000);
				ageFileFilter = new AgeFileFilter(cutoff);
			} else {
				ageFileFilter = new AgeFileFilter(System.currentTimeMillis());
			}
			WildcardFileFilter fileNameWildcardFilter = null;
			if (this.contentFileNamePrefix == null || this.contentFileNamePrefix.trim().length() == 0) {
				fileNameWildcardFilter = new WildcardFileFilter("*.xml");
			} else {
				fileNameWildcardFilter = new WildcardFileFilter(this.contentFileNamePrefix.trim() + "*.xml");
			}
			AndFileFilter andFileFilter = new AndFileFilter(fileNameWildcardFilter, ageFileFilter);
			Collection<File> fileCollection = FileUtils.listFiles(new File(directoryName), andFileFilter, null);
			if (fileCollection != null && fileCollection.size() > 0) {
				return Arrays.asList(FileUtils.convertFileCollectionToFileArray(fileCollection));
			}
		}
		return null;
	}

	/**
	 * Get a String property from the properties file. 
	 * @param propertyName
	 * @return
	 */
	private String getStringProperty(String propertyName) {
		return getProperty(propertyName);
	}

	/**
	 * Get a property from the properties file.  Note that this method will kick off the loadPropertyValues() method if its 
	 * not been performed yet.
	 * Also note that we first look for an override property value and then fall back on to the default setup property value.
	 * @param propertyName
	 * @return
	 */
	private Integer getIntProperty(String propertyName) {
		Integer responseInteger = null;
		String propertyValueString = getProperty(propertyName);
		if (propertyValueString != null) {
			try {
				responseInteger = new Integer(Integer.parseInt(propertyValueString.trim()));
			} catch (NumberFormatException nfe) {}
		}
		return responseInteger;
	}

	/**
	 * Get a property from the properties file.  Note that this method will kick off the loadPropertyValues() method if its 
	 * not been performed yet.
	 * Also note that we first look for an override property value and then fall back on to the default setup property value.
	 * @param propertyName
	 * @return
	 */
	private String getProperty(String propertyName) {
		if (this.override_properties == null && this.setup_properties == null) {
			this.loadPropertyValues();
		}
		String value = null;
		// read the value from overridden properties, if any
		if (this.override_properties != null && this.override_properties.containsKey(propertyName)) {
			value = this.override_properties.getProperty(propertyName);
		}
		// if value remains null, read the value from setup properties, if any
		if (value == null) {
			if (this.setup_properties != null && this.setup_properties.containsKey(propertyName)) {
				value = this.setup_properties.getProperty(propertyName);
			}
		}
		
		return value;
	}

	/**
	 * Load the Property Values for the Program.  Note that we load the Default System properties located somewhere 
	 * on the class path.  We also load any override properties file.
	 */
	private void loadPropertyValues() {
		try {
			// Load the Setup properties file on the JAR.  Complain if its missing. 
			this.setup_properties = new Properties();
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(SETUP_PROPERTIES_FILE_NAME);
			if (inputStream != null) {
				this.setup_properties.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + SETUP_PROPERTIES_FILE_NAME + "' not found in the classpath");
			}
 
			// Load any overridden properties. 
			String override_properties_file = System.getProperty(OVERRIDE_PROPERTIES_VARIABLE_NAME);
			if (override_properties_file != null && override_properties_file.trim().length() > 0 && override_properties_file.trim().endsWith(".properties")) {
				this.override_properties = new Properties();
				FileInputStream overridePropertiesFileInputStream = null;
				try {
					overridePropertiesFileInputStream = new FileInputStream(override_properties_file);
				} catch (FileNotFoundException fnfe){
					// print to logger
					log.error(OVERRIDE_PROPERTIES_VARIABLE_NAME + ": '" + override_properties_file + "' not found");
					log.error("Exiting...");
					// print to system console (may be the same...so will duplicate message)
					System.out.println(OVERRIDE_PROPERTIES_VARIABLE_NAME + ": '" + override_properties_file + "' not found");
					System.out.println("Exiting...");
					// exit.
					System.exit(-1);
				} 
				
				if (overridePropertiesFileInputStream != null) {
					this.override_properties.load(overridePropertiesFileInputStream);
				}
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e);
			System.exit(-1);
		} 
	}
	
//	/**
//	 * Private method used to get rid of SOAP envelope if one is present.
//	 * @param fileString
//	 * @return
//	 */
//	private static String trimSoapHeaders(String fileString) {
//		if (fileString != null && fileString.contains("http://schemas.xmlsoap.org/soap/envelope/")) {
//			int start = fileString.indexOf("<LoadSectionsRequest");
//			int end = fileString.indexOf("</LoadSectionsRequest>");
//			if (start > -1 && end > -1) {
//				fileString = fileString.substring(start, end + "</LoadSectionsRequest>".length());
//	        	if (log.isDebugEnabled()) {
//	        		log.debug(fileString);
//	        	}
//			} else {
//				log.error("LoadSectionsRequest Node not found in the file: " + fileString);
//			}
//		}
//		return fileString;
//	}
//	
//	/**
//	 * Note that this method takes a File containing a SOAP wrapper (in a string format) and tries to get the relevant 
//	 * LoadSectionsRequest node from it.  It returns a string version of the node.
//	 * @param fileString
//	 * @return
//	 */
//	private static String trimSoapHeadersWithSAX(String fileString) {
//		try {
//			if (fileString != null && fileString.contains("http://schemas.xmlsoap.org/soap/envelope/")) {
//		        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
////		        dbf.setNamespaceAware(true);
//		        Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(fileString)));
////		        doc.getN
//		        XPath xPath = XPathFactory.newInstance().newXPath();
//		        Node resultNode = (Node) xPath.evaluate("//LoadSectionsRequest", doc, XPathConstants.NODE);
//		        if (resultNode != null) {
//		        	fileString = nodeToString(resultNode);
//		        	if (log.isDebugEnabled()) {
//		        		log.debug(fileString);
//		        	}
//		        } else {
//					log.error("LoadSectionsRequest Node not found in the file: " + fileString);
//		        }
//			}
//		} catch (ParserConfigurationException pce) {
//			log.error("ParserConfigurationException: " + pce.getMessage());
//			pce.printStackTrace();
//			System.exit(1);			
//		} catch (SAXException se) {
//			log.error("SAXException: " + se.getMessage());
//			se.printStackTrace();
//			System.exit(1);			
//		} catch (XPathExpressionException xpe) {
//			log.error("XPathExpressionException: " + xpe.getMessage());
//			xpe.printStackTrace();
//			System.exit(1);			
//		} catch (IOException ioe) {
//			log.error("IOException: " + ioe.getMessage());
//			ioe.printStackTrace();
//			System.exit(1);			
//		}
//		return fileString;
//	}
//
//	/**
//	 * private method to convert a Node to an XML String
//	 * @param node
//	 * @return
//	 */
//    private static String nodeToString(Node node) {
//    	try {
//            StringWriter buf = new StringWriter();
//            Transformer xform = TransformerFactory.newInstance().newTransformer();
//            xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//            xform.transform(new DOMSource(node), new StreamResult(buf));
//            return(buf.toString());
//    	} catch (TransformerException te) {
//    		log.error("TransformerException: " + te.getMessage());
//			te.printStackTrace();
//			System.exit(1);			
//    	}
//    	return null;
//    }
}
