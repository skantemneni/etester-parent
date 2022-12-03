package com.etester.dataloader.util;

import com.rulefree.rfloaddataschema.GetInstructionsRequest;
import com.rulefree.rfloaddataschema.GetInstructionsResponse;
import com.rulefree.rfloaddataschema.GetSectionsRequest;
import com.rulefree.rfloaddataschema.GetSectionsResponse;
import com.rulefree.rfloaddataschema.GetTestsRequest;
import com.rulefree.rfloaddataschema.GetTestsResponse;
import com.rulefree.rfloaddataschema.GetWordsRequest;
import com.rulefree.rfloaddataschema.GetWordsResponse;
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
import com.rulefree.rfloaddataschema.SaveTestsRequest;
import com.rulefree.rfloaddataschema.SaveTestsResponse;
import com.rulefree.rfloaddataschema.UpdateWordsRequest;
import com.rulefree.rfloaddataschema.UpdateWordsResponse;

import jakarta.xml.bind.JAXBException;

public interface DataloaderService {

	public static final int UPLOAD_SUCCESS_STATUS_CODE = 0;
	public static final String UPLOAD_SUCCESS_MESSAGE = "Success!";
	public static final int UPLOAD_UNKNOWN_FAILURE_STATUS_CODE = -1;
	public static final String UPLOAD_UNKNOWN_FAILURE_MESSAGE = "Unknown Failure.";

	/**
	 * This call is invoked by the DataloaderEndpoints class on receiving a message that has a payload 
	 * with GetSectionsRequest as a root node.  Will throw a RfDataloadParseException if there is any 
	 * issue in successfully delivering the payload.  
	 * This method can also be invoked by a java main program that takes on the responsibility of reading 
	 * the response and serializing it to a file. 
	 *     
	 * @param getSectionsRequest - A GetSectionsRequest object containing the criteria used to derive 
	 * 								the sections - with	questions and answers - to download. 
	 * @return GetSectionsResponse - A GetSectionsResponse object containing the requested sections.   
	 */
	public GetSectionsResponse getSections(GetSectionsRequest getSectionsRequest); 
	
	public GetSectionsResponse getSections(String getSectionsRequestSerialized) throws JAXBException; 
	
	/**
	 * This call is invoked by the DataloaderEndpoints class on receiving a message that has a payload 
	 * with GetSectionsRequest as a root node.  Will throw a RfDataloadParseException if there is any 
	 * issue in successfully delivering the payload.  
	 * This method can also be invoked by a java main program that takes on the responsibility of reading 
	 * the response and serializing it to a file. 
	 *     
	 * @param getSectionsRequest - A GetSectionsRequest object containing the criteria used to derive 
	 * 								the sections - with	questions and answers - to download. 
	 * @return GetSectionsResponse - A GetSectionsResponse object containing the requested sections.   
	 */
	public String getSectionsSerialized (GetSectionsRequest getSectionsRequest) throws JAXBException;
	
	public String getSectionsSerialized (String getSectionsRequestSerialized) throws JAXBException; 
	
	/**
	 * This call is typically invoked by the DataloaderEndpoints class on receiving a 
	 * message that has a payload with LoadSectionsRequest as a root node.  Will throw 
	 * a RfDataloadParseException if a Level, Topic or Skill cannot be successfully 
	 * derived from the data contained in the payload.
	 * This method can also be invoked by a java main program that takes on the responsibility 
	 * of reading a file and parsing it into the LoadSectionsRequest payload.
	 *     
	 * @param sectionsPayload - A LoadSectionsRequest object containing sections with 
	 * 							questions and answers.  Can also simply have Sections Metadata
	 * @return LoadSectionsResponse - object containing a status code.  0 = success.   
	 */
	public LoadSectionsResponse saveSections(LoadSectionsRequest sectionsPayload/*, MessageContext messageContext*/); 

