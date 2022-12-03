package com.etester.data.domain.util.cache;

import org.springframework.data.redis.core.RedisTemplate;

import com.etester.data.domain.content.instance.Testinstance;

public class EtesterTestinstanceRedisCacheClient {
	
	private static final String TESTINSTANCE_KEY = "TESTINSTANCE";

	private RedisTemplate<String, Testinstance> redisTestinstanceTemplate;


	public static void main(String[] args) {
//		Jedis jedis = new Jedis("localhost");
//		jedis.rpush("authors", "Marten Deinum", "Josh Long", "Daniel Rubio", "Gary Mak");
//		System.out.println("Authors: " + jedis.lrange("authors", 0, -1));
//		jedis.hset("sr_3", "authors", "Gary Mak, Danial Rubio, Josh Long, Marten Deinum");
//		jedis.hset("sr_3", "published", "2014");
//		System.out.println("Spring Recipes 3rd: " + jedis.hgetAll("sr_3"));
	}


	/**
	 * Locate and return a Testinstance from the redis database
	 * @param idTestinstance
	 * @return
	 */
	public Testinstance locateTestinstance(Long idTestinstance) {
		Testinstance testinstance = (Testinstance) redisTestinstanceTemplate.opsForHash().get(TESTINSTANCE_KEY, idTestinstance.toString());
		return testinstance;
	}

	/**
	 * Locate and return a Testinstance from the redis database
	 * @param idTestinstance
	 * @return
	 */
	public boolean storeTestinstance(Testinstance testinstance) {
		redisTestinstanceTemplate.opsForHash().put(TESTINSTANCE_KEY, testinstance.getIdTestinstance().toString(), testinstance);
		return true;
	}


	/**
	 * @return the redisTestinstanceTemplate
	 */
	public RedisTemplate<String, Testinstance> getRedisTestinstanceTemplate() {
		return redisTestinstanceTemplate;
	}


	/**
	 * @param redisTestinstanceTemplate the redisTestinstanceTemplate to set
	 */
	public void setRedisTestinstanceTemplate(
			RedisTemplate<String, Testinstance> redisTestinstanceTemplate) {
		this.redisTestinstanceTemplate = redisTestinstanceTemplate;
	}
	
	



}
