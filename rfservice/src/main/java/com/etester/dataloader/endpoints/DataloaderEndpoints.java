package com.etester.dataloader.endpoints;

import javax.xml.namespace.QName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.etester.dataloader.util.DataloaderService;
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

import jakarta.xml.bind.JAXBElement;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Endpoint
public class DataloaderEndpoints {

	private static final String SAVE_TARGET_NAMESPACE = "http://www.rulefree.com/RfLoadDataSchema";

//	static final Logger logger = Logger.getLogger(DataloaderEndpoints.class); 
	
	@Autowired
	private DataloaderService dataloaderService;

	@PayloadRoot(localPart = "GetSectionsRequest", namespace = SAVE_TARGET_NAMESPACE)
	@ResponsePayload
	public JAXBElement<GetSectionsResponse> getSections(@RequestPayload JAXBElement<GetSectionsRequest> getSectionsRequest) {
		System.out.println("Get Sections!");
		GetSectionsResponse response = dataloaderService.getSections(getSectionsRequest.getValue());
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "GetSectionsResponse");
		return new JAXBElement<GetSectionsResponse> (qname, GetSectionsResponse.class, response);
	}


	@PayloadRoot(localPart = "LoadSectionsRequest", namespace = SAVE_TARGET_NAMESPACE)
	@ResponsePayload 
	public JAXBElement<LoadSectionsResponse> loadSections(@RequestPayload JAXBElement<LoadSectionsRequest> loadSectionsRequest /*, MessageContext messageContext*/) {
		System.out.println("Save Sections !");
		log.info("Save Sections !");
		
//		// read SOAP Header from request and append in response
//        SaajSoapMessage soapRequest = (SaajSoapMessage) messageContext.getRequest();
//        SoapHeader reqHeader = soapRequest.getSoapHeader();
//        SaajSoapMessage soapResponse = (SaajSoapMessage) messageContext.getResponse();
//        SoapHeader respHeader = soapResponse.getSoapHeader();
////        TransformerUtils transformerFactory = TransformerFactory
////                .newInstance();
////        Transformer transformer = transformerFactory.newTransformer();
//        Iterator<SoapHeaderElement> itr = reqHeader.examineAllHeaderElements();
//        while (itr.hasNext()) {
//            SoapHeaderElement ele = itr.next();
//            System.out.println("SoapHeaderElement.getSource() : " + ele.getSource());
//            System.out.println("SoapHeaderElement.getName() : " + ele.getName());
//            System.out.println("SoapHeaderElement.getText() : " + ele.getText());
//            System.out.println("SoapHeaderElement.getAllAttributes() : " + ele.getAllAttributes());
//        }
		
		LoadSectionsResponse response = dataloaderService.saveSections(loadSectionsRequest.getValue()/*, messageContext*/);
		
		if (response == null) {
			response = new LoadSectionsResponse();
			response.setStatus(DataloaderService.UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			response.setMessage(DataloaderService.UPLOAD_UNKNOWN_FAILURE_MESSAGE);
		}
		
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "LoadSectionsResponse");
		return new JAXBElement<LoadSectionsResponse> (qname, LoadSectionsResponse.class, response);
	}

	@PayloadRoot(localPart = "LoadSkillsOnlyRequest", namespace = SAVE_TARGET_NAMESPACE)
	public @ResponsePayload
	JAXBElement<LoadSkillsOnlyResponse> loadSkillsOnly(@RequestPayload JAXBElement<LoadSkillsOnlyRequest> loadSkillsOnlyRequest) {
		System.out.println("Save Skills Only!");
		log.info("Save Skills Only!");
		LoadSkillsOnlyResponse response = dataloaderService.saveSkillsOnly(loadSkillsOnlyRequest.getValue());
		if (response == null) {
			response = new LoadSkillsOnlyResponse();
			response.setStatus(DataloaderService.UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			response.setMessage(DataloaderService.UPLOAD_UNKNOWN_FAILURE_MESSAGE);
		}
		
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "LoadSkillsOnlyResponse");
		return new JAXBElement<LoadSkillsOnlyResponse> (qname, LoadSkillsOnlyResponse.class, response);
	}

	@PayloadRoot(localPart = "LoadSkillsRequest", namespace = SAVE_TARGET_NAMESPACE)
	public @ResponsePayload
	JAXBElement<LoadSkillsResponse> loadSkills(@RequestPayload JAXBElement<LoadSkillsRequest> loadSkillsRequest) {
		System.out.println("Save Skills !");
		LoadSkillsResponse response = dataloaderService.saveSkills(loadSkillsRequest.getValue());
		if (response == null) {
			response = new LoadSkillsResponse();
			response.setStatus(DataloaderService.UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			response.setMessage(DataloaderService.UPLOAD_UNKNOWN_FAILURE_MESSAGE);
		}
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "LoadSkillsResponse");
		return new JAXBElement<LoadSkillsResponse> (qname, LoadSkillsResponse.class, response);
	}

	@PayloadRoot(localPart = "LoadTopicsOnlyRequest", namespace = SAVE_TARGET_NAMESPACE)
	public @ResponsePayload
	JAXBElement<LoadTopicsOnlyResponse> loadTopicsOnly(@RequestPayload JAXBElement<LoadTopicsOnlyRequest> loadTopicsOnlyRequest) {
		System.out.println("Save Topics Only!");
		LoadTopicsOnlyResponse response = dataloaderService.saveTopicsOnly(loadTopicsOnlyRequest.getValue());
		if (response == null) {
			response = new LoadTopicsOnlyResponse();
			response.setStatus(DataloaderService.UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			response.setMessage(DataloaderService.UPLOAD_UNKNOWN_FAILURE_MESSAGE);
		}
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "LoadTopicsOnlyResponse");
		return new JAXBElement<LoadTopicsOnlyResponse> (qname, LoadTopicsOnlyResponse.class, response);
	}

	@PayloadRoot(localPart = "LoadTopicsRequest", namespace = SAVE_TARGET_NAMESPACE)
	public @ResponsePayload
	JAXBElement<LoadTopicsResponse> loadTopics(@RequestPayload JAXBElement<LoadTopicsRequest> loadTopicsRequest) {
		System.out.println("Save Topics !");
		LoadTopicsResponse response = dataloaderService.saveTopics(loadTopicsRequest.getValue());
		if (response == null) {
			response = new LoadTopicsResponse();
			response.setStatus(DataloaderService.UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			response.setMessage(DataloaderService.UPLOAD_UNKNOWN_FAILURE_MESSAGE);
		}
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "LoadTopicsResponse");
		return new JAXBElement<LoadTopicsResponse> (qname, LoadTopicsResponse.class, response);
	}

	@PayloadRoot(localPart = "LoadLevelsOnlyRequest", namespace = SAVE_TARGET_NAMESPACE)
	public @ResponsePayload
	JAXBElement<LoadLevelsOnlyResponse> loadLevelsOnly(@RequestPayload JAXBElement<LoadLevelsOnlyRequest> loadLevelsOnlyRequest) {
		System.out.println("Save Levels Only!");
		LoadLevelsOnlyResponse response = dataloaderService.saveLevelsOnly(loadLevelsOnlyRequest.getValue());
		if (response == null) {
			response = new LoadLevelsOnlyResponse();
			response.setStatus(DataloaderService.UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			response.setMessage(DataloaderService.UPLOAD_UNKNOWN_FAILURE_MESSAGE);
		}
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "LoadLevelsOnlyResponse");
		return new JAXBElement<LoadLevelsOnlyResponse> (qname, LoadLevelsOnlyResponse.class, response);
	}

	@PayloadRoot(localPart = "LoadLevelsRequest", namespace = SAVE_TARGET_NAMESPACE)
	public @ResponsePayload
	JAXBElement<LoadLevelsResponse> loadLevels(@RequestPayload JAXBElement<LoadLevelsRequest> loadLevelsRequest) {
		System.out.println("Save Levels !");
		LoadLevelsResponse response = dataloaderService.saveLevels(loadLevelsRequest.getValue());
		if (response == null) {
			response = new LoadLevelsResponse();
			response.setStatus(DataloaderService.UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			response.setMessage(DataloaderService.UPLOAD_UNKNOWN_FAILURE_MESSAGE);
		}
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "LoadLevelsResponse");
		return new JAXBElement<LoadLevelsResponse> (qname, LoadLevelsResponse.class, response);
	}

	@PayloadRoot(localPart = "LoadWordlistsRequest", namespace = SAVE_TARGET_NAMESPACE)
	public @ResponsePayload
	JAXBElement<LoadWordlistsResponse> loadWordlists(@RequestPayload JAXBElement<LoadWordlistsRequest> loadWordlistsRequest) {
		System.out.println("Save Wordlists !");
		LoadWordlistsResponse response = dataloaderService.saveWordlists(loadWordlistsRequest.getValue());
		if (response == null) {
			response = new LoadWordlistsResponse();
			response.setStatus(DataloaderService.UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			response.setMessage(DataloaderService.UPLOAD_UNKNOWN_FAILURE_MESSAGE);
		}
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "LoadWordlistsResponse");
		return new JAXBElement<LoadWordlistsResponse> (qname, LoadWordlistsResponse.class, response);
	}

	@PayloadRoot(localPart = "UpdateWordsRequest", namespace = SAVE_TARGET_NAMESPACE)
	public @ResponsePayload
	JAXBElement<UpdateWordsResponse> updateWords(@RequestPayload JAXBElement<UpdateWordsRequest> updateWordsRequest) {
		System.out.println("Update Words !");
		UpdateWordsResponse response = dataloaderService.updateWords(updateWordsRequest.getValue());
		if (response == null) {
			response = new UpdateWordsResponse();
			response.setStatus(DataloaderService.UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			response.setMessage(DataloaderService.UPLOAD_UNKNOWN_FAILURE_MESSAGE);
		}
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "UpdateWordsResponse");
		return new JAXBElement<UpdateWordsResponse> (qname, UpdateWordsResponse.class, response);
	}

	@PayloadRoot(localPart = "GetWordsRequest", namespace = SAVE_TARGET_NAMESPACE)
	public @ResponsePayload
	JAXBElement<GetWordsResponse> getWords(@RequestPayload JAXBElement<GetWordsRequest> getWordsRequest) {
		System.out.println("Get Words !");
		GetWordsResponse response = dataloaderService.getWords(getWordsRequest.getValue());
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "GetWordsResponse");
		return new JAXBElement<GetWordsResponse> (qname, GetWordsResponse.class, response);
	}


	@PayloadRoot(localPart = "GetTestsRequest", namespace = SAVE_TARGET_NAMESPACE)
	@ResponsePayload
	public JAXBElement<GetTestsResponse> getTests(@RequestPayload JAXBElement<GetTestsRequest> getTestsRequest) {
		System.out.println("Get Tests!");
		GetTestsResponse response = dataloaderService.getTests(getTestsRequest.getValue());
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "GetTestsResponse");
		return new JAXBElement<GetTestsResponse> (qname, GetTestsResponse.class, response);
	}

	@PayloadRoot(localPart = "SaveTestsRequest", namespace = SAVE_TARGET_NAMESPACE)
	@ResponsePayload 
	public JAXBElement<SaveTestsResponse> loadTests(@RequestPayload JAXBElement<SaveTestsRequest> saveTestsRequest /*, MessageContext messageContext*/) {
		System.out.println("Save Tests !");
		log.info("Save Tests !");
		SaveTestsResponse response = dataloaderService.saveTests(saveTestsRequest.getValue());
		if (response == null) {
			response = new SaveTestsResponse();
			response.setStatus(DataloaderService.UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			response.setMessage(DataloaderService.UPLOAD_UNKNOWN_FAILURE_MESSAGE);
		}
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "SaveTestsResponse");
		return new JAXBElement<SaveTestsResponse> (qname, SaveTestsResponse.class, response);
	}

	
	
	
	
	
	@PayloadRoot(localPart = "GetInstructionsRequest", namespace = SAVE_TARGET_NAMESPACE)
	@ResponsePayload
	public JAXBElement<GetInstructionsResponse> getInstructions(@RequestPayload JAXBElement<GetInstructionsRequest> getInstructionsRequest) {
		System.out.println("Get Instructions!");
		GetInstructionsResponse response = dataloaderService.getInstructions(getInstructionsRequest.getValue());
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "GetInstructionsResponse");
		return new JAXBElement<GetInstructionsResponse> (qname, GetInstructionsResponse.class, response);
	}

	@PayloadRoot(localPart = "LoadInstructionsRequest", namespace = SAVE_TARGET_NAMESPACE)
	@ResponsePayload 
	public JAXBElement<LoadInstructionsResponse> loadInstructions(@RequestPayload JAXBElement<LoadInstructionsRequest> loadInstructionsRequest) {
		System.out.println("Load Instructions !");
		log.info("Load Instructions !");
		LoadInstructionsResponse loadInstructionsResponse = dataloaderService.loadInstructions(loadInstructionsRequest.getValue());
		if (loadInstructionsResponse == null) {
			loadInstructionsResponse = new LoadInstructionsResponse();
			loadInstructionsResponse.setStatus(DataloaderService.UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			loadInstructionsResponse.setMessage(DataloaderService.UPLOAD_UNKNOWN_FAILURE_MESSAGE);
		}
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "LoadInstructionsResponse");
		return new JAXBElement<LoadInstructionsResponse> (qname, LoadInstructionsResponse.class, loadInstructionsResponse);
	}

	
	
	
	/**
	 * @param dataloaderService the loaderService to set
	 */
	public void setDataloaderService(DataloaderService dataloaderService) {
		this.dataloaderService = dataloaderService;
	}

}
