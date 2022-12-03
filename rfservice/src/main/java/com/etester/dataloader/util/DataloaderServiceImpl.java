package com.etester.dataloader.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.etester.data.domain.content.DerivedSectionQuestion;
import com.etester.data.domain.content.additional.wordlist.Cword;
import com.etester.data.domain.content.additional.wordlist.CwordDao;
import com.etester.data.domain.content.additional.wordlist.Cworddef;
import com.etester.data.domain.content.additional.wordlist.Cwordusage;
import com.etester.data.domain.content.additional.wordlist.WlPassage;
import com.etester.data.domain.content.additional.wordlist.WlWord;
import com.etester.data.domain.content.additional.wordlist.WlWordlist;
import com.etester.data.domain.content.additional.wordlist.WlWordlistDao;
import com.etester.data.domain.content.core.Answer;
import com.etester.data.domain.content.core.Channel;
import com.etester.data.domain.content.core.ChannelDao;
import com.etester.data.domain.content.core.Grade;
import com.etester.data.domain.content.core.GradeDao;
import com.etester.data.domain.content.core.Level;
import com.etester.data.domain.content.core.LevelDao;
import com.etester.data.domain.content.core.Question;
import com.etester.data.domain.content.core.Questionset;
import com.etester.data.domain.content.core.Section;
import com.etester.data.domain.content.core.SectionDao;
import com.etester.data.domain.content.core.Skill;
import com.etester.data.domain.content.core.SkillDao;
import com.etester.data.domain.content.core.Topic;
import com.etester.data.domain.content.core.TopicDao;
import com.etester.data.domain.test.JdbcDaoStaticHelper;
import com.etester.data.domain.test.Test;
import com.etester.data.domain.test.TestConstants;
import com.etester.data.domain.test.TestConstants.TestsectionArtifactType;
import com.etester.data.domain.test.TestDao;
import com.etester.data.domain.test.Testsection;
import com.etester.data.domain.test.TestsectionInstructions;
import com.etester.data.domain.test.TestsectionInstructionsDao;
import com.etester.data.domain.test.Testsegment;
import com.etester.data.domain.test.Testsynopsislink;
import com.etester.data.domain.user.Gradeskill;
import com.etester.data.domain.user.User;
import com.etester.data.domain.user.UserDao;
import com.etester.data.domain.util.QuestionCompareConstants;
import com.etester.data.domain.util.UpdateStatusBean;
import com.rulefree.rfloaddataschema.AnswerArrayType;
import com.rulefree.rfloaddataschema.AnswerType;
import com.rulefree.rfloaddataschema.CompareCriteriaType;
import com.rulefree.rfloaddataschema.CoreLevelReferenceType;
import com.rulefree.rfloaddataschema.CoreSkillReferenceType;
import com.rulefree.rfloaddataschema.CoreTopicReferenceType;
import com.rulefree.rfloaddataschema.DecimalCompareType;
import com.rulefree.rfloaddataschema.DefinitionArrayType;
import com.rulefree.rfloaddataschema.DerivedSectionQuestionArrayType;
import com.rulefree.rfloaddataschema.DerivedSectionQuestionType;
import com.rulefree.rfloaddataschema.ExamtrackType;
import com.rulefree.rfloaddataschema.GetInstructionsRequest;
import com.rulefree.rfloaddataschema.GetInstructionsResponse;
import com.rulefree.rfloaddataschema.GetSectionsRequest;
import com.rulefree.rfloaddataschema.GetSectionsResponse;
import com.rulefree.rfloaddataschema.GetTestsRequest;
import com.rulefree.rfloaddataschema.GetTestsResponse;
import com.rulefree.rfloaddataschema.GetWordsRequest;
import com.rulefree.rfloaddataschema.GetWordsResponse;
import com.rulefree.rfloaddataschema.GradeType;
import com.rulefree.rfloaddataschema.IntegerCompareType;
import com.rulefree.rfloaddataschema.LevelArrayType;
import com.rulefree.rfloaddataschema.LevelChoiceType;
import com.rulefree.rfloaddataschema.LevelOnlyType;
import com.rulefree.rfloaddataschema.LevelType;
import com.rulefree.rfloaddataschema.LoadInstructionsRequest;
import com.rulefree.rfloaddataschema.LoadInstructionsResponse;
import com.rulefree.rfloaddataschema.LoadLevelsOnlyRequest;
import com.rulefree.rfloaddataschema.LoadLevelsOnlyResponse;
import com.rulefree.rfloaddataschema.LoadLevelsRequest;
import com.rulefree.rfloaddataschema.LoadLevelsResponse;
import com.rulefree.rfloaddataschema.LoadSectionsRequest;
import com.rulefree.rfloaddataschema.LoadSectionsResponse;
import com.rulefree.rfloaddataschema.LoadSkillsOnlyRequest;
import com.rulefree.rfloaddataschema.LoadSkillsOnlyResponse;
import com.rulefree.rfloaddataschema.LoadSkillsRequest;
import com.rulefree.rfloaddataschema.LoadSkillsResponse;
import com.rulefree.rfloaddataschema.LoadTopicsOnlyRequest;
import com.rulefree.rfloaddataschema.LoadTopicsOnlyResponse;
import com.rulefree.rfloaddataschema.LoadTopicsRequest;
import com.rulefree.rfloaddataschema.LoadTopicsResponse;
import com.rulefree.rfloaddataschema.LoadWordlistsRequest;
import com.rulefree.rfloaddataschema.LoadWordlistsResponse;
import com.rulefree.rfloaddataschema.PartsOfSpeechKind;
import com.rulefree.rfloaddataschema.PassageType;
import com.rulefree.rfloaddataschema.PictureType;
import com.rulefree.rfloaddataschema.QuestionArrayType;
import com.rulefree.rfloaddataschema.QuestionChoiceArrayType;
import com.rulefree.rfloaddataschema.QuestionChoiceType;
import com.rulefree.rfloaddataschema.QuestionKind;
import com.rulefree.rfloaddataschema.QuestionSetType;
import com.rulefree.rfloaddataschema.QuestionType;
import com.rulefree.rfloaddataschema.ReferenceInfoType;
import com.rulefree.rfloaddataschema.SaveTestsRequest;
import com.rulefree.rfloaddataschema.SaveTestsResponse;
import com.rulefree.rfloaddataschema.SectionArrayType;
import com.rulefree.rfloaddataschema.SectionType;
import com.rulefree.rfloaddataschema.SkillArrayType;
import com.rulefree.rfloaddataschema.SkillChoiceType;
import com.rulefree.rfloaddataschema.SkillOnlyType;
import com.rulefree.rfloaddataschema.SkillReferenceArrayType;
import com.rulefree.rfloaddataschema.SkillType;
import com.rulefree.rfloaddataschema.SynopsislinktypeType;
import com.rulefree.rfloaddataschema.TestAccessLevelType;
import com.rulefree.rfloaddataschema.TestArrayType;
import com.rulefree.rfloaddataschema.TestType;
import com.rulefree.rfloaddataschema.TestsByChannelIdAndProviderNameType;
import com.rulefree.rfloaddataschema.TestsByChannelIdType;
import com.rulefree.rfloaddataschema.TestsByProviderNameType;
import com.rulefree.rfloaddataschema.TestsectionArrayType;
import com.rulefree.rfloaddataschema.TestsectionType;
import com.rulefree.rfloaddataschema.TestsegmentArrayType;
import com.rulefree.rfloaddataschema.TestsegmentType;
import com.rulefree.rfloaddataschema.TestsynopsislinkArrayType;
import com.rulefree.rfloaddataschema.TestsynopsislinkType;
import com.rulefree.rfloaddataschema.TesttypeType;
import com.rulefree.rfloaddataschema.TextCompareType;
import com.rulefree.rfloaddataschema.TextContentType;
import com.rulefree.rfloaddataschema.TopicArrayType;
import com.rulefree.rfloaddataschema.TopicChoiceType;
import com.rulefree.rfloaddataschema.TopicOnlyType;
import com.rulefree.rfloaddataschema.TopicType;
import com.rulefree.rfloaddataschema.UpdateWordsRequest;
import com.rulefree.rfloaddataschema.UpdateWordsResponse;
import com.rulefree.rfloaddataschema.UsageArrayType;
import com.rulefree.rfloaddataschema.WordArrayType;
import com.rulefree.rfloaddataschema.WordDefinitionType;
import com.rulefree.rfloaddataschema.WordNameType;
import com.rulefree.rfloaddataschema.WordType;
import com.rulefree.rfloaddataschema.WordUsageType;
import com.rulefree.rfloaddataschema.WordlistArrayType;
import com.rulefree.rfloaddataschema.WordlistType;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DataloaderServiceImpl implements DataloaderService {

	private static final Long CHANNEL_ADMINISTRATOR = 100l;
	private static final Long SYSTEM_ADMINISTRATOR = 0l;
	private static final String DEFAULT_CONTENT_LOADER_PROVIDER = "channeladmin";
	
	// roles in eTester
	private static final String USER_ROLE_LOGIN = "ROLE_LOGIN";
	private static final String USER_ROLE_USER = "ROLE_USER";
	private static final String USER_ROLE_PROVIDER = "ROLE_PROVIDER";
	private static final String USER_ROLE_ADMIN = "ROLE_ADMIN";
	private static final String USER_ROLE_SYSTEM_ADMIN = "ROLE_SYSTEM";
	
	private static final Integer ANSWER_COMPARE_TYPE_STRING = 1;
	private static final Integer ANSWER_COMPARE_TYPE_INTEGER = 2;
	private static final Integer ANSWER_COMPARE_TYPE_DECIMAL = 2;
	
	
	private static final String UPLOAD_PATH = "C:\\rffiles";
	
	private static final String MULTI_LINE_INPUT_SEPERATOR = "$$$$";
	
//	@Autowired
	private SectionDao sectionDao;
	private UserDao userDao;
	private ChannelDao channelDao;
	private LevelDao levelDao;
	private TopicDao topicDao;
	private SkillDao skillDao;
	private GradeDao gradeDao;
	private WlWordlistDao wordlistDao;
	private CwordDao cwordDao;
	private TestDao testDao;
	private TestsectionInstructionsDao testsectionInstructionsDao;
	
	public DataloaderServiceImpl (UserDao userDao, ChannelDao channelDao, LevelDao levelDao, TopicDao topicDao, SkillDao skillDao, 
								GradeDao gradeDao, SectionDao sectionDao, CwordDao cwordDao, WlWordlistDao wordlistDao, TestDao testDao, TestsectionInstructionsDao testsectionInstructionsDao) {
		this.userDao = userDao;
		this.sectionDao = sectionDao;
		this.channelDao = channelDao;
		this.levelDao = levelDao;
		this.topicDao = topicDao;
		this.skillDao = skillDao;
		this.gradeDao = gradeDao;
		this.cwordDao = cwordDao;
		this.wordlistDao = wordlistDao;
		this.testDao = testDao;
		this.testsectionInstructionsDao = testsectionInstructionsDao;
//		init();
	}

	private static List<Level> ALL_LEVELS = new ArrayList<Level>();

	// cache the lookup info to cross check validity of data entered 
	private static List <String> ALL_GRADES = new ArrayList<String>();

	private void init() {
		
		if (ALL_GRADES.size() == 0) {
			List<Grade> grades = gradeDao.findAllGrades();
			if (grades != null && grades.size() > 0) {
				for (Grade grade : grades) {
					ALL_GRADES.add(grade.getGradeName());
				}
			}
		}
		// check to see if the data is already loaded
		if (ALL_LEVELS.size() > 0 && ALL_GRADES.size() > 0) {
			return;
		}
		
		ALL_LEVELS = levelDao.findAllLevels();
		if (ALL_LEVELS != null && ALL_LEVELS.size() > 0) {
			for (Level level : ALL_LEVELS) {
				level.setTopics(topicDao.findTopicsForLevel(level.getIdLevel()));
			}
		}
		
	}
	/**
	 * Main method.
	 * @param args
	 */
	public static void main(String[] args) {
	}

	/**
	 * Private method used to get rid of SOAP envelope if one is present.
	 * @param fileString
	 * @return
	 */
	private static String trimSoapHeaders(String fileString) {
		if (fileString != null && fileString.contains("http://schemas.xmlsoap.org/soap/envelope/")) {
			int start = fileString.indexOf("<LoadSectionsRequest");
			int end = fileString.indexOf("</LoadSectionsRequest>");
			if (start > -1 && end > -1) {
				fileString = fileString.substring(start, end + "</LoadSectionsRequest>".length());
	        	if (log.isDebugEnabled()) {
	        		log.debug(fileString);
	        	}
			} else {
				log.error("LoadSectionsRequest Node not found in the file: " + fileString);
			}
		}
		return fileString;
	}
	
	/**
	 * Note that this method takes a File containing a SOAP wrapper (in a string format) and tries to get the relevant 
	 * LoadSectionsRequest node from it.  It returns a string version of the node.
	 * @param fileString
	 * @return
	 */
	private static String trimSaveSectionsSoapHeadersWithSAX(String fileString) {
		if (fileString != null && fileString.contains("http://schemas.xmlsoap.org/soap/envelope/")) {
			return getSubNodeWithSAX(fileString, "//LoadSectionsRequest");
		} else {
			return fileString;
		}
	}
	/**
	 * Returns a Node represented by the xpath returned in a substring
	 * @param fileString
	 * @param xPathString
	 * @return
	 */
	private static String getSubNodeWithSAX(String fileString, String xPathString) {
		try {
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//	        dbf.setNamespaceAware(true);
	        Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(fileString)));
//	        doc.getN
	        XPath xPath = XPathFactory.newInstance().newXPath();
	        Node resultNode = (Node) xPath.evaluate(xPathString, doc, XPathConstants.NODE);
	        if (resultNode != null) {
	        	fileString = nodeToString(resultNode);
	        	if (log.isDebugEnabled()) {
	        		log.debug(fileString);
	        	}
	        } else {
				log.error("'" + xPathString + "' Node not found in the file: " + fileString);
	        }
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException: " + pce.getMessage());
			pce.printStackTrace();
			System.exit(1);			
		} catch (SAXException se) {
			log.error("SAXException: " + se.getMessage());
			se.printStackTrace();
			System.exit(1);			
		} catch (XPathExpressionException xpe) {
			log.error("XPathExpressionException: " + xpe.getMessage());
			xpe.printStackTrace();
			System.exit(1);			
		} catch (IOException ioe) {
			log.error("IOException: " + ioe.getMessage());
			ioe.printStackTrace();
			System.exit(1);			
		}
		return fileString;
	}
	

	/**
	 * private method to convert a Node to an XML String
	 * @param node
	 * @return
	 */
    private static String nodeToString(Node node) {
    	try {
            StringWriter buf = new StringWriter();
            Transformer xform = TransformerFactory.newInstance().newTransformer();
            xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            xform.transform(new DOMSource(node), new StreamResult(buf));
            return(buf.toString());
    	} catch (TransformerException te) {
    		log.error("TransformerException: " + te.getMessage());
			te.printStackTrace();
			System.exit(1);			
    	}
    	return null;
    }
    
	/**
	 * Returns an Unmarshaller object corresponding to the class.
	 * @param classObject
	 * @return
	 * @throws JAXBException
	 */
	@SuppressWarnings("rawtypes")
	/* static */public Unmarshaller getPayloadUnmarshaller(Class classObject) throws JAXBException {
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
	public Marshaller getPayloadMarshaller(Class classObject) throws JAXBException {
		JAXBContext context = JAXBContext
				.newInstance(classObject);
		Marshaller m = context.createMarshaller();
		return m;
	}

//    /**
//     * Returns an Unmarshaller corresponding to the 
//     * @return
//     * @throws JAXBException
//     */
//	private Unmarshaller getPayloadUnmarshaller() throws JAXBException {
//		JAXBContext context = JAXBContext
//				.newInstance(LoadSectionsRequest.class);
//		Unmarshaller um = context.createUnmarshaller();
//		return um;
//	}
//
//	private Marshaller getPayloadMarshaller() throws JAXBException {
//		JAXBContext context = JAXBContext
//				.newInstance(LoadSectionsRequest.class);
//		Marshaller m = context.createMarshaller();
//		return m;
//	}

	@Override
	public String saveSectionsWithStringResponse(String sectionsPayloadString,
			String forcedLoginUsername, boolean validateOnlyOverride, boolean reloadOverride) throws JAXBException {
		LoadSectionsResponse response = saveSections(sectionsPayloadString, forcedLoginUsername, validateOnlyOverride, reloadOverride);
		// marshall to create the payload response object -
		Writer writer = new StringWriter();
		Marshaller marshaller = getPayloadMarshaller(LoadSectionsResponse.class);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		getPayloadMarshaller(LoadSectionsResponse.class).marshal(response, writer);

		return writer.toString();
	}
	
	@Override
	public LoadSectionsResponse saveSections(String sectionsPayloadString, String forcedLoginUsername, boolean validateOnlyOverride, boolean reloadOverride)
			throws JAXBException {
		LoadSectionsResponse response = null;
		// see if the message is of the "GetSectionsResponse" veriety (indicating a previously downloaded file).  If 
		// so convert it to a "LoadSectionsRequest"
		if (sectionsPayloadString.contains("GetSectionsResponse")) {
			sectionsPayloadString = convertGetSectionsResponseToLoadSectionsRequest(sectionsPayloadString);
//			sectionsPayloadStringTrimmed = sectionsPayloadStringTrimmed.replaceAll("GetSectionsResponse", "LoadSectionsRequest");
		}

		// get rid of any double escaped space
		if (sectionsPayloadString.contains("&amp;nbsp;")) {
			sectionsPayloadString = sectionsPayloadString.replaceAll("&amp;nbsp;", "&nbsp;");
		}

		// check and get rid of any SOAP envelope.
		String sectionsPayloadStringTrimmed = trimSaveSectionsSoapHeadersWithSAX(sectionsPayloadString);
		// LoadPracticesectionsRequest
		if (sectionsPayloadStringTrimmed != null) {

			// log the "trimmed" XML message
			if (log.isDebugEnabled()) {
				log.debug("sectionsPayload:");
				Writer writer = new StringWriter();
				getPayloadMarshaller(LoadSectionsRequest.class).marshal(sectionsPayloadStringTrimmed, writer);
				log.debug("Response: " + writer.toString());
			}
			
			// unmarshall to get the payload request object -
			LoadSectionsRequest sectionsPayload = ((JAXBElement<LoadSectionsRequest>) getPayloadUnmarshaller(LoadSectionsRequest.class)
					.unmarshal(new StringReader(sectionsPayloadStringTrimmed))).getValue();

			// call the method to process the payload
			response = saveSections(sectionsPayload, forcedLoginUsername, validateOnlyOverride, reloadOverride);
		} else {
			// create a blank response - indicating Another kind of error
			response = new LoadSectionsResponse();
			response.setStatus(-2);
			response.setMessage("LoadSections Node not found in XML");
		}

		if (response == null) {
			// create a blank response
			response = new LoadSectionsResponse();
			response.setStatus(-1);
			response.setMessage(UPLOAD_UNKNOWN_FAILURE_MESSAGE);
		}
		return response;
	}

	// This Converts an Downloaded file to a Uploadable file
	private String convertGetSectionsResponseToLoadSectionsRequest(
			String fileString) {
		if (fileString == null) {
			return null;
		}

//		try {
//			if (fileString != null && fileString.contains("</GetSectionsResponse>")) {
//		        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
////		        dbf.setNamespaceAware(true);
//		        Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(fileString)));
//		        NodeList nodes = doc.getElementsByTagName("GetSectionsResponse");
//		        for (int i = 0; i < nodes.getLength(); i++) {
//		        	doc.renameNode(nodes.item(i), null, "LoadSectionsRequest");
//		        }
//		        return nodeToString(doc);
//			}
//		} catch (ParserConfigurationException pce) {
//			logger.error("ParserConfigurationException: " + pce.getMessage());
//			pce.printStackTrace();
//			System.exit(1);			
//		} catch (SAXException se) {
//			logger.error("SAXException: " + se.getMessage());
//			se.printStackTrace();
//			System.exit(1);			
//		} catch (IOException ioe) {
//			logger.error("IOException: " + ioe.getMessage());
//			ioe.printStackTrace();
//			System.exit(1);			
//		}
//		return fileString;

		return fileString.replaceAll("GetSectionsResponse", "LoadSectionsRequest");
	}
	
	@Override
	public LoadSectionsResponse saveSections(
			LoadSectionsRequest sectionsPayload/*, MessageContext messageContext*/) {
		return saveSections(sectionsPayload, JdbcDaoStaticHelper.getCurrentUserName(), false, false);
	}
	
	@Override
	public LoadSectionsResponse saveSections(
			LoadSectionsRequest sectionsPayload, String forcedLoginUsername, boolean validateOnlyOverride, boolean reloadOverride) {
		
		// the following function performs the check to ensure a channel is capable of ingesting content.  For that, a channel should be marked "editable".
		ensureChannelIsEditable(sectionsPayload.getIdSystem() == null ? null : sectionsPayload.getIdSystem().longValue());
		
		// the following function performs all the security and validation checks to load data. 
		Long overrideProviderId = validateAndReturnProviderId(sectionsPayload.getIdSystem() == null ? null : sectionsPayload.getIdSystem().longValue(), 
																sectionsPayload.getIdProvider() == null ? null : sectionsPayload.getIdProvider().longValue(),
																forcedLoginUsername);
		
		// first get a list of practice sections - totally filled with questions and answers
		List<Section> sections = parseSectionsPayloadIntoDamainData(sectionsPayload, overrideProviderId);
		
		// log the created sections
//		logger.debug(sections);
		
		// save the data to the database
		List<UpdateStatusBean> updateStatuses = null;
		boolean operationSuccess = true;
		try {
			if (validateOnlyOverride || !sectionsPayload.isValidateOnly()) {
				updateStatuses = sectionDao.insertBatch(sections, reloadOverride || sectionsPayload.isReload());
				// see if any of the statuses are not 0.  In which case set the response as FAIL
				if (updateStatuses != null && updateStatuses.size() > 0) {
					for (UpdateStatusBean updateStatus : updateStatuses) {
						if (updateStatus.hasFailed()) {
							operationSuccess = false;
							break;
						}
					}
				}
			}
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			operationSuccess = false;
			throw new RfDataloadException(e.getMessage(), e);
		}

		// create the response
		LoadSectionsResponse response = new LoadSectionsResponse();
		if (operationSuccess) {
			response.setStatus(UPLOAD_SUCCESS_STATUS_CODE);
			response.setMessage(UPLOAD_SUCCESS_MESSAGE + (updateStatuses == null ? "" : "\n" + updateStatuses.toString()));
		} else {
			response.setStatus(UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			response.setMessage(UPLOAD_UNKNOWN_FAILURE_MESSAGE + (updateStatuses == null ? "" : "\n" + updateStatuses.toString()));
		}

		return response;
	}
	
	// This functions ensures there is a valid channel id.  And that particular channel is marked "editable" (editable=1)
	private boolean ensureChannelIsEditable(Long idChannel) {
		if (idChannel == null || idChannel == 0l) {
			throw new RfDataloadException("Invaid Channel ID.  Exiting immediately.");
		}
		Channel channel = channelDao.findByChannelId(idChannel);
		if (channel == null) {
			throw new RfDataloadException("Invaid Channel ID: '" + idChannel + "'.  Exiting immediately.");
		}
		if (channel.getEditable() == null || channel.getEditable() != 1l) {
			throw new RfDataloadException("Channel ID: '" + idChannel + "' is NOT Editable.  Please contact your Channel Administrator");
		}
		return true;
	}

	/**
	 * Queries for the current logged in username and calls the same function with username. 
	 * @param idChannel
	 * @param idProvider
	 * @return
	 */
	private Long validateAndReturnProviderId(Long idChannel, Long idProvider) {
		return validateAndReturnProviderId(idChannel, idProvider, JdbcDaoStaticHelper.getCurrentUserName());
	}

	/**
	 * The following function performs all the security and validation checks to load data. 
	 * @param idChannel
	 * @param idProvider
	 * @param userName
	 * @return
	 */
	private Long validateAndReturnProviderId(Long idChannel, Long idProvider, String userName) {
		// check to see if the user is a Channel Admin
		User user = userDao.findByUsername(userName);
		// user has to be logged in
		if (user == null) {
			throw new RfDataloadException("Invaid User ID.  Exiting immediately.");
		}

		// logged in user has to be an administrator or provider
		if (!user.getAuthorities().contains(USER_ROLE_ADMIN) && !user.getAuthorities().contains(USER_ROLE_PROVIDER)) {
			throw new RfDataloadException("Only Providers and Channel Administrators can load data.  The logged in user is neither.");
		}

		// validate provider has channel access to upload content (for non-admin user)
		if (!user.getAuthorities().contains(USER_ROLE_ADMIN)) {
			if (!channelDao.providerHasChannelUploadPermission(idChannel, user.getIdUser())) {
				throw new RfDataloadException("Logged in Provider '" + user.getIdUser() + "' does not have permissions to Load content into Channel '" + idChannel + "'");
			}
		}
		
		if (idProvider == null)  {
			if (user.getAuthorities().contains(USER_ROLE_ADMIN)) {
				idProvider = 0l; 
			} else {
				throw new RfDataloadException("Only Channel Administrators can load Content without a Provider ID.");
			}
		} else {
			if (user.getAuthorities().contains(USER_ROLE_ADMIN)) {
				// make sure its a valid provider
				User proxyUser =  userDao.findByUserId(idProvider);
				if (proxyUser == null || !proxyUser.getAuthorities().contains(USER_ROLE_PROVIDER)) {
					throw new RfDataloadException("Content cannot be loaded for a Non-Provider.");
				}
				
			} else {
				if (!idProvider.equals(user.getIdUser())) {
					throw new RfDataloadException("Provider '" + user.getIdUser() + "' can Load Custom Content for his/her account only.");
				}
			}
		}
		return idProvider;
	}

	@Override
	public LoadSkillsResponse saveSkills(LoadSkillsRequest skillsPayload) {
		// the following function does all the security and validation checks to load data. 
		Long overrideProviderId = validateAndReturnProviderId(skillsPayload.getIdSystem() == null ? null : skillsPayload.getIdSystem().longValue(), 
																skillsPayload.getIdProvider() == null ? null : skillsPayload.getIdProvider().longValue());
		// first get a list of skills -
		List<Skill> skills = parseSkillsPayloadIntoDomainData(skillsPayload, overrideProviderId);
		// log the created skills
		log.debug(skills.toString());
		
		// save the data to the database
		try {
			if (skillsPayload.isValidateOnly() == false) {
				// not allowed for now
				// skillDao.insertBatch(skills, skillsPayload.isReload());
				throw new org.springframework.dao.DataIntegrityViolationException("The service to Load Skills has been Turned Off!");
			}
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			throw new RfDataloadException(e.getMessage(), e);
		}

		// create the response
		LoadSkillsResponse response = new LoadSkillsResponse();
		response.setStatus(UPLOAD_SUCCESS_STATUS_CODE);
		response.setMessage(UPLOAD_SUCCESS_MESSAGE);

		return response;
	}

	@Override
	public String saveSkills(String skillsPayloadString) throws JAXBException {
		// unmarshall to get the payload request object -
		// LoadSkillsRequest
		StringReader skillsPayloadStringReader = new StringReader(
				skillsPayloadString);
		LoadSkillsRequest skillsPayload = (LoadSkillsRequest) getPayloadUnmarshaller(LoadSkillsRequest.class)
				.unmarshal(skillsPayloadStringReader);

		// call the method to process the payload
		LoadSkillsResponse response = saveSkills(skillsPayload);

		// marshall to create the payload response object -
		Writer writer = new StringWriter();
		getPayloadMarshaller(LoadSkillsResponse.class).marshal(response, writer);

		return writer.toString();
	}

	@Override
	public LoadSkillsOnlyResponse saveSkillsOnly(LoadSkillsOnlyRequest skillsOnlyPayload) {
		// the following function does all the security and validation checks to load data. 
		Long overrideProviderId = validateAndReturnProviderId(skillsOnlyPayload.getIdSystem() == null ? null : skillsOnlyPayload.getIdSystem().longValue(), 
																skillsOnlyPayload.getIdProvider() == null ? null : skillsOnlyPayload.getIdProvider().longValue());
		// first get a list of skills -
		List<Skill> skillsOnly = parseSkillsOnlyPayloadIntoDomainData(skillsOnlyPayload, overrideProviderId);
		// log the created skills
		log.debug(skillsOnly.toString());
		
		// save the data to the database
		try {
			if (skillsOnlyPayload.isValidateOnly() == false) {
				// use "false" for reload - always
				skillDao.insertBatch(skillsOnly, false);
//				throw new org.springframework.dao.DataIntegrityViolationException("The service to Load Skills Only has been Turned Off!");
			}
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			throw new RfDataloadException(e.getMessage(), e);
		}

		// create the response
		LoadSkillsOnlyResponse response = new LoadSkillsOnlyResponse();
		response.setStatus(UPLOAD_SUCCESS_STATUS_CODE);
		response.setMessage(UPLOAD_SUCCESS_MESSAGE);

		return response;
	}

	@Override
	public LoadTopicsResponse saveTopics(LoadTopicsRequest topicsPayload) {
		// the following function does all the security and validation checks to load data. 
		Long overrideProviderId = validateAndReturnProviderId(topicsPayload.getIdSystem() == null ? null : topicsPayload.getIdSystem().longValue(), 
																topicsPayload.getIdProvider() == null ? null : topicsPayload.getIdProvider().longValue());
		// first get a list of topics -
		List<Topic> topics = parseTopicsPayloadIntoDomainData(topicsPayload, overrideProviderId);
		// log the created topics
		log.debug(topics.toString());
		
		// save the data to the database
		try {
			if (topicsPayload.isValidateOnly() == false) {
				// not allowed for now
				// topicDao.insertBatch(topics, topicsPayload.isReload());
				throw new org.springframework.dao.DataIntegrityViolationException("The service to Load Topics has been Turned Off!");
			}
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			throw new RfDataloadException(e.getMessage(), e);
		}

		// create the response
		LoadTopicsResponse response = new LoadTopicsResponse();
		response.setStatus(UPLOAD_SUCCESS_STATUS_CODE);
		response.setMessage(UPLOAD_SUCCESS_MESSAGE);

		return response;
	}
	
	@Override
	public String saveTopics(String topicsPayloadString) throws JAXBException {
		// unmarshall to get the payload request object -
		// LoadTopicsRequest
		StringReader topicsPayloadStringReader = new StringReader(
				topicsPayloadString);
		LoadTopicsRequest topicsPayload = (LoadTopicsRequest) getPayloadUnmarshaller(LoadTopicsRequest.class)
				.unmarshal(topicsPayloadStringReader);

		// call the method to process the payload
		LoadTopicsResponse response = saveTopics(topicsPayload);

		// marshall to create the payload response object -
		Writer writer = new StringWriter();
		getPayloadMarshaller(LoadTopicsResponse.class).marshal(response, writer);

		return writer.toString();
	}

	@Override
	public LoadTopicsOnlyResponse saveTopicsOnly(LoadTopicsOnlyRequest topicsOnlyPayload) {
		// the following function does all the security and validation checks to load data. 
		Long overrideProviderId = validateAndReturnProviderId(topicsOnlyPayload.getIdSystem() == null ? null : topicsOnlyPayload.getIdSystem().longValue(), 
																topicsOnlyPayload.getIdProvider() == null ? null : topicsOnlyPayload.getIdProvider().longValue());
		// first get a list of topics -
		List<Topic> topics = parseTopicsOnlyPayloadIntoDomainData(topicsOnlyPayload, overrideProviderId);
		// log the created topics
		log.debug(topics.toString());
		
		// save the data to the database
		try {
			if (topicsOnlyPayload.isValidateOnly() == false) {
				if (CHANNEL_ADMINISTRATOR.equals(overrideProviderId) || SYSTEM_ADMINISTRATOR.equals(overrideProviderId)) {
					topicDao.insertBatch(topics, true);
				} else {
					throw new RfDataloadException("Not a Channel Administrator.  The service to Load Topics Only has been Turned Off for this User.");
				}
			} 
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			throw new RfDataloadException(e.getMessage(), e);
		}

		// create the response
		LoadTopicsOnlyResponse response = new LoadTopicsOnlyResponse();
		response.setStatus(UPLOAD_SUCCESS_STATUS_CODE);
		response.setMessage(UPLOAD_SUCCESS_MESSAGE);

		return response;
	}
	
	@Override
	public LoadLevelsResponse saveLevels(LoadLevelsRequest levelsPayload) {
		// the following function does all the security and validation checks to load data. 
		Long overrideProviderId = validateAndReturnProviderId(levelsPayload.getIdSystem() == null ? null : levelsPayload.getIdSystem().longValue(), 
																levelsPayload.getIdProvider() == null ? null : levelsPayload.getIdProvider().longValue());
		// first get a list of level -
		List<Level> levels = parseLevelsPayloadIntoDomainData(levelsPayload,overrideProviderId);
		// log the created levels
		log.debug(levels.toString());
		
		// save the data to the database
		try {
			if (levelsPayload.isValidateOnly() == false) {
				// not allowed for now
				throw new org.springframework.dao.DataIntegrityViolationException("The service to Load Levels has been Turned Off!");
			}
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			throw new RfDataloadException(e.getMessage(), e);
		}

		// create the response
		LoadLevelsResponse response = new LoadLevelsResponse();
		response.setStatus(UPLOAD_SUCCESS_STATUS_CODE);
		response.setMessage(UPLOAD_SUCCESS_MESSAGE);

		return response;
	}
	
	@Override
	public String saveLevels(String levelsPayloadString) throws JAXBException {
		// unmarshall to get the payload request object -
		// LoadLevelsRequest
		StringReader levelsPayloadStringReader = new StringReader(
				levelsPayloadString);
		LoadLevelsRequest levelsPayload = (LoadLevelsRequest) getPayloadUnmarshaller(LoadLevelsRequest.class)
				.unmarshal(levelsPayloadStringReader);

		// call the method to process the payload
		LoadLevelsResponse response = saveLevels(levelsPayload);

		// marshall to create the payload response object -
		Writer writer = new StringWriter();
		getPayloadMarshaller(LoadLevelsResponse.class).marshal(response, writer);

		return writer.toString();
	}

	@Override
	public LoadLevelsOnlyResponse saveLevelsOnly(LoadLevelsOnlyRequest levelsOnlyPayload) {
		// the following function does all the security and validation checks to load data. 
		Long overrideProviderId = validateAndReturnProviderId(levelsOnlyPayload.getIdSystem() == null ? null : levelsOnlyPayload.getIdSystem().longValue(), 
																levelsOnlyPayload.getIdProvider() == null ? null : levelsOnlyPayload.getIdProvider().longValue());
		// first get a list of level -
		List<Level> levels = parseLevelsOnlyPayloadIntoDomainData(levelsOnlyPayload,overrideProviderId);
		// log the created levels
		log.debug(levels.toString());
		
		// save the data to the database
		try {
			if (levelsOnlyPayload.isValidateOnly() == false) {
				if (CHANNEL_ADMINISTRATOR.equals(overrideProviderId) || SYSTEM_ADMINISTRATOR.equals(overrideProviderId)) {
					levelDao.insertBatch(levels, levelsOnlyPayload.isReload());
				} else {
					throw new RfDataloadException("Not a Channel Administrator.  The service to Load Levels has been Turned Off for this User.");
				}
			} 
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			throw new RfDataloadException(e.getMessage(), e);
		}

		// create the response
		LoadLevelsOnlyResponse response = new LoadLevelsOnlyResponse();
		response.setStatus(UPLOAD_SUCCESS_STATUS_CODE);
		response.setMessage(UPLOAD_SUCCESS_MESSAGE);

		return response;
	}
	
	/**
	 * @param sectionDao the sectionDao to set
	 */
	public void setSectionDao(SectionDao sectionDao) {
		this.sectionDao = sectionDao;
	}
	/**
	 * @param levelDao the levelDao to set
	 */
	public void setLevelDao(LevelDao levelDao) {
		this.levelDao = levelDao;
	}
	/**
	 * @param topicDao the topicDao to set
	 */
	public void setTopicDao(TopicDao topicDao) {
		this.topicDao = topicDao;
	}
	/**
	 * @param skillDao the skillDao to set
	 */
	public void setSkillDao(SkillDao skillDao) {
		this.skillDao = skillDao;
	}

	
	// -----------------------------------------------------------------------------
	// private methods
	// -----------------------------------------------------------------------------
	
	// This is the routine that reads the JAXB section objects and creates the corresponding structure of domain objects
	private List<Section> parseSectionsPayloadIntoDamainData( LoadSectionsRequest sectionsPayload, Long overrideProviderId) {
		Long _systemId = new Long(sectionsPayload.getIdSystem().longValue());
		Long _levelId = new Long(sectionsPayload.getIdLevel());
		Long _topicId = new Long(sectionsPayload.getIdTopic());
		Long _skillId = new Long(sectionsPayload.getIdSkill());
		Long providerId = overrideProviderId;
		
		Long fullLevelId = _systemId * 1000 + _levelId;
		Long fullTopicId = fullLevelId * 1000 + _topicId;
		Long fullSkillId = fullTopicId * 100 + _skillId;
		
		// these methods throw business exceptions 
		validateLevel (fullLevelId);
		validateTopic (fullTopicId, fullLevelId);
		validateSkill (fullSkillId, fullTopicId, fullLevelId);
		
		
		List<Section> sectionDaoList = null;
		
		SectionArrayType sectionArrayType = sectionsPayload.getSectionArray();
		if (sectionArrayType != null && sectionArrayType.getSection() != null && sectionArrayType.getSection().size() > 0) {
			// create the section dao list
			sectionDaoList = new ArrayList<Section>();
			List<SectionType> sectionList = sectionArrayType.getSection();
			for (int i = 0; i < sectionList.size(); i++) {
				SectionType section = sectionList.get(i);
				// create the section dao object
				Section sectionDao = createDatabaseSection (section, fullSkillId, providerId, i, _systemId, _levelId, _topicId, _skillId);
				// add section to section list
				sectionDaoList.add(i, sectionDao);
			}
			
		}
		
		return sectionDaoList;
	}

	// Superseded on 10/17/2016 to support SkillChoice Array in the LoadSkillsOnly payload 
//	// This is the extensive routine that reads the JAXB objects and creates the corresponding structure of domain objects
//	private List<Skill> parseSkillsOnlyPayloadIntoDomainData(LoadSkillsOnlyRequest skillsOnlyPayload, Long overrideProviderId ) {
//		Long _systemId = new Long(skillsOnlyPayload.getIdSystem().longValue());
//		Long _levelId = new Long(skillsOnlyPayload.getIdLevel());
//		Long _topicId = new Long(skillsOnlyPayload.getIdTopic());
//		Long providerId = overrideProviderId;
//		
//		Long fullLevelId = _systemId * 1000 + _levelId;
//		Long fullTopicId = fullLevelId * 1000 + _topicId;
//		
//		// these methods throw business exceptions 
//		validateLevel (fullLevelId);
//		validateTopic (fullTopicId, fullLevelId);
//		
//		List<Skill> skillDaoList = null;
//		
//		SkillOnlyArrayType skillOnlyArrayType = skillsOnlyPayload.getSkillOnlyArray();
//		if (skillOnlyArrayType != null && skillOnlyArrayType.getSkillOnly() != null && skillOnlyArrayType.getSkillOnly().size() > 0) {
//			// create the section dao list
//			skillDaoList = new ArrayList<Skill>();
//			List<SkillOnlyType> skillOnlyList = skillOnlyArrayType.getSkillOnly();
//			for (int i = 0; i < skillOnlyList.size(); i++) {
//				SkillOnlyType skillOnly = skillOnlyList.get(i);
//				// create the section dao object
//				Skill skillDao = createDatabaseSkillOnly (skillOnly, fullTopicId, providerId, i, _systemId, _levelId, _topicId);
//				// add section to section list
//				skillDaoList.add(i, skillDao);
//			}
//		}
//		
//		return skillDaoList;
//	}

	// This is the extensive routine that reads the JAXB objects and creates the corresponding structure of domain objects
	private List<Skill> parseSkillsOnlyPayloadIntoDomainData(LoadSkillsOnlyRequest skillsOnlyPayload, Long overrideProviderId ) {
		Long _systemId = new Long(skillsOnlyPayload.getIdSystem().longValue());
		Long _levelId = new Long(skillsOnlyPayload.getIdLevel());
		Long _topicId = new Long(skillsOnlyPayload.getIdTopic());
		Long providerId = overrideProviderId;
		
		Long fullLevelId = _systemId * 1000 + _levelId;
		Long fullTopicId = fullLevelId * 1000 + _topicId;
		
		// these methods throw business exceptions 
		validateLevel (fullLevelId);
		validateTopic (fullTopicId, fullLevelId);
		
		List<Skill> skillDaoList = new ArrayList<Skill>();

		// now create a list of skills
		// account for overlay channels where the topics are loaded with references to other (core) skills
		if (skillsOnlyPayload.getSkillChoiceArray() != null && skillsOnlyPayload.getSkillChoiceArray().getSkillChoiceType() != null 
				&& skillsOnlyPayload.getSkillChoiceArray().getSkillChoiceType().size() > 0) {
			List<SkillChoiceType> skillChoiceList = skillsOnlyPayload.getSkillChoiceArray().getSkillChoiceType();
			for (int i = 0; i < skillChoiceList.size(); i++) {
				if (skillChoiceList.get(i).getCoreSkillReference() != null) {
					Skill skillDao = createDatabaseSkillWithReference (skillChoiceList.get(i).getCoreSkillReference(), fullTopicId, providerId, i, _systemId, _levelId, _topicId);
					skillDaoList.add(i, skillDao);
				} else if (skillChoiceList.get(i).getSkillOnly() != null) {
					Skill skillDao = createDatabaseSkillOnly (skillChoiceList.get(i).getSkillOnly(), fullTopicId, providerId, i, _systemId, _levelId, _topicId);
					skillDaoList.add(i, skillDao);
				} else {
					// bitch about it and leave
					throw new RfDataloadException("Skill Choice Cannot be missing both SkillOnly and SkillReference.  Need one of the 2...");
				}
			}
		} else if (skillsOnlyPayload.getSkillOnlyArray() != null && skillsOnlyPayload.getSkillOnlyArray().getSkillOnly() != null 
					&& skillsOnlyPayload.getSkillOnlyArray().getSkillOnly().size() > 0) {
			List<SkillOnlyType> skillOnlyList = skillsOnlyPayload.getSkillOnlyArray().getSkillOnly();
			for (int i = 0; i < skillOnlyList.size(); i++) {
				// create the skill dao object
				Skill skillDao = createDatabaseSkillOnly (skillOnlyList.get(i), fullTopicId, providerId, i, _systemId, _levelId, _topicId);
				// add skill to skill list
				skillDaoList.add(i, skillDao);
			}
		} else {
			// bitch about it and leave
			throw new RfDataloadException("One of SkillChoiceArray or SkillOnlyArray is required.  Payload has both missing...");
		}
		return skillDaoList;
	}
	
	// This is the extensive routine that reads the JAXB objects and creates the corresponding structure of domain objects
	private List<Skill> parseSkillsPayloadIntoDomainData( LoadSkillsRequest skillsPayload, Long overrideProviderId ) {
		Long _systemId = new Long(skillsPayload.getIdSystem().longValue());
		Long _levelId = new Long(skillsPayload.getIdLevel());
		Long _topicId = new Long(skillsPayload.getIdTopic());
		Long providerId = overrideProviderId;
		
		Long fullLevelId = _systemId * 1000 + _levelId;
		Long fullTopicId = fullLevelId * 1000 + _topicId;
		
		// these methods throw business exceptions 
		validateLevel (fullLevelId);
		validateTopic (fullTopicId, fullLevelId);
//		validateSkill (fullSkillId, fullTopicId, fullLevelId);
		
		
		List<Skill> skillDaoList = null;
		
		SkillArrayType skillArrayType = skillsPayload.getSkillArray();
		if (skillArrayType != null && skillArrayType.getSkill() != null && skillArrayType.getSkill().size() > 0) {
			// create the section dao list
			skillDaoList = new ArrayList<Skill>();
			List<SkillType> skillList = skillArrayType.getSkill();
			for (int i = 0; i < skillList.size(); i++) {
				SkillType skill = skillList.get(i);
				// create the section dao object
				Skill skillDao = createDatabaseSkill (skill, fullTopicId, providerId, i, _systemId, _levelId, _topicId);
				// add section to section list
				skillDaoList.add(i, skillDao);
			}
		}
		
		return skillDaoList;
	}
	
	// This is the extensive routine that reads the JAXB objects and creates the corresponding structure of domain objects
	private List<Topic> parseTopicsOnlyPayloadIntoDomainData( LoadTopicsOnlyRequest topicsOnlyPayload, Long overrideProviderId ) {
		Long _systemId = new Long(topicsOnlyPayload.getIdSystem().longValue());
		Long _levelId = new Long(topicsOnlyPayload.getIdLevel());
		Long providerId = overrideProviderId;
		
		Long fullLevelId = _systemId * 1000 + _levelId;
		
		// these methods throw business exceptions 
		validateLevel (fullLevelId);
		
		List<Topic> topicDaoList = new ArrayList<Topic>();

		// now create a list of topics
		if (topicsOnlyPayload.getTopicChoiceArray() != null && topicsOnlyPayload.getTopicChoiceArray().getTopicChoiceType() != null 
				&& topicsOnlyPayload.getTopicChoiceArray().getTopicChoiceType().size() > 0) {
			List<TopicChoiceType> topicChoiceList = topicsOnlyPayload.getTopicChoiceArray().getTopicChoiceType();
			
			for (int i = 0; i < topicChoiceList.size(); i++) {
				if (topicChoiceList.get(i).getCoreTopicReference() != null) {
					Topic topicDao = createDatabaseTopicWithReference (topicChoiceList.get(i).getCoreTopicReference(), fullLevelId, providerId, i, _systemId, _levelId);
					topicDaoList.add(i, topicDao);
				} else if (topicChoiceList.get(i).getTopicOnly() != null) {
					Topic topicDao = createDatabaseTopicOnly (topicChoiceList.get(i).getTopicOnly(), fullLevelId, providerId, i, _systemId, _levelId);
					topicDaoList.add(i, topicDao);
				} else {
					// bitch about it and leave
					throw new RfDataloadException("Topic Choice Cannot be missing both SkillOnly and SkillReference.  Need one of the 2...");
				}
			}
		} else if (topicsOnlyPayload.getTopicOnlyArray() != null && topicsOnlyPayload.getTopicOnlyArray().getTopicOnly() != null 
					&& topicsOnlyPayload.getTopicOnlyArray().getTopicOnly().size() > 0) {
			List<TopicOnlyType> topicOnlyList = topicsOnlyPayload.getTopicOnlyArray().getTopicOnly();
			for (int i = 0; i < topicOnlyList.size(); i++) {
				// create the skill dao object
				Topic topicDao = createDatabaseTopicOnly (topicOnlyList.get(i), fullLevelId, providerId, i, _systemId, _levelId);
				// add skill to skill list
				topicDaoList.add(i, topicDao);
			}
		} else {
			// bitch about it and leave
			throw new RfDataloadException("One of TopicChoiceArray or TopicOnlyArray is required.  Payload has both missing...");
		}
		return topicDaoList;
	}
	
	// This is the extensive routine that reads the JAXB objects and creates the corresponding structure of domain objects
	private List<Topic> parseTopicsPayloadIntoDomainData( LoadTopicsRequest topicsPayload, Long overrideProviderId ) {
		Long _systemId = new Long(topicsPayload.getIdSystem().longValue());
		Long _levelId = new Long(topicsPayload.getIdLevel());
		Long providerId = overrideProviderId;
		
		Long fullLevelId = _systemId * 1000 + _levelId;
		
		// these methods throw business exceptions 
		validateLevel (fullLevelId);
		
		
		List<Topic> topicDaoList = null;
		
		TopicArrayType topicArrayType = topicsPayload.getTopicArray();
		if (topicArrayType != null && topicArrayType.getTopic() != null && topicArrayType.getTopic().size() > 0) {
			// create the topic dao list
			topicDaoList = new ArrayList<Topic>();
			List<TopicType> topicList = topicArrayType.getTopic();
			for (int i = 0; i < topicList.size(); i++) {
				TopicType topic = topicList.get(i);
				// create the topic dao object
				Topic topicDao = createDatabaseTopic (topic, fullLevelId, providerId, i, _systemId, _levelId);
				// add section to section list
				topicDaoList.add(i, topicDao);
			}
		}
		
		return topicDaoList;
	}
	
//	// This is the extensive routine that reads the JAXB objects and creates the corresponding structure of domain objects
//	private List<Level> parseLevelsOnlyPayloadIntoDomainData( LoadLevelsOnlyRequest levelsOnlyPayload, Long overrideProviderId ) {
//		Long systemId = new Long(levelsOnlyPayload.getIdSystem().longValue());
//		Long providerId = overrideProviderId;
//		
//		List<Level> levelDaoList = null;
//		
//		LevelOnlyArrayType levelOnlyArrayType = levelsOnlyPayload.getLevelOnlyArray();
//		if (levelOnlyArrayType != null && levelOnlyArrayType.getLevelOnly() != null && levelOnlyArrayType.getLevelOnly().size() > 0) {
//			// create the topic dao list
//			levelDaoList = new ArrayList<Level>();
//			List<LevelOnlyType> levelOnlyList = levelOnlyArrayType.getLevelOnly();
//			for (int i = 0; i < levelOnlyList.size(); i++) {
//				LevelOnlyType levelOnly = levelOnlyList.get(i);
//				// create the topic dao object
//				Level levelDao = createDatabaseLevelOnly (levelOnly, systemId, providerId, i);
//				// add section to section list
//				levelDaoList.add(i, levelDao);
//			}
//		}
//		
//		return levelDaoList;
//	}
	
	// This is the extensive routine that reads the JAXB objects and creates the corresponding structure of domain objects
	private List<Level> parseLevelsOnlyPayloadIntoDomainData( LoadLevelsOnlyRequest levelsOnlyPayload, Long overrideProviderId ) {
		Long systemId = new Long(levelsOnlyPayload.getIdSystem().longValue());
		Long providerId = overrideProviderId;
		
		List<Level> levelDaoList = new ArrayList<Level>();

		// now create a list of levels
		if (levelsOnlyPayload.getLevelChoiceArray() != null && levelsOnlyPayload.getLevelChoiceArray().getLevelChoiceType() != null 
				&& levelsOnlyPayload.getLevelChoiceArray().getLevelChoiceType().size() > 0) {
			List<LevelChoiceType> levelChoiceList = levelsOnlyPayload.getLevelChoiceArray().getLevelChoiceType();
			
			for (int i = 0; i < levelChoiceList.size(); i++) {
				if (levelChoiceList.get(i).getCoreLevelReference() != null) {
					Level levelDao = createDatabaseLevelWithReference (levelChoiceList.get(i).getCoreLevelReference(), systemId, providerId, i);
					levelDaoList.add(i, levelDao);
				} else if (levelChoiceList.get(i).getLevelOnly() != null) {
					Level levelDao = createDatabaseLevelOnly (levelChoiceList.get(i).getLevelOnly(), systemId, providerId, i);
					levelDaoList.add(i, levelDao);
				} else {
					// bitch about it and leave
					throw new RfDataloadException("Level Choice Cannot be missing both LevelOnly and LevelReference.  Need one of the 2...");
				}
			}
		} else if (levelsOnlyPayload.getLevelOnlyArray() != null && levelsOnlyPayload.getLevelOnlyArray().getLevelOnly() != null 
					&& levelsOnlyPayload.getLevelOnlyArray().getLevelOnly().size() > 0) {
			List<LevelOnlyType> levelOnlyList = levelsOnlyPayload.getLevelOnlyArray().getLevelOnly();
			for (int i = 0; i < levelOnlyList.size(); i++) {
				// create the skill dao object
				Level levelDao = createDatabaseLevelOnly (levelOnlyList.get(i), systemId, providerId, i);
				// add skill to skill list
				levelDaoList.add(i, levelDao);
			}
		} else {
			// bitch about it and leave
			throw new RfDataloadException("One of LevelChoiceArray or LevelOnlyArray is required.  Payload has both missing...");
		}
		return levelDaoList;
	}

	// This is the extensive routine that reads the JAXB objects and creates the corresponding structure of domain objects
	private List<Level> parseLevelsPayloadIntoDomainData( LoadLevelsRequest levelsPayload, Long overrideProviderId ) {
		Long systemId = new Long(levelsPayload.getIdSystem().longValue());
		Long providerId = overrideProviderId;
		
		List<Level> levelDaoList = null;
		
		LevelArrayType levelArrayType = levelsPayload.getLevelArray();
		if (levelArrayType != null && levelArrayType.getLevel() != null && levelArrayType.getLevel().size() > 0) {
			// create the topic dao list
			levelDaoList = new ArrayList<Level>();
			List<LevelType> levelList = levelArrayType.getLevel();
			for (int i = 0; i < levelList.size(); i++) {
				LevelType level = levelList.get(i);
				// create the topic dao object
				Level levelDao = createDatabaseLevel (level, systemId, providerId, i);
				// add section to section list
				levelDaoList.add(i, levelDao);
			}
		}
		
		return levelDaoList;
	}
	
	// This method creates a database domain Level object from a load schema LevelReference object
	private Level createDatabaseLevelWithReference (CoreLevelReferenceType coreLevelReference, Long systemId, Long providerId, int levelListIndex) 
	{
		Level levelDao = new Level();
		// set the level id
		levelDao.setIdSystem(systemId);
		// set the level id 
		Integer idLevel = coreLevelReference.getIdLevel();
		if (idLevel == null) {
			idLevel = levelListIndex + 1;
		}
		Long fullLevelId = new Long(systemId * 1000 + idLevel.intValue());
		levelDao.setIdLevel(fullLevelId);
		levelDao.setIdProvider(providerId);
		// set the levelReference value
		levelDao.setIdLevelReference(coreLevelReference.getIdLevelReference().longValue());
		// set the SubjectOverride value
		if (coreLevelReference.getSubjectOverride() != null && coreLevelReference.getSubjectOverride().trim().length() > 0) {
			levelDao.setSubject(escapeDatabaseSingleQuotes(coreLevelReference.getSubjectOverride()));
		}
		// set the SubjectHeaderOverride value
		if (coreLevelReference.getSubjectHeaderOverride() != null && coreLevelReference.getSubjectHeaderOverride().trim().length() > 0) {
			levelDao.setSubjectHeader(escapeDatabaseSingleQuotes(coreLevelReference.getSubjectHeaderOverride()));
		}
		// set the NameOverride value
		if (coreLevelReference.getNameOverride() != null && coreLevelReference.getNameOverride().trim().length() > 0) {
			levelDao.setName(escapeDatabaseSingleQuotes(coreLevelReference.getNameOverride()));
		}
		// set the DescriptionOverride value
		if (coreLevelReference.getDescriptionOverride() != null && coreLevelReference.getDescriptionOverride().trim().length() > 0) {
			levelDao.setDescription(escapeDatabaseSingleQuotes(coreLevelReference.getDescriptionOverride()));
		}
		levelDao.setPublished(coreLevelReference.isPublished() != null && !coreLevelReference.isPublished().booleanValue() ? 0 : 1);
		levelDao.setDerived(0);
		return levelDao;
	}
	
	
	// This method creates a database domain skill object from a load schema skill object
	private Level createDatabaseLevel (LevelType level, Long systemId, Long providerId, int levelListIndex) {
		
		Level levelDao = new Level();
		// set the level id
		levelDao.setIdSystem(systemId);
		// set the level id 
		Integer idLevel = level.getIdLevel();
		if (idLevel == null) {
			idLevel = levelListIndex + 1;
		}
		Long fullLevelId = new Long(systemId * 1000 + idLevel.intValue());
		levelDao.setIdLevel(fullLevelId);
		levelDao.setIdProvider(providerId);
		levelDao.setSubject(level.getSubject().value());
		levelDao.setSubjectHeader(escapeDatabaseSingleQuotes(level.getSubjectHeader()));
		levelDao.setName(escapeDatabaseSingleQuotes(level.getName()));
		levelDao.setDisplayName(escapeDatabaseSingleQuotes(level.getDisplayName()));
		levelDao.setDescription(escapeDatabaseSingleQuotes(level.getDescription()));
		levelDao.setText(escapeDatabaseSingleQuotes(level.getText()));
		levelDao.setAddlInfo(escapeDatabaseSingleQuotes(level.getAddlInfo()));
		levelDao.setPublished(level.isPublished() != null && !level.isPublished().booleanValue() ? 0 : 1);
		levelDao.setDerived(level.isDerived() != null && level.isDerived().booleanValue() ? 1 : 0);
		// now create a list of topics
		if (level.getTopicArray() != null && level.getTopicArray().getTopic() != null 
				&& level.getTopicArray().getTopic().size() > 0) {
			List<TopicType> topicList = level.getTopicArray().getTopic();
			List<Topic> topicDaoList = new ArrayList<Topic>();
			for (int i = 0; i < topicList.size(); i++) {
				// create the skill dao object
				Topic topicDao = createDatabaseTopic (topicList.get(i), fullLevelId, providerId, i, systemId, new Long (idLevel));
				// add skill to skill list
				topicDaoList.add(i, topicDao);
			}
			levelDao.setTopics(topicDaoList);
		}
		return levelDao;
	}
	
	// This method creates a database domain skill object from a load schema skill object
	private Level createDatabaseLevelOnly (LevelOnlyType levelOnly, Long systemId, Long providerId, int levelListIndex) {
		
		Level levelDao = new Level();
		// set the level id
		levelDao.setIdSystem(systemId);
		// set the level id 
		Integer idLevel = levelOnly.getIdLevel();
		if (idLevel == null) {
			idLevel = levelListIndex + 1;
		}
		Long fullLevelId = new Long(systemId * 1000 + idLevel.intValue());
		levelDao.setIdLevel(fullLevelId);
		levelDao.setIdProvider(providerId);
		levelDao.setSubject(levelOnly.getSubject().value());
		levelDao.setSubjectHeader(escapeDatabaseSingleQuotes(levelOnly.getSubjectHeader()));
		levelDao.setName(escapeDatabaseSingleQuotes(levelOnly.getName()));
		levelDao.setDisplayName(escapeDatabaseSingleQuotes(levelOnly.getDisplayName()));
		levelDao.setDescription(escapeDatabaseSingleQuotes(levelOnly.getDescription()));
		levelDao.setText(escapeDatabaseSingleQuotes(levelOnly.getText()));
		levelDao.setAddlInfo(escapeDatabaseSingleQuotes(levelOnly.getAddlInfo()));
		levelDao.setPublished(levelOnly.isPublished() != null && !levelOnly.isPublished().booleanValue() ? 0 : 1);
		levelDao.setDerived(levelOnly.isDerived() != null && levelOnly.isDerived().booleanValue() ? 1 : 0);

		// now create a list of topics
		if (levelOnly.getTopicChoiceArray() != null && levelOnly.getTopicChoiceArray().getTopicChoiceType() != null 
				&& levelOnly.getTopicChoiceArray().getTopicChoiceType().size() > 0) {
			List<TopicChoiceType> topicChoiceList = levelOnly.getTopicChoiceArray().getTopicChoiceType();
			List<Topic> topicDaoList = new ArrayList<Topic>();
			for (int i = 0; i < topicChoiceList.size(); i++) {
				if (topicChoiceList.get(i).getCoreTopicReference() != null) {
					Topic topicDao = createDatabaseTopicWithReference (topicChoiceList.get(i).getCoreTopicReference(), fullLevelId, providerId, i, systemId, new Long(idLevel));
					topicDaoList.add(i, topicDao);
				} else if (topicChoiceList.get(i).getTopicOnly() != null) {
					Topic topicDao = createDatabaseTopicOnly (topicChoiceList.get(i).getTopicOnly(), fullLevelId, providerId, i, systemId, new Long(idLevel));
					topicDaoList.add(i, topicDao);
				} else {
					// bitch about it and leave
					throw new RfDataloadException("Topic Choice Cannot be missing both SkillOnly and SkillReference.  Need one of the 2...");
				}
			}
			levelDao.setTopics(topicDaoList);
		} else {
			// traditional topics array
			if (levelOnly.getTopicOnlyArray() != null && levelOnly.getTopicOnlyArray().getTopicOnly() != null 
					&& levelOnly.getTopicOnlyArray().getTopicOnly().size() > 0) {
				List<TopicOnlyType> topicOnlyList = levelOnly.getTopicOnlyArray().getTopicOnly();
				List<Topic> topicDaoList = new ArrayList<Topic>();
				for (int i = 0; i < topicOnlyList.size(); i++) {
					// create the skill dao object
					Topic topicDao = createDatabaseTopicOnly (topicOnlyList.get(i), fullLevelId, providerId, i, systemId, new Long(idLevel));
					// add skill to skill list
					topicDaoList.add(i, topicDao);
				}
				levelDao.setTopics(topicDaoList);
			}
		}
		return levelDao;
	}
	
	// This method creates a database domain skill object from a load schema skill object
	private Topic createDatabaseTopicWithReference (CoreTopicReferenceType coreTopicReference, Long fullLevelId, Long providerId, int topicListIndex, Long _systemId, Long _topicId) 
	{
		Topic topicDao = new Topic();
		// set the level id
		topicDao.setIdLevel(fullLevelId);
		// set the topic id 
		Integer idTopic = coreTopicReference.getIdTopic();
		if (idTopic == null) {
			idTopic = topicListIndex + 1;
		}
		Long fullTopicId = new Long(fullLevelId * 1000 + idTopic.intValue());
		topicDao.setIdTopic(fullTopicId);
		topicDao.setIdProvider(providerId);
		// set the skillReference value
		topicDao.setIdTopicReference(coreTopicReference.getIdTopicReference().longValue());
		// set the SubjectOverride value
		if (coreTopicReference.getSubjectOverride() != null && coreTopicReference.getSubjectOverride().trim().length() > 0) {
			topicDao.setSubject(escapeDatabaseSingleQuotes(coreTopicReference.getSubjectOverride()));
		}
		// set the NameOverride value
		if (coreTopicReference.getNameOverride() != null && coreTopicReference.getNameOverride().trim().length() > 0) {
			topicDao.setName(escapeDatabaseSingleQuotes(coreTopicReference.getNameOverride()));
		}
		// set the DescriptionOverride value
		if (coreTopicReference.getDescriptionOverride() != null && coreTopicReference.getDescriptionOverride().trim().length() > 0) {
			topicDao.setDescription(escapeDatabaseSingleQuotes(coreTopicReference.getDescriptionOverride()));
		}
		topicDao.setPublished(coreTopicReference.isPublished() != null && !coreTopicReference.isPublished().booleanValue() ? 0 : 1);
		topicDao.setDerived(0);
		return topicDao;
	}
	
	// This method creates a database domain skill object from a load schema skill object
	private Topic createDatabaseTopic (TopicType topic, Long fullLevelId, Long providerId, int topicListIndex, 
			Long _systemId, Long _levelId) 
	{
		
		Topic topicDao = new Topic();
		// set the level id
		topicDao.setIdLevel(fullLevelId);
		// set the topic id 
		Integer idTopic = topic.getIdTopic();
		if (idTopic == null) {
			idTopic = topicListIndex + 1;
		}
		Long fullTopicId = new Long(fullLevelId * 1000 + idTopic.intValue());
		topicDao.setIdTopic(fullTopicId);
		topicDao.setIdProvider(providerId);
		topicDao.setSubject(escapeDatabaseSingleQuotes(topic.getSubject()));
		topicDao.setName(escapeDatabaseSingleQuotes(topic.getName()));
		topicDao.setDisplayName(escapeDatabaseSingleQuotes(topic.getDisplayName()));
		topicDao.setDescription(escapeDatabaseSingleQuotes(topic.getDescription()));
		topicDao.setText(escapeDatabaseSingleQuotes(topic.getText()));
		topicDao.setAddlInfo(escapeDatabaseSingleQuotes(topic.getAddlInfo()));
		topicDao.setPublished(topic.isPublished() != null && !topic.isPublished().booleanValue() ? 0 : 1);
		topicDao.setDerived(topic.isDerived() != null && topic.isDerived().booleanValue() ? 1 : 0);
		// now create a list of skills
		if (topic.getSkillArray() != null && topic.getSkillArray().getSkill() != null 
				&& topic.getSkillArray().getSkill().size() > 0) {
			List<SkillType> skillList = topic.getSkillArray().getSkill();
			List<Skill> skillDaoList = new ArrayList<Skill>();
			for (int i = 0; i < skillList.size(); i++) {
				// create the skill dao object
				Skill skillDao = createDatabaseSkill (skillList.get(i), fullTopicId, providerId, i, _systemId, _levelId, new Long(idTopic));
				// add skill to skill list
				skillDaoList.add(i, skillDao);
			}
			topicDao.setSkills(skillDaoList);
		}
		return topicDao;
	}
	
	// This method creates a database domain skill object from a load schema skill object
	private Topic createDatabaseTopicOnly (TopicOnlyType topicOnly, Long fullLevelId, Long providerId, int topicListIndex,
			Long _systemId, Long _levelId) 
	{
		
		Topic topicDao = new Topic();
		// set the level id
		topicDao.setIdLevel(fullLevelId);
		// set the topic id 
		Integer idTopic = topicOnly.getIdTopic();
		if (idTopic == null) {
			idTopic = topicListIndex + 1;
		}
		Long fullTopicId = new Long(fullLevelId * 1000 + idTopic.intValue());
		topicDao.setIdTopic(fullTopicId);
		topicDao.setIdProvider(providerId);
		topicDao.setSubject(escapeDatabaseSingleQuotes(topicOnly.getSubject()));
		topicDao.setName(escapeDatabaseSingleQuotes(topicOnly.getName()));
		topicDao.setDisplayName(escapeDatabaseSingleQuotes(topicOnly.getDisplayName()));
		topicDao.setDescription(escapeDatabaseSingleQuotes(topicOnly.getDescription()));
		topicDao.setText(escapeDatabaseSingleQuotes(topicOnly.getText()));
		topicDao.setAddlInfo(escapeDatabaseSingleQuotes(topicOnly.getAddlInfo()));
		topicDao.setPublished(topicOnly.isPublished() != null && !topicOnly.isPublished().booleanValue() ? 0 : 1);
		topicDao.setDerived(topicOnly.isDerived() != null && topicOnly.isDerived().booleanValue() ? 1 : 0);
		
		
		// now create a list of skills
		// account for overlay channels where the topics are loaded with references to other (core) skills
		if (topicOnly.getSkillChoiceArray() != null && topicOnly.getSkillChoiceArray().getSkillChoiceType() != null 
				&& topicOnly.getSkillChoiceArray().getSkillChoiceType().size() > 0) {
			List<SkillChoiceType> skillChoiceList = topicOnly.getSkillChoiceArray().getSkillChoiceType();
			List<Skill> skillDaoList = new ArrayList<Skill>();
			for (int i = 0; i < skillChoiceList.size(); i++) {
				if (skillChoiceList.get(i).getCoreSkillReference() != null) {
					Skill skillDao = createDatabaseSkillWithReference (skillChoiceList.get(i).getCoreSkillReference(), fullTopicId, providerId, i, _systemId, _levelId, new Long(idTopic));
					skillDaoList.add(i, skillDao);
				} else if (skillChoiceList.get(i).getSkillOnly() != null) {
					Skill skillDao = createDatabaseSkillOnly (skillChoiceList.get(i).getSkillOnly(), fullTopicId, providerId, i, _systemId, _levelId, new Long(idTopic));
					skillDaoList.add(i, skillDao);
				} else {
					// bitch about it and leave
					throw new RfDataloadException("Skill Choice Cannot be missing both SkillOnly and SkillReference.  Need one of the 2...");
				}
			}
			topicDao.setSkills(skillDaoList);
		} else {
			// traditional skills array
			if (topicOnly.getSkillOnlyArray() != null && topicOnly.getSkillOnlyArray().getSkillOnly() != null 
					&& topicOnly.getSkillOnlyArray().getSkillOnly().size() > 0) {
				List<SkillOnlyType> skillOnlyList = topicOnly.getSkillOnlyArray().getSkillOnly();
				List<Skill> skillDaoList = new ArrayList<Skill>();
				for (int i = 0; i < skillOnlyList.size(); i++) {
					// create the skill dao object
					Skill skillDao = createDatabaseSkillOnly (skillOnlyList.get(i), fullTopicId, providerId, i, _systemId, _levelId, new Long(idTopic));
					// add skill to skill list
					skillDaoList.add(i, skillDao);
				}
				topicDao.setSkills(skillDaoList);
			}
		}
		
		return topicDao;
	}
	
	// This method creates a database domain skill object from a load schema skill object
	private Skill createDatabaseSkillWithReference (CoreSkillReferenceType coreSkillReference, Long fullTopicId, Long providerId, int skillListIndex,
			Long _systemId, Long _levelId, Long _topicId) 
	{
		Skill skillDao = new Skill();
		// set the topic id
		skillDao.setIdTopic(fullTopicId);
		// set the skill id 
		Integer idSkill = coreSkillReference.getIdSkill();
		if (idSkill == null) {
			idSkill = skillListIndex + 1;
		}
		Long fullSkillId = new Long(fullTopicId * 100 + idSkill.intValue());
		skillDao.setIdSkill(fullSkillId);
		skillDao.setIdProvider(providerId);
		// set the skillReference value
		skillDao.setIdSkillReference(coreSkillReference.getIdSkillReference().longValue());
		// set the NameOverride value
		if (coreSkillReference.getNameOverride() != null && coreSkillReference.getNameOverride().trim().length() > 0) {
			skillDao.setName(escapeDatabaseSingleQuotes(coreSkillReference.getNameOverride()));
		}
		// set the DescriptionOverride value
		if (coreSkillReference.getDescriptionOverride() != null && coreSkillReference.getDescriptionOverride().trim().length() > 0) {
			skillDao.setDescription(escapeDatabaseSingleQuotes(coreSkillReference.getDescriptionOverride()));
		}
		skillDao.setPublished(coreSkillReference.isPublished() != null && !coreSkillReference.isPublished().booleanValue() ? 0 : 1);
		skillDao.setDerivedSkill(0);
		return skillDao;
	}
	
	// This method creates a database domain skill object from a load schema skill object
	private Skill createDatabaseSkill (SkillType skill, Long fullTopicId, Long providerId, int skillListIndex,
			Long _systemId, Long _levelId, Long _topicId) 
	{
		
		Skill skillDao = new Skill();
		// set the topic id
		skillDao.setIdTopic(fullTopicId);
		// set the skill id 
		Integer idSkill = skill.getIdSkill();
		if (idSkill == null) {
			idSkill = skillListIndex + 1;
		}
		Long fullSkillId = new Long(fullTopicId * 100 + idSkill.intValue());
		skillDao.setIdSkill(fullSkillId);
		skillDao.setIdProvider(providerId);
		skillDao.setSubject(escapeDatabaseSingleQuotes(skill.getSubject()));
		skillDao.setName(escapeDatabaseSingleQuotes(skill.getName()));
		skillDao.setDisplayName(escapeDatabaseSingleQuotes(skill.getDisplayName()));
		skillDao.setDescription(escapeDatabaseSingleQuotes(skill.getDescription()));
		skillDao.setText(escapeDatabaseSingleQuotes(skill.getText()));
		skillDao.setAddlInfo(escapeDatabaseSingleQuotes(skill.getAddlInfo()));
		skillDao.setPublished(skill.isPublished() != null && !skill.isPublished().booleanValue() ? 0 : 1);
		skillDao.setDerivedSkill(0);
		// now create a list of sections
		if (skill.getSectionArray() != null && skill.getSectionArray().getSection() != null 
				&& skill.getSectionArray().getSection().size() > 0) {
			List<SectionType> sectionList = skill.getSectionArray().getSection();
			List<Section> sectionDaoList = new ArrayList<Section>();
			for (int i = 0; i < sectionList.size(); i++) {
				// create the section dao object
				Section sectionDao = createDatabaseSection (sectionList.get(i), fullSkillId, providerId, i, _systemId, _levelId, _topicId, new Long(idSkill));
				// add section to section list
				sectionDaoList.add(i, sectionDao);
			}
			skillDao.setSections(sectionDaoList);
		}
		// now create a list of grades skills
		if (skill.getGradeArray() != null && skill.getGradeArray().getGrade() != null 
				&& skill.getGradeArray().getGrade().size() > 0) {
			skillDao.setGradeskills(createGradeskillListFromGradeTypeList (skill.getGradeArray().getGrade(), fullSkillId));
		}
		return skillDao;
	}
	
	// This method creates a database domain skill object from a load schema skill object
	private Skill createDatabaseSkillOnly (SkillOnlyType skillOnly, Long fullTopicId, Long providerId, int skillListIndex,
			Long _systemId, Long _levelId, Long _topicId) 
	{
		
		Skill skillDao = new Skill();
		// set the topic id
		skillDao.setIdTopic(fullTopicId);
		// set the skill id 
		Integer idSkill = skillOnly.getIdSkill();
		if (idSkill == null) {
			idSkill = skillListIndex + 1;
		}
		Long fullSkillId = new Long(fullTopicId * 100 + idSkill.intValue());
		skillDao.setIdSkill(fullSkillId);
		skillDao.setIdProvider(providerId);
		skillDao.setSubject(escapeDatabaseSingleQuotes(skillOnly.getSubject()));
		skillDao.setName(escapeDatabaseSingleQuotes(skillOnly.getName()));
		skillDao.setDisplayName(escapeDatabaseSingleQuotes(skillOnly.getDisplayName()));
		skillDao.setDescription(escapeDatabaseSingleQuotes(skillOnly.getDescription()));
		skillDao.setText(escapeDatabaseSingleQuotes(skillOnly.getText()));
		skillDao.setAddlInfo(escapeDatabaseSingleQuotes(skillOnly.getAddlInfo()));
		skillDao.setPublished(skillOnly.isPublished() != null && !skillOnly.isPublished().booleanValue() ? 0 : 1);
		skillDao.setDerivedSkill(0);
		// now create a list of grades skills
		if (skillOnly.getGradeArray() != null && skillOnly.getGradeArray().getGrade() != null 
				&& skillOnly.getGradeArray().getGrade().size() > 0) {
			skillDao.setGradeskills(createGradeskillListFromGradeTypeList (skillOnly.getGradeArray().getGrade(), fullSkillId));
		}
		return skillDao;
	}
	
	private List<Gradeskill> createGradeskillListFromGradeTypeList (List<GradeType> gradeList, Long skillId) {
		if (gradeList == null || gradeList.size() == 0) {
			return null; 
		} else {
			List<Gradeskill> gradeskillDaoList = new ArrayList<Gradeskill>();
			for (GradeType gradeType : gradeList) {
				if (ALL_GRADES.contains(gradeType.getGradeName().value())) {
					// create the gradeskill dao object
					Gradeskill gradeskillDao = new Gradeskill (gradeType.getGradeName().value(), skillId, gradeType.getAltName(), gradeType.getAltDescription());
					// add gradeskilldao to gradeskilldao list
					gradeskillDaoList.add(gradeskillDao);
				}
			}
			return gradeskillDaoList;
		}
	}
	
	// This method creates a database domain section object from a load schema section object
	private Section createDatabaseSection (SectionType section, Long fullSkillId, Long providerId, int sectionListIndex,
			Long _systemId, Long _levelId, Long _topicId, Long _skillId) {
		
		Section sectionDao = new Section();
		// set the student topic id
		sectionDao.setIdSkill(fullSkillId);
		// set the section 
		Integer idSection = section.getIdSection();
		if (idSection == null) {
			idSection = sectionListIndex + 1;
		}
		sectionDao.setIdSection(new Long(fullSkillId * 10000 + idSection.intValue()));
		sectionDao.setIdProvider(providerId);
		// mark this as an internal section (meaning, not a wordlist)
		sectionDao.setIsExternal(0);
		
		sectionDao.setName(escapeDatabaseSingleQuotes(section.getName()));
		sectionDao.setDescription(escapeDatabaseSingleQuotes(section.getDescription()));
		sectionDao.setText(escapeDatabaseSingleQuotes(section.getText()));
		sectionDao.setAddlInfo(escapeDatabaseSingleQuotes(section.getAddlInfo()));
		if (section.isIsDerived() != null && section.isIsDerived()) {
			sectionDao.setSectionType(Section.DERIVED_SECTION_TYPE);
		}
		sectionDao.setTimeToAnswer(section.getTimeToAnswer() == null ? null : section.getTimeToAnswer().intValue());
		sectionDao.setAutoGenerated(section.isAutoGenerated() != null && section.isAutoGenerated().booleanValue() ? 1 : 0);
		sectionDao.setGeneratorMetadata(escapeDatabaseSingleQuotes(section.getGeneratorMetadata()));
		sectionDao.setQuestionBanner(escapeDatabaseSingleQuotes(section.getQuestionBanner()));
		sectionDao.setQuestionHeading(escapeDatabaseSingleQuotes(section.getQuestionHeading()));
		sectionDao.setQuestionInstructions(escapeDatabaseSingleQuotes(section.getQuestionInstructions()));
		sectionDao.setIsLinktext(section.isIsLinktext() != null && section.isIsLinktext().booleanValue() ? 1 : 0);
		sectionDao.setLinktextAddress(section.getLinktextAddress());
		sectionDao.setIsPractice(section.isIsPractice() != null && section.isIsPractice().booleanValue() ? 1 : 0);
		if (section.getPictureArray() != null && section.getPictureArray().getPicture() != null && section.getPictureArray().getPicture().size() > 0) {
			int picIndex = 0;
			for (PictureType pictureType : section.getPictureArray().getPicture()) {
				String fileName = saveFile (sectionDao.getIdSection().toString(), pictureType, ++picIndex);
				System.out.println("File successfully created: " + fileName); 
			}
		}
		
		// There is kind of fuzzy programming here.  The Section can either have a QuestionArrayType or a QuestionChoiceArrayType 
		// bit not both.  If both exist, we simply do not process the QuestionChoiceArrayType.  
		// Ideally I would have liked to use a choice element on xsd.  However in the interest of limiting rework and owing to 
		// the fact that most of the time we only use QuestionArrayType, I have followed this approach....
		
		List<Question> questionDaoList = new ArrayList<Question>();
		List<Questionset> questionsetDaoList = new ArrayList<Questionset>();
		int questionNumberIndex = 0;
		int questionsetNumberIndex = 0;

		// There are 2 kinds of Section Types - Regular and Derived.  
		// Regular Sections Have one of two kinds of Question Arrays (depending on whether we need question sets) - QuestionArray or QuestionChoiceArray 
		// Derived Sections has Question References in the third kind of QuestionArray - a DerivedSectionQuestion Array
		// First let check if this is a Derived Section - And if it is, we need to mark the section as such.  Note that derived sections are Provider 
		// compositions using an online mechanism.  They are typically only uploaded when ther have been previously downloaded to a file programatically
		if (section.getDerivedSectionQuestionArray() != null && section.getDerivedSectionQuestionArray().getDerivedSectionQuestion() != null && section.getDerivedSectionQuestionArray().getDerivedSectionQuestion().size() > 0) {
			// this is a derived section....mark the section as such...
			sectionDao.setSectionType(Section.DERIVED_SECTION_TYPE);
			List<DerivedSectionQuestion> derivedSectionQuestionDaoList = new ArrayList<DerivedSectionQuestion>();
			for (DerivedSectionQuestionType jaxbDerivedSectionQuestion : section.getDerivedSectionQuestionArray().getDerivedSectionQuestion()) {
				DerivedSectionQuestion derivedSectionQuestionDao = new DerivedSectionQuestion();
				derivedSectionQuestionDao.setIdSection(sectionDao.getIdSection());
				derivedSectionQuestionDao.setIdQuestion(jaxbDerivedSectionQuestion.getIdQuestionFull().longValue());
				derivedSectionQuestionDao.setQuestionOrder(jaxbDerivedSectionQuestion.getQuestionOrder().intValue());
				derivedSectionQuestionDaoList.add(derivedSectionQuestionDao);
			}
			sectionDao.setDerivedSectionQuestions(derivedSectionQuestionDaoList);
		} else {
			// Must have to be a Regular Section.  See what kind of Question Array it has... 
			if (section.getQuestionArray() != null && section.getQuestionArray().getQuestion() != null && section.getQuestionArray().getQuestion().size() > 0) {
				// Regular questions Array...No Question Choices
				// create the question dao list
				List<QuestionType> questionList = section.getQuestionArray().getQuestion();
				for (int j = 0; j < questionList.size(); j++) {
					QuestionType question = questionList.get(j);
					// create the question dao object
					Question questionDao = createDatabaseQuestionAnswer (question, sectionDao.getIdSection(), providerId, questionNumberIndex, _systemId, _levelId, _topicId, _skillId, new Long(idSection));
					// add question to question list
					questionDaoList.add(questionNumberIndex++, questionDao);
				}
				// add question set to section here
				sectionDao.setQuestions(questionDaoList);
			
			} else if (section.getQuestionChoiceArray() != null && section.getQuestionChoiceArray().getQuestionChoiceType() != null && section.getQuestionChoiceArray().getQuestionChoiceType().size() > 0) {
				// Question Choice Array...May contain Question sets
				// create the question dao list
				List<QuestionChoiceType> questionChoiceTypeList = section.getQuestionChoiceArray().getQuestionChoiceType();
				// create a separate variable for questionNumber cause we can have questionSets combined with questions that need to be flattened out
				for (int j = 0; j < questionChoiceTypeList.size(); j++) {
					QuestionChoiceType questionChoice = questionChoiceTypeList.get(j);
					if (questionChoice.getQuestion() != null) {
						QuestionType question = questionChoice.getQuestion();
						// create the question dao object
						Question questionDao = createDatabaseQuestionAnswer (question, sectionDao.getIdSection(), providerId, questionNumberIndex, _systemId, _levelId, _topicId, _skillId, new Long(idSection));
						// add question to question list and increment questionNumber, after words
						questionDaoList.add(questionNumberIndex++, questionDao);
					}
					if (questionChoice.getQuestionSet() != null) {
						QuestionSetType questionSet = questionChoice.getQuestionSet();
						String questionSetText = questionSet.getText();
						// create the questionset dao object
						Questionset questionsetDao = createDatabaseQuestionset (sectionDao.getIdSection(), questionSetText, questionsetNumberIndex);
						questionsetDaoList.add(questionsetNumberIndex++, questionsetDao);
						// create the question objects (by sending the questionset id
						List<QuestionType> questions = questionSet.getQuestion();
						// create and set the question objects
						if (questions != null && questions.size() > 0) {
							for (int k = 0; k < questions.size(); k++) {
								QuestionType question = questions.get(k);
								// create the question dao object
								Question questionDao = createDatabaseQuestionAnswer (question, sectionDao.getIdSection(), providerId, questionNumberIndex, questionsetDao.getIdQuestionset(), _systemId, _levelId, _topicId, _skillId, new Long(idSection));
								// add question to question list and increment questionNumber, after words
								questionDaoList.add(questionNumberIndex++, questionDao);
							}
						}
							
					}
				}
				// add question set to section here
				sectionDao.setQuestions(questionDaoList);
				// add any questionsets to section here
				sectionDao.setQuestionsets(questionsetDaoList);
			}
		}
		
		return sectionDao;
		
	}
	
	private Questionset createDatabaseQuestionset(Long fullSectionId, String questionSetText, int questionsetNumberIndex) {
		Questionset questionsetDao = new Questionset();
		Long fullQuestionsetId = new Long(fullSectionId * 100 + questionsetNumberIndex + 1);
		questionsetDao.setIdQuestionset(fullQuestionsetId);
		questionsetDao.setIdSection(fullSectionId);
		questionsetDao.setText(questionSetText);
		return questionsetDao;
	}
	
	// This method creates a database domain skill object from a load schema skill object
	private Question createDatabaseQuestionAnswer (QuestionType question, Long fullSectionId, Long providerId, int questionNumberIndex, Long _systemId, Long _levelId, Long _topicId, Long _skillId, Long _sectionId) {
		return createDatabaseQuestionAnswer(question, fullSectionId, providerId, questionNumberIndex, null, _systemId, _levelId, _topicId, _skillId, _sectionId);
	}	
	
	// This method creates a database domain skill object from a load schema skill object
	private Question createDatabaseQuestionAnswer (QuestionType question, Long fullSectionId, Long providerId, int questionNumberIndex, Long idQuestionset, Long _systemId, Long _levelId, Long _topicId, Long _skillId, Long _sectionId) {
		Question questionDao = new Question();
		Integer idQuestion = question.getIdQuestion();
		Long fullQuestionId = new Long(fullSectionId * 100 + (idQuestion == null ? (questionNumberIndex + 1) : idQuestion.intValue()));
		questionDao.setIdQuestion (fullQuestionId);
		questionDao.setIdSection(fullSectionId);
		questionDao.setName(escapeDatabaseSingleQuotes(question.getName()));
		questionDao.setDescription(escapeDatabaseSingleQuotes(question.getDescription()));
		questionDao.setBanner(escapeDatabaseSingleQuotes(question.getBanner()));
		questionDao.setHeading(escapeDatabaseSingleQuotes(question.getHeading()));
		questionDao.setInstructions(escapeDatabaseSingleQuotes(question.getInstructions()));
		questionDao.setQuestionType(escapeDatabaseSingleQuotes(question.getType() == null ? "MultipleChoice" : question.getType().value()));
		//questionDao.setChallengeLevel(question.getChallengeLevel() == null ? null : question.getChallengeLevel().intValue());

		questionDao.setTextPrecontext(escapeDatabaseSingleQuotes(question.getTextPrecontext()));

		questionDao.setText(escapeDatabaseSingleQuotes(getMultilineText(question.getText())));
//		convertMathExpressions(getMultilineText(question.getText()), "QuestionText");

		questionDao.setTextPostcontext(escapeDatabaseSingleQuotes(question.getTextPostcontext()));

		questionDao.setAddlInfo(escapeDatabaseSingleQuotes(getMultilineText(question.getAddlInfo())));
//		convertMathExpressions(getMultilineText(question.getAddlInfo()), "QuestionAddlInfo");
		
		questionDao.setMultipleAnswers(question.isMultipleAnswers() ? 1 : 0);
		questionDao.setIdQuestionset(idQuestionset);
		
		// Set any question reference_info information.  Applies to Questions in Mock tests and such where 
		// the parent Skill, Topic and Level might not mean anything
		setReferenceInfo (questionDao, question, _systemId, _levelId, _topicId, _skillId, _sectionId);
		
		AnswerArrayType answerArrayType = question.getAnswerArray();
		if (answerArrayType != null && answerArrayType.getAnswer() != null && answerArrayType.getAnswer().size() > 0) {
			// create the answer dao list
			List<Answer> answerDaoList = new ArrayList<Answer>();
			List<AnswerType> answerList = answerArrayType.getAnswer();
			for (int k = 0; k < answerList.size(); k++) {
				AnswerType answer = answerList.get(k);
				// create the answer dao object
				Answer answerDao = new Answer();
				Integer idAnswer = answer.getIdAnswer();
				if (idAnswer == null) { // auto-generate answer id number based on question id
					answerDao.setIdAnswer (new Long(questionDao.getIdQuestion() * 10 + k + 1));
				} else {
					answerDao.setIdAnswer (new Long(questionDao.getIdQuestion() * 10 + idAnswer.intValue()));
				}
				answerDao.setIdQuestion(questionDao.getIdQuestion());
				answerDao.setSeq(answer.getSeq() == null ? 0 : answer.getSeq().intValue());
				answerDao.setText(escapeDatabaseSingleQuotes(answer.getText()));
				
				// now set the compare criteria...
				// note that this sort of compare mechanism is only applicable when we compare text of answer entered by the user (fill in the blanks) 
				// to what we have setup as the answer text.
				// we however, ingest the CompareCriteria information irrespective if weather it makes sense
				if (answer.getCompareCriteria() != null) {
					// has CompareCriteria.
					// Note that CompareCriteria can have a choice between 1 of 3 values - IntegerCompare, DecimalCompare or TextCompare
					if (answer.getCompareCriteria().getIntegerCompare() != null) {
						answerDao.setAnswerCompareType(QuestionCompareConstants.QuestionCompareTypes.INTEGER_COMPARE.compareType());
					} else if (answer.getCompareCriteria().getDecimalCompare() != null) {
						answerDao.setAnswerCompareType(QuestionCompareConstants.QuestionCompareTypes.DECIMAL_COMPARE.compareType());
						// set the answerDao.setAnswerCompareAddlInfo to number of digits of precision to compare
						if (answer.getCompareCriteria().getDecimalCompare().getPrecisionDigits() != null) {
							answerDao.setAnswerCompareAddlInfo(answer.getCompareCriteria().getDecimalCompare().getPrecisionDigits().toString());
						}
					} else if (answer.getCompareCriteria().getTextCompare() != null) {
						answerDao.setAnswerCompareType(QuestionCompareConstants.QuestionCompareTypes.TEXT_COMPARE.compareType());
						// default for CaseSensitivety is False  (and hence the && below)
						answerDao.setCaseSensitive(answer.getCompareCriteria().getTextCompare().isCaseSensitive() != null && answer.getCompareCriteria().getTextCompare().isCaseSensitive());
						// default for Trim outer spaces is true  (and hence the || below)
						answerDao.setTrimOuterSpaces(answer.getCompareCriteria().getTextCompare().isTrimOuterSpaces() == null || answer.getCompareCriteria().getTextCompare().isTrimOuterSpaces());
						// default for Trim extra inner spaces is true (and hence the || below)
						answerDao.setTrimExtraInnerSpaces(answer.getCompareCriteria().getTextCompare().isTrimExtraInnerSpaces() == null || answer.getCompareCriteria().getTextCompare().isTrimExtraInnerSpaces());
					}
				}
				
				convertMathExpressions(answer.getText(), "AnswerText");

				answerDao.setAddlInfo(escapeDatabaseSingleQuotes(answer.getAddlInfo()));

				convertMathExpressions(answer.getAddlInfo(), "AnswerAddlInfo");

				answerDao.setCorrect(answer.isCorrect() ? 1 : 0);
				// add answer to answer list
				answerDaoList.add(k, answerDao);
			}
			// add answer set to question here
			questionDao.setAnswers(answerDaoList);
		}
		return questionDao;
	}

	private void setReferenceInfo(Question questionDao, QuestionType question, Long _systemId, Long _levelId, Long _topicId, Long _skillId, Long _sectionId) {
		// first set all questionDao values to null
		questionDao.setIdReferenceLevel(null);
		questionDao.setIdReferenceTopic(null);
		questionDao.setReferenceSkills(null);

		// now see if we need to override
		if (question.getReferenceInfo() != null) {
			Long fullLevelId = _systemId * 1000 + _levelId;
			Long fullTopicId = fullLevelId * 1000 + _topicId;
			Long fullSkillId = fullTopicId * 100 + _skillId;
			
			ReferenceInfoType referenceInfo = question.getReferenceInfo();
			// set reference Level
			Long idLevelRef = null;
			if (referenceInfo.getIdRefLevel() != null) {
				idLevelRef = _systemId * 1000 + referenceInfo.getIdRefLevel();
			} else {
				idLevelRef = _systemId * 1000 + _levelId;
			}
			if (!idLevelRef.equals(fullLevelId)) {
				questionDao.setIdReferenceLevel(idLevelRef);
			}
			
			// set reference topic
			Long idTopicRef = null;
			if (referenceInfo.getIdRefTopic() != null) {
				idTopicRef = idLevelRef * 1000 + referenceInfo.getIdRefTopic();
			} else {
				idTopicRef = idLevelRef * 1000 + _topicId;
			}
			if (!idTopicRef.equals(fullTopicId)) {
				questionDao.setIdReferenceTopic(idTopicRef);
			}
			
			if (referenceInfo.getSkillReferenceArray() != null && referenceInfo.getSkillReferenceArray().getIdRefSkill() != null && referenceInfo.getSkillReferenceArray().getIdRefSkill().size() > 0) {
				List<Integer> idReferenceSkillList = referenceInfo.getSkillReferenceArray().getIdRefSkill();
				if (idReferenceSkillList.size() == 1 && (new Long (idTopicRef * 100 + idReferenceSkillList.get(0))).equals(fullSkillId)) {
					// do nothing
				} else {
					StringBuffer idRefSkillStringBuffer = new StringBuffer();
					for (Integer idReferenceSkill : idReferenceSkillList) {
						Long idSkillRef = idTopicRef * 100 + idReferenceSkill;
						if (idRefSkillStringBuffer.length() > 0) {
							// not first
							idRefSkillStringBuffer.append(",");
						}
						idRefSkillStringBuffer.append(idSkillRef);
					}
					if (idRefSkillStringBuffer.length() > 0) {
						questionDao.setReferenceSkills(idRefSkillStringBuffer.toString());
					}
				}
			}
		}
	}
	
	
	
	private String saveFile(String sectionId, PictureType picture, int index) {
		// check tom make sure the image is okay in size
		if (picture != null) {
			System.out.println("Picture Name: " + picture.getName());
		}
		picture.getImage().getDataSource().toString().length();
		String fileName = sectionId + "_" + index + "_"+ picture.getName();
		File file = new File(UPLOAD_PATH + File.separator + fileName);
		// writing attachment to file
	    try {
	    	FileOutputStream fos = new FileOutputStream(file);
	    	picture.getImage().writeTo(fos);
	    } catch (FileNotFoundException fnf) {
	    	System.out.println ("FileNotFoundException: " + fnf.getMessage());
	    } catch (IOException ioe) {
	    	System.out.println ("FileNotFoundException: " + ioe.getMessage());
	    }
		return fileName;
	}
	
	private String getMultilineText (TextContentType textContentType) {
		if (textContentType == null) {
			return null;
		} else {
			List<Serializable> contentList = textContentType.getContent();
			StringBuffer questionTextBuffer = new StringBuffer();
			if (contentList != null && contentList.size() > 0) {
				for (int i = 0; i < contentList.size(); i++) {
					Serializable line = contentList.get(i);
					String lineVal = null;
					if (line instanceof JAXBElement) {
						lineVal = ((JAXBElement<String>)line).getValue();
					} else {
						lineVal = line.toString();
					}
					String lineValTrim = lineVal.trim();
					if (lineValTrim != null && lineValTrim.length() > 0) {
						questionTextBuffer.append(lineValTrim).append(i < (contentList.size() - 1) ? MULTI_LINE_INPUT_SEPERATOR : "");
					}
				}
			} 
			return questionTextBuffer.toString();
		}
	}
	
	private String escapeDatabaseSingleQuotes (String inString) {
		// do not seem ike I need this function.  Will eliminate it soon
		// TODO: Sesi - get rid of this
		return inString;
//		if (inString == null) {
//			return null;
//		} else {
//			return inString.replace("'","''");
//		}
		
		
	}

	private String convertMathExpressions (String inString, String type) {
		return null;
		
//		System.out.println("Math Conversion for " + type + ": '" + inString + "'");
//		if (inString != null) {
//			MathToWeb mtw = new MathToWeb("conversion_utility");
//			String[] sArrayReturn = mtw.convertLatexToMathMLUtility(inString, "");
//			if (sArrayReturn != null) {
//				for (int i = 0; i < sArrayReturn.length; i++) {
//					System.out.println (i + ": '" + sArrayReturn[i] + "'");
//				}
//			} else {
//				System.out.println ("Some effed up. Try again...");	
//			}
//		}
//		return null;
	}
	
	private void validateLevel (Long fullLevelId) {
		if (fullLevelId != null) {
			for (Level level : ALL_LEVELS) {
				if (level.getIdLevel().equals(fullLevelId)) {
					return;
				}
			}
			StringBuffer contentlevelIncorrectMessageStringBuffer = new StringBuffer();
			contentlevelIncorrectMessageStringBuffer.append("Content Level Value is incorrect.  Please set it to one of the following: ");
			for (Level level : ALL_LEVELS) { 
				contentlevelIncorrectMessageStringBuffer.append("\n[").append (level.getIdLevel() % 100 + " for Content Level: '" + level.getName() + "']  ");
			}
			throw new RfDataloadException(contentlevelIncorrectMessageStringBuffer.toString());
		} else {
			throw new RfDataloadException("Content Level is missing or otherwise set to NULL.");
		}
	}
	
	private void validateTopic (Long fullTopicId, Long fullLevelId) {
		if (fullTopicId != null) {
			Topic topic = null;
			try {
				topic = topicDao.findByTopicId(fullTopicId);
			} catch (Exception e) {}
			if (topic == null) {
				throw new RfDataloadException("Topic '" + fullTopicId + "' is Invalid for Level: '" + fullLevelId + "'");
			}
		}
	}
		
	private void validateSkill (Long fullSkillId, Long fullTopicId, Long fullLevelId) {
		if (fullSkillId != null) {
			Skill skill = null;
			try {
				skill = skillDao.findBySkillId(fullSkillId);
			} catch (Exception e) {}
			if (skill == null) {
				throw new RfDataloadException("Skill '" + fullSkillId + "' is Invalid for Level: '" + fullLevelId + "' and Topic: '" + fullTopicId + "'");
			}
		}
	}
	
	
	/*****************************************************************************************************************************************
	 * Please move these methods about word lists elsewhere.....
	 *****************************************************************************************************************************************/
	@Override
	public LoadWordlistsResponse saveWordlists(
			LoadWordlistsRequest wordlistsPayload) {
		// the following function does all the security and validation checks to load data. 
		Long overrideProviderId = validateAndReturnProviderId(wordlistsPayload.getIdSystem() == null ? null : wordlistsPayload.getIdSystem().longValue(), 
																	wordlistsPayload.getIdProvider() == null ? null : wordlistsPayload.getIdProvider().longValue());
		// first get a list of level -
		List<WlWordlist> wordlists = parseWordlistsPayloadIntoDomainData(wordlistsPayload, overrideProviderId);
		// log the created levels
		log.debug(wordlists.toString());
		System.out.println("wordlists:\n" + wordlists);
		// save the data to the database
		try {
			if (wordlistsPayload.isValidateOnly() == false) {
				wordlistDao.insertBatch(wordlists, wordlistsPayload.isReload());
				//throw new org.springframework.dao.DataIntegrityViolationException("The service to Load Wordlists has been Turned Off!");
			}
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			throw new RfDataloadException(e.getMessage(), e);
		}

		// create the response
		LoadWordlistsResponse response = new LoadWordlistsResponse();
		response.setStatus(UPLOAD_SUCCESS_STATUS_CODE);
		response.setMessage(UPLOAD_SUCCESS_MESSAGE);

		return response;
	}

	@Override
	public String saveWordlists(String wordlistsPayloadString)
			throws JAXBException {
		// TODO Auto-generated method stub
		return null;
	}
	
	// This is the extensive routine that reads the JAXB objects and creates the corresponding structure of domain objects
	private List<WlWordlist> parseWordlistsPayloadIntoDomainData(LoadWordlistsRequest wordlistsPayload, Long overrideProviderId ) {
		Long systemId = new Long(wordlistsPayload.getIdSystem().longValue());
		Long levelId = new Long(wordlistsPayload.getIdLevel());
		Long topicId = new Long(wordlistsPayload.getIdTopic());
		Long skillId = new Long(wordlistsPayload.getIdSkill());
		Long providerId = overrideProviderId;
		
		Long fullLevelId = systemId * 1000 + levelId;
		Long fullTopicId = fullLevelId * 1000 + topicId;
		Long fullSkillId = fullTopicId * 100 + skillId;
		
		// these methods throw business exceptions 
		validateLevel (fullLevelId);
		validateTopic (fullTopicId, fullLevelId);
		validateSkill (fullSkillId, fullTopicId, fullLevelId);
		List<WlWordlist> wordlistDaoList = null;
		
		WordlistArrayType wordlistArrayType = wordlistsPayload.getWordlistArray();
		if (wordlistArrayType != null && wordlistArrayType.getWordlist() != null && wordlistArrayType.getWordlist().size() > 0) {
			// create the wordlist dao list
			wordlistDaoList = new ArrayList<WlWordlist>();
			List<WordlistType> wordlistList = wordlistArrayType.getWordlist();
			for (int i = 0; i < wordlistList.size(); i++) {
				WordlistType wordlist = wordlistList.get(i);
				// create the wordlist dao object
				WlWordlist wordlistDao = createDatabaseWordlist (wordlist, fullSkillId, providerId, i);
				// add section to section list
				wordlistDaoList.add(i, wordlistDao);
			}
		}
		
		return wordlistDaoList;
	}

	// This method creates a database domain wl wordlist object
	// note that this also creates and attaches a parent/dummy section to the wordlist that is marked as place holder 
	private WlWordlist createDatabaseWordlist (WordlistType wordlist, Long fullSkillId, Long providerId, int wordlistListIndex) {
		
		WlWordlist wordlistDao = new WlWordlist();
		// set the level id
		wordlistDao.setIdSkill(fullSkillId);
		// set the level id 
		Integer idWordlist = wordlist.getIdWordlist();
		Long fullWordlistId = new Long(fullSkillId * 10000 + (idWordlist == null ? (wordlistListIndex + 1) : idWordlist.intValue()));
		wordlistDao.setIdWordlist(fullWordlistId);
		wordlistDao.setIdProvider(providerId);
		wordlistDao.setName(escapeDatabaseSingleQuotes(wordlist.getName()));
		wordlistDao.setDescription(escapeDatabaseSingleQuotes(wordlist.getDescription()));

		// create and attach a parent section - this so we have no referential integrity issues when creating questions.
		Section parentSection = new Section();
		parentSection.setIdSkill(fullSkillId);
		parentSection.setIdSection(fullWordlistId);
		parentSection.setIdProvider(providerId);
		parentSection.setIsExternal(1);
		parentSection.setSectionType(Section.WORD_LIST_SECTION_TYPE);
		wordlistDao.setParentSection(parentSection);
		
		//set the words, if any
		if (wordlist.getWordNameArray() != null && wordlist.getWordNameArray().getWordName() != null 
				&& wordlist.getWordNameArray().getWordName().size() > 0) {
			List<WordNameType> wordNameList = wordlist.getWordNameArray().getWordName();
			List<WlWord> wordDaoList = new ArrayList<WlWord>();
			for (int i = 0; i < wordNameList.size(); i++) {
				// create the wlword dao object
				WlWord wordDao = createDatabaseWordName (wordNameList.get(i), fullWordlistId, providerId, i);
				// add skill to skill list
				wordDaoList.add(i, wordDao);
			}
			wordlistDao.setWords(wordDaoList);
		}
		//set passages, if any
		if (wordlist.getPassageArray() != null && wordlist.getPassageArray().getPassage() != null 
				&& wordlist.getPassageArray().getPassage().size() > 0) {
			List<PassageType> passageList = wordlist.getPassageArray().getPassage();
			List<WlPassage> passageDaoList = new ArrayList<WlPassage>();
			for (int i = 0; i < passageList.size(); i++) {
				// create the wlword dao object
				WlPassage passageDao = createDatabasePassage (passageList.get(i), fullWordlistId, providerId, i);
				// add skill to skill list
				passageDaoList.add(i, passageDao);
			}
			wordlistDao.setPassages(passageDaoList);
		}
		//set questions, if any
		if (wordlist.getQuestionArray() != null && wordlist.getQuestionArray().getQuestion() != null 
				&& wordlist.getQuestionArray().getQuestion().size() > 0) {
			List<QuestionType> questionList = wordlist.getQuestionArray().getQuestion();
			List<Question> questionDaoList = new ArrayList<Question>();
			for (int i = 0; i < questionList.size(); i++) {
				// create the wlword dao object
				Question questionDao = createDatabaseQuestionAnswer (questionList.get(i), fullWordlistId, providerId, i, null, null, null, null, null);
				// add skill to skill list
				questionDaoList.add(i, questionDao);
			}
			wordlistDao.setQuestions(questionDaoList);
//		} else {
//			// There is kind of fuzzy programming here.  The Section can either have a QuestionArrayType or a QuestionChoiceArrayType 
//			// bit not both.  If both exist, we simply do not process the QuestionChoiceArrayType.  
//			// Ideally I would have liked to use a choice element on xsd.  However in the interest of limiting rework and owing to 
//			// the fact that most of the time we only use QuestionArrayType, I have followed this approach....
//
//			//set questions, if any
//			QuestionArrayType questionArrayType = wordlist.getQuestionArray();
//			if (questionArrayType != null && questionArrayType.getQuestionChoiceType() != null 
//					&& questionArrayType.getQuestionChoiceType().size() > 0) {
//				List<Question> questionDaoList = new ArrayList<Question>();
//				List<Questionset> questionsetDaoList = new ArrayList<Questionset>();
//				List<QuestionChoiceType> questionChoiceTypeList = questionArrayType.getQuestionChoiceType();
//				// create a separate variable for questionNumber cause we can have questionSets combined with questions that need to be flattened out
//				int questionNumberIndex = 0;
//				int questionsetNumberIndex = 0;
//				for (int j = 0; j < questionChoiceTypeList.size(); j++) {
//					QuestionChoiceType questionChoice = questionChoiceTypeList.get(j);
//					if (questionChoice.getQuestion() != null) {
//						QuestionType question = questionChoice.getQuestion();
//						// create the question dao object
//						Question questionDao = createDatabaseQuestionAnswer (question, fullWordlistId, providerId, questionNumberIndex);
//						// add question to question list and increment questionNumber, after words
//						questionDaoList.add(questionNumberIndex++, questionDao);
//					}
//					if (questionChoice.getQuestionSet() != null) {
//						QuestionSetType questionSet = questionChoice.getQuestionSet();
//						String questionSetText = questionSet.getText();
//						// create the questionset dao object
//						Questionset questionsetDao = createDatabaseQuestionset (sectionDao.getIdSection(), questionSetText, questionsetNumberIndex);
//						questionsetDaoList.add(questionsetNumberIndex++, questionsetDao);
//						// create the question objects (by sending the questionset id
//						List<QuestionType> questions = questionSet.getQuestion();
//						// create and set the question objects
//						if (questions != null && questions.size() > 0) {
//							for (int k = 0; k < questions.size(); k++) {
//								QuestionType question = questions.get(k);
//								// create the question dao object
//								Question questionDao = createDatabaseQuestionAnswer (question, fullWordlistId, providerId, questionNumberIndex, questionsetDao.getIdQuestionset());
//								// add question to question list and increment questionNumber, after words
//								questionDaoList.add(questionNumberIndex++, questionDao);
//							}
//						}
//					}
//				}
//				wordlistDao.setQuestions(questionDaoList);
//				// add any questionsets to section here
//				// for now theer are no question sets on a wordlist
//				// sectionDao.setQuestionsets(questionsetDaoList);
//
//			}
		}
		return wordlistDao;
	}
	
	
//	// This method creates a database domain skill object from a load schema skill object
//	private Question createDatabaseWlQuestionAnswer (QuestionType question, Long fullWordlistId, Long providerId, int questionIndex) {
//		Question wlquestionDao = new Question();
//		// set the wl wordlist id
//		wlquestionDao.setIdSection(fullWordlistId);
//		// set the wl word id 
//		Integer idQuestion = question.getIdQuestion();
//		Long fullQuestionId = new Long(fullWordlistId * 100 + (idQuestion == null ? (questionIndex + 1) : idQuestion.intValue()));
//		wlquestionDao.setIdQuestion(fullQuestionId);
//		wlquestionDao.setQuestionType(question.getType().value());
//		wlquestionDao.setText(escapeDatabaseSingleQuotes(getMultilineText(question.getText())));
//		wlquestionDao.setAddlInfo(escapeDatabaseSingleQuotes(getMultilineText(question.getAddlInfo())));
//		// now look for any answers
//		AnswerArrayType answerArrayType = question.getAnswerArray();
//		if (answerArrayType != null && answerArrayType.getAnswer() != null && answerArrayType.getAnswer().size() > 0) {
//			// create the answer dao list
//			List<Answer> answerDaoList = new ArrayList<Answer>();
//			List<AnswerType> answerList = answerArrayType.getAnswer();
//			for (int k = 0; k < answerList.size(); k++) {
//				AnswerType answer = answerList.get(k);
//				// create the answer dao object
//				Answer wlanswerDao = new Answer();
//				Integer idAnswer = answer.getIdAnswer();
//				if (idAnswer == null) { // auto-generate answer id number based on question id
//					wlanswerDao.setIdAnswer (new Long(wlquestionDao.getIdQuestion() * 10 + k + 1));
//				} else {
//					wlanswerDao.setIdAnswer (new Long(wlquestionDao.getIdQuestion() * 10 + idAnswer.intValue()));
//				}
//				wlanswerDao.setIdQuestion(wlquestionDao.getIdQuestion());
//				wlanswerDao.setText(escapeDatabaseSingleQuotes(answer.getText()));
//				// answerDao.setCorrect(answer.isCorrect() ? 1 : 0);
//				// add answer to answer list
//				answerDaoList.add(k, wlanswerDao);
//			}
//			// add answer set to question here
//			wlquestionDao.setAnswers(answerDaoList);
//		}
//
//		return wlquestionDao;
//	}

	
	// This method creates a database domain skill object from a load schema skill object
	private WlWord createDatabaseWordName (WordNameType wordName, Long fullWordlistId, Long providerId, int wordIndex) {
		WlWord wlwordDao = new WlWord();
		// set the wl wordlist id
		wlwordDao.setIdWordlist(fullWordlistId);
		// set the wl word id 
		Integer idWordName = wordName.getIdWordName();
		Long fullWordNameId = new Long(fullWordlistId * 1000 + (idWordName == null ? (wordIndex + 1) : idWordName.intValue()));
		wlwordDao.setIdWord(fullWordNameId);
		wlwordDao.setWord(wordName.getWordName());
//		wlwordDao.setDefinition(word.getDefinition());
//		wlwordDao.setPronunciation(word.getPronunciation());
//		wlwordDao.setSynonym(word.getSynonym());
//		wlwordDao.setAntonym(word.getAntonym());
//		wlwordDao.setThesaurus(word.getThesaurus());
//		wlwordDao.setSampletext(word.getSampletext());
		return wlwordDao;
	}
	
	// This method creates a database domain skill object from a load schema skill object
	private WlPassage createDatabasePassage (PassageType passage, Long fullWordlistId, Long providerId, int passageIndex) {
		WlPassage wlpassageDao = new WlPassage();
		// set the wl wordlist id
		wlpassageDao.setIdWordlist(fullWordlistId);
		// set the wl word id 
		Integer idPassage = passage.getIdPassage();
		Long fullPassageId = new Long(fullWordlistId * 1000 + (idPassage == null ? (passageIndex + 1) : idPassage.intValue()));
		wlpassageDao.setIdPassage(fullPassageId);
		wlpassageDao.setText(passage.getText());
		return wlpassageDao;
	}
	
	@Override
	public UpdateWordsResponse updateWords(UpdateWordsRequest wordsPayload) {
		// the following function does all the security and validation checks to load data. 
		Long overrideProviderId = validateAndReturnProviderId(wordsPayload.getIdSystem() == null ? null : wordsPayload.getIdSystem().longValue(), 
																	wordsPayload.getIdProvider() == null ? null : wordsPayload.getIdProvider().longValue());
		// first get a list of level -
		List<Cword> cwords = parseCwordsPayloadIntoDomainData(wordsPayload, overrideProviderId);
		// log the created levels
		log.debug(cwords.toString());
		System.out.println("cwords:\n" + cwords);
		// save the data to the database
		try {
			if (wordsPayload.isValidateOnly() == false) {
				cwordDao.updateBatch(cwords);
				//throw new org.springframework.dao.DataIntegrityViolationException("The service to Load Wordlists has been Turned Off!");
			}
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			throw new RfDataloadException(e.getMessage(), e);
		}

		// create the response
		UpdateWordsResponse response = new UpdateWordsResponse();
		response.setStatus(UPLOAD_SUCCESS_STATUS_CODE);
		response.setMessage(UPLOAD_SUCCESS_MESSAGE);


		return response;
	}
	@Override
	public String updateWords(String wordsPayloadString) throws JAXBException {
		// unmarshall to get the payload request object -
		// LoadTopicsRequest
		StringReader wordsPayloadStringReader = new StringReader(
				wordsPayloadString);
		UpdateWordsRequest wordsPayload = (UpdateWordsRequest) getPayloadUnmarshaller(UpdateWordsRequest.class)
				.unmarshal(wordsPayloadStringReader);

		// call the method to process the payload
		UpdateWordsResponse response = updateWords(wordsPayload);

		// marshall to create the payload response object -
		Writer writer = new StringWriter();
		getPayloadMarshaller(UpdateWordsResponse.class).marshal(response, writer);

		return writer.toString();
	}
	
	@Override
	public GetWordsResponse getWords(GetWordsRequest getWordsPayload) {
		Long systemId = new Long(getWordsPayload.getIdSystem().longValue());
		Long levelId = new Long(getWordsPayload.getIdLevel());
		Long clistId = new Long(getWordsPayload.getIdClist());

		Long fullLevelId = systemId * 1000 + levelId;
		// these methods throw business exceptions 
		validateLevel (fullLevelId);

		List<Cword> cwords = cwordDao.getWordsByClistId(fullLevelId, clistId);
		
		// build a response
		GetWordsResponse response = new GetWordsResponse();
		// set the main indexes.  copy these from the request
		response.setIdClist(getWordsPayload.getIdClist());
		response.setIdSystem(getWordsPayload.getIdSystem());
		response.setIdLevel(getWordsPayload.getIdLevel());
		// now set the array of cwords
		if (cwords != null && cwords.size() > 0) {
			WordArrayType cwordArrayType = new WordArrayType();
			for (Cword cword : cwords) {
				WordType wordType = new WordType();
				// note that id_word has to be divided by clistId
				if (cword.getIdCword().intValue() / 100 > clistId && cword.getIdCword().intValue() % 100 == 0) {
					wordType.setIdWord((cword.getIdCword().intValue() / 100 - clistId.intValue()) * 100);
				} else {
					wordType.setIdWord(cword.getIdCword().intValue() % 100);
				}
				wordType.setName(cword.getName());
				wordType.setPronunciation(cword.getPronunciation());
				wordType.setAudioFileUrl(cword.getAudioFileUrl());
				wordType.setVideoFileUrl(cword.getVideoFileUrl());
				if (cword.getWordDefinitions() != null && cword.getWordDefinitions().size() > 0) {
					DefinitionArrayType definitionArrayType = new DefinitionArrayType();
					for (Cworddef cworddef : cword.getWordDefinitions()) {
						WordDefinitionType wordDefinitionType = new WordDefinitionType();
						wordDefinitionType.setIdWordDefinition(cworddef.getIdCworddef().intValue() % 10);
						wordDefinitionType.setPartsOfSpeech(PartsOfSpeechKind.fromValue(cworddef.getPos()));
						wordDefinitionType.setDefinition(cworddef.getDefinition());
						wordDefinitionType.setSampletext(cworddef.getSampletext());
						wordDefinitionType.setSynonym(cworddef.getSynonym());
						wordDefinitionType.setAntonym(cworddef.getAntonym());
						wordDefinitionType.setThesaurus(cworddef.getThesaurus());
						definitionArrayType.getWordDefinition().add(wordDefinitionType);
					}
					wordType.setDefinitionArray(definitionArrayType);
				}
				if (cword.getWordUsages() != null && cword.getWordUsages().size() > 0) {
					UsageArrayType usageArrayType = new UsageArrayType();
					for (Cwordusage cwordusage : cword.getWordUsages()) {
						WordUsageType wordUsageType = new WordUsageType();
						wordUsageType.setIdWordUsage(cwordusage.getIdCwordusage().intValue() % 10);
						wordUsageType.setText(cwordusage.getText());
						wordUsageType.setSource(cwordusage.getSource());
						usageArrayType.getWordUsage().add(wordUsageType);
					}
					wordType.setUsageArray(usageArrayType);
				}
				cwordArrayType.getWord().add(wordType);
			}
			response.setWordArray(cwordArrayType);
		}
		return response;
	}
	
	@Override
	public String getWords(String getWordsPayloadString) throws JAXBException {
		// TODO Auto-generated method stub
		return null;
	}
	
	// This is the extensive routine that reads the JAXB objects and creates the corresponding structure of domain objects
	private List<Cword> parseCwordsPayloadIntoDomainData(UpdateWordsRequest wordsPayload, Long overrideProviderId ) {
		Long systemId = new Long(wordsPayload.getIdSystem().longValue());
		Long levelId = new Long(wordsPayload.getIdLevel());
		Long providerId = overrideProviderId;
		Long clistId = new Long(wordsPayload.getIdClist());
		
		Long fullLevelId = systemId * 1000 + levelId;
		// these methods throw business exceptions 
		validateLevel (fullLevelId);
		List<Cword> cwordDaoList = null;
		
		WordArrayType cwordArrayType = wordsPayload.getWordArray();
		if (cwordArrayType != null && cwordArrayType.getWord() != null && cwordArrayType.getWord().size() > 0) {
			// create the wordlist dao list
			cwordDaoList = new ArrayList<Cword>();
			List<WordType> cwordList = cwordArrayType.getWord();
			for (int i = 0; i < cwordList.size(); i++) {
				WordType cwordType = cwordList.get(i);
				// create the wordlist dao object
				Cword cword = createDatabaseCword (cwordType, clistId, systemId, levelId, providerId);
				// add section to section list
				cwordDaoList.add(i, cword);
			}
		}
		
		return cwordDaoList;
	}

	// This method creates a database domain skill object from a load schema skill object
	private Cword createDatabaseCword (WordType cwordType, Long clistId, Long systemId, Long levelId, Long providerId) {
		Cword cwordDao = new Cword();
		Long idCword = clistId * 100 + cwordType.getIdWord();
		cwordDao.setIdCword(idCword);
		cwordDao.setIdSystem(systemId);
		cwordDao.setIdLevel(levelId);
		cwordDao.setIdProvider(providerId);
		cwordDao.setName(cwordType.getName());
		cwordDao.setPronunciation(cwordType.getPronunciation());
		cwordDao.setAudioFileUrl(cwordType.getAudioFileUrl());
		cwordDao.setVideoFileUrl(cwordType.getVideoFileUrl());
		if (cwordType.getDefinitionArray() != null && cwordType.getDefinitionArray().getWordDefinition() != null && cwordType.getDefinitionArray().getWordDefinition().size() > 0) {
			List<Cworddef> cworddefDaoList = new ArrayList<Cworddef>();
			int index = 0;
			for (WordDefinitionType wordDefinitionType : cwordType.getDefinitionArray().getWordDefinition()) {
				cworddefDaoList.add(createDatabaseCwordDef (wordDefinitionType, idCword, index++));
			}
			cwordDao.setWordDefinitions(cworddefDaoList);
		}
		if (cwordType.getUsageArray() != null && cwordType.getUsageArray().getWordUsage() != null && cwordType.getUsageArray().getWordUsage().size() > 0) {
			List<Cwordusage> cwordusageDaoList = new ArrayList<Cwordusage>();
			int index = 0;
			for (WordUsageType wordUsageType : cwordType.getUsageArray().getWordUsage()) {
				cwordusageDaoList.add(createDatabaseCwordUsage (wordUsageType, idCword, index++));
			}
			cwordDao.setWordUsages(cwordusageDaoList);
		}
		return cwordDao;
	}
	
	// This method creates a database domain skill object from a load schema skill object
	private Cworddef createDatabaseCwordDef (WordDefinitionType wordDefinitionType, Long idCword, int index) {
		Cworddef cworddefDao = new Cworddef();
		cworddefDao.setIdCword(idCword);
		Integer idCworddef = wordDefinitionType.getIdWordDefinition();
		Long fullCworddefId = new Long(idCword * 10 + (idCworddef == null ? (index + 1) : idCworddef.intValue()));
		cworddefDao.setIdCworddef(fullCworddefId);
		cworddefDao.setDefinition(wordDefinitionType.getDefinition());
		cworddefDao.setPos(wordDefinitionType.getPartsOfSpeech().value());
		cworddefDao.setSynonym(wordDefinitionType.getSynonym());
		cworddefDao.setAntonym(wordDefinitionType.getAntonym());
		cworddefDao.setThesaurus(wordDefinitionType.getThesaurus());
		cworddefDao.setSampletext(wordDefinitionType.getSampletext());
		return cworddefDao;
	}
	
	// This method creates a database domain skill object from a load schema skill object
	private Cwordusage createDatabaseCwordUsage (WordUsageType wordUsageType, Long idCword, int index) {
		Cwordusage cwordusageDao = new Cwordusage();
		cwordusageDao.setIdCword(idCword);
		Integer idCwordusage = wordUsageType.getIdWordUsage();
		Long fullCwordusageId = new Long(idCword * 10 + (idCwordusage == null ? (index + 1) : idCwordusage.intValue()));
		cwordusageDao.setIdCwordusage(fullCwordusageId);
		cwordusageDao.setText(wordUsageType.getText());
		cwordusageDao.setSource(wordUsageType.getSource());
		return cwordusageDao;
	}
	
	
	/**********************************************************************************************************
	 * Read And Generate Sections
	 **********************************************************************************************************/
	@Override
	public GetSectionsResponse getSections(GetSectionsRequest getSectionsRequest) {
		// null check first
		if (getSectionsRequest == null) {
			return null;
		}
		
		// read the various variables
		Long systemId = new Long(getSectionsRequest.getIdSystem().longValue());
		Integer _levelId = getSectionsRequest.getIdLevel();
		Integer _topicId = getSectionsRequest.getIdTopic();
		Integer _skillId = getSectionsRequest.getIdSkill();
		// note that sectionId can be null - in which case we simply return all sections in the skill
		Integer _sectionId = getSectionsRequest.getIdSection();
		
		// create the full Id's and Section Id's
		Long fullLevelId = systemId * 1000 + _levelId;
		Long fullTopicId = fullLevelId * 1000 + _topicId;
		Long fullSkillId = fullTopicId * 100 + _skillId;
		Long fullSectionId = _sectionId == null ? null : fullSkillId * 10000 + _sectionId; 
		
		// now query for the section
		List<Section> sectionDaoList = null; 
		if (fullSectionId == null) {
//			sectionDaoList = this.sectionDao.findSectionsForSkillWithQuestions(fullSkillId);
			sectionDaoList = this.sectionDao.findSectionsForSkillWithQuestionsForDownload(fullSkillId);
		} else {
//			Section section = this.sectionDao.findBySectionId(fullSectionId);
			Section section = this.sectionDao.findSectionBySectionIdForDownload(fullSectionId);
			sectionDaoList = new ArrayList<Section>();
			if (section != null) {
				sectionDaoList.add(section);
			}
		}
		
		// Now create and write the response object
		// build a response
		GetSectionsResponse response = new GetSectionsResponse();
		// set the main indexes.  copy these from the request
		response.setIdSystem(new BigInteger(systemId.toString()));
		response.setIdLevel(_levelId);
		response.setIdTopic(_topicId);
		response.setIdSkill(_skillId);
		// now set the array of sections
		if (sectionDaoList != null && sectionDaoList.size() > 0) {
			SectionArrayType jaxbSectionArrayType = new SectionArrayType();
			for (Section sectionDao : sectionDaoList) {
				jaxbSectionArrayType.getSection().add(createSectionTypeFromDatabaseSection(sectionDao));
			}
			response.setSectionArray(jaxbSectionArrayType);
		}
		return response;
	}
	
	@Override
	public GetSectionsResponse getSections(String getSectionsRequestSerialized) throws JAXBException {
		// unmarshall to get the payload request object -
		GetSectionsRequest getSectionsRequest = ((JAXBElement<GetSectionsRequest>) getPayloadUnmarshaller(GetSectionsRequest.class)
				.unmarshal(new StringReader(getSectionsRequestSerialized))).getValue();
		// now call the other method that takes a request object
		return getSections(getSectionsRequest);
	}

	@Override
	public String getSectionsSerialized(GetSectionsRequest getSectionsRequest) throws JAXBException {
		GetSectionsResponse getSectionsResponse = getSections(getSectionsRequest);
		// marshall to create the payload response object -
		Writer stringWriter = new StringWriter();
		Marshaller marshaller = getPayloadMarshaller(GetSectionsResponse.class);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(getSectionsResponse, stringWriter);
		// Now teh response is good to go, however, it has the "<" and ">" characters in content escaped into "&lt;" and "&gt;".  
		// There are different ways to fix the problem....  Mine may not be the most efficient, but it works.
		// look for a more optimal solution at: "http://stackoverflow.com/questions/3136375/how-to-generate-cdata-block-using-jaxb"
		String responseString = stringWriter.toString().replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;nbsp;", "&nbsp;");
		// log the response message
		
		if (log.isDebugEnabled()) {
			log.debug("Serialized Section: " + responseString);
		}
		return responseString;
	}
	
	@Override
	public String getSectionsSerialized(String getSectionsRequestSerialized) throws JAXBException {
		// unmarshall to get the payload request object -
//		GetSectionsRequest getSectionsRequest = ((JAXBElement<GetSectionsRequest>) getPayloadUnmarshaller(GetSectionsRequest.class)
//				.unmarshal(new StringReader(getSectionsRequestSerialized))).getValue();
		GetSectionsRequest getSectionsRequest = (GetSectionsRequest) getPayloadUnmarshaller(GetSectionsRequest.class).unmarshal(new StringReader(getSectionsRequestSerialized));
		return getSectionsSerialized(getSectionsRequest);
//		GetSectionsResponse getSectionsResponse = getSections(getSectionsRequest);
//		// marshall to create the payload response object -
//		Writer stringWriter = new StringWriter();
//		Marshaller marshaller = getPayloadMarshaller(GetSectionsResponse.class);
//		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//		marshaller.marshal(getSectionsResponse, stringWriter);
//		// Now teh response is good to go, however, it has the "<" and ">" characters in content escaped into "&lt;" and "&gt;".  
//		// There are different ways to fix the problem....  Mine may not be the most efficient, but it works.
//		// look for a more optimal solution at: "http://stackoverflow.com/questions/3136375/how-to-generate-cdata-block-using-jaxb"
//		String responseString = stringWriter.toString().replaceAll("&lt;", "<").replaceAll("&gt;", ">");
//		// log the response message
//		
//		if (logger.isDebugEnabled()) {
//			logger.info("Serialized Section: " + responseString);
//		}
//		return responseString;
	}
	

	
	private static final Pattern XML_CHARS = Pattern.compile("[<>&]");
	private static final String CDATA_START_CHARS = "<![CDATA[";
	private static final String CDATA_END_CHARS = "]]>";
	
	private String wrapInCDATAIfNecessary(String sourceText) {
		if (sourceText == null) {
			return null;
		}
	    boolean useCData = XML_CHARS.matcher(sourceText).find();
	    if (useCData) {
	    	return CDATA_START_CHARS + sourceText + CDATA_END_CHARS;
	    } else {
	    	return sourceText;
	    }
	}
	
	// This method creates a database domain section object from a load schema section object
	private SectionType createSectionTypeFromDatabaseSection (Section sectionDao)
	{
		SectionType jaxbSectionType = new SectionType();
		if (sectionDao != null) {
			// set the id
			long idSection = sectionDao.getIdSection() % 10000;
			jaxbSectionType.setIdSection((int)idSection);
			// set the main values
			if (sectionDao.getName() != null) jaxbSectionType.setName(wrapInCDATAIfNecessary(sectionDao.getName()));
			if (sectionDao.getDescription() != null) jaxbSectionType.setDescription(wrapInCDATAIfNecessary(sectionDao.getDescription()));
			if (sectionDao.getText() != null) jaxbSectionType.setText(wrapInCDATAIfNecessary(sectionDao.getText()));
			if (sectionDao.getAddlInfo() != null) jaxbSectionType.setAddlInfo(wrapInCDATAIfNecessary(sectionDao.getAddlInfo()));
			if (sectionDao.getTimeToAnswer() != null) jaxbSectionType.setTimeToAnswer(new BigInteger(sectionDao.getTimeToAnswer().toString()));
			if (sectionDao.getAutoGenerated() != null) jaxbSectionType.setAutoGenerated(sectionDao.getAutoGenerated().intValue() == 1 ? true : false);
			if (sectionDao.getQuestionBanner() != null) jaxbSectionType.setQuestionBanner(wrapInCDATAIfNecessary(sectionDao.getQuestionBanner()));
			if (sectionDao.getQuestionHeading() != null) jaxbSectionType.setQuestionHeading(wrapInCDATAIfNecessary(sectionDao.getQuestionHeading()));
			if (sectionDao.getQuestionInstructions() != null) jaxbSectionType.setQuestionInstructions(wrapInCDATAIfNecessary(sectionDao.getQuestionInstructions()));
			if (sectionDao.getIsLinktext() != null) jaxbSectionType.setAutoGenerated(sectionDao.getIsLinktext().intValue() == 1 ? true : false);
			if (sectionDao.getLinktextAddress() != null) jaxbSectionType.setLinktextAddress(wrapInCDATAIfNecessary(sectionDao.getLinktextAddress()));
			if (sectionDao.getIsPractice() != null) jaxbSectionType.setIsPractice(sectionDao.getIsPractice().intValue() == 1 ? true : false);
			if (sectionDao.getSectionType() != null && Section.DERIVED_SECTION_TYPE.equalsIgnoreCase(sectionDao.getSectionType())) jaxbSectionType.setIsDerived(true);

			if (Section.DERIVED_SECTION_TYPE.equalsIgnoreCase(sectionDao.getSectionType())) {
				if (sectionDao.getDerivedSectionQuestions() != null && sectionDao.getDerivedSectionQuestions().size() > 0) {
					DerivedSectionQuestionArrayType jaxbDerivedSectionQuestionArrayType = new DerivedSectionQuestionArrayType();
					for (DerivedSectionQuestion derivedSectionQuestionDao : sectionDao.getDerivedSectionQuestions()) {
						DerivedSectionQuestionType jaxbDerivedSectionQuestionType = new DerivedSectionQuestionType();
						// set the Full Question ID
						jaxbDerivedSectionQuestionType.setIdQuestionFull(new BigInteger(derivedSectionQuestionDao.getIdQuestion().toString()));
						jaxbDerivedSectionQuestionType.setQuestionOrder(new BigInteger(derivedSectionQuestionDao.getQuestionOrder().toString()));
						// add it to the array
						jaxbDerivedSectionQuestionArrayType.getDerivedSectionQuestion().add(jaxbDerivedSectionQuestionType);
					}
					// add the derived question array to the Section 
					jaxbSectionType.setDerivedSectionQuestionArray(jaxbDerivedSectionQuestionArrayType);
				}
			} else {
				if (sectionDao.getQuestionsets() == null || sectionDao.getQuestionsets().size() == 0) {
					// no question sets......follow the old approach
					if (sectionDao.getQuestions() != null && sectionDao.getQuestions().size() > 0) {
						QuestionArrayType jaxbQuestionArrayType = new QuestionArrayType();
						for (Question question : sectionDao.getQuestions()) {
							QuestionType jaxbQuestionType = createQuestionTypeFromDatabaseQuestion(question);
							jaxbQuestionArrayType.getQuestion().add(jaxbQuestionType);
						}
						jaxbSectionType.setQuestionArray(jaxbQuestionArrayType);
					} 
				} else {
					// other than pre arranging questions accorging to question sets, I cannot think of another way....please let me know otherwise
					if (sectionDao.getQuestions() != null && sectionDao.getQuestions().size() > 0) {
						LinkedHashMap<String, List<Question>> rearrangedQuestionMap = new LinkedHashMap<String, List<Question>>();
						for (int i = 0; i < sectionDao.getQuestions().size(); i++) {
							Question question = sectionDao.getQuestions().get(i);
							if (question.getIdQuestionset() == null || question.getIdQuestionset() == 0l) {
								String key = "Q" + i;
								rearrangedQuestionMap.put(key, Arrays.asList(question));
							} else {
								String key = "S" + question.getIdQuestionset();
								if (rearrangedQuestionMap.get(key) == null) {
//										rearrangedQuestionMap.put(key, Arrays.asList(question));
									List<Question> questionList = new ArrayList<Question>();
									questionList.add(question);
									rearrangedQuestionMap.put(key, questionList);
								} else {
									rearrangedQuestionMap.get(key).add(question);
								}
							}
						}
						// rearrangement complete...not create the content...
						QuestionChoiceArrayType jaxbQuestionChoiceArrayType = new QuestionChoiceArrayType();
						List<String> rearrangedQuestionMapKeyList = new ArrayList<String>(rearrangedQuestionMap.keySet());
						for (String key : rearrangedQuestionMapKeyList) {
							if (key.startsWith("S")) { // questionset in a question choice array...
								// first get a handle on the questionset item
								Long idQuestionset = Long.parseLong(key.substring(1));
								Questionset questionset = null;
								for (Questionset qs : sectionDao.getQuestionsets()) {
									if (qs.getIdQuestionset().equals(idQuestionset)) {
										questionset = qs;
										break;
									}
								}
								// now lets create the questions in the set
								// set the text
								QuestionChoiceType jaxbQuestionChoiceType = new QuestionChoiceType();
								QuestionSetType jaxbQuestionSetType = new QuestionSetType();
								jaxbQuestionSetType.setText(wrapInCDATAIfNecessary(questionset.getText()));
								// now create and add the questions to the questionset's question list
								for (Question question : rearrangedQuestionMap.get(key)) {
									QuestionType jaxbQuestionType = createQuestionTypeFromDatabaseQuestion(question);
									jaxbQuestionSetType.getQuestion().add(jaxbQuestionType);
								}
								// add the newly minted question set to the QuestionChoiceType 
								jaxbQuestionChoiceType.setQuestionSet(jaxbQuestionSetType);
								// add the QuestionChoiceType to the QuestionChoiceTypeArray
								jaxbQuestionChoiceArrayType.getQuestionChoiceType().add(jaxbQuestionChoiceType);
							} else { // regular question in a question choice array...
								QuestionChoiceType jaxbQuestionChoiceType = new QuestionChoiceType();
								QuestionType jaxbQuestionType = createQuestionTypeFromDatabaseQuestion(rearrangedQuestionMap.get(key).get(0));
								jaxbQuestionChoiceType.setQuestion(jaxbQuestionType);
								jaxbQuestionChoiceArrayType.getQuestionChoiceType().add(jaxbQuestionChoiceType);
							}
						}
						jaxbSectionType.setQuestionChoiceArray(jaxbQuestionChoiceArrayType);
					}
				}
			}
		}
		return jaxbSectionType;
	}

	// This method creates a database domain skill object from a load schema skill object
	private QuestionType createQuestionTypeFromDatabaseQuestion (Question questionDao) {
		QuestionType jadbQuestionType = new QuestionType(); 
		if (questionDao != null) {
			// set the id
			long idQuestion = questionDao.getIdQuestion() % 100;
			jadbQuestionType.setIdQuestion((int)idQuestion);
			// set the main values
			if (questionDao.getName() != null) jadbQuestionType.setName(wrapInCDATAIfNecessary(questionDao.getName()));
			if (questionDao.getDescription() != null) jadbQuestionType.setDescription(wrapInCDATAIfNecessary(questionDao.getDescription()));
			if (questionDao.getText() != null) {
				// note that multi "line" texts are not being split into individual lines (yet) 
				// we need to get rid of any MULTI_LINE_INPUT_SEPERATOR terminator at the end of content....
				String questionText = questionDao.getText();
				if (questionText.endsWith(MULTI_LINE_INPUT_SEPERATOR)) {
					questionText = questionText.substring(0, questionText.length() - MULTI_LINE_INPUT_SEPERATOR.length());
				}
				TextContentType jaxbQuestionTextTextContentType = new TextContentType();
				jaxbQuestionTextTextContentType.getContent().add(wrapInCDATAIfNecessary(questionText));
				jadbQuestionType.setText(jaxbQuestionTextTextContentType);
			}
			if (questionDao.getAddlInfo() != null) {
				// note that multi "line" texts are not being split into individual lines (yet)
				// we need to get rid of any MULTI_LINE_INPUT_SEPERATOR terminator at the end of content....
				String questionAddlInfo = questionDao.getAddlInfo();
				if (questionAddlInfo.endsWith(MULTI_LINE_INPUT_SEPERATOR)) {
					questionAddlInfo = questionAddlInfo.substring(0, questionAddlInfo.length() - MULTI_LINE_INPUT_SEPERATOR.length());
				}
				TextContentType jaxbQuestionAddlInfoTextContentType = new TextContentType();
				jaxbQuestionAddlInfoTextContentType.getContent().add(wrapInCDATAIfNecessary(questionAddlInfo));
				jadbQuestionType.setAddlInfo(jaxbQuestionAddlInfoTextContentType);
			}
			if (questionDao.getTextPrecontext() != null) jadbQuestionType.setTextPrecontext(wrapInCDATAIfNecessary(questionDao.getTextPrecontext()));
			if (questionDao.getTextPostcontext() != null) jadbQuestionType.setTextPostcontext(wrapInCDATAIfNecessary(questionDao.getTextPostcontext()));
			if (questionDao.getBanner() != null) jadbQuestionType.setBanner(wrapInCDATAIfNecessary(questionDao.getBanner()));
			if (questionDao.getHeading() != null) jadbQuestionType.setHeading(wrapInCDATAIfNecessary(questionDao.getHeading()));
			if (questionDao.getInstructions() != null) jadbQuestionType.setInstructions(wrapInCDATAIfNecessary(questionDao.getInstructions()));
			if (questionDao.getQuestionType() != null) {
				QuestionKind jaxbQuestionKind = QuestionKind.fromValue(questionDao.getQuestionType());
				jadbQuestionType.setType(jaxbQuestionKind);
			}
			if (questionDao.getMultipleAnswers() != null) {
				jadbQuestionType.setMultipleAnswers(questionDao.getMultipleAnswers() == 1 ? true : false);
			}
			
			// Set any question Reference Info...
			// Set any question reference_info information.  Applies to Questions in Mock tests and such where 
			// the parent Skill, Topic and Level might not mean anything
			if (questionDao.getIdReferenceLevel() != null || questionDao.getIdReferenceTopic() != null || (questionDao.getReferenceSkills() != null && questionDao.getReferenceSkills().trim().length() > 0)) {
				jadbQuestionType.setReferenceInfo(createReferenceInfoType(questionDao));
			}

			if (questionDao.getAnswers() != null && questionDao.getAnswers().size() > 0) {
				AnswerArrayType jaxbAnswerArrayType = new AnswerArrayType();
				for (Answer andwerDao : questionDao.getAnswers()) {
					jaxbAnswerArrayType.getAnswer().add(createAnswerTypeFromDatabaseAnswer(andwerDao));
				}
				jadbQuestionType.setAnswerArray(jaxbAnswerArrayType);
			}
		}
		return jadbQuestionType;
	}

	/**
	 * Recreates a Question's Reference information based on the information in teh database.  Note that the ReferenceSkill
	 * information is stored as a comma seperated list of Skill ID's and so, we have to split.
	 * @param questionDao
	 * @return
	 */
	private ReferenceInfoType createReferenceInfoType(Question questionDao) {
		ReferenceInfoType jaxbReferenceInfoType = new ReferenceInfoType();
		if (questionDao.getIdReferenceLevel() != null) {
			jaxbReferenceInfoType.setIdRefLevel((int) (questionDao.getIdReferenceLevel() % 1000));
		}
		if (questionDao.getIdReferenceTopic() != null) {
			jaxbReferenceInfoType.setIdRefTopic((int) (questionDao.getIdReferenceTopic() % 1000));
		}
		if (questionDao.getReferenceSkills() != null && questionDao.getReferenceSkills().trim().length() > 0) {
			String referenceSkillsConcatString = questionDao.getReferenceSkills().trim();
			String[] referenceSkillStrings = referenceSkillsConcatString.split(",");
			if (referenceSkillStrings.length > 0) {
				SkillReferenceArrayType jaxbSkillReferenceArrayType = new SkillReferenceArrayType();
				for (String referenceSkillString : referenceSkillStrings) {
					try {
						Long idReferenceSkill = Long.parseLong(referenceSkillString.trim()) % 100;
						jaxbSkillReferenceArrayType.getIdRefSkill().add(idReferenceSkill.intValue());
					} catch (NumberFormatException nfe) {}
				}
				jaxbReferenceInfoType.setSkillReferenceArray(jaxbSkillReferenceArrayType);
			}
		}
		return jaxbReferenceInfoType;
	}

	// This method creates a database domain skill object from a load schema skill object
	private AnswerType createAnswerTypeFromDatabaseAnswer (Answer answerDao) {
		AnswerType jadbAnswerType = new AnswerType(); 
		if (answerDao != null) {
			// set the id
			long idAnswer = answerDao.getIdAnswer() % 10;
			jadbAnswerType.setIdAnswer((int)idAnswer);
			// sequence is in the database as Default 0.  However 0 is not a valid value in XML (because the restriction is a "positive integer"
			if (answerDao.getSeq() != null && answerDao.getSeq() != 0) {
				jadbAnswerType.setSeq(new BigInteger(answerDao.getSeq().toString()));
			}
			jadbAnswerType.setText(wrapInCDATAIfNecessary(answerDao.getText()));
			jadbAnswerType.setAddlInfo(wrapInCDATAIfNecessary(answerDao.getAddlInfo()));
			if (answerDao.getCorrect() != null) { 
				jadbAnswerType.setCorrect(answerDao.getCorrect() == 1 ? true : false);
			}
			// now set the compare criteria...
			// note that this sort of compare mechanism is only applicable when we compare text of answer entered by the user (fill in the blanks) 
			// to what we have setup as the answer text.
			// we however, ingest the CompareCriteria information irrespective if weather it makes sense
			CompareCriteriaType jaxbCompareCriteriaType = null;
			if (answerDao.getAnswerCompareType() != null) {
				if (answerDao.getAnswerCompareType().equals(QuestionCompareConstants.QuestionCompareTypes.INTEGER_COMPARE.compareType())) {
					jaxbCompareCriteriaType = new CompareCriteriaType();
					IntegerCompareType jaxbIntegerCompareType = new IntegerCompareType();
					jaxbIntegerCompareType.setInteger(true);
					jaxbCompareCriteriaType.setIntegerCompare(jaxbIntegerCompareType);
				} else if (answerDao.getAnswerCompareType().equals(QuestionCompareConstants.QuestionCompareTypes.DECIMAL_COMPARE.compareType())) {
					jaxbCompareCriteriaType = new CompareCriteriaType();
					DecimalCompareType jaxbDecimalCompareType = new DecimalCompareType(); 
					jaxbDecimalCompareType.setPrecisionDigits(answerDao.getPrecisionDigits() == null ? 0 : answerDao.getPrecisionDigits());
					jaxbCompareCriteriaType.setDecimalCompare(jaxbDecimalCompareType);
				} else if (answerDao.getAnswerCompareType().equals(QuestionCompareConstants.QuestionCompareTypes.TEXT_COMPARE.compareType())) {
					jaxbCompareCriteriaType = new CompareCriteriaType();
					TextCompareType jaxbTextCompareType = new TextCompareType();
					String aaa = answerDao.getAnswerCompareAddlInfo();
					if (aaa != null && aaa.trim().length() == 3) {
						jaxbTextCompareType.setCaseSensitive(aaa.substring(0, 1).equals("1"));
						jaxbTextCompareType.setTrimOuterSpaces(aaa.substring(1, 2).equals("1"));
						jaxbTextCompareType.setTrimExtraInnerSpaces(aaa.substring(2, 3).equals("1"));
					} else {
						// set Defaults
						jaxbTextCompareType.setCaseSensitive(false);
						jaxbTextCompareType.setTrimOuterSpaces(true);
						jaxbTextCompareType.setTrimExtraInnerSpaces(true);
					}
					jaxbCompareCriteriaType.setTextCompare(jaxbTextCompareType);
				}
			}
			if (jaxbCompareCriteriaType != null) {
				jadbAnswerType.setCompareCriteria(jaxbCompareCriteriaType);
			}
		}
		return jadbAnswerType;
	}
	
	
	
	
	
	
	/**********************************************************************************************************
	 * Read And Generate Tests
	 **********************************************************************************************************/
	
	@Override
	public GetTestsResponse getTests(GetTestsRequest getTestsRequest) {
		// null check first
		if (getTestsRequest == null) {
			return null;
		}
		
		List<Test> testDaoObjectList = null;
		
		if (getTestsRequest.getTestRequestChoice().getTestsByChannelIdAndProviderName() != null) {
			TestsByChannelIdAndProviderNameType jaxbTestsByChannelIdAndProviderNameType = getTestsRequest.getTestRequestChoice().getTestsByChannelIdAndProviderName(); 
			Long idChannel = new Long(jaxbTestsByChannelIdAndProviderNameType.getIdChannel().longValue());
			String testType = jaxbTestsByChannelIdAndProviderNameType.getTesttype().toString();
			String providerName = jaxbTestsByChannelIdAndProviderNameType.getProviderName();
			Long idProvider = findProviderIdForProvider(providerName);
			if (idProvider == null) {
				throw new RfDataloadException("Invalid Provider Name: " + providerName);
			}
			testDaoObjectList = testDao.findTestsForChannelProviderAndTypeForDownload(idChannel, idProvider, testType);
		} else if (getTestsRequest.getTestRequestChoice().getTestsByChannelId() != null) {
			TestsByChannelIdType jaxbTestsByChannelIdType = getTestsRequest.getTestRequestChoice().getTestsByChannelId(); 
			Long idChannel = new Long(jaxbTestsByChannelIdType.getIdChannel().longValue());
			String testType = jaxbTestsByChannelIdType.getTesttype().toString();
			testDaoObjectList = testDao.findTestsForChannelAndTypeForDownload(idChannel, testType);
		} else if (getTestsRequest.getTestRequestChoice().getTestsByProviderName() != null) {
			TestsByProviderNameType jaxbTestsByProviderNameType = getTestsRequest.getTestRequestChoice().getTestsByProviderName(); 
			String providerName = jaxbTestsByProviderNameType.getProviderName();
			String testType = jaxbTestsByProviderNameType.getTesttype().toString();
			Long idProvider = findProviderIdForProvider(providerName);
			if (idProvider == null) {
				throw new RfDataloadException("Invalid Provider Name: " + providerName);
			}
			testDaoObjectList = testDao.findTestsForProviderAndTypeForDownload(idProvider, testType);
		} else if (getTestsRequest.getTestRequestChoice().getTestsByIdArray() != null && getTestsRequest.getTestRequestChoice().getTestsByIdArray().getTestIdArray() != null &&
				getTestsRequest.getTestRequestChoice().getTestsByIdArray().getTestIdArray().getIdTest() != null && getTestsRequest.getTestRequestChoice().getTestsByIdArray().getTestIdArray().getIdTest().size() > 0) {
			List<Long> testIdList = getTestsRequest.getTestRequestChoice().getTestsByIdArray().getTestIdArray().getIdTest();
			testDaoObjectList = testDao.findTestsForTestIdListForDownload(testIdList);
		}
		
		// if no tests found
		if (testDaoObjectList == null || testDaoObjectList.size() == 0) {
			return null;
		}
		
		// Now that we have tests, lets create the response
		// build a response
		GetTestsResponse response = new GetTestsResponse();
		// now set the array of sections
		TestArrayType jaxbTestArrayType = new TestArrayType();
		for (Test testDaoObject : testDaoObjectList) {
			jaxbTestArrayType.getTest().add(createTestTypeFromDatabaseTest(testDaoObject));
		}
		response.setTestArray(jaxbTestArrayType);
		
		return response;
	}
	
	private Long findProviderIdForProvider(String providerName) {
		User provider = userDao.findByUsername(providerName);
		return provider == null ? null : provider.getIdUser();
	}
	
	private String findProviderNameForProviderId(Long idProvider) {
		User provider = userDao.findByUserId(idProvider);
		return provider == null ? null : provider.getUsername();
	}
	
	private TestType createTestTypeFromDatabaseTest(Test testDaoObject) {
		TestType jaxbTestType = new TestType();
		if (testDaoObject != null) {
			// set the id's
			jaxbTestType.setIdTest(testDaoObject.getIdTest());
			jaxbTestType.setIdChannel(new BigInteger(testDaoObject.getIdChannel().toString()));
			// do the providerid to provider name translation
			String providerName = findProviderNameForProviderId(testDaoObject.getIdProvider());
			jaxbTestType.setProviderName(providerName == null ? "" : providerName);
			
			jaxbTestType.setIdOrganization(testDaoObject.getIdOrganization());
			// set the main values
			if (testDaoObject.getName() != null) jaxbTestType.setName(wrapInCDATAIfNecessary(testDaoObject.getName()));
			if (testDaoObject.getDescription() != null) jaxbTestType.setDescription(wrapInCDATAIfNecessary(testDaoObject.getDescription()));
			if (testDaoObject.getText() != null) jaxbTestType.setText(wrapInCDATAIfNecessary(testDaoObject.getText()));
			if (testDaoObject.getAddlInfo() != null) jaxbTestType.setAddlInfo(wrapInCDATAIfNecessary(testDaoObject.getAddlInfo()));
			
			if (testDaoObject.getTestType() != null) jaxbTestType.setTesttype(TesttypeType.fromValue(testDaoObject.getTestType()));

			if (testDaoObject.getTestLevel() != null) jaxbTestType.setTestLevel(testDaoObject.getTestLevel());
			jaxbTestType.setTimed((testDaoObject.getTimed() != null && testDaoObject.getTimed().intValue() == 1) ? true : false);
			if (testDaoObject.getTimeToAnswer() != null) jaxbTestType.setTimeToAnswer(new BigInteger(testDaoObject.getTimeToAnswer().toString()));
			jaxbTestType.setPublished((testDaoObject.getPublished() != null && testDaoObject.getPublished().intValue() == 1) ? true : false);
			
			if (testDaoObject.getAccessLevel() != null && testDaoObject.getAccessLevel().equals(TestConstants.AccessLevelVisibility.ORGANIZATION)) {
				jaxbTestType.setTestAccessLevel(TestAccessLevelType.ORGANIZATION);
			} else if (testDaoObject.getAccessLevel() != null && testDaoObject.getAccessLevel().equals(TestConstants.AccessLevelVisibility.PUBLIC)) {
				jaxbTestType.setTestAccessLevel(TestAccessLevelType.PUBLIC);
			} else {
				jaxbTestType.setTestAccessLevel(TestAccessLevelType.PRIVATE);
			}
			
			// default IsPractice to false
			jaxbTestType.setIsPractice((testDaoObject.getIsPractice() != null && testDaoObject.getIsPractice().intValue() == 1) ? true : false);

			jaxbTestType.setQuestionCount(testDaoObject.getQuestionCount());
			jaxbTestType.setPointCount(testDaoObject.getPointCount());

			// default AutoGrade to true
			jaxbTestType.setAutoGrade((testDaoObject.getAutoGrade() != null && testDaoObject.getAutoGrade().intValue() == 0) ? false : true);
			// default AutoPublishResults to true
			jaxbTestType.setAutoPublishResults((testDaoObject.getAutoPublishResults() != null && testDaoObject.getAutoPublishResults().intValue() == 0) ? false : true);

			if (testDaoObject.getExamtrack() != null) jaxbTestType.setExamtrack(ExamtrackType.fromValue(testDaoObject.getExamtrack()));

			// default IsFree to false
			jaxbTestType.setIsFree((testDaoObject.getIsFree() != null && testDaoObject.getIsFree().intValue() == 1) ? true : false);
			
			// default ReportBySubject to false
			jaxbTestType.setReportBySubject((testDaoObject.getReportBySubject() != null && testDaoObject.getReportBySubject().intValue() == 1) ? true : false);
			
			// default CombineSections to false
			jaxbTestType.setCombineSections((testDaoObject.getCombineSections() != null && testDaoObject.getCombineSections().intValue() == 1) ? true : false);
			
			// default IsFree to false
			jaxbTestType.setIsFree((testDaoObject.getIsFree() != null && testDaoObject.getIsFree().intValue() == 1) ? true : false);
			
			// now create and attach any testsegments...
			if(testDaoObject.getTestsegments() != null && testDaoObject.getTestsegments().size() > 0) {
				TestsegmentArrayType jaxbTestsegmentArrayType = new TestsegmentArrayType();
				for (Testsegment testsegmentDaoObject : testDaoObject.getTestsegments()) {
					TestsegmentType jaxbTestsegmentType = createTestsegmentTypeFromDatabaseTestsegment(testsegmentDaoObject);
					jaxbTestsegmentArrayType.getTestsegment().add(jaxbTestsegmentType);
				}
				jaxbTestType.setTestsegmentArray(jaxbTestsegmentArrayType);
			}
		}
		return jaxbTestType;
			
	}
	
	private TestsegmentType createTestsegmentTypeFromDatabaseTestsegment(Testsegment testsegmentDaoObject) {
		TestsegmentType jaxbTestsegmentType = new TestsegmentType();
		if (testsegmentDaoObject != null) {
			// set the id's
			jaxbTestsegmentType.setIdTestsegment(testsegmentDaoObject.getIdTestsegment());
			// set the main values
			if (testsegmentDaoObject.getName() != null) jaxbTestsegmentType.setName(wrapInCDATAIfNecessary(testsegmentDaoObject.getName()));
			if (testsegmentDaoObject.getDescription() != null) jaxbTestsegmentType.setDescription(wrapInCDATAIfNecessary(testsegmentDaoObject.getDescription()));
			if (testsegmentDaoObject.getText() != null) jaxbTestsegmentType.setText(wrapInCDATAIfNecessary(testsegmentDaoObject.getText()));
			if (testsegmentDaoObject.getAddlInfo() != null) jaxbTestsegmentType.setAddlInfo(wrapInCDATAIfNecessary(testsegmentDaoObject.getAddlInfo()));
			if (testsegmentDaoObject.getSeq() != null) jaxbTestsegmentType.setSeq(testsegmentDaoObject.getSeq());
			// default Published true
			jaxbTestsegmentType.setPublished((testsegmentDaoObject.getPublished() != null && testsegmentDaoObject.getPublished().intValue() == 0) ? false : true);
			// default Sectionwrapper false
			jaxbTestsegmentType.setSectionwrapper((testsegmentDaoObject.getSectionwrapper() != null && testsegmentDaoObject.getSectionwrapper().intValue() == 1) ? true : false);
			jaxbTestsegmentType.setQuestionCount(testsegmentDaoObject.getQuestionCount());
			jaxbTestsegmentType.setPointCount(testsegmentDaoObject.getPointCount());
			if (testsegmentDaoObject.getTimeToAnswer() != null) jaxbTestsegmentType.setTimeToAnswer(new BigInteger(testsegmentDaoObject.getTimeToAnswer().toString()));
			// now create and attach any testsections...
			if(testsegmentDaoObject.getTestsections() != null && testsegmentDaoObject.getTestsections().size() > 0) {
				TestsectionArrayType jaxbTestsectionArrayType = new TestsectionArrayType();
				for (Testsection testsectionDaoObject : testsegmentDaoObject.getTestsections()) {
					TestsectionType jaxbTestsectionType = createTestsectionTypeFromDatabaseTestsection(testsectionDaoObject);
					jaxbTestsectionArrayType.getTestsection().add(jaxbTestsectionType);
				}
				jaxbTestsegmentType.setTestsectionArray(jaxbTestsectionArrayType);
			}
			// now create and attach any testsynopsislinks...
			if(testsegmentDaoObject.getTestsynopsislinks() != null && testsegmentDaoObject.getTestsynopsislinks().size() > 0) {
				TestsynopsislinkArrayType jaxbTestsynopsislinkArrayType = new TestsynopsislinkArrayType();
				for (Testsynopsislink testsynopsislinkDaoObject : testsegmentDaoObject.getTestsynopsislinks()) {
					TestsynopsislinkType jaxbTestsynopsislinkType = createTestsynopsislinkTypeFromDatabaseTestsynopsislink(testsynopsislinkDaoObject);
					jaxbTestsynopsislinkArrayType.getTestsynopsislink().add(jaxbTestsynopsislinkType);
				}
				jaxbTestsegmentType.setTestsynopsislinkArray(jaxbTestsynopsislinkArrayType);
			}
		}
		return jaxbTestsegmentType;
	}
	
	private TestsynopsislinkType createTestsynopsislinkTypeFromDatabaseTestsynopsislink(Testsynopsislink testsynopsislinkDaoObject) {
		TestsynopsislinkType jaxbTestsynopsislinkType = new TestsynopsislinkType();
		if (testsynopsislinkDaoObject != null) {
			// set the id's
			jaxbTestsynopsislinkType.setIdTestsynopsislink(testsynopsislinkDaoObject.getIdTestsynopsislink());
			jaxbTestsynopsislinkType.setIdSynopsisLinkRef(testsynopsislinkDaoObject.getIdSynopsisLinkRef());
			if (testsynopsislinkDaoObject.getName() != null) jaxbTestsynopsislinkType.setName(wrapInCDATAIfNecessary(testsynopsislinkDaoObject.getName()));
			if (testsynopsislinkDaoObject.getDescription() != null) jaxbTestsynopsislinkType.setDescription(wrapInCDATAIfNecessary(testsynopsislinkDaoObject.getDescription()));
			if (testsynopsislinkDaoObject.getLink() != null) jaxbTestsynopsislinkType.setLink(wrapInCDATAIfNecessary(testsynopsislinkDaoObject.getLink()));
			if (testsynopsislinkDaoObject.getLinkType() != null && testsynopsislinkDaoObject.getLinkType().equals(TestsectionArtifactType.SYNOPSIS_TEXT)) {
				jaxbTestsynopsislinkType.setLinkType(SynopsislinktypeType.SYNOPSIS_TEXT);
			} else if (testsynopsislinkDaoObject.getLinkType() != null && testsynopsislinkDaoObject.getLinkType().equals(TestsectionArtifactType.SYNOPSIS_VIDEO)) {
				jaxbTestsynopsislinkType.setLinkType(SynopsislinktypeType.SYNOPSIS_VIDEO);
			} else if (testsynopsislinkDaoObject.getLinkType() != null && testsynopsislinkDaoObject.getLinkType().equals(TestsectionArtifactType.SECTION)) {
				jaxbTestsynopsislinkType.setLinkType(SynopsislinktypeType.SECTION);
			} else {
				jaxbTestsynopsislinkType.setLinkType(SynopsislinktypeType.SYNOPSIS_TEXT);
			}
			if (testsynopsislinkDaoObject.getSeq() != null) jaxbTestsynopsislinkType.setSeq(testsynopsislinkDaoObject.getSeq());
		}
		return jaxbTestsynopsislinkType;
	}
	
	
	private TestsectionType createTestsectionTypeFromDatabaseTestsection(Testsection testsectionDaoObject) {
		TestsectionType jaxbTestsectionType = new TestsectionType();
		if (testsectionDaoObject != null) {
			// set the id's
			jaxbTestsectionType.setIdTestsection(testsectionDaoObject.getIdTestsection());
			jaxbTestsectionType.setIdSectionRef(new BigInteger(testsectionDaoObject.getIdSectionRef().toString()));
			if (testsectionDaoObject.getName() != null) jaxbTestsectionType.setName(wrapInCDATAIfNecessary(testsectionDaoObject.getName()));
			if (testsectionDaoObject.getDescription() != null) jaxbTestsectionType.setDescription(wrapInCDATAIfNecessary(testsectionDaoObject.getDescription()));
			if (testsectionDaoObject.getSeq() != null) jaxbTestsectionType.setSeq(testsectionDaoObject.getSeq());
			// default Published true
			if (testsectionDaoObject.getReportSubject() != null) jaxbTestsectionType.setReportSubject(testsectionDaoObject.getReportSubject());
			if (testsectionDaoObject.getTimeToAnswer() != null) jaxbTestsectionType.setTimeToAnswer(new BigInteger(testsectionDaoObject.getTimeToAnswer().toString()));
			if (testsectionDaoObject.getPointsPerQuestion() != null) jaxbTestsectionType.setPointsPerQuestion(testsectionDaoObject.getPointsPerQuestion());
			if (testsectionDaoObject.getNegativePointsPerQuestion() != null) jaxbTestsectionType.setNegativePointsPerQuestion(testsectionDaoObject.getNegativePointsPerQuestion());
			if (testsectionDaoObject.getUnansweredPointsPerQuestion() != null) jaxbTestsectionType.setUnansweredPointsPerQuestion(testsectionDaoObject.getUnansweredPointsPerQuestion());
			if (testsectionDaoObject.getQuestionStartIndex() != null) jaxbTestsectionType.setQuestionStartIndex(testsectionDaoObject.getQuestionStartIndex());
			// default Sectionwrapper false
			jaxbTestsectionType.setDistributedScoring((testsectionDaoObject.getDistributedScoring() != null && testsectionDaoObject.getDistributedScoring().intValue() == 1) ? true : false);
			jaxbTestsectionType.setQuestionCount(testsectionDaoObject.getQuestionCount());
			jaxbTestsectionType.setPointCount(testsectionDaoObject.getPointCount());
			// set any instructions name
			if (testsectionDaoObject.getInstructionsName() != null) jaxbTestsectionType.setInstructionsName(testsectionDaoObject.getInstructionsName());
		}
		return jaxbTestsectionType;
	}

	
	
	@Override
	public GetTestsResponse getTests(String getTestsRequestSerialized)
			throws JAXBException {
		// unmarshall to get the payload request object -
		GetTestsRequest getTestsRequest = ((JAXBElement<GetTestsRequest>) getPayloadUnmarshaller(GetTestsRequest.class)
				.unmarshal(new StringReader(getTestsRequestSerialized))).getValue();
		// now call the other method that takes a request object
		return getTests(getTestsRequest);
	}
	@Override
	public String getTestsSerialized(GetTestsRequest getTestsRequest)
			throws JAXBException {
		GetTestsResponse getTestsResponse = getTests(getTestsRequest);
		// marshall to create the payload response object -
		Writer stringWriter = new StringWriter();
		Marshaller marshaller = getPayloadMarshaller(GetTestsResponse.class);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(getTestsResponse, stringWriter);
		// Now teh response is good to go, however, it has the "<" and ">" characters in content escaped into "&lt;" and "&gt;".  
		// There are different ways to fix the problem....  Mine may not be the most efficient, but it works.
		// look for a more optimal solution at: "http://stackoverflow.com/questions/3136375/how-to-generate-cdata-block-using-jaxb"
		String responseString = stringWriter.toString().replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;nbsp;", "&nbsp;");
		// log the response message
		
		if (log.isDebugEnabled()) {
			log.debug("Serialized Tests: " + responseString);
		}
		return responseString;
	}
	@Override
	public String getTestsSerialized(String getTestsRequestSerialized)
			throws JAXBException {
		// unmarshall to get the payload request object -
		GetTestsRequest getTestsRequest = (GetTestsRequest) getPayloadUnmarshaller(GetTestsRequest.class).unmarshal(new StringReader(getTestsRequestSerialized));
		return getTestsSerialized(getTestsRequest);
	}
	
	// This Converts an Downloaded file to a Uploadable file
	private String convertGetTestsResponseToSaveTestsRequest(String fileString) {
		if (fileString == null) {
			return null;
		}
		return fileString.replaceAll("GetTestsResponse", "SaveTestsRequest");
	}
	
	@Override
	public SaveTestsResponse saveTests(String testsPayloadString, String forcedLoginUsername, boolean validateOnlyOverride,	boolean reloadOverride) throws JAXBException {
		SaveTestsResponse response = null;
		// see if the message is of the "GetTestsResponse" veriety (indicating a previously downloaded file).  If 
		// so convert it to a "SaveTestsRequest"
		if (testsPayloadString.contains("GetTestsResponse")) {
			testsPayloadString = convertGetTestsResponseToSaveTestsRequest(testsPayloadString);
		}

		// get rid of any double escaped space
		if (testsPayloadString.contains("&amp;nbsp;")) {
			testsPayloadString = testsPayloadString.replaceAll("&amp;nbsp;", "&nbsp;");
		}

		// check and get rid of any SOAP envelope.
		String testsPayloadStringTrimmed = trimSaveTestsSoapHeadersWithSAX(testsPayloadString);
		//SaveSectionsRequest
		if (testsPayloadStringTrimmed != null) {
			// log the "trimmed" XML message
			if (log.isDebugEnabled()) {
				log.debug("testsPayload:");
				Writer writer = new StringWriter();
				getPayloadMarshaller(SaveTestsRequest.class).marshal(testsPayloadStringTrimmed, writer);
				log.debug("Request: " + writer.toString());
			}
			
			// unmarshall to get the payload request object -
			SaveTestsRequest testsPayload = (SaveTestsRequest) getPayloadUnmarshaller(SaveTestsRequest.class).unmarshal(new StringReader(testsPayloadStringTrimmed));

			// call the method to process the payload
			response = saveTests(testsPayload, forcedLoginUsername, validateOnlyOverride, reloadOverride);
		} else {
			// create a blank response - indicating Another kind of error
			response = new SaveTestsResponse();
			response.setStatus(-2);
			response.setMessage("SaveTests Node not found in XML");
		}

		if (response == null) {
			// create a blank response
			response = new SaveTestsResponse();
			response.setStatus(-1);
			response.setMessage(UPLOAD_UNKNOWN_FAILURE_MESSAGE);
		}
		return response;
	}
	
	/**
	 * Note that this method takes a File containing a SOAP wrapper (in a string format) and tries to get the relevant 
	 * LoadSectionsRequest node from it.  It returns a string version of the node.
	 * @param fileString
	 * @return
	 */
	private static String trimSaveTestsSoapHeadersWithSAX(String fileString) {
		if (fileString != null && fileString.contains("http://schemas.xmlsoap.org/soap/envelope/")) {
			return getSubNodeWithSAX(fileString, "//SaveTestsRequest");
		} else {
			return fileString;
		}
	}

	@Override
	public String saveTestsWithStringResponse(String testsPayloadString, String forcedLoginUsername, boolean validateOnlyOverride, boolean reloadOverride) throws JAXBException {
		SaveTestsResponse response = saveTests(testsPayloadString, forcedLoginUsername, validateOnlyOverride, reloadOverride);
		// marshall to create the payload response object -
		Writer writer = new StringWriter();
		Marshaller marshaller = getPayloadMarshaller(LoadSectionsResponse.class);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		getPayloadMarshaller(LoadSectionsResponse.class).marshal(response, writer);

		return writer.toString();
	}

	
	@Override
	public SaveTestsResponse saveTests(SaveTestsRequest saveTestsRequest) {
		return saveTests(saveTestsRequest, JdbcDaoStaticHelper.getCurrentUserName(), false, false);
	}
	
	@Override
	public SaveTestsResponse saveTests(
			SaveTestsRequest saveTestsRequest, String forcedLoginUsername, boolean validateOnlyOverride, boolean reloadOverride) {
		
		Long idProviderOverride = null;
		String providerNameOverride = saveTestsRequest.getProviderNameOverride();
		if (providerNameOverride != null) {
			idProviderOverride = findProviderIdForProvider(providerNameOverride);
			if (idProviderOverride == null) {
				throw new RfDataloadException("Invalid ProviderName Override: '" + providerNameOverride + "'");
			}
		}

		// first get a list of practice sections - totally filled with questions and answers
		List<Test> tests = parseTestsPayloadIntoDamainData(saveTestsRequest, idProviderOverride);
		
		// log the created sections
//		logger.debug(sections);
		
		// save the data to the database
		List<UpdateStatusBean> updateStatuses = null;
		boolean operationSuccess = true;
		try {
			if (validateOnlyOverride || !saveTestsRequest.isValidateOnly()) {
				updateStatuses = testDao.insertTestBatch(tests, reloadOverride || saveTestsRequest.isReload());
				// see if any of the statuses are not 0.  In which case set the response as FAIL
				if (updateStatuses != null && updateStatuses.size() > 0) {
					for (UpdateStatusBean updateStatus : updateStatuses) {
						if (updateStatus.hasFailed()) {
							operationSuccess = false;
							break;
						}
					}
				}
			}
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			operationSuccess = false;
			throw new RfDataloadException(e.getMessage(), e);
		}

		// create the response
		SaveTestsResponse response = new SaveTestsResponse();
		if (operationSuccess) {
			response.setStatus(UPLOAD_SUCCESS_STATUS_CODE);
			response.setMessage(UPLOAD_SUCCESS_MESSAGE + (updateStatuses == null ? "" : "\n" + updateStatuses.toString()));
		} else {
			response.setStatus(UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			response.setMessage(UPLOAD_UNKNOWN_FAILURE_MESSAGE + (updateStatuses == null ? "" : "\n" + updateStatuses.toString()));
		}

		return response;
	}
	private List<Test> parseTestsPayloadIntoDamainData(SaveTestsRequest loadTestsRequest, Long idProviderOverride) {
		List<Test> testDaoList = null;
		
		TestArrayType testArrayType = loadTestsRequest.getTestArray();
		
		if (testArrayType != null && testArrayType.getTest() != null && testArrayType.getTest().size() > 0) {
			// create the section dao list
			testDaoList = new ArrayList<Test>();
			List<TestType> jaxbTestList = testArrayType.getTest();
			for (int i = 0; i < jaxbTestList.size(); i++) {
				TestType jaxbTestType = jaxbTestList.get(i);
				// create the section dao object
				Test testDaoObject = createDatabaseTest (jaxbTestType, idProviderOverride);
				// add test to test list
				testDaoList.add(i, testDaoObject);
			}
		}
		return testDaoList;
	}
	private Test createDatabaseTest(TestType jaxbTestType, Long idProviderOverride) {
		Test testDaoObject = new Test();
		// set the student topic id
		testDaoObject.setIdTest(jaxbTestType.getIdTest());
		testDaoObject.setIdChannel(jaxbTestType.getIdChannel().longValue());

		// see if we have an override provider...if we do, set to that provider
		if (idProviderOverride != null) {
			testDaoObject.setIdProvider(idProviderOverride);
		} else {
			// do the provider name to id tranalation
			String providerName = jaxbTestType.getProviderName();
			Long idProvider = findProviderIdForProvider(providerName);
			if (idProvider == null) {
				throw new RfDataloadException("Invalid Provider Name: '" + providerName + "'.  Please fix the Provider name before uploading Tests.");
			}
			testDaoObject.setIdProvider(idProvider);
		}
		
		testDaoObject.setIdOrganization(jaxbTestType.getIdOrganization());
		testDaoObject.setName(escapeDatabaseSingleQuotes(jaxbTestType.getName()));
		testDaoObject.setDescription(escapeDatabaseSingleQuotes(jaxbTestType.getDescription()));
		testDaoObject.setText(escapeDatabaseSingleQuotes(jaxbTestType.getText()));
		testDaoObject.setAddlInfo(escapeDatabaseSingleQuotes(jaxbTestType.getAddlInfo()));
		testDaoObject.setTestType(jaxbTestType.getTesttype().value());
		testDaoObject.setTestLevel(jaxbTestType.getTestLevel());
		testDaoObject.setTimed(jaxbTestType.isTimed() ? 1 : 0);
		// default to 10 mins
		testDaoObject.setTimeToAnswer(jaxbTestType.getTimeToAnswer() != null ? jaxbTestType.getTimeToAnswer().intValue() : 10);
		testDaoObject.setPublished(jaxbTestType.isPublished() ? 1 : 0);
		if (jaxbTestType.getTestAccessLevel() != null) {
			if (jaxbTestType.getTestAccessLevel().equals(TestAccessLevelType.PUBLIC)) {
				testDaoObject.setAccessLevel(TestConstants.AccessLevelVisibility.PUBLIC.visibility());
			} else if (jaxbTestType.getTestAccessLevel().equals(TestAccessLevelType.ORGANIZATION)) {
				testDaoObject.setAccessLevel(TestConstants.AccessLevelVisibility.ORGANIZATION.visibility());
			} else {
				testDaoObject.setAccessLevel(TestConstants.AccessLevelVisibility.PRIVATE.visibility());
			}
		} else {
			testDaoObject.setAccessLevel(TestConstants.AccessLevelVisibility.PRIVATE.visibility());
		}
		testDaoObject.setIsPractice(jaxbTestType.isIsPractice() ? 1 : 0);
		testDaoObject.setQuestionCount(jaxbTestType.getQuestionCount());
		testDaoObject.setPointCount(jaxbTestType.getPointCount());
		testDaoObject.setAutoGrade(jaxbTestType.isAutoGrade() ? 1 : 0);
		testDaoObject.setAutoPublishResults(jaxbTestType.isAutoPublishResults() ? 1 : 0);
		testDaoObject.setExamtrack(jaxbTestType.getExamtrack().value());
		testDaoObject.setIsFree(jaxbTestType.isIsFree() ? 1 : 0);
		testDaoObject.setReportBySubject(jaxbTestType.isReportBySubject() ? 1 : 0);
		testDaoObject.setCombineSections(jaxbTestType.isCombineSections() ? 1 : 0);
		// create any test segments
		if (jaxbTestType.getTestsegmentArray() != null && jaxbTestType.getTestsegmentArray().getTestsegment() != null && jaxbTestType.getTestsegmentArray().getTestsegment().size() > 0) {
			// create the testsegments dao list
			List<Testsegment> testsegmentDaoObjectList = new ArrayList<Testsegment>();
			List<TestsegmentType> jaxbTestsegmentTypeList = jaxbTestType.getTestsegmentArray().getTestsegment();
			for (int j = 0; j < jaxbTestsegmentTypeList.size(); j++) {
				TestsegmentType jaxbTestsegmentType = jaxbTestsegmentTypeList.get(j);
				// create the question dao object
				Testsegment testsegmentDaoObject = createDatabaseTestsegmentObject (jaxbTestsegmentType);
				// add question to question list
				testsegmentDaoObjectList.add(testsegmentDaoObject);
			}
			testDaoObject.setTestsegments(testsegmentDaoObjectList);
		}
		return testDaoObject;
	}
		
	private Testsegment createDatabaseTestsegmentObject(TestsegmentType jaxbTestsegmentType) {
		Testsegment testsegmentDaoObject = new Testsegment();
		// set the testsegment id
		testsegmentDaoObject.setIdTestsegment(jaxbTestsegmentType.getIdTestsegment());
		testsegmentDaoObject.setName(escapeDatabaseSingleQuotes(jaxbTestsegmentType.getName()));
		testsegmentDaoObject.setDescription(escapeDatabaseSingleQuotes(jaxbTestsegmentType.getDescription()));
		testsegmentDaoObject.setText(escapeDatabaseSingleQuotes(jaxbTestsegmentType.getText()));
		testsegmentDaoObject.setAddlInfo(escapeDatabaseSingleQuotes(jaxbTestsegmentType.getAddlInfo()));
		testsegmentDaoObject.setSeq(jaxbTestsegmentType.getSeq());
		testsegmentDaoObject.setPublished(jaxbTestsegmentType.isPublished() ? 1 : 0);
		testsegmentDaoObject.setSectionwrapper(jaxbTestsegmentType.isSectionwrapper() ? 1 : 0);
		testsegmentDaoObject.setQuestionCount(jaxbTestsegmentType.getQuestionCount());
		testsegmentDaoObject.setPointCount(jaxbTestsegmentType.getPointCount());
		testsegmentDaoObject.setTimeToAnswer(jaxbTestsegmentType.getTimeToAnswer().intValue());
		// now create and attach any testsections...
		if(jaxbTestsegmentType.getTestsectionArray() != null && jaxbTestsegmentType.getTestsectionArray().getTestsection() != null && jaxbTestsegmentType.getTestsectionArray().getTestsection().size() > 0) {
			// create the testsections dao list
			List<Testsection> testsectionDaoObjectList = new ArrayList<Testsection>();
			for (TestsectionType jaxbTestsectionType : jaxbTestsegmentType.getTestsectionArray().getTestsection()) {
				// create the question dao object
				Testsection testsectionDaoObject = createDatabaseTestsectionObject (jaxbTestsectionType);
				// add question to question list
				testsectionDaoObjectList.add(testsectionDaoObject);
			}
			testsegmentDaoObject.setTestsections(testsectionDaoObjectList);
		}
		// now create and attach any testsynopsislinks...
		if(jaxbTestsegmentType.getTestsynopsislinkArray() != null && jaxbTestsegmentType.getTestsynopsislinkArray().getTestsynopsislink() != null && jaxbTestsegmentType.getTestsynopsislinkArray().getTestsynopsislink().size() > 0) {
			// create the testsynopsislink dao list
			List<Testsynopsislink> testsynopsislinkDaoObjectList = new ArrayList<Testsynopsislink>();
			for (TestsynopsislinkType jaxbTestsynopsislinkType : jaxbTestsegmentType.getTestsynopsislinkArray().getTestsynopsislink()) {
				// create the question dao object
				Testsynopsislink testsynopsislinkDaoObject = createDatabaseTestsynopsislinkObject (jaxbTestsynopsislinkType);
				// add question to question list
				testsynopsislinkDaoObjectList.add(testsynopsislinkDaoObject);
			}
			testsegmentDaoObject.setTestsynopsislinks(testsynopsislinkDaoObjectList);
		}
		
		return testsegmentDaoObject;
	}

	private Testsynopsislink createDatabaseTestsynopsislinkObject(
			TestsynopsislinkType jaxbTestsynopsislinkType) {
		Testsynopsislink testsynopsislinkDaoObject = new Testsynopsislink();
		// set the testsection id
		testsynopsislinkDaoObject.setIdTestsynopsislink(jaxbTestsynopsislinkType.getIdTestsynopsislink());
		testsynopsislinkDaoObject.setIdSynopsisLinkRef(jaxbTestsynopsislinkType.getIdSynopsisLinkRef());
		testsynopsislinkDaoObject.setName(escapeDatabaseSingleQuotes(jaxbTestsynopsislinkType.getName()));
		testsynopsislinkDaoObject.setDescription(escapeDatabaseSingleQuotes(jaxbTestsynopsislinkType.getDescription()));
		testsynopsislinkDaoObject.setSeq(jaxbTestsynopsislinkType.getSeq());
		testsynopsislinkDaoObject.setLink(jaxbTestsynopsislinkType.getLink());
		if (jaxbTestsynopsislinkType.getLinkType() != null) {
			if (jaxbTestsynopsislinkType.getLinkType().equals(SynopsislinktypeType.SYNOPSIS_TEXT)) {
				testsynopsislinkDaoObject.setLinkType(TestConstants.TestsectionArtifactType.SYNOPSIS_TEXT.artifactType());	
			} else if (jaxbTestsynopsislinkType.getLinkType().equals(SynopsislinktypeType.SYNOPSIS_VIDEO)) {
				testsynopsislinkDaoObject.setLinkType(TestConstants.TestsectionArtifactType.SYNOPSIS_VIDEO.artifactType());
			} else if (jaxbTestsynopsislinkType.getLinkType().equals(SynopsislinktypeType.SECTION)) {
				testsynopsislinkDaoObject.setLinkType(TestConstants.TestsectionArtifactType.SECTION.artifactType());
			}
		} else {
			testsynopsislinkDaoObject.setLinkType(TestConstants.TestsectionArtifactType.SECTION.artifactType());
		}
		return testsynopsislinkDaoObject;
	}
	
	// set the section 
	private Testsection createDatabaseTestsectionObject(TestsectionType jaxbTestsectionType) {
		Testsection testsectionDaoObject = new Testsection();
		// set the testsection id
		testsectionDaoObject.setIdTestsection(jaxbTestsectionType.getIdTestsection());
		testsectionDaoObject.setIdSectionRef(jaxbTestsectionType.getIdSectionRef().longValue());
		testsectionDaoObject.setName(escapeDatabaseSingleQuotes(jaxbTestsectionType.getName()));
		testsectionDaoObject.setDescription(escapeDatabaseSingleQuotes(jaxbTestsectionType.getDescription()));
		testsectionDaoObject.setSeq(jaxbTestsectionType.getSeq());
		testsectionDaoObject.setReportSubject(jaxbTestsectionType.getReportSubject());
		testsectionDaoObject.setTimeToAnswer(jaxbTestsectionType.getTimeToAnswer().intValue());
		testsectionDaoObject.setPointsPerQuestion(jaxbTestsectionType.getPointsPerQuestion());
		testsectionDaoObject.setNegativePointsPerQuestion(jaxbTestsectionType.getNegativePointsPerQuestion());
		testsectionDaoObject.setUnansweredPointsPerQuestion(jaxbTestsectionType.getUnansweredPointsPerQuestion());
		testsectionDaoObject.setQuestionStartIndex(jaxbTestsectionType.getQuestionStartIndex());
		testsectionDaoObject.setDistributedScoring(jaxbTestsectionType.isDistributedScoring() ? 1 : 0);
		testsectionDaoObject.setQuestionCount(jaxbTestsectionType.getQuestionCount());
		testsectionDaoObject.setPointCount(jaxbTestsectionType.getPointCount());
		testsectionDaoObject.setInstructionsName(jaxbTestsectionType.getInstructionsName());
		return testsectionDaoObject;
	}
	
	
	
	
	
	
	
	@Override
	public LoadInstructionsResponse loadInstructions(
			LoadInstructionsRequest loadInstructionsRequest) {
		
		TestsectionInstructions testsectionInstructions = new TestsectionInstructions();
		testsectionInstructions.setInstructionsName(escapeDatabaseSingleQuotes(loadInstructionsRequest.getInstructionsName()));
		testsectionInstructions.setDescription(escapeDatabaseSingleQuotes(loadInstructionsRequest.getDescription()));
		testsectionInstructions.setText(escapeDatabaseSingleQuotes(loadInstructionsRequest.getText()));
		testsectionInstructions.setAddlInfo(escapeDatabaseSingleQuotes(loadInstructionsRequest.getAddlInfo()));

		// save the data to the database
		try {
			if (loadInstructionsRequest.isValidateOnly() == false) {
				testsectionInstructionsDao.updateTestsectionInstructions(testsectionInstructions);
			} 
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			throw new RfDataloadException(e.getMessage(), e);
		}

		// create the response
		LoadInstructionsResponse response = new LoadInstructionsResponse();
		response.setStatus(UPLOAD_SUCCESS_STATUS_CODE);
		response.setMessage(UPLOAD_SUCCESS_MESSAGE);

		return response;
	}
	
	@Override
	public GetInstructionsResponse getInstructions(
			GetInstructionsRequest getInstructionsRequest) {

		TestsectionInstructions testsectionInstructions = testsectionInstructionsDao.getTestsectionInstructions(getInstructionsRequest.getInstructionsName());
		// build a response
		GetInstructionsResponse getInstructionsResponse = new GetInstructionsResponse();
		if (testsectionInstructions != null) {
			getInstructionsResponse.setInstructionsName(wrapInCDATAIfNecessary(testsectionInstructions.getInstructionsName()));
			getInstructionsResponse.setDescription(wrapInCDATAIfNecessary(testsectionInstructions.getDescription()));
			getInstructionsResponse.setText(wrapInCDATAIfNecessary(testsectionInstructions.getText()));
			getInstructionsResponse.setAddlInfo(wrapInCDATAIfNecessary(testsectionInstructions.getAddlInfo()));
		}
		return getInstructionsResponse;
	}
}