	/**
	 * Same as the above message, but to be executed by a local program that forces a login user when there 
	 * is no login session.
	 * @param sectionsPayload
	 * @param forcedLoginUsername
	 * @return
	 */
	public LoadSectionsResponse saveSections(LoadSectionsRequest sectionsPayload, String forcedLoginUsername, boolean validateOnlyOverride, boolean reloadOverride); 

	/**
	 * This method is typically invoked by a java main program that takes on the responsibility 
	 * of reading a file containing data that can be successfully parsed into a LoadSectionsRequest 
	 * payload.  Will throw a RfDataloadParseException if a Level, Topic or Skill cannot be 
	 * successfully derived from the data contained in the payload.
	 * It responds with a serialized string version of the LoadSectionsResponse status. 
	 *     
	 * @param sectionsPayloadString - A string that can be parsed into LoadSectionsRequest object
	 * @return String 				- Serialized string version of LoadSectionsResponse object 
	 * 								  containing a status code.  0 = success.    
	 */
	public LoadSectionsResponse saveSections(String sectionsPayloadString, String forcedLoginUsername, boolean validateOnlyOverride, boolean reloadOverride) throws JAXBException; 

	/**
	 * This method is typically invoked by a java main program that takes on the responsibility 
	 * of reading a file containing data that can be successfully parsed into a LoadSectionsRequest 
	 * payload.  Will throw a RfDataloadParseException if a Level, Topic or Skill cannot be 
	 * successfully derived from the data contained in the payload.
	 * It responds with a serialized string version of the LoadSectionsResponse status. 
	 *     
	 * @param sectionsPayloadString - A string that can be parsed into LoadSectionsRequest object
	 * @return String 				- Serialized string version of LoadSectionsResponse object 
	 * 								  containing a status code.  0 = success.    
	 */
	public String saveSectionsWithStringResponse(String sectionsPayloadString, String forcedLoginUsername, boolean validateOnlyOverride, boolean reloadOverride) throws JAXBException; 

	/**
	 * Similar to the 'saveSections' call, except the payload in this case is a LoadSkillsRequest.
	 *     
	 * @param skillsPayload - A LoadSkillsRequest object containing skills with sections, 
	 * 						  questions and answers.  Can also simply have Skills Metadata.
	 * @return LoadSkillsResponse - object containing a status code.  0 = success.   
	 */
	public LoadSkillsResponse saveSkills(LoadSkillsRequest skillsPayload); 

	/**
	 * Similar to the 'saveSections' call, except the payload in this case is a string version 
	 * of a LoadSkillsRequest. 
	 *     
	 * @param skillsPayloadString - A String version of LoadSkillsRequest object containing skills with 
	 * 						  	    sections, questions and answers.  Can also simply have Skills Metadata.
	 * @return String 			  - Serialized string version of LoadSkillsResponse object 
	 * 								containing a status code.  0 = success.    
	 */
	public String saveSkills(String skillsPayloadString) throws JAXBException; 

	/**
	 * Similar to the 'saveSections' call, except the payload in this case is a LoadSkillsOnlyRequest.
	 *     
	 * @param skillsOnlyPayload - A LoadSkillsOnlyRequest object containing skills without sections, 
	 * 						  	questions and answers.  
	 * @return LoadSkillsOnlyResponse - object containing a status code.  0 = success.   
	 */
	public LoadSkillsOnlyResponse saveSkillsOnly(LoadSkillsOnlyRequest skillsOnlyPayload); 

	/**
	 * Similar to the 'saveSections' call, except the payload in this case is a LoadTopicsRequest.
	 *     
	 * @param topicsPayload - A LoadTopicsRequest object containing topics with skills, sections,  
	 * 						  questions and answers.  Can also simply have Topics Metadata.
	 * @return LoadTopicsResponse - object containing a status code.  0 = success.   
	 */
	public LoadTopicsResponse saveTopics(LoadTopicsRequest topicsPayload); 

