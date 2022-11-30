package com.etester.dataloader.endpoints;

import javax.xml.namespace.QName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.etester.dataloader.templevels.TestRepository;
import com.rulefree.rfloaddataschema.GetTestsRequest;
import com.rulefree.rfloaddataschema.GetTestsResponse;
import com.rulefree.rfloaddataschema.TestArrayType;
import com.rulefree.rfloaddataschema.TestType;

import jakarta.xml.bind.JAXBElement;

@Endpoint
public class TestEndpoint {
	private static final String NAMESPACE_URI = "http://www.rulefree.com/RfLoadDataSchema";

	private TestRepository testRepository;

	@Autowired
	public TestEndpoint(TestRepository testRepository) {
		this.testRepository = testRepository;
	}

	// **********************************************************************************************************************
	// Error: No adapter for endpoint [public
	// com.rulefree.rfloaddataschema.GetTestsResponse
	// com.etester.dataloader.endpoints.TestEndpoint.getTest(com.rulefree.rfloaddataschema.GetTestsRequest)]:
	// Is your endpoint annotated with @Endpoint, or does it implement a supported
	// interface like MessageHandler
	// or PayloadEndpoint?
	//
	// https://stackoverflow.com/questions/14390474/no-adapter-for-endpoint-is-your-endpoint-annotated-with-endpoint-or-does-it-i
	//
	// I had a similar error message. My problem was in request and response class
	// that I generated from XSD.
	// It missed @XMLRootElement annotation. This caused that description of
	// operation (in WSDL) and description
	// of implemented method (in Endpoint) did not match. Adding JAXBElement to my
	// endpoint method solved my problem.
	// ***********************************************************************************************************************
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetTestsRequest")
	@ResponsePayload
	public JAXBElement<GetTestsResponse> getTest(@RequestPayload JAXBElement<GetTestsRequest> request) {
		GetTestsResponse response = new GetTestsResponse();
		if (request != null && request.getValue() != null && request.getValue().getTestRequestChoice() != null
				&& request.getValue().getTestRequestChoice().getTestsByIdArray() != null
				&& request.getValue().getTestRequestChoice().getTestsByIdArray().getTestIdArray() != null
				&& request.getValue().getTestRequestChoice().getTestsByIdArray().getTestIdArray().getIdTest() != null
				&& request.getValue().getTestRequestChoice().getTestsByIdArray().getTestIdArray().getIdTest()
						.size() > 0) {
			TestArrayType testArrayType = new TestArrayType();
			for (Long testId : request.getValue().getTestRequestChoice().getTestsByIdArray().getTestIdArray().getIdTest() ) {
				TestType test = this.testRepository.findTest(testId);
				testArrayType.getTest().add(test);
			}
			response.setTestArray(testArrayType);
		} else {
			;
		}

		return createJaxbElement(response, GetTestsResponse.class);
	}

	private <T> JAXBElement<T> createJaxbElement(T object, Class<T> clazz) {
		return new JAXBElement<>(new QName(clazz.getSimpleName()), clazz, object);
	}

}
