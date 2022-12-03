package com.etester.data.domain.util.cache;

import java.util.Timer;
import java.util.TimerTask;

import com.etester.data.domain.content.core.Channel;
import com.etester.data.domain.content.core.Level;
import com.etester.data.domain.content.core.Section;
import com.etester.data.domain.content.instance.Testinstance;
import com.etester.data.domain.test.Test;

public class EtesterCacheController {
	
	private static final int DEFAULT_DISABLE_CACHING_TIME_IN_SECONDS = 7200;
	private static boolean IS_CACHING = true;
	private static Timer timer = null;
//	
//	private static Thread t = new Thread(new Runnable() {
//		public void run() {
//			try {
//				Thread.sleep(120 * 1000);
//			} catch (InterruptedException ex) {
//			}
//			EtesterCacheController.DISABLE_CACHE = true;
//		}
//	}); 
	
	public static void disableCaching () {
        System.out.println("Disabling Cache.  Setting 'IS_CACHING = false'");
		EtesterCacheController.IS_CACHING = false;
		// cancel any pre existing timers
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	    timer = new Timer();
	    timer.schedule(new TimerTask () {
	    	@Override
	        public void run() {
	            System.out.println("Time's up!  Setting 'IS_CACHING = true'");
	            EtesterCacheController.IS_CACHING = true;
	            timer.cancel();
	            timer = null;
	          }
	        }
	    , DEFAULT_DISABLE_CACHING_TIME_IN_SECONDS * 1000);
	}

	public static boolean isCaching() {
		return IS_CACHING;
	}
	
	// Test Cache
	// Tests stay in cache for 30 minutes of inactivity (1800 seconds)
	// Tests are cleared (& reloaded) after 5 hours even if there is active usage (18000)
	// Cache is checked every 5 minutes to clear inactive and really old tests (300)
	// Cache has a max of 100 tests
	private static CrunchifyInMemoryCache<Long, Test> TEST_CACHE = new CrunchifyInMemoryCache<Long, Test> (1800, 18000, 300, 100 /* 100 tests */);
	public static void putTest (Test test) {
		TEST_CACHE.put(test.getIdTest(), test);
	}
	public static Test getTest (Long idTest) {
		return TEST_CACHE.get(idTest);
	}
	public static void nukeTestCache () {
		TEST_CACHE.forceClearAll();
	}
	public static void setMaxTestCacheSize(int noOfTests) {
		TEST_CACHE.setMaxSize(noOfTests);
	}
	public static int getMaxTestCacheSize() {
		return TEST_CACHE.getMaxSize();
	}
	public static int getCurrentTestCacheSize() {
		return TEST_CACHE.getCurrentSize();
	}

	
	
	// Testinstance Cache (Reports Cache) for reports
	// Testinstances stay in cache for 15 minutes of inactivity (900 seconds)
	// Testinstances are cleared (& reloaded) after 60 minutes even if there is active usage (3600)
	// Cache is checked every 5 minutes to clear inactive and really old Testinstances (300)
	// Cache has a max of 1000 Testinstances
	private static CrunchifyInMemoryCache<Long, Testinstance> TESTINSTANCE_CACHE = new CrunchifyInMemoryCache<Long, Testinstance> (900, 3600, 300, 1000 /* 1000 reports */);
	public static void putTestinstance (Testinstance testinstance) {
		TESTINSTANCE_CACHE.put(testinstance.getIdTestinstance(), testinstance);
	}
	public static Testinstance getTestinstance (Long idTestinstance) {
		return TESTINSTANCE_CACHE.get(idTestinstance);
	}
	public static void nukeTestinstanceCache () {
		TESTINSTANCE_CACHE.forceClearAll();
	}
	public static void setMaxTestinstanceCacheSize(int noOfTestinstances) {
		TESTINSTANCE_CACHE.setMaxSize(noOfTestinstances);
	}
	public static int getMaxTestinstanceCacheSize() {
		return TESTINSTANCE_CACHE.getMaxSize();
	}
	public static int getCurrentTestinstanceCacheSize() {
		return TESTINSTANCE_CACHE.getCurrentSize();
	}
	
	
	// Channel with Levels Cache
	// Channels stay in cache for 30 minutes of inactivity (1800 seconds)
	// Channels are cleared (& reloaded) after 5 hours even if there is active usage (18000)
	// Cache is checked every 5 minutes to clear inactive and really old levels (300)
	// Cache has a max of 20 Channels
	private static CrunchifyInMemoryCache<Long, Channel> CHANNELSWITHLEVELS_CACHE = new CrunchifyInMemoryCache<Long, Channel> (1800, 18000, 300, 20 /* 100 levels */);
	public static void putChannelWithLevels (Channel channel) {
		CHANNELSWITHLEVELS_CACHE.put(channel.getIdSystem(), channel);
	}
	public static Channel getChannelWithLevels (Long idChannel) {
		return CHANNELSWITHLEVELS_CACHE.get(idChannel);
	}
	public static void nukeChannelWithLevelsCache () {
		CHANNELSWITHLEVELS_CACHE.forceClearAll();
	}
	public static void setMaxChannelWithLevelsCacheSize(int noOfChannels) {
		CHANNELSWITHLEVELS_CACHE.setMaxSize(noOfChannels);
	}
	public static int getMaxChannelWithLevelsCacheSize() {
		return CHANNELSWITHLEVELS_CACHE.getMaxSize();
	}
	public static int getCurrentChannelWithLevelsCacheSize() {
		return CHANNELSWITHLEVELS_CACHE.getCurrentSize();
	}
	
