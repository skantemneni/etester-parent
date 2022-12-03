package com.etester.data.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.etester.data.domain.test.Test;
import com.etester.data.domain.test.TestDao;
import com.etester.data.domain.test.TestResponse;
import com.etester.data.domain.test.TestWithResponse;
import com.etester.data.domain.test.instance.JdbcUsertestDao;
import com.etester.data.domain.test.instance.JdbcUsertestresponseDao;
import com.etester.data.domain.test.instance.Usertest;
import com.etester.data.domain.test.instance.Usertestresponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Add @CrossOrigin("http://localhost:4200") to get Angular DEV Mode (running on http://localhost:4200 to talk to Spring Controller
 * @author skantemneni
 *
 */
@Slf4j
@RestController
// @CrossOrigin("http://localhost:4200")
//@CrossOrigin
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/data/test")
public class TestController {

	private final JdbcUsertestDao usertestDao;
	private final JdbcUsertestresponseDao usertestresponseDao;
	private final TestDao testDao;

	public TestController(JdbcUsertestDao usertestDao, JdbcUsertestresponseDao usertestresponseDao, TestDao testDao) {
		this.usertestDao = usertestDao;
		this.usertestresponseDao = usertestresponseDao;
		this.testDao = testDao;
	}


	@GetMapping("/get/alltests")
	List<Test> getAllTests(HttpServletResponse httpServletResponse) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("User: {} is calling getAllTests: {}", username);
			httpServletResponse.setStatus(HttpServletResponse.SC_OK);
			return testDao.findAllTests();
	}

	
	/**
	 * This retrieves the Test  for administering the test in the eTester User app.  
	 * @param httpServletResponse
	 * @param idTest
	 * @return
	 */
	@GetMapping("/get/alltestsinchannel/{idChannel}")
	List<Test> getAllTestsForChannel(HttpServletResponse httpServletResponse, @PathVariable Long idChannel) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("User: {} is calling getAllTestsForChannel for Test with ID: {} and Channel {}", username, idChannel);
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
		return testDao.findAllTestsInChannel(idChannel);
	}

	/**
	 * This retrieves the Test  for administering the test in the eTester User app.  
	 * @param httpServletResponse
	 * @param idTest
	 * @return
	 */
	@GetMapping("/get/test/{idTest}")
	Test getTest(HttpServletResponse httpServletResponse, @PathVariable Long idTest) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("User: {} is calling getTest for Test with ID: {}", username, idTest);
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
		return testDao.findByTestId(idTest);
	}

	/**
	 * This retrieves the Test  for administering the test in the eTester User app.  
	 * @param httpServletResponse
	 * @param idTest
	 * @return
	 */
	@GetMapping("/get/completetest/{idTest}")
	Test getCompleteTest(HttpServletResponse httpServletResponse, @PathVariable Long idTest) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("User: {} is calling getCompleteTest for Test with ID: {}", username, idTest);
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
		return testDao.findCompleteTestById(idTest);
	}

	/**
	 * This retrieves the Usertest Response without actual Test object buried within.  This can help overlay 
	 * a previously saved response on a eTester rendered test.  
	 * @param httpServletResponse
	 * @param idTest
	 * @return
	 */
	@GetMapping("/get/response/{idTest}")
	TestResponse getResponse(HttpServletResponse httpServletResponse, @PathVariable Long idTest) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("User: {} is calling getResponse for Test with ID: {}", username, idTest);
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
		return this.usertestDao.findUsertestresponseForTest(idTest);
	}


	
	/**
	 * This retrieves the Test and Response for administering the test in the eTester User app.  
	 * @param httpServletResponse
	 * @param idUsertest
	 * @return
	 */
	  @PreAuthorize("hasRole('USER') or hasRole('PROVIDER') or hasRole('ADMIN')")
	@GetMapping("/get/testwithresponse/{idUsertest}")
	  ResponseEntity<TestWithResponse> getTestWithResponse(HttpServletResponse httpServletResponse, @PathVariable Long idUsertest) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("User: {} is calling getTestWithResponse for User test with ID: {}", username, idUsertest);
//
//		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
//		return testDao.findTestByUsertestIdWithResponse(idUsertest);
		
		
		
		Optional<TestWithResponse> testWithResponse = null;
		testWithResponse = testDao.findTestByUsertestIdWithResponse(idUsertest);
		if (testWithResponse.isPresent()) {
			return ResponseEntity.ok().body(testWithResponse.get());
		} else {
			String errorMessage = String.format("TestWithResponse Not found for username: '%s' and UsertestID: '%s'", username, idUsertest);
			log.info(errorMessage);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		
		
		
	}

	/**
	 * This method saves the test response from administering the test in the eTester User app
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @param usertestresponse - Contains the actual response and the flag (completed) to indicate the test status. 
	 * 							 Completed=true triggers subsequent workflow for grading on an asynchronous thread  
	 * @return
	 */
	@PostMapping("/save/response")
	@ResponseBody
	Usertestresponse saveResponse(HttpServletRequest httpServletRequest, 
			HttpServletResponse httpServletResponse, 
			@RequestBody Usertestresponse usertestresponse
			) {
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Long idUsertest = usertestresponse.getIdUsertest();
		log.info("User: {} is calling saveResponse for Usertest with ID: {}", username, idUsertest);
		this.usertestresponseDao.saveTestResponse(usertestresponse);
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
		return usertestresponse;
	}

	/**
	 * This method returns all user tests for a username
	 * @param httpServletResponse
	 * @return List<Usertest>
	 */
	@GetMapping("/get/alltestsforcurrentuser")
	List<Usertest> getAllTestsForCurrentUser(HttpServletResponse httpServletResponse) {
		log.info("User: {} is calling getAllTestsForCurrentUser", SecurityContextHolder.getContext().getAuthentication().getName());
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
		return findAllAssignedUsertestsForUsernameInternal(username);
	}
	@GetMapping("/get/alltestsforusername/{username}")
	List<Usertest> getAllTestsForForUsername(HttpServletResponse httpServletResponse, @PathVariable String username) {
		log.info("User: {} is calling getAllTestsForForUsername for: " + username, SecurityContextHolder.getContext().getAuthentication().getName());
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
		return findAllAssignedUsertestsForUsernameInternal(username);
	}
	/**
	 * findAllAssignedUsertestsForUsernameInternal calls "usertestDao.findAllUsertestsForUsername" which retrieves tests in all states - assigned, started, correctione etc.
	 * There is another method findAllAssignedUsertestsForUsername that would limit to assigned and started states only.
	 * @param username
	 * @return
	 */
	private List<Usertest> findAllAssignedUsertestsForUsernameInternal(String username) {
		return usertestDao.findAllUsertestsForUsername(username);
	}
	
	
	
	
	
	
	
	
	
//	
//	
//	
//	@GetMapping("/usertest/{idUsertest}")
//	Usertest findByUsertestId(HttpServletResponse httpServletResponse, @PathVariable Long idUsertest) {
//		String username = SecurityContextHolder.getContext().getAuthentication().getName();
//		log.info("User: {} is calling findByUsertestId for User test with ID: {}", username, idUsertest);
//		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
//		return usertestDao.findByUsertestId(idUsertest);
//	}
//
//	@GetMapping("/userid/{id}")
//	List<Usertest> findAllAssignedUsertestsForUserId(HttpServletResponse httpServletResponse, @PathVariable Long id) {
//		log.info("User: {} is calling findAllAssignedUsertestsForUserId for: " + id, SecurityContextHolder.getContext().getAuthentication().getName());
//		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
//		return usertestDao.findAllAssignedUsertestsForUserId(id);
//	}
//	
	
}
