package com.etester.data.domain.test;

import java.util.Arrays;
import java.util.List;

public class TestConstants {

	// various question statuses as stored in the database
	public static final String QUESTION_STATUS_CORRECT = "C";
	public static final String QUESTION_STATUS_WRONG = "W";
	public static final String QUESTION_STATUS_UNANSWERED = "U";
	
	// Constants for Test Types
	public static final String TEST = "Test";
	public static final String ASSIGNMENT = "Assignment";
	public static final String QUIZ = "Quiz";
	public static final String CHALLENGE = "Challenge";
	public static final String ALL_TEST_TYPES = "All";

	public static final Long MISSING_OBJECT_ID = -1l;

	public static enum ProfileType {
		FIXED_DATE (1),
		START_DAY (2),
		NON_FIXED (3);
		private final int type;
		ProfileType(int type) {
	        this.type = type;
	    }
		public int type() { return type; }
	}
	public static final String PROFILE_TYPE_FIXED_DATE = "Exact Dates";
	public static final String PROFILE_TYPE_START_DAY = "Fixed Days";
	public static final String PROFILE_TYPE_NON_FIXED = "Sequenced";
	
	public static enum TestRenderMode {
		TAKE_TEST (0),
		REVIEW_CORRECTIONS (1),
		GRADE_TEST (2);
		private final int mode;
		TestRenderMode(int mode) {
	        this.mode = mode;
	    }
		public int mode() { return mode; }
	}
	
	public static enum QuestionStatus {
		NOT_ANSWERED (0),
		ANSWERED (1),
		CORRECT (2),
		WRONG (3);
		private final int status;
		QuestionStatus(int status) {
	        this.status = status;
	    }
		public int status() { return status; }
	}
	
	public static enum TestresponseSaveResponse {
		SAVE_SUCCESS (0),
		SAVE_FAIL_PREVIOUSLY_SUBMITTED (1),
		SAVE_FAIL_NO_USER (2),
		SAVE_FAIL_NO_TEST (3),
		SAVE_FAIL_NOT_OWNED (4),
		SAVE_FAIL_NOT_PROVIDER (11),
		SAVE_FAIL_NOT_SUBMITTED (12),
		SAVE_FAIL_CANNOT_ARCHIVE (21),
		SAVE_FAIL_UNKNOWN (-1);
		
		private final int response_code;
		TestresponseSaveResponse(int response_code) {
	        this.response_code = response_code;
	    }
		public int responseCode() { return response_code; }
	}
	
	public static enum WebuserSaveResponse {
		SAVE_SUCCESS (0),
		SAVE_FAIL_DEPLICATE_USER (1),
		SAVE_FAIL_UNKNOWN_USER (-1),
		SAVE_FAIL_UNKNOWN_AUTHORITY (-2);

		private final int response_code;
		WebuserSaveResponse(int response_code) {
	        this.response_code = response_code;
	    }
		public int responseCode() { return response_code; }
	}
	
	public static enum WebuserUpdateResponse {
		UPDATE_SUCCESS (0),
		UPDATE_FAIL_NO_USER (1),
		UPDATE_FAIL_NO_EMAIL (2),
		SAVE_FAIL_UNKNOWN_PASSWORD_TRANSACTION (-3);
		
		private final int response_code;
		WebuserUpdateResponse(int response_code) {
	        this.response_code = response_code;
	    }
		public int responseCode() { return response_code; }
	}

	public static enum DerivedSectionDeleteResponse {
		DELETE_SECTION_SUCCESS (0),
		DELETE_SECTION_FAIL_NOT_DERIVED (1),
		DELETE_SECTION_FAIL_NOT_OWNED (2),
		DELETE_SECTION_FAIL_NO_PERMISSION (3),
		DELETE_SECTION_FAIL_UNKNOWN (-1);
		
		private final int response_code;
		DerivedSectionDeleteResponse(int response_code) {
	        this.response_code = response_code;
	    }
		public int responseCode() { return response_code; }
	}
	
	public static enum DerivedSectionSaveResponse {
		SAVE_SECTION_SUCCESS (0),
		SAVE_SECTION_FAIL_NOT_DERIVED (1),
		SAVE_SECTION_FAIL_NOT_OWNED (2),
		SAVE_SECTION_FAIL_NO_PERMISSION (3),
		SAVE_SECTION_FAIL_UNKNOWN (-1);
		