	// Level Cache
	// Levels stay in cache for 30 minutes of inactivity (1800 seconds)
	// Levels are cleared (& reloaded) after 5 hours even if there is active usage (18000)
	// Cache is checked every 5 minutes to clear inactive and really old levels (300)
	// Cache has a max of 100 levels
	private static CrunchifyInMemoryCache<Long, Level> LEVEL_CACHE = new CrunchifyInMemoryCache<Long, Level> (1800, 18000, 300, 100 /* 100 levels */);
	public static void putLevel (Level level) {
		LEVEL_CACHE.put(level.getIdLevel(), level);
	}
	public static Level getLevel (Long idLevel) {
		return LEVEL_CACHE.get(idLevel);
	}
	public static void nukeLevelCache () {
		LEVEL_CACHE.forceClearAll();
	}
	public static void setMaxLevelCacheSize(int noOfLevels) {
		LEVEL_CACHE.setMaxSize(noOfLevels);
	}
	public static int getMaxLevelCacheSize() {
		return LEVEL_CACHE.getMaxSize();
	}
	public static int getCurrentLevelCacheSize() {
		return LEVEL_CACHE.getCurrentSize();
	}
	

	// Level Cache
	// Levels stay in cache for 30 minutes of inactivity (1800 seconds)
	// Levels are cleared (& reloaded) after 5 hours even if there is active usage (18000)
	// Cache is checked every 5 minutes to clear inactive and really old levels (300)
	// Cache has a max of 100 levels
	private static CrunchifyInMemoryCache<Long, Level> LEVELWITHSECTIONS_CACHE = new CrunchifyInMemoryCache<Long, Level> (1800, 18000, 300, 100 /* 100 levels */);
	public static void putLevelWithSections (Level level) {
		LEVELWITHSECTIONS_CACHE.put(level.getIdLevel(), level);
	}
	public static Level getLevelWithSections (Long idLevel) {
		return LEVELWITHSECTIONS_CACHE.get(idLevel);
	}
	public static void nukeLevelWithSectionsCache () {
		LEVELWITHSECTIONS_CACHE.forceClearAll();
	}
	public static void setMaxLevelWithSectionsCacheSize(int noOfLevels) {
		LEVELWITHSECTIONS_CACHE.setMaxSize(noOfLevels);
	}
	public static int getMaxLevelWithSectionsCacheSize() {
		return LEVELWITHSECTIONS_CACHE.getMaxSize();
	}
	public static int getCurrentLevelWithSectionsCacheSize() {
		return LEVELWITHSECTIONS_CACHE.getCurrentSize();
	}
	
	//	private static final long DEFAULT_CLEANUP_TIMER_INTERVAL = 300; // 5 minutes

	// Section Cache
	// Sections stay in cache for 30 minutes of inactivity (1800 seconds)
	// Sections are cleared (& reloaded) after 5 hours even if there is active usage (18000)
	// Cache is checked every 5 minutes to clear inactive and really old levels (300)
	// Cache has a max of 100 levels
	private static CrunchifyInMemoryCache<Long, Section> SECTION_CACHE = new CrunchifyInMemoryCache<Long, Section> (1800, 18000, 300, 100 /* 100 levels */);
	public static void putSection (Section section) {
		SECTION_CACHE.put(section.getIdSection(), section);
	}
	public static Section getSection (Long idSection) {
		return SECTION_CACHE.get(idSection);
	}
	public static void nukeSectionCache() {
		SECTION_CACHE.forceClearAll();
	}
	public static void setMaxSectionCacheSize(int noOfSections) {
		SECTION_CACHE.setMaxSize(noOfSections);
	}
	public static int getMaxSectionCacheSize() {
		return SECTION_CACHE.getMaxSize();
	}
	public static int getCurrentSectionCacheSize() {
		return SECTION_CACHE.getCurrentSize();
	}
	


}
