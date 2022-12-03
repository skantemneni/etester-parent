package com.etester.data.domain.content.core;

import java.util.List;

public interface TopicDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.
	public static final String findByTopicIdSQL = "SELECT * FROM topic WHERE id_topic = :idTopic";

	public static String insertTopicSQL = "INSERT INTO topic (id_topic, id_level, id_provider, subject, name, display_name, description, text, addl_info, published, derived, id_topic_reference) "
			+ " VALUES (:idTopic, :idLevel, :idProvider, :subject, :name, :displayName, :description, :text, :addlInfo, :published, :derived, :idTopicReference)";

	public void insert(Topic topic);

    public void update(Topic topic);

    public void delete(Long idTopic);

    public Topic findByTopicId(Long idTopic);

	public List<Topic> findTopicsForLevel (Long idLevel);

	public List<Topic> findTopicsForLevelAndProvider (Long idTopic, Long idProvider);

	public List<Topic> findUnattachedTopicsForProvider (Long idProvider);

	public List<Topic> findAllTopicsForLevelAndProvider (Long idLevel, Long idProvider);

	public List<Topic> findByTopicName(String name, Long idProvider);

    public List<Topic> findAllTopics();

    public List<Topic> findAllTopicsAndSkills();

    public void insertBatch(List<Topic> topics);

    // This deletes the data before reloading the same
    public void insertBatch(List<Topic> topics, boolean reload);

    public int countAll();
}
