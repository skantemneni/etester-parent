package com.etester.dataloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.extern.slf4j.Slf4j;

@ComponentScan({ "com.etester.data", "com.etester.dataloader" })

@Slf4j
@SpringBootApplication
public class EtesterLoadContentApplication {

	public static void main(String[] args) {
		log.info("STARTING THE APPLICATION");
		SpringApplication.run(EtesterLoadContentApplication.class, args);
		log.info("APPLICATION FINISHED");
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

//	@Bean
//	public DataSource dataSource() {
//		return new DriverManagerDataSource();
//	}
//
//	@Bean
//	public UserDao userDao() {
//		return new JdbcUserDao(dataSource(), passwordEncoder());
//	}
//
//	@Bean
//	public ChannelDao channelDao() {
//		return new JdbcChannelDao(dataSource());
//	}
//
//	@Bean
//	public LevelDao levelDao() {
//		return new JdbcLevelDao();
//	}
//
//	@Bean
//	public TopicDao topicDao() {
//		return new JdbcTopicDao();
//	}
//
//	@Bean
//	public SkillDao skillDao() {
//		return new JdbcSkillDao();
//	}
//
//	@Bean
//	public GradeDao gradeDao() {
//		return new JdbcGradeDao();
//	}
//
//	@Bean
//	public SectionDao sectionDao() {
//		return new JdbcSectionDao();
//	}
//
//	@Bean
//	public CwordDao cwordDao() {
//		return new JdbcCwordDao();
//	}
//
//	@Bean
//	public WlWordlistDao wordlistDao() {
//		return new JdbcWlWordlistDao();
//	}
//
//	@Bean
//	public TestDao testDao() {
//		return new JdbcTestDao(dataSource());
//	}
//
//	@Bean
//	public TestsectionInstructionsDao testsectionInstructionsDao() {
//		return new JdbcTestsectionInstructionsDao();
//	}
//
////	@Bean
////	public DataloaderServiceImpl dataloaderServiceImpl() {
////		return new DataloaderServiceImpl (userDao(), channelDao(), levelDao(), topicDao(), skillDao(), 
////				gradeDao(), sectionDao(), cwordDao(), wordlistDao(), testDao(), testsectionInstructionsDao());
////	}
	
}
