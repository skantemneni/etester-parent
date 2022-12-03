package com.etester.data.domain.content.core;

import java.util.List;

public interface GradeDao {

    public Grade findByGradeName(String gradeName);

	public List<Grade> findAllGrades ();

}