	/**
	 * Similar to the 'saveSections' call, except the payload in this case is a string version 
	 * of a LoadTopicsRequest. 
	 *     
	 * @param topicsPayloadString - A String version of LoadTopicsRequest object containing topics with skills,  
	 * 						  	    sections, questions and answers.  Can also simply have Topics Metadata.
	 * @return String 			  - Serialized string version of LoadTopicsResponse object 
	 * 								containing a status code.  0 = success.    
	 */
	public String saveTopics(String topicsPayloadString) throws JAXBException; 

	/**
	 * Similar to the 'saveSections' call, except the payload in this case is a LoadTopicsOnlyRequest.
	 *     
	 * @param topicsOnlyPayload - A LoadTopicsOnlyRequest object containing topics with skillsOnly - no sections,  
	 * 						  		questions and answers.  Can also simply have TopicsOnly Metadata.
	 * @return LoadTopicsOnlyResponse - object containing a status code.  0 = success.   
	 */
	public LoadTopicsOnlyResponse saveTopicsOnly(LoadTopicsOnlyRequest topicsOnlyPayload); 

	/**
	 * Similar to the 'saveSections' call, except the payload in this case is a LoadLevelsRequest.
	 *     
	 * @param levelsPayload - A LoadLevelsRequest object containing levels with topics, skills,   
	 * 						  sections, questions and answers.  Can also simply have Levels Metadata.
	 * @return LoadLevelsResponse - object containing a status code.  0 = success.   
	 */
	public LoadLevelsResponse saveLevels(LoadLevelsRequest levelsPayload); 

	/**
	 * Similar to the 'saveSections' call, except the payload in this case is a string version 
	 * of a LoadLevelsOnlyRequest. 
	 *     
	 * @param levelsPayloadString - A String version of LoadLevelsOnlyRequest object containing levels with topics,
	 * 						  	    skills, sections, questions and answers.  Can also simply have Levels Metadata.
	 * @return String 			  - Serialized string version of LoadLevelsResponse object 
	 * 								containing a status code.  0 = success.    
	 */
	public String saveLevels(String levelsPayloadString) throws JAXBException; 

	/**
	 * Similar to the 'saveSections' call, except the payload in this case is a LoadLevelsOnlyRequest.
	 *     
	 * @param levelsOnlyPayload - A LoadLevelsOnlyRequest object containing levelsOnly
	 * @return LoadLevelsOnlyResponse - object containing a status code.  0 = success.   
	 */
	public LoadLevelsOnlyResponse saveLevelsOnly(LoadLevelsOnlyRequest levelsOnlyPayload); 

	/**
	 * Similar to the 'saveWordlists' call, except the payload in this case is a LoadWordlistsRequest.
	 *     
	 * @param wordlistsPayload - A LoadWordlistsRequest object containing wordlists with words, passages, questions
	 * @return LoadWordlistsResponse - object containing a status code.  0 = success.   
	 */
	public LoadWordlistsResponse saveWordlists(LoadWordlistsRequest wordlistsPayload); 

	/**
	 * Similar to the 'saveSections' call, except the payload in this case is a string version 
	 * of a LoadLevelsOnlyRequest. 
	 *     
	 * @param wordlistsPayloadString - A String version of LoadWordlistsRequest object containing wordlists with words, passages, questions
	 * @return String 			  - Serialized string version of LoadWordlistsResponse object 
	 * 								containing a status code.  0 = success.    
	 */
	public String saveWordlists(String wordlistsPayloadString) throws JAXBException; 

	/**
	 * Similar to the 'saveWordlists' call, except the payload in this case is a UpdateWordsRequest.
	 *     
	 * @param wordsPayload - A UpdateWordsRequest object containing words that need to be updated in the database
	 * @return UpdateWordsResponse - object containing a status code.  0 = success.   
	 */
	public UpdateWordsResponse updateWords(UpdateWordsRequest wordsPayload); 

	/**
	 * Similar to the 'updateWords' call, except the payload in this case is a string version 
	 * of a UpdateWordsRequest. 
	 *     
	 * @param wordsPayloadString - A String version of UpdateWordsRequest object containing words
	 * @return String 			  - Serialized string version of UpdateWordsResponse object 
	 * 								containing a status code.  0 = success.    
	 */
	public String updateWords(String wordsPayloadString) throws JAXBException; 

	/**
	 * Similar to the 'saveWordlists' call, except the payload in this case is a GetWordsRequest.
	 *     
	 * @param getWordsPayload - A GetWordsRequest object containing information that is needed to retrieve and return words from the database
	 * @return GetWordsResponse - object containing the list of words in all their glory.   
	 */
	public GetWordsResponse getWords(GetWordsRequest getWordsPayload); 

	/**
	 * Similar to the 'updateWords' call, except the payload in this case is a string version 
	 * of a UpdateWordsRequest. 
	 *     
	 * @param getWordsPayloadString - A String version of GetWordsRequest object containing information that is needed to retrieve and return words from the database
	 * @return String 			  - Serialized string version of GetWordsResponse object 
	 * 								containing the list of words in all their glory.   
	 */
	public String getWords(String getWordsPayloadString) throws JAXBException;

	
	
	
	
	
	
	
	/**
	 * This call is invoked by the DataloaderEndpoints class on receiving a message that has a payload 
	 * with GetTestsRequest as a root node.  Will throw a RfDataloadParseException if there is any 
	 * issue in successfully delivering the payload.  
	 * This method can also be invoked by a java main program that takes on the responsibility of reading 
	 * the response and serializing it to a file. 
	 *     
	 * @param getTestsRequest - A GetTestsRequest object containing the criteria used to derive 
	 * 								the tests - with testsegments and testsections. 
	 * @return GetSectionsResponse - A GetSectionsResponse object containing the requested sections.   
	 */
	public GetTestsResponse getTests(GetTestsRequest getTestsRequest);
	
	public GetTestsResponse getTests(String getTestsRequestSerialized) throws JAXBException; 
	
	public String getTestsSerialized (GetTestsRequest getTestsRequest) throws JAXBException;
	
	public String getTestsSerialized (String getTestsRequestSerialized) throws JAXBException; 
	
	/**
	 * This call is typically invoked by the DataloaderEndpoints class on receiving a 
	 * message that has a payload with SaveTestsRequest as a root node.  Will throw 
	 * a RfDataloadParseException if a Section referred (id_section_ref) in any of the 
	 * Testsections cannot be found in the system. 
	 * This method can also be invoked by a java main program that takes on the responsibility 
	 * of reading a file and parsing it into the LoadSectionsRequest payload.
	 *     
	 * @param saveTestsRequest - A LoadSectionsRequest object containing sections with 
	 * 							questions and answers.  Can also simply have Sections Metadata
	 * @return SaveTestsResponse - object containing a status code.  0 = success.   
	 */
	public SaveTestsResponse saveTests(SaveTestsRequest saveTestsRequest);

	public SaveTestsResponse saveTests(SaveTestsRequest saveTestsRequest,
			String forcedLoginUsername, boolean validateOnlyOverride,
			boolean reloadOverride);

	public SaveTestsResponse saveTests(String testsPayloadString, String forcedLoginUsername, boolean validateOnlyOverride, boolean reloadOverride) throws JAXBException; 
	
	public String saveTestsWithStringResponse(String testsPayloadString, String forcedLoginUsername, boolean validateOnlyOverride, boolean reloadOverride) throws JAXBException; 
	
	
	
	/**
	 * Load and Get Instructions stuff
	 * @param loadInstructionsRequest
	 * @return
	 */
	public LoadInstructionsResponse loadInstructions(LoadInstructionsRequest loadInstructionsRequest); 
	public GetInstructionsResponse getInstructions(GetInstructionsRequest getInstructionsRequest);

	
	
	
}
