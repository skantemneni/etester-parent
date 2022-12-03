package com.etester.data.domain.test;


public interface TestsectionInstructionsDao {

	public static final String upsertTestsectionInstructionsSQL = "INSERT INTO testsection_instructions (instructions_name, description, text, addl_info) "
			+ " VALUES (:instructionsName, :description, :text, :addlInfo) "
			+ " ON DUPLICATE KEY "
			+ " UPDATE instructions_name = :instructionsName, description = :description, text = :text, addl_info = :addlInfo ";
	
	public static final String getTestsectionInstructionsSQL = "SELECT * FROM testsection_instructions WHERE instructions_name = :instructionsName";  
	
	public Integer updateTestsectionInstructions(TestsectionInstructions testsectionInstructions);

	public TestsectionInstructions getTestsectionInstructions(String instructionsName);

}

