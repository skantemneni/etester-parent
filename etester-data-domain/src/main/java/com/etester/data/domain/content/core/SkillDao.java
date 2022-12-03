package com.etester.data.domain.content.core;

import java.util.List;

public interface SkillDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.

	public static final String findBySkillIdSQL = "SELECT * FROM skill WHERE id_skill = :idSkill";

	public static String insertSkillSQL = "INSERT INTO skill (id_skill, id_topic, id_provider, subject, name, display_name, description, text, addl_info, published, derived_skill, id_skill_reference) "
			+ " VALUES (:idSkill, :idTopic, :idProvider, :subject, :name, :displayName, :description, :text, :addlInfo, :published, :derivedSkill, :idSkillReference)";

	public static String insertGradeskillSQL = "INSERT INTO gradeskill (grade_name, id_skill, alt_name, alt_description) "
			+ " VALUES (:gradeName, :idSkill, :altName, :altDescription)";

	public void insert(Skill skill);

    public void update(Skill skill);

    public void delete(Long idSkill);

    public Skill findBySkillId(Long idSkill);

	public List<Skill> findSkillsForTopic (Long idTopic);

	public List<Skill> findSkillsForTopicAndProvider (Long idTopic, Long idProvider);

	public List<Skill> findUnattachedSkillsForProvider (Long idProvider);

	public List<Skill> findAllSkillsForTopicAndProvider (Long idTopic, Long idProvider);

	public List<Skill> findBySkillName(String name, Long idProvider);

    public List<Skill> findAllSkills();

    public void insertBatch(List<Skill> skills);

    // This deletes the data before reloading the same
    public void insertBatch(List<Skill> skills, boolean reload);

    public int countAll();
}