		private final int response_code;
		DerivedSectionSaveResponse(int response_code) {
	        this.response_code = response_code;
	    }
		public int responseCode() { return response_code; }
	}
	
	// Indicates if we want to combine testsections for each testsegment or test (for a combined QuestionIndexFlowPanel - for the testsegment)
	public static enum CombineSectionsForTestAdminister {
		NO_COMBINE_SECTIONS (0),
		COMBINE_SECTIONS_FOR_SEGMENT (1),
		COMBINE_SECTIONS_FOR_TEST (2);
		private final int combine_code;
		CombineSectionsForTestAdminister(int combine_code) {
	        this.combine_code = combine_code;
	    }
		public int combineCode() { return combine_code; }
	}
	
	// Indicates if we want to combine testsections for each testsegment or test (for a combined QuestionIndexFlowPanel - for the testsegment)
	public static enum TestsectionArtifactType {
		SECTION (0),
		SYNOPSIS_TEXT (1),
		SYNOPSIS_VIDEO (2);
		private final int artifact_type;
		TestsectionArtifactType(int artifact_type) {
	        this.artifact_type = artifact_type;
	    }
		public int artifactType() { return artifact_type; }
	}
	
	// Channels IDs
	// 10+2
	public static final Long CHANNEL_ID_CORE_10PLUS2 = 103l;
	public static final Long CHANNEL_ID_CORE_FOUNDATION = 104l;
	
	public static final Long CHANNEL_ID_IIT = 131l;
	public static final Long CHANNEL_ID_BITSAT = 132l;
	public static final Long CHANNEL_ID_EAMECT_AP = 133l;
	public static final Long CHANNEL_ID_EAMECT_TS = 134l;
	public static final Long CHANNEL_ID_EAMECT_TN = 135l;
	public static final Long CHANNEL_ID_EAMECT_KS = 136l;
	// BankExam
	public static final Long CHANNEL_ID_CORE_BANK = 111l;
	
	public static final Long CHANNEL_ID_BANK_SBI_PO = 121l;
	public static final Long CHANNEL_ID_BANK_SBI_CLERK = 122l;
	public static final Long CHANNEL_ID_BANK_IBPS_PO = 123l;
	public static final Long CHANNEL_ID_BANK_IBPS_CLERK = 124l;
	// CAT and CPT
	public static final Long CHANNEL_ID_CAT = 112l;
	public static final Long CHANNEL_ID_CPT = 113l;
	
	// Foundation Channels
	public static final Long CHANNEL_ID_FOUNDATION = 141l;
	

	// Number of points awarded to questions that have no default points set.
	// Used while administering and grading a test
	// Get rid of these setting because they may not be useful any longer (with us doing testsection based scoring)  
//	public static final float DEFAULT_QUESTION_POINTS = 1.0f;
//	public static final float DEFAULT_QUESTION_NEGATIVE_POINTS = 0.0f;
	
	// Offest (increment) by which the Question Index numbers should be incremented while rendering a Testsection.  Usually 0.
	public static final int DEFAULT_QUESTION_INDEX_OFFSET = 0;
	
	public static final int USERTEST_TYPE_USER = 1;
	public static final int USERTEST_TYPE_GROUP = 2;
	
	// Constants for Core types
	public static final String CORE_TYPE_CHANNEL = "Channel";
	public static final String CORE_TYPE_SUBJECT = "Subject";
	public static final String CORE_TYPE_LEVEL = "Level";
	public static final String CORE_TYPE_TOPIC = "Topic";
	public static final String CORE_TYPE_SKILL = "Skill";
	public static final String CORE_TYPE_SECTION = "Section";

	// Constants for Test Conducting types
	public static final String TEST_METHOD_CLASSIC = "Classic";
	public static final String TEST_METHOD_ADAPTIVE = "Adaptive";

	// Constants for Derived types
	public static final String TEST_TYPE_ASSIGNMENT = "Assignment";
	public static final String TEST_TYPE_TEST = "Test";
	public static final String TEST_TYPE_QUIZ = "Quiz";
	public static final String TEST_TYPE_CHALLENGE = "Challenge";
	public static final String TEST_TYPE_ALL = "All";
	
	// Constants for Test Statuses
	public static final String TEST_STATUS_ASSIGNED = "assigned";
	public static final String TEST_STATUS_STARTED = "started";
	public static final String TEST_STATUS_SUBMITTED = "submitted";
	public static final String TEST_STATUS_CORRECTIONS = "corrections";
	public static final String TEST_STATUS_COMPLETED = "completed";
	public static final String TEST_STATUS_ARCHIVED = "archived";
	public static final String TEST_STATUS_FAILED = "failed";

	/**
	 * AccessLevelVisibility is used, mostly in tests to see who can see the test during assignments.
	 * By default, when a test/assignment is created, only the Owner/Provider can see it.  
	 * However, once tests are Published (& frozen) they can be shared within an Organization or made 
	 * open to public.
	 * @author sesi
	 *
	 */
	public static enum AccessLevelVisibility {
		PRIVATE (1),
		ORGANIZATION (2),
		PUBLIC (3),
		UNKNOWN (-1);
		private final int visibility;
		AccessLevelVisibility(int visibility) {
	        this.visibility = visibility;
	    }
		public int visibility() { return visibility; }
	}

	public static enum AssignableStatus {
		NOT_PUBLISHED (0),
		PUBLISHED (1),
		ARCHIVED (2),
		UNKNOWN (-1);
		private final int assignableStatus;
		AssignableStatus(int assignableStatus) {
	        this.assignableStatus = assignableStatus;
	    }
		public int assignableStatus() { return assignableStatus; }
	}
	
	private static final List<String> PracticeStatusDisplayDescriptions = Arrays.asList("Try", "Continue", "Failed", "Try Again", "Completed", "Unknown");
	public static enum PracticeStatus {
		DEFAULT_STATUS (0),
		STARTED_STATUS (1),
		FAILED_STATUS (2),
		TRY_AGAIN_STATUS (3),
		FINISHED_STATUS (4),
		UNKNOWN_STATUS (-1);
		private final int practiceStatus;
		PracticeStatus(int practiceStatus) {
			if (practiceStatus > 4 || practiceStatus < 0) {
				practiceStatus = -1;
			}
	        this.practiceStatus = practiceStatus;
	    }
		public String stringDisplayValue() {
			if (practiceStatus == UNKNOWN_STATUS.intValue()) {
				return "Unknown";
			}
			return PracticeStatusDisplayDescriptions.get(practiceStatus); 
		}
		public int intValue() { return practiceStatus; }
	    public static PracticeStatus fromValue(int value) {
	        for (PracticeStatus status : PracticeStatus.values()) {
	            if (status.practiceStatus == value) {
	                return status;
	            }
	        }
	        return UNKNOWN_STATUS;
	    }
	    public static PracticeStatus fromValue(String value) {
	    	if (value == null) {
	    		return UNKNOWN_STATUS;
	    	}
	    	try {
	    		int practiceStatus = Integer.valueOf(value);
	    		return fromValue(practiceStatus);
	    	} catch (Exception e) {
	    		return UNKNOWN_STATUS;
	    	}
	    }
		
	}

	private static final List<String> UsermessageStatusDescriptions = Arrays.asList("Active", "Acknowledged", "Expired", "Deleted", "Unknown");
	public static enum UsermessageStatus {
		ACTIVE_STATUS (0),
		ACKNOWLEDGED_STATUS (1),
		EXPIRED_STATUS (2),
		DELETED_STATUS (3),
		UNKNOWN_STATUS (-1);
		private final int usermessageStatus;
		UsermessageStatus(int usermessageStatus) {
			if (usermessageStatus > 3 || usermessageStatus < 0) {
				usermessageStatus = -1;
			}
	        this.usermessageStatus = usermessageStatus;
	    }
		public String stringDisplayValue() {
			if (usermessageStatus == UNKNOWN_STATUS.intValue()) {
				return "Unknown";
			}
			return UsermessageStatusDescriptions.get(usermessageStatus); 
		}
		public int intValue() { return usermessageStatus; }
	    public static UsermessageStatus fromValue(int value) {
	        for (UsermessageStatus status : UsermessageStatus.values()) {
	            if (status.usermessageStatus == value) {
	                return status;
	            }
	        }
	        return UNKNOWN_STATUS;
	    }
	    public static UsermessageStatus fromValue(String value) {
	    	if (value == null) {
	    		return UNKNOWN_STATUS;
	    	}
	    	try {
	    		int usermessageStatus = Integer.valueOf(value);
	    		return fromValue(usermessageStatus);
	    	} catch (Exception e) {
	    		return UNKNOWN_STATUS;
	    	}
	    }
		
	}

	
}
