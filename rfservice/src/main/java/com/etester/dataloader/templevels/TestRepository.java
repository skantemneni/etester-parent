package com.etester.dataloader.templevels;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.rulefree.rfloaddataschema.TestType;

@Component
public class TestRepository {
	private static final Map<Long, TestType> tests = new HashMap<>();

	@jakarta.annotation.PostConstruct
	public void initData() {
		TestType testIIT = new TestType();
		testIIT.setName("IIT");
		testIIT.setDescription("IIT Delhi");
		testIIT.setIdTest(101);
		testIIT.setPublished(true);

		tests.put(testIIT.getIdTest(), testIIT);

		TestType testEAMCET = new TestType();
		testEAMCET.setName("EAMCET");
		testEAMCET.setDescription("EAMCET Andhra");
		testEAMCET.setIdTest(102);
		testEAMCET.setPublished(true);

		tests.put(testEAMCET.getIdTest(), testEAMCET);

		TestType testNEET = new TestType();
		testNEET.setName("NEET");
		testNEET.setDescription("NEET National");
		testNEET.setIdTest(103);
		testNEET.setPublished(true);

		tests.put(testNEET.getIdTest(), testNEET);

	}

	public TestType findTest(Long idTest) {
		Assert.notNull(idTest, "The Test's name must not be null");
		return tests.get(idTest);
	}

	public TestType[] getAllTest() {
		if (tests != null && tests.size() > 0) {
			return (TestType[]) tests.values().toArray();
		} else {
			return null;
		}
	}

}