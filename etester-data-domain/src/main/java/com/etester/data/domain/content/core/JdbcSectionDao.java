package com.etester.data.domain.content.core;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.etester.data.dao.JdbcDataDaoParent;
import com.etester.data.domain.content.DerivedSectionQuestion;
import com.etester.data.domain.content.Printsectionsettings;
import com.etester.data.domain.content.additional.wordlist.WlPassage;
import com.etester.data.domain.content.additional.wordlist.WlWord;
import com.etester.data.domain.content.additional.wordlist.WlWordlist;
import com.etester.data.domain.content.instance.Practiceinstance;
import com.etester.data.domain.content.instance.Usersectionresponse;
import com.etester.data.domain.test.JdbcDaoStaticHelper;
import com.etester.data.domain.test.TestConstants.DerivedSectionDeleteResponse;
import com.etester.data.domain.test.TestConstants.DerivedSectionSaveResponse;
import com.etester.data.domain.user.Mysection;
import com.etester.data.domain.user.User;
import com.etester.data.domain.user.UserDao;
import com.etester.data.domain.util.UpdateStatusBean;
import com.etester.data.domain.util.cache.EtesterCacheController;

/**
 * Note that Sections can be of multiple types:
 * 1.) Standard Section (SectionType='')
 * 2.) WordList Section (SectionType=Section.WORD_LIST_SECTION_TYPE)
 * 3.) WordReview Section (SectionType=Section.WORD_REVIEW_SECTION_TYPE)
 * 4.) Derived Section (SectionType=Section.DERIVED_SECTION_TYPE)
 * 
 * Sections are all in the Section Table, however, many related artifacts for these varied section types (including questions) 
 * might be located elsewhere.
 * @author sesi
 *
 */
@Repository
public class JdbcSectionDao extends JdbcDataDaoParent
		implements SectionDao {
	
	public JdbcSectionDao(DataSource dataSource) {
		super(dataSource);
	}

	// This so I do not have to repeat 100 database queries each time there is a call for 
	// loaded level
	private static Map<Long, Section> SECTION_WORDLIST_WITH_DETAILS_CACHE = new HashMap<Long, Section>();
	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.
	@Override
	public Section findBySectionId(Long idSection) {
		return findSectionBySectionId(idSection, getNamedParameterJdbcTemplate());
	}

	public static Section findSectionBySectionId(Long idSection, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		
		String sql = findSectionBySectionIdSQL;
		BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper
				.newInstance(Section.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSection", idSection);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		Section section = null;
		try {
			section = namedParameterJdbcTemplate.queryForObject(sql, args, sectionRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		// now get the questions for this section
		if (section != null) {
			if (section.isWordlist()) {
				// see if this is a wordlist
				// do one of 2 things based on if we have a wordlist of wlwords or a wordreview with cwords.
				// note that for now, cwords and wlwords are both wrapped into wlwords
				if (Section.WORD_LIST_SECTION_TYPE.equals(section.getSectionType())) {
					section = getWordlistSectionWithDetails(idSection, namedParameterJdbcTemplate);
				} else if (Section.WORD_REVIEW_SECTION_TYPE.equals(section.getSectionType())) {
					setWordsAndPassages (section, namedParameterJdbcTemplate);
					// see if there are any questions...set them if there are.
					section.setQuestions(findQuestionsForSection(section.getIdSection(), namedParameterJdbcTemplate));
				}
			} else if (Section.DERIVED_SECTION_TYPE.equalsIgnoreCase(section.getSectionType())) {
				section.setQuestions(findQuestionsForDerivedSection(section.getIdSection(), namedParameterJdbcTemplate));
				section.setQuestionsets(findQuestionsetsForDerivedSection(section.getIdSection(), namedParameterJdbcTemplate));
			} else {
				section.setQuestions(findQuestionsForSection(section.getIdSection(), namedParameterJdbcTemplate));
				section.setQuestionsets(findQuestionsetsForSection(section.getIdSection(), namedParameterJdbcTemplate));
			} 
		}
		return section;
	}
	
	/**
	 * PLEASE DO NOT USE THIS METHOD ITS CURRENTLY ONLY USED FROM THE SERVICELOCATOR
	 */
	@Override
	public Usersectionresponse findBySectionIdWithResponse(Long idSection) {
		String findUsersectionresponseByUserAndSectionSQL_INCORRECT= "SELECT usr.* FROM usersectionresponse usr WHERE usr.id_section = :idSection AND usr.id_user = :idUser ";
		Usersectionresponse usersectionresponse = null;
		Long loggedinUserId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinUserId != null) {
			String sql = findUsersectionresponseByUserAndSectionSQL_INCORRECT;
			BeanPropertyRowMapper<Usersectionresponse> usersectionresponseRowMapper = BeanPropertyRowMapper.newInstance(Usersectionresponse.class);
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("idSection", idSection);
			args.put("idUser", loggedinUserId);
			// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
			try {
				usersectionresponse = getNamedParameterJdbcTemplate().queryForObject(sql, args, usersectionresponseRowMapper);
			} catch (IncorrectResultSizeDataAccessException e) {}
		
			if (usersectionresponse == null) {
				usersectionresponse = new Usersectionresponse();
				usersectionresponse.setIdUser(loggedinUserId);
				usersectionresponse.setIdSection(idSection);
			}
			Section section = locateSectionBySectionId(idSection);
			if (section != null) {
				usersectionresponse.setSection(section);
			}
		}
		return usersectionresponse;
	}
	
	@Override
	public Usersectionresponse findByArtifactIdSectionIdWithResponse(Long idArtifact, Long idSection) {
		String findUsersectionresponseByUserAndSectionSQL_INCORRECT= findByArtifactIdSectionIdWithResponseSQL;
		Usersectionresponse usersectionresponse = null;
		Long loggedinUserId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinUserId != null) {
			String sql = findUsersectionresponseByUserAndSectionSQL_INCORRECT;
			BeanPropertyRowMapper<Usersectionresponse> usersectionresponseRowMapper = BeanPropertyRowMapper.newInstance(Usersectionresponse.class);
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("idArtifact", idArtifact);
			args.put("idSection", idSection);
			args.put("idUser", loggedinUserId);
			// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
			try {
				usersectionresponse = getNamedParameterJdbcTemplate().queryForObject(sql, args, usersectionresponseRowMapper);
			} catch (IncorrectResultSizeDataAccessException e) {}
		
			if (usersectionresponse == null) {
				usersectionresponse = new Usersectionresponse();
				usersectionresponse.setIdUser(loggedinUserId);
				usersectionresponse.setIdSection(idSection);
			}
			Section section = locateSectionBySectionId(idSection);
			if (section != null) {
				usersectionresponse.setSection(section);
			}
		}
		return usersectionresponse;
	}
	
	private Section locateSectionBySectionId(Long idSection) {
		// see if its available in the cache to begin...
		if (EtesterCacheController.isCaching()) {
			Section section = EtesterCacheController.getSection(idSection);
			if (section == null) {
				System.out.println ("Creating Section: '" + idSection + "'.");
				// find section
				section = findBySectionId(idSection);
				// cache it for potential reuse later
				EtesterCacheController.putSection(section);
			} else {
				System.out.println ("Found Section: '" + idSection + "' in Cache.");
			}
			return section;
		} else {
			return findBySectionId(idSection);
		}
	}

	@Override
	public Section findAdaptiveSectionsForArtifact(Long idArtifact) {
		String sql = findAdaptiveSectionsForArtifactSQL;
		BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper
				.newInstance(Section.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idArtifact", idArtifact);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		Section section = null;
		try {
			section = getNamedParameterJdbcTemplate().queryForObject(sql, args, sectionRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		if (section == null) {
			return null;
		} else {
			return locateSectionBySectionId(section.getIdSection());
		}
	}

	@Override
	public List<Usersectionresponse> findAssessmentSectionsWithResponseForArtifact(Long idArtifact) {
		return this.findAssessmentSectionsWithResponseForArtifactAndSection(idArtifact, null);
	}

//	@Override
//	public List<Usersectionresponse> findAssessmentSectionsWithResponseForArtifactAndSection(Long idArtifact, Long idSection) {
//		
//		String sql = idSection == null ? findAssessmentSectionsWithResponseForArtifactSQL : findAssessmentSectionsWithResponseForArtifactAndSectionSQL;
//		BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper
//				.newInstance(Section.class);
//		Map<String, Object> args = new HashMap<String, Object>();
//		args.put("idArtifact", idArtifact);
//		args.put("idSection", idSection);
//		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
//		List<Section> sections = getNamedParameterJdbcTemplate().query(sql, args, sectionRowMapper);
//		List<Usersectionresponse> usersectionresponses = null;
//		if (sections != null && sections.size() > 0) {
//			usersectionresponses = new ArrayList<Usersectionresponse>();
//			for (Section section : sections) {
//				Usersectionresponse usersectionresponse = findBySectionIdWithResponse(section.getIdSection());
//				usersectionresponses.add(usersectionresponse);
//			}
//		}
//		return usersectionresponses;
//	}

	@Override
	public List<Usersectionresponse> findAssessmentSectionsWithResponseForArtifactAndSection(Long idArtifact, Long idSection) {
		Long loggedinUserId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinUserId == null) {
			return null;
		}
		String sql = idSection == null ? findAssessmentSectionsWithResponseForArtifactSQL : findAssessmentSectionsWithResponseForArtifactAndSectionSQL;
		BeanPropertyRowMapper<Usersectionresponse> usersectionresponseRowMapper = BeanPropertyRowMapper.newInstance(Usersectionresponse.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idArtifact", idArtifact);
		args.put("idSection", idSection);
		args.put("idUser", loggedinUserId);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		List<Usersectionresponse> usersectionresponses = getNamedParameterJdbcTemplate().query(sql, args, usersectionresponseRowMapper);
		if (usersectionresponses != null && usersectionresponses.size() > 0) {
			for (Usersectionresponse usersectionresponse : usersectionresponses) {
				usersectionresponse.setSection(locateSectionBySectionId(usersectionresponse.getIdSection()));
//				Usersectionresponse usersectionresponse = findBySectionIdWithResponse(section.getIdSection());
//				usersectionresponses.add(usersectionresponse);
			}
		}
		return usersectionresponses;
	}

//	private  String findUsersectionresponseForCurrentUser(Long idSection) {
//		String responseString = null;
//		String loggedinUsername = JdbcDaoStaticHelper.getCurrentUserName();
//		if (loggedinUsername != null && loggedinUsername.trim().length() > 0) {
//			String sql = "SELECT * FROM usersectionresponse WHERE id_section = :idSection and username = :username";
//			BeanPropertyRowMapper<Usersectionresponse> usersectionresponseRowMapper = BeanPropertyRowMapper.newInstance(Usersectionresponse.class);
//			Map<String, Object> args = new HashMap<String, Object>();
//			args.put("idSection", idSection);
//			args.put("username", loggedinUsername);
//			try {
//				Usersectionresponse responseObject = getNamedParameterJdbcTemplate()
//						.queryForObject(sql, args, usersectionresponseRowMapper);
//				responseString = responseObject.getResponse();
//			} catch (IncorrectResultSizeDataAccessException e) {}
//		}
//		return responseString;
//	}

//	@Override
//	public Integer saveUsersectionresponseForCurrentUser(Usersectionresponse usersectionresponse) {
//		String loggedinUsername = JdbcDaoStaticHelper.getCurrentUserName();
//		Long idUser = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
//		if (loggedinUsername != null && loggedinUsername.trim().length() > 0) {
//			if (!usersectionresponse.isCompleted()) {
//				usersectionresponse.setUsername(loggedinUsername);
//				SqlParameterSource usersectionresponseSource = new BeanPropertySqlParameterSource(usersectionresponse);
//				try {
//					getNamedParameterJdbcTemplate().update(upsertUsersectionresponseSQL, usersectionresponseSource);
//				} catch (Exception e) {
//					return -1;
//				}
//			} else {
//				Practiceinstance practiceinstance = new Practiceinstance();
//				practiceinstance.setIdUser(idUser);
//				practiceinstance.setIdArtifact(usersectionresponse.getIdArtifact());
//				practiceinstance.setArtifactType(usersectionresponse.getArtifactType());
//				practiceinstance.setPracticeStatus(TestConstants.TEST_STATUS_COMPLETED);
//				practiceinstance.setPracticeMethod(TestConstants.TEST_METHOD_ADAPTIVE);
//				SqlParameterSource practiceinstanceSource = new BeanPropertySqlParameterSource(practiceinstance);
//				try {
//					getNamedParameterJdbcTemplate().update(upsertPracticeinstanceSQL, practiceinstanceSource);
//				} catch (Exception e) {
//					return -1;
//				}
//				// also delete any existing usersectionresponse
//		        Map<String, Object> args = new HashMap<String, Object>();
//		        args.put("username", loggedinUsername);
//		        args.put("idSection", usersectionresponse.getIdSection());
//		        String sql = deleteUsersectionresponseSQL;
//		        getNamedParameterJdbcTemplate().update(sql, args);
//			}
//			
//		}
//        return 0;
//	}

	@Override
	public Integer saveUsersectionresponseForCurrentUser(Usersectionresponse usersectionresponse) {
		String loggedinUsername = JdbcDaoStaticHelper.getCurrentUserName();
		Long idUser = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (idUser != null) {
			// update practiceinstance
			Practiceinstance practiceinstance = new Practiceinstance();
			practiceinstance.setIdUser(idUser);
			practiceinstance.setIdArtifact(usersectionresponse.getIdArtifact());
			practiceinstance.setArtifactType(usersectionresponse.getArtifactType());
			practiceinstance.setPracticeMethod(usersectionresponse.getPracticeMethod());
			practiceinstance.setPracticeStatus(usersectionresponse.getCompletionStatus());
//			if (usersectionresponse.isCompleted()) {
//				if (usersectionresponse.getCompletionStatus() != null && usersectionresponse.getCompletionStatus().equalsIgnoreCase(TestConstants.TEST_STATUS_COMPLETED)) {
//					practiceinstance.setPracticeStatus(TestConstants.TEST_STATUS_COMPLETED);
//				} else if (usersectionresponse.getCompletionStatus() != null && usersectionresponse.getCompletionStatus().equalsIgnoreCase(TestConstants.TEST_STATUS_FAILED)) {
//					practiceinstance.setPracticeStatus(TestConstants.TEST_STATUS_FAILED);
//				} else {
//					practiceinstance.setPracticeStatus(TestConstants.TEST_STATUS_CORRECTIONS);
//				}
//			} else {
//				practiceinstance.setPracticeStatus(TestConstants.TEST_STATUS_STARTED);
//			}
			practiceinstance.setPracticeAdditionalInfo(usersectionresponse.getPracticeAdditionalInfo());
			
			SqlParameterSource practiceinstanceSource = new BeanPropertySqlParameterSource(practiceinstance);
			try {
				getNamedParameterJdbcTemplate().update(upsertPracticeinstanceSQL, practiceinstanceSource);
			} catch (Exception e) {
				return -1;
			}
			// update usersectionresponse to indicate latest status
			if (!usersectionresponse.isCompleted()) {
				usersectionresponse.setIdUser(idUser);
				SqlParameterSource usersectionresponseSource = new BeanPropertySqlParameterSource(usersectionresponse);
				try {
					getNamedParameterJdbcTemplate().update(upsertUsersectionresponseSQL, usersectionresponseSource);
				} catch (Exception e) {
					return -1;
				}
			} else {
				// also delete any existing usersectionresponse
		        Map<String, Object> args = new HashMap<String, Object>();
		        args.put("idUser", idUser);
		        args.put("idSection", usersectionresponse.getIdSection());
		        args.put("idArtifact", usersectionresponse.getIdArtifact());
		        String sql = deleteUsersectionresponseSQL;
		        getNamedParameterJdbcTemplate().update(sql, args);
			}
		} else {
			return -10;
		}
        return 0;
	}

	@Override
	public List<Section> findSectionsForSectionIdList(List<Long> idSectionList) {
		if (idSectionList == null || idSectionList.size() == 0) {
			// nothing to do here
			return new ArrayList<Section>();
		}
		
		String sql = "SELECT * FROM section WHERE id_section IN (:idSectionList)";
		BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper
				.newInstance(Section.class);
		Map<String, List<Long>> args = Collections.singletonMap("idSectionList", idSectionList);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		List<Section> sections = getNamedParameterJdbcTemplate().query(sql, args, sectionRowMapper);
		// now get the questions for this section
		if (sections != null && sections.size() > 0) {
			// go in reverse so  I can delete wordlist sections if they are not found 
			for (int i = sections.size() - 1; i >= 0; i--) {
				if (sections.get(i).isWordlist()) {
					// its this is a word list, replace the section with the corresponding wordlist section
					Section replacementSection = getWordlistSectionWithDetails(sections.get(i).getIdSection(), getNamedParameterJdbcTemplate());
					if (replacementSection != null) {
						sections.set(i, getWordlistSectionWithDetails(sections.get(i).getIdSection(), getNamedParameterJdbcTemplate()));
					} else {
						sections.remove(i);
					}
				} else if (Section.DERIVED_SECTION_TYPE.equalsIgnoreCase(sections.get(i).getSectionType())) {
					sections.get(i).setQuestions(findQuestionsForDerivedSection(sections.get(i).getIdSection(),getNamedParameterJdbcTemplate()));
				} else {
					sections.get(i).setQuestions(findQuestionsForSection(sections.get(i).getIdSection(), getNamedParameterJdbcTemplate()));
				}
				
			}
		}
		return sections;
	}

	@Override
	public List<Section> findSectionsForSkill(Long idSkill) {
		
//		String sql = "SELECT * FROM section WHERE id_skill = :idSkill";
		String sql = selectSectionListWithParentSkillInfoSQL;
		BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper
				.newInstance(Section.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSkill", idSkill);
		List<Section> sections = getNamedParameterJdbcTemplate()
				.query(sql, args, sectionRowMapper);
		// delete any external wordlist sections - they will be replaces later
		// I can certainly change the query here, however, for now (for clarity sake) I will do the following
		for (int i = sections.size() - 1; i >= 0; i--) {
			if (sections.get(i).isWordlist() && Section.WORD_LIST_SECTION_TYPE.equals(sections.get(i).getSectionType())) {
				sections.remove(i);
			}
		}
		// here is something I am doing that probably should be done differently
		sql = "SELECT * FROM wl_wordlist WHERE id_skill = :idSkill";
		BeanPropertyRowMapper<WlWordlist> wordlistRowMapper = BeanPropertyRowMapper.newInstance(WlWordlist.class);
		List<WlWordlist> wordlists = getNamedParameterJdbcTemplate().query(sql, args, wordlistRowMapper);
		sections.addAll(mapWordListsToSections (wordlists));
		return sections;
	}
	
	private List<Section> mapWordListsToSections (List<WlWordlist> wordlists) {
		List<Section> mappedSections = new ArrayList<Section>();
		if (wordlists != null && wordlists.size() > 0) {
			for (WlWordlist wordlist : wordlists) {
				Section section = new Section();
				section.setIdSection(wordlist.getIdWordlist());
				section.setName(wordlist.getName());
				section.setDescription(wordlist.getDescription());
				section.setIsExternal(1);
				section.setSectionType(Section.WORD_LIST_SECTION_TYPE);
				mappedSections.add(section);
			}
		}
		return mappedSections;
	}

	public Section findSectionBySectionIdForDownload(Long idSection) {
		String sql = findSectionBySectionIdSQL;
		BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper
				.newInstance(Section.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSection", idSection);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		Section section = null;
		try {
			section = getNamedParameterJdbcTemplate().queryForObject(sql, args, sectionRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		// now get the questions for this section
		if (section != null) {
			if (section.isWordlist()) {
				// see if this is a wordlist
				// do one of 2 things based on if we have a wordlist of wlwords or a wordreview with cwords.
				// note that for now, cwords and wlwords are both wrapped into wlwords
				if (Section.WORD_LIST_SECTION_TYPE.equals(section.getSectionType())) {
					section = getWordlistSectionWithDetails(idSection, getNamedParameterJdbcTemplate());
				} else if (Section.WORD_REVIEW_SECTION_TYPE.equals(section.getSectionType())) {
					setWordsAndPassages (section, getNamedParameterJdbcTemplate());
					// see if there are any questions...set them if there are.
					section.setQuestions(findQuestionsForSection(section.getIdSection(), getNamedParameterJdbcTemplate()));
				}
			} else if (Section.DERIVED_SECTION_TYPE.equalsIgnoreCase(section.getSectionType())) {
				section.setDerivedSectionQuestions(findDerivedSectionQuestionsForSection(section.getIdSection(), getNamedParameterJdbcTemplate()));
			} else {
				section.setQuestions(findQuestionsForSection(section.getIdSection(), getNamedParameterJdbcTemplate()));
				section.setQuestionsets(findQuestionsetsForSection(section.getIdSection(), getNamedParameterJdbcTemplate()));
			} 
		}
		return section;
	}
	
	/**
	 * This message is used exclusively by the Downloader to get content.  This is very similar to its namesake method 
	 * (with the "forDownload") except that it formats content for download...as follows
	 * 1.) On Derived Sections, it creates the the "DerivedSectionQuestion" list instead of the actual question
	 *  
	 */
	@Override
	public List<Section> findSectionsForSkillWithQuestionsForDownload( Long idSkill) {
		
//		String userName = JdbcDaoStaticHelper.getCurrentUserName(getNamedParameterJdbcTemplate());

		String sql = "SELECT * FROM section WHERE id_skill = :idSkill";
		BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper
				.newInstance(Section.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSkill", idSkill);
		List<Section> sections = getNamedParameterJdbcTemplate()
				.query(sql, args, sectionRowMapper);
		// now get the questions for this section
		if (sections != null && sections.size() > 0) {
			for (int i = 0; i < sections.size(); i++) {
				if (sections.get(i).isWordlist()) {
					sections.set(i, getWordlistSectionWithDetails(sections.get(i).getIdSection(), getNamedParameterJdbcTemplate()));
				} else if (Section.DERIVED_SECTION_TYPE.equalsIgnoreCase(sections.get(i).getSectionType())) {
					sections.get(i).setDerivedSectionQuestions(findDerivedSectionQuestionsForSection(sections.get(i).getIdSection(), getNamedParameterJdbcTemplate()));
				} else {
					sections.get(i).setQuestions(findQuestionsForSection(sections.get(i).getIdSection(), getNamedParameterJdbcTemplate()));
					sections.get(i).setQuestionsets(findQuestionsetsForSection(sections.get(i).getIdSection(), getNamedParameterJdbcTemplate()));
				}
			}
		}
		return sections;
	}

	private List<DerivedSectionQuestion> findDerivedSectionQuestionsForSection (
			Long idSection,
			NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		BeanPropertyRowMapper<DerivedSectionQuestion> derivedSectionQuestionRowMapper = BeanPropertyRowMapper.newInstance(DerivedSectionQuestion.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSection", idSection);
		List<DerivedSectionQuestion> derivedSectionQuestionList = namedParameterJdbcTemplate
				.query(findDerivedSectionQuestionsForSectionSQL, args, derivedSectionQuestionRowMapper);
		return derivedSectionQuestionList;
	}

	@Override
	public List<Section> findSectionsForSkillWithQuestions( Long idSkill) {
		
//		String userName = JdbcDaoStaticHelper.getCurrentUserName(getNamedParameterJdbcTemplate());

		String sql = "SELECT * FROM section WHERE id_skill = :idSkill";
		BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper
				.newInstance(Section.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSkill", idSkill);
		List<Section> sections = getNamedParameterJdbcTemplate()
				.query(sql, args, sectionRowMapper);
		// now get the questions for this section
		if (sections != null && sections.size() > 0) {
			for (int i = 0; i < sections.size(); i++) {
				if (sections.get(i).isWordlist()) {
					sections.set(i, getWordlistSectionWithDetails(sections.get(i).getIdSection(), getNamedParameterJdbcTemplate()));
				} else if (Section.DERIVED_SECTION_TYPE.equalsIgnoreCase(sections.get(i).getSectionType())) {
					sections.get(i).setQuestions(findQuestionsForDerivedSection(sections.get(i).getIdSection(), getNamedParameterJdbcTemplate()));
					sections.get(i).setQuestionsets(findQuestionsetsForDerivedSection(sections.get(i).getIdSection(), getNamedParameterJdbcTemplate()));
				} else {
					sections.get(i).setQuestions(findQuestionsForSection(sections.get(i).getIdSection(), getNamedParameterJdbcTemplate()));
					sections.get(i).setQuestionsets(findQuestionsetsForSection(sections.get(i).getIdSection(), getNamedParameterJdbcTemplate()));
				}
			}
		}
		return sections;
	}

	@Override
	public List<Section> getMySectionList() {
		// This is kind of a tricky method.  The section id's are stored in the Mysection table
		List<Long> sectionIdList = JdbcDaoStaticHelper.getMySectionIdList(getNamedParameterJdbcTemplate());
		return findSectionsForSectionIdList(sectionIdList);
	}
	
	@Override
	public List<Section> findDerivedSectionsForSkill (Long idSkill) {
		List<Section> sections = null;
		// make sure we have a valid and logged in user
		Long loggedinUserId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinUserId != null) {
		// validate logged in used has channel access
			//		if (userValidInChannel(loggedinUserId, idSystem)) {
			//			
			//		}
			String sql = "SELECT * FROM section WHERE id_skill = :idSkill AND section_type = :sectionType";
			BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper
					.newInstance(Section.class);
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("idSkill", idSkill);
			args.put("sectionType", Section.DERIVED_SECTION_TYPE);
			sections = getNamedParameterJdbcTemplate()
					.query(sql, args, sectionRowMapper);
		}
		return sections;
	}

	@Override
	public Integer saveDerivedSection(Section derivedSection) {
		User loggedinUser = JdbcDaoStaticHelper.findCurrentUser(getNamedParameterJdbcTemplate());
		if (loggedinUser == null) { 
			// cannot delete nothing
			return DerivedSectionSaveResponse.SAVE_SECTION_FAIL_UNKNOWN.responseCode();
		} 
		
		// set some default attributes - figure out a way to fix these (may be they do not need to be REQUIRED)
		derivedSection.setIsExternal(0);
		derivedSection.setIsPractice(0);
		derivedSection.setSectionType(Section.DERIVED_SECTION_TYPE);
		
		if (derivedSection.getIdSection() == null || derivedSection.getIdSection().equals(0l)) {
			// new Derived Section
			// 1.) check to make sure the user jas authority to do this
			if (loggedinUser.getAuthorities() == null || !loggedinUser.getAuthorities().contains(UserDao.ROLE_ADMIN)) {
				return DerivedSectionSaveResponse.SAVE_SECTION_FAIL_NO_PERMISSION.responseCode();
			}
			// 2.) now, insert the section
			JdbcDaoStaticHelper.insertDerivedSection(derivedSection, getNamedParameterJdbcTemplate());
		} else {
			// existing Derived Section
			Section sectionFromDatabase = findBySectionId(derivedSection.getIdSection());
			// make sure we have a section
			if (sectionFromDatabase == null) {
				// weird scenario.  Should probably send a more valid response code
				return DerivedSectionSaveResponse.SAVE_SECTION_FAIL_UNKNOWN.responseCode();
			}
			// 1.) first make sure the logged in user is a Channel Admin or is the Owner of the the said section
			if (!loggedinUser.getIdUser().equals(sectionFromDatabase.getIdProvider()) && (loggedinUser.getAuthorities() == null || !loggedinUser.getAuthorities().contains(UserDao.ROLE_ADMIN))) {
				return DerivedSectionDeleteResponse.DELETE_SECTION_FAIL_NOT_OWNED.responseCode();
			}
			// 2.) next, validate this is infact a derived section
			if (sectionFromDatabase.getSectionType() == null || (sectionFromDatabase.getSectionType() != null && !sectionFromDatabase.getSectionType().trim().equalsIgnoreCase(Section.DERIVED_SECTION_TYPE))) {
				return DerivedSectionSaveResponse.SAVE_SECTION_FAIL_NOT_DERIVED.responseCode();
			}
			// 3.) Now, update the section
			JdbcDaoStaticHelper.updateDerivedSection(derivedSection, getNamedParameterJdbcTemplate());
		}
		return DerivedSectionSaveResponse.SAVE_SECTION_SUCCESS.responseCode();
	}

	@Override
	public Integer deleteDerivedSection(Long idDerivedSection) {
		User loggedinUser = JdbcDaoStaticHelper.findCurrentUser(getNamedParameterJdbcTemplate());
		if (loggedinUser == null) { 
			// cannot delete nothing
			return DerivedSectionDeleteResponse.DELETE_SECTION_FAIL_UNKNOWN.responseCode();
		} 
		
		String sql = "SELECT * FROM section WHERE id_section = :idSection";
		BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper
				.newInstance(Section.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSection", idDerivedSection);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		Section section = null;
		try {
			section = getNamedParameterJdbcTemplate()
					.queryForObject(sql, args, sectionRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		// make sure we have a section
		if (section == null) {
			// nothing to delete.  Should probably send a more valid response code
			return DerivedSectionDeleteResponse.DELETE_SECTION_FAIL_UNKNOWN.responseCode();
		}
		// MAKE SURE THIS IS A DERIVED SECTION BEFORE DELETING
		if (section.getSectionType() == null || (section.getSectionType() != null && !section.getSectionType().trim().equalsIgnoreCase(Section.DERIVED_SECTION_TYPE))) {
			return DerivedSectionDeleteResponse.DELETE_SECTION_FAIL_NOT_DERIVED.responseCode();
		}
		// now make sure the logged in user is a Channel Admin or is the Owner of the the said section
		if (!loggedinUser.getIdUser().equals(section.getIdProvider()) && (loggedinUser.getAuthorities() == null || !loggedinUser.getAuthorities().contains(UserDao.ROLE_ADMIN))) {
			return DerivedSectionDeleteResponse.DELETE_SECTION_FAIL_NOT_OWNED.responseCode();
		}
		// now do the actual delete
//		getNamedParameterJdbcTemplate().update("call rulefree.delete_derived_section(:idDerivedSection)", new MapSqlParameterSource().
//		addValue("idDerivedSection", section.getIdSection(), Types.NUMERIC));

/////////////////////////////////
// BAD BAD attempt do not try this...
//        int deleteStatusCode = -99;
//        String deleteStatusMessage = "What";
//		Map<String, Object> args2 = new HashMap<String, Object>();
//		args2.put("idDerivedSection", section.getIdSection());
//		args2.put("status_code", deleteStatusCode);
//		args2.put("status_message", deleteStatusMessage);
//        getNamedParameterJdbcTemplate().update("call rulefree.delete_derived_section(:idDerivedSection)", new MapSqlParameterSource().addValues(args2));
//	    System.out.println("statusCode: " + deleteStatusCode);
//	    System.out.println("statusMessage: " + deleteStatusMessage);
/////////////////////////////////

		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("rulefree").withProcedureName("delete_derived_section");
        SqlParameterSource in = new MapSqlParameterSource().addValue("idDerivedSection", section.getIdSection(), Types.NUMERIC);
        Map<String, Object> out = simpleJdbcCall.execute(in);
        int deleteStatusCode = (Integer) out.get("status_code");
        String deleteStatusMessage = (String) out.get("status_message");
	    System.out.println("statusCode: " + deleteStatusCode);
	    System.out.println("statusMessage: " + deleteStatusMessage);

		
		// send a success response
	    if (DerivedSectionDeleteResponse.DELETE_SECTION_SUCCESS.responseCode() == deleteStatusCode) {
	    	return DerivedSectionDeleteResponse.DELETE_SECTION_SUCCESS.responseCode();
	    } else {
	    	return DerivedSectionDeleteResponse.DELETE_SECTION_FAIL_UNKNOWN.responseCode();
	    }
		
	}

	private static List<Question> findQuestionsForSection(Long idSection, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
//		String userName = JdbcDaoStaticHelper.getCurrentUserName(getNamedParameterJdbcTemplate());
//		String sql = "SELECT * FROM question WHERE id_section = :idSection";
		BeanPropertyRowMapper<Question> questionRowMapper = BeanPropertyRowMapper
				.newInstance(Question.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSection", idSection);
		List<Question> questions = namedParameterJdbcTemplate.query(findQuestionsForSectionSQL, args, questionRowMapper);
		// Try and see if this works
		// get the answers also...
//		BeanPropertyRowMapper<Answer> answerRowMapper = null;
		for (int i = 0; i < questions.size(); i++) {
			questions.get(i).setAnswers(findAnswersForQuestion(questions.get(i).getIdQuestion(), namedParameterJdbcTemplate));
//			answerRowMapper = BeanPropertyRowMapper
//					.newInstance(Answer.class);
//			args = new HashMap<String, Object>();
//			args.put("idQuestion", questions.get(i)
//					.getIdQuestion());
//			List<Answer> answers = getNamedParameterJdbcTemplate()
//					.query(findAnswersForQuestionSql, args, answerRowMapper);
//			questions.get(i).setAnswers(answers);
		}
		return questions;
	}

	private static List<Questionset> findQuestionsetsForSection(Long idSection, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		BeanPropertyRowMapper<Questionset> questionsetRowMapper = BeanPropertyRowMapper.newInstance(Questionset.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSection", idSection);
		List<Questionset> questionsets = namedParameterJdbcTemplate.query(findQuestionsetsForSectionSQL, args, questionsetRowMapper);
		return questionsets;
	}
	
	private static List<Question> findQuestionsForDerivedSection(Long idSection, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
//		String userName = JdbcDaoStaticHelper.getCurrentUserName(getNamedParameterJdbcTemplate());
//		String sql = "SELECT * FROM question WHERE id_section = :idSection";
		BeanPropertyRowMapper<Question> questionRowMapper = BeanPropertyRowMapper
				.newInstance(Question.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSection", idSection);
		List<Question> questions = namedParameterJdbcTemplate
				.query(findQuestionsForDerivedSectionSQL, args, questionRowMapper);
		// Try and see if this works
		// get the answers also...
//		BeanPropertyRowMapper<Answer> answerRowMapper = null;
		for (int i = 0; i < questions.size(); i++) {
			questions.get(i).setAnswers(findAnswersForQuestion(questions.get(i).getIdQuestion(), namedParameterJdbcTemplate));
//			answerRowMapper = BeanPropertyRowMapper
//					.newInstance(Answer.class);
//			args = new HashMap<String, Object>();
//			args.put("idQuestion", questions.get(i)
//					.getIdQuestion());
//			List<Answer> answers = getNamedParameterJdbcTemplate()
//					.query(findAnswersForQuestionSql, args, answerRowMapper);
//			questions.get(i).setAnswers(answers);
		}
		return questions;
	}
	
	private static List<Questionset> findQuestionsetsForDerivedSection(Long idSection, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		BeanPropertyRowMapper<Questionset> questionsetRowMapper = BeanPropertyRowMapper.newInstance(Questionset.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSection", idSection);
		List<Questionset> questionsets = namedParameterJdbcTemplate.query(findQuestionsetsForDerivedSectionSQL, args, questionsetRowMapper);
		return questionsets;
	}
	
	public static Question findQuestionByQuestionIdWithAnswers (Long idQuestion, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		BeanPropertyRowMapper<Question> questionRowMapper = BeanPropertyRowMapper.newInstance(Question.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idQuestion", idQuestion);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		Question question = null;
		try {
			question = namedParameterJdbcTemplate.queryForObject(findQuestionByQuestionIdSQL, args, questionRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		if (question == null) {
			return null;
		}
		question.setAnswers(findAnswersForQuestion(question.getIdQuestion(), namedParameterJdbcTemplate));
        return question;
	}

	public static List<Answer> findAnswersForQuestion (Long idQuestion, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		BeanPropertyRowMapper<Answer> answerRowMapper = null;
		Map<String, Object> args = new HashMap<String, Object>();
		answerRowMapper = BeanPropertyRowMapper
				.newInstance(Answer.class);
		args = new HashMap<String, Object>();
		args.put("idQuestion", idQuestion);
		List<Answer> answers = namedParameterJdbcTemplate
				.query(findAnswersForQuestionSQL, args, answerRowMapper);
		return JdbcDaoStaticHelper.parseAnswerCompareAddlInfoOnAnswers(answers);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void insert(Section section) {

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(Section section) {

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(Long idSection) {

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<UpdateStatusBean> insertBatch(List<Section> sections) {
		return insertBatch (sections, false);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<UpdateStatusBean> insertBatch(List<Section> sections, boolean reload) {
		return JdbcDaoStaticHelper.insertSectionBatch(sections, getNamedParameterJdbcTemplate(), getJdbcTemplate(), reload);
	}

	@Override
	public void saveMysections(List<Mysection> mysectionList) {
		saveMysections(mysectionList, true);
	}

	@Override
	public void addMysections(List<Mysection> mysectionList) {
		saveMysections(mysectionList, false);
	}

	private void saveMysections(List<Mysection> mysectionList, boolean reload) {
		JdbcDaoStaticHelper.insertMysectionBatch(mysectionList, getNamedParameterJdbcTemplate(), reload);
	}

	@Override
	public void addSectionsToMyList(List<Long> sectionIdList) {
		saveSectionsToMyList(sectionIdList, false);
	}

	@Override
	public void saveSectionsToMyList(List<Long> sectionIdList) {
		saveSectionsToMyList(sectionIdList, true);
	}

	private void saveSectionsToMyList(List<Long> sectionIdList, boolean reload) {
		JdbcDaoStaticHelper.insertSectionsToMyList(sectionIdList, getNamedParameterJdbcTemplate(), reload);
	}
	
/******************************************************************************************************************************************************/
	// Word Lists Functionality........
/******************************************************************************************************************************************************/
	
	private List<Section> findWordlistSectionsForSectionIdList(List<Long> idWordlistList) {
		List<Section> mappedSections = new ArrayList<Section>();
		if (idWordlistList == null || idWordlistList.size() == 0) {
			// nothing to do here
			return mappedSections;
		}
		
		String sql = "SELECT * FROM wl_wordlist WHERE id_wordlist IN (:idWordlistList)";
		BeanPropertyRowMapper<WlWordlist> wordlistRowMapper = BeanPropertyRowMapper
				.newInstance(WlWordlist.class);
		Map<String, List<Long>> args = Collections.singletonMap("idWordlistList", idWordlistList);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		List<WlWordlist> wordlists = getNamedParameterJdbcTemplate().query(sql, args, wordlistRowMapper);
		// now get the questions for this section
		if (wordlists != null && wordlists.size() > 0) {
			for (int i = 0; i < wordlists.size(); i++) {
				mappedSections.add (getWordlistSectionWithDetails(wordlists.get(i).getIdWordlist(), getNamedParameterJdbcTemplate()));
			}
		}
		return mappedSections;
	}
	
	
	private static Section getWordlistSectionWithDetails(Long idWordlist, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		// check to see if the information is available in the cache
		if (SECTION_WORDLIST_WITH_DETAILS_CACHE.containsKey(idWordlist)) {
			return SECTION_WORDLIST_WITH_DETAILS_CACHE.get(idWordlist);
		} else {
			WlWordlist wordlist = findByWordlistId(idWordlist, namedParameterJdbcTemplate);
			if (wordlist == null) {
				return null;
			}
			wordlist.setWords(findWordsForWordlist (idWordlist, namedParameterJdbcTemplate));
			wordlist.setPassages(findPassagesForWordlist (idWordlist, namedParameterJdbcTemplate));
			wordlist.setQuestions(findQuestionsForWordlist (idWordlist, namedParameterJdbcTemplate));
			Section mappedSection = mapWordlistToSection (wordlist);
			SECTION_WORDLIST_WITH_DETAILS_CACHE.put(idWordlist, mappedSection);
			return mappedSection;
		}
	}
	
	private static Section mapWordlistToSection(WlWordlist wordlist) {
		Section mappedSection = new Section();
		if (wordlist != null) {
			mappedSection.setIsExternal(1);
			mappedSection.setSectionType(Section.WORD_LIST_SECTION_TYPE);
			mappedSection.setIdProvider(wordlist.getIdProvider());
			mappedSection.setIdSection(wordlist.getIdWordlist());
			mappedSection.setIdSkill(wordlist.getIdSkill());
			mappedSection.setName(wordlist.getName());
			mappedSection.setDescription(wordlist.getDescription());
			mappedSection.setWords(wordlist.getWords());
			mappedSection.setPassages(wordlist.getPassages());
			mappedSection.setQuestions(wordlist.getQuestions());
		}
		return mappedSection;
	}

//	private List<Question> mapWlQestionsToQuestions(List<WlQuestion> wlQuestions) {
//		if (wlQuestions == null || wlQuestions.size() == 0) {
//			return null;
//		}
//		List<Question> mappedResults = new ArrayList<Question>();
//		for (WlQuestion wlQuestion : wlQuestions) {
//			mappedResults.add(WlQuestion.mapToStandardQuestion (wlQuestion));
//		}
//		return mappedResults;
//	}

	private static WlWordlist findByWordlistId(Long idWordlist, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        String sql = "SELECT * FROM wl_wordlist WHERE id_wordlist = :idWordlist";
        BeanPropertyRowMapper<WlWordlist> wordlistRowMapper = BeanPropertyRowMapper.newInstance(WlWordlist.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idWordlist", idWordlist);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        WlWordlist wlWordlist = null;
        try {
        	wlWordlist = namedParameterJdbcTemplate.queryForObject(sql, args, wordlistRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return wlWordlist;
	}

	private static List<WlWord> findWordsForWordlist (Long idWordlist, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        String sql = "SELECT * FROM wl_word WHERE id_wordlist = :idWordlist ORDER BY id_word";
        BeanPropertyRowMapper<WlWord> wordRowMapper = BeanPropertyRowMapper.newInstance(WlWord.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idWordlist", idWordlist);
        List<WlWord> words = namedParameterJdbcTemplate.query(sql, args, wordRowMapper);
        return words;
	}

	private static List<WlPassage> findPassagesForWordlist (Long idWordlist, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        String sql = "SELECT * FROM wl_passage WHERE id_wordlist = :idWordlist ORDER BY id_passage";
        BeanPropertyRowMapper<WlPassage> passageRowMapper = BeanPropertyRowMapper.newInstance(WlPassage.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idWordlist", idWordlist);
        List<WlPassage> passages = namedParameterJdbcTemplate.query(sql, args, passageRowMapper);
        return passages;
	}

	private static List<Question> findQuestionsForWordlist (Long idWordlist, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        String sql = "SELECT * FROM question WHERE id_section = :idWordlist ORDER BY id_question";
        BeanPropertyRowMapper<Question> questionRowMapper = BeanPropertyRowMapper.newInstance(Question.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idWordlist", idWordlist);
        List<Question> questions = namedParameterJdbcTemplate.query(sql, args, questionRowMapper);
        if (questions != null && questions.size() > 0) {
        	for (Question question : questions) {
        		question.setAnswers(findAnswersForWordlistQuestion (question.getIdQuestion(), namedParameterJdbcTemplate));
        	}
        }
        return questions;
	}

	private static List<Answer> findAnswersForWordlistQuestion (Long idQuestion, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
//        String sql = "SELECT * FROM answer WHERE id_question = :idQuestion ORDER BY id_answer";
        BeanPropertyRowMapper<Answer> answerRowMapper = BeanPropertyRowMapper.newInstance(Answer.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idQuestion", idQuestion);
        List<Answer> answers = namedParameterJdbcTemplate.query(findAnswersForQuestionSQL, args, answerRowMapper);
        return JdbcDaoStaticHelper.parseAnswerCompareAddlInfoOnAnswers(answers);
	}

	private static void setWordsAndPassages(Section section, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        String sql = "SELECT c.id_cword AS id_cword, " +
        		"	c.id_system AS id_system, " +
        		"	c.id_provider AS id_provider, " +
        		"	c.id_level AS id_level, " +
        		"	c.name AS word_name, " +
        		"	c.pronunciation AS pronunciation, " +
        		"	c.audio_file_url AS audio_file_url, " +
        		"	c.syllables AS syllables, " +
        		"	d.pos AS pos, " +
        		"	d.definition AS word_definition, " +
        		"	d.synonym AS synonym, " +
        		"	d.antonym AS antonym, " +
        		"	d.thesaurus AS thesaurus, " +
        		"	d.sampletext AS sampletext, " +
        		"	u.text AS passages, " +
        		"	u.source AS passages_source " +
        		"FROM cgradeword cg " +
        		"	INNER JOIN cword c on cg.id_cword = c.id_cword " +
        		"	LEFT OUTER JOIN cworddef d on c.id_cword = d.id_cword " +
        		"	LEFT OUTER JOIN cwordusage u on c.id_cword = u.id_cword " +
        		"WHERE c.published = 1 AND cg.id_section = :idSection " +
        		"ORDER BY c.id_cword"; 

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idSection", section.getIdSection());
        ColumnMapRowMapper rowMapper = new ColumnMapRowMapper();
        List<Map<String, Object>> cwordDataList =  namedParameterJdbcTemplate.query(sql, args, rowMapper);
    	List<WlWord> wlwords = new ArrayList<WlWord>();
    	List<WlPassage> wlpassages = new ArrayList<WlPassage>();
        for(Map<String, Object> map: cwordDataList){
    		WlWord wlword = new WlWord();
    		wlword.setIdWord((Long)map.get("id_cword"));
    		wlword.setIdWordlist(section.getIdSection());
    		wlword.setWord((String)map.get("word_name"));
    		wlword.setPronunciation(map.get("pronunciation") != null ? (String)(map.get("pronunciation")) : null);
    		wlword.setSyllables(map.get("syllables") != null ? (String)(map.get("syllables")) : null);
    		wlword.setPos(map.get("pos") != null ? ((String)(map.get("pos"))).toLowerCase() : null);
    		wlword.setAudioFileUrl(map.get("audio_file_url") != null ? (String)(map.get("audio_file_url")) : null);
    		wlword.setDefinition(map.get("word_definition") != null ? (String)(map.get("word_definition")) : null);
    		wlword.setSynonym(map.get("synonym") != null ? (String)(map.get("synonym")) : null);
    		wlword.setAntonym(map.get("antonym") != null ? (String)(map.get("antonym")) : null);
    		wlword.setThesaurus(map.get("thesaurus") != null ? (String)(map.get("thesaurus")) : null);
    		wlword.setSampletext(map.get("sampletext") != null ? (String)(map.get("sampletext")) : null);
    		wlwords.add(wlword);
    		
    		// see if there are passages
    		if (map.get("passages") != null) {
    			WlPassage wlpassage = new WlPassage();
    			wlpassage.setIdPassage((Long)map.get("id_cword"));
    			wlpassage.setIdWordlist(section.getIdSection());
    			wlpassage.setText((String)map.get("passages"));
    			wlpassages.add(wlpassage);
    		}
        }
        if (wlwords.size() > 0) {
        	section.setWords(wlwords);
        }
        if (wlpassages.size() > 0) {
        	section.setPassages(wlpassages);
        }
		
	}

//	private List<WlWord> getCwordsForSection (Long idSection) {
//        String sql = "SELECT c.* FROM cgradeword cg INNER JOIN cword c on cg.id_cword = c.id_cword WHERE cg.id_section = :idSection ORDER BY c.id_cword;";
//        BeanPropertyRowMapper<Cword> cwordRowMapper = BeanPropertyRowMapper.newInstance(Cword.class);
//        Map<String, Object> args = new HashMap<String, Object>();
//        args.put("idSection", idSection);
//        List<Cword> cwords = getNamedParameterJdbcTemplate().query(sql, args, cwordRowMapper);
//        if (cwords == null) {
//        	return null;
//        } else {
//        	List<WlWord> wlwords = new ArrayList<WlWord>();
//        	for (Cword cword : cwords) {
//        		wlwords.add(mapCwordToWlWord(cword, idSection));
//        	}
//            return wlwords;
//        }
//	}
//
//	private WlWord mapCwordToWlWord(Cword cword, Long idSection) {
//		if (cword == null) {
//			return null;
//		}
//		WlWord wlword = new WlWord();
//		wlword.setIdWord(cword.getIdCword());
//		wlword.setIdWordlist(idSection);
//		wlword.setWord(cword.getName());
//		wlword.setPronunciation(cword.getPronunciation());
//		wlword.setDefinition(cword.getWordDefinitions() != null && cword.getWordDefinitions().size() > 0 ? cword.getWordDefinitions().get(0).getDefinition() : "Definition for: " + cword.getName());
//		wlword.setSynonym(cword.getWordDefinitions() != null && cword.getWordDefinitions().size() > 0 ? cword.getWordDefinitions().get(0).getSynonym() : "Synonym for: " + cword.getName());
//		wlword.setAntonym(cword.getWordDefinitions() != null && cword.getWordDefinitions().size() > 0 ? cword.getWordDefinitions().get(0).getAntonym() : "Antonym for: " + cword.getName());
//		wlword.setThesaurus(cword.getWordDefinitions() != null && cword.getWordDefinitions().size() > 0 ? cword.getWordDefinitions().get(0).getThesaurus() : "Thesaurus for: " + cword.getName());
//		wlword.setSampletext(cword.getWordDefinitions() != null && cword.getWordDefinitions().size() > 0 ? cword.getWordDefinitions().get(0).getSampletext() : "Sample text for: " + cword.getName());
//		return wlword;
//	}


	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update question Admin Functionality
	// below...
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public Question findQuestionForEdit(Long idQuestion) {
		return findQuestionByQuestionIdWithAnswers(idQuestion, getNamedParameterJdbcTemplate());
	}

	@Override
	public Integer saveQuestionText(Question question) {
		// create BeanPropertySqlParameterSource objects for question
		BeanPropertySqlParameterSource questionParameters = new BeanPropertySqlParameterSource(question);
		try {
			getNamedParameterJdbcTemplate().update(SectionDao.saveQuestionTextSQL, questionParameters);
		} catch (DataAccessException dae) {
			throw dae;
		}
		return 0;
	}

	@Override
	public Integer saveQuestionTextWithAnswers(Question question) {
		// save questionText 
		Integer savdResponse = this.saveQuestionText(question);
		// now save answers
		if (savdResponse == 0) {
			if (question.getAnswers() != null && question.getAnswers().size() > 0) {
				List<SqlParameterSource> answerParameters = new ArrayList<SqlParameterSource>();
				for (Answer answer : question.getAnswers()) {
					answerParameters.add(new BeanPropertySqlParameterSource(answer)); // idQuestion is not set
				}
				getNamedParameterJdbcTemplate().batchUpdate(SectionDao.saveAnswerTextSQL, answerParameters.toArray(new SqlParameterSource[0]));
			}
		}
		return 0;
	}

	@Override
	public Integer saveQuestionExplanation(Question question) {
		// create BeanPropertySqlParameterSource objects for question
		BeanPropertySqlParameterSource questionParameters = new BeanPropertySqlParameterSource(question);
		try {
			getNamedParameterJdbcTemplate().update(SectionDao.saveQuestionAddlInfoSQL, questionParameters);
		} catch (DataAccessException dae) {
			throw dae;
		}
		return 0;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// above...
	// Update question Admin Functionality
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public Section findSectionBySectionIdForPrint(Long idSection) {
		String sql = findSectionBySectionIdForPrintSQL;
		BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper
				.newInstance(Section.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSection", idSection);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		Section section = null;
		try {
			section = getNamedParameterJdbcTemplate().queryForObject(sql, args, sectionRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		// now get the questions for this section
		if (section != null) {
			if (Section.DERIVED_SECTION_TYPE.equalsIgnoreCase(section.getSectionType())) {
				section.setQuestions(findQuestionsForDerivedSection(section.getIdSection(), getNamedParameterJdbcTemplate()));
				section.setQuestionsets(findQuestionsetsForDerivedSection(section.getIdSection(), getNamedParameterJdbcTemplate()));
			} else {
				section.setQuestions(findQuestionsForSection(section.getIdSection(), getNamedParameterJdbcTemplate()));
				section.setQuestionsets(findQuestionsetsForSection(section.getIdSection(), getNamedParameterJdbcTemplate()));
			} 
		}
		return section;
	}

	// Exclusive Print Related Functions
	@Override
	public Integer savePrintsectionsettings(Printsectionsettings printsectionsettings) {
		// create BeanPropertySqlParameterSource objects for section
		BeanPropertySqlParameterSource printsectionsettingsParameters = new BeanPropertySqlParameterSource(printsectionsettings);
		// add section
		getNamedParameterJdbcTemplate().update(updatePrintsectionsettingsSQL, printsectionsettingsParameters);
		return 0;
	}

	@Override
	public Integer deletePrintsectionsettings(Long idSection) {
		// delete any print settings for test
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idSection", idSection);
        getNamedParameterJdbcTemplate().update(deletePrintsectionsettingsSQL, args);
		return 0;
	}

	
	
	
}
