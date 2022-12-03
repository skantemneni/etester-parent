package com.etester.data.domain.util.cache;

import java.util.ArrayList;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;


/**
 * Adopted from the original author by Sesi Kantemneni...with the following modifications.
 * 1.) A hard clean - which empties teh cache, no matter what.
 * 2.) Force Clean after Max time - irrespective of last access
 *  
 * @author Crunchify.com
 */

public class CrunchifyInMemoryCache<K, T> {

	private static final long DEFAULT_TIME_TO_LIVE = 3600; // 1 hour 
	private static final long DEFAULT_MAX_TIME_TO_LIVE = 18000; // 5 hours
	private static final long DEFAULT_CLEANUP_TIMER_INTERVAL = 300; // 5 minutes
	private static final int DEFAULT_MAX_ITEMS_TO_CACHE = 20; // 1 hour 

	private long timeToLive;
	private long maxTimeToLive;
	private long timerInterval;
	@SuppressWarnings("rawtypes")
	private LRUMap crunchifyCacheMap;
	protected class CrunchifyCacheObject {
		public long birthTime = System.currentTimeMillis();
		public long lastAccessed = System.currentTimeMillis();
		public T value;

		protected CrunchifyCacheObject(T value) {
			this.value = value;
		}
	}

	/**
	 * Create a Cache with all defaults.  Uses defaults for the folowing Params:
	 * timeToLiveInSeconds (1 hour), maxTimeToLiveInSeconds (5 hours) and timerIntervalInSeconds (5 minutes)
	 * 
	 * @param maxItems - max items in cache
	 */
	public CrunchifyInMemoryCache() {
		this (DEFAULT_TIME_TO_LIVE, DEFAULT_MAX_TIME_TO_LIVE, DEFAULT_CLEANUP_TIMER_INTERVAL, DEFAULT_MAX_ITEMS_TO_CACHE);
	}
	/**
	 * Create a Cache with just the Max Items param.  Uses defaults for the folowing Params:
	 * timeToLiveInSeconds (1 hour), maxTimeToLiveInSeconds (5 hours) and timerIntervalInSeconds (5 minutes)
	 * 
	 * @param maxItems - max items in cache
	 */
	public CrunchifyInMemoryCache(int maxItems) {
		this (DEFAULT_TIME_TO_LIVE, DEFAULT_MAX_TIME_TO_LIVE, DEFAULT_CLEANUP_TIMER_INTERVAL, maxItems);
	}
	/**
	 * Create a Cache with the following params:
	 * @param timeToLiveInSeconds - Continues Inactivity time - in seconds - after which the cache is deleted 
	 * @param timeToLiveInSeconds - Total MAX time to live - in seconds - after which the cache is deleted anyway
	 * @param timerIntervalInSeconds - time intervals - in seconds - between inactivity checks
	 * @param maxItems - max items in cache
	 */
	public CrunchifyInMemoryCache(long timeToLiveInSeconds, long maxTimeToLiveInSeconds, 
			final long timerIntervalInSeconds, int maxItems) {
		if (timeToLiveInSeconds <= 0) {
			this.timeToLive = DEFAULT_TIME_TO_LIVE * 1000;
		} else {
			this.timeToLive = timeToLiveInSeconds * 1000;
		}
		
		if (maxTimeToLiveInSeconds <= 0) {
			this.maxTimeToLive = DEFAULT_MAX_TIME_TO_LIVE * 1000;
		} else {
			this.maxTimeToLive = maxTimeToLiveInSeconds * 1000;
		}
		
		if (timerIntervalInSeconds <= 0) {
			this.timerInterval = DEFAULT_CLEANUP_TIMER_INTERVAL * 1000;
		} else {
			this.timerInterval = timerIntervalInSeconds * 1000;
		}
		
		crunchifyCacheMap = new LRUMap(maxItems);

		if (this.timeToLive > 0 && this.timerInterval > 0) {

			Thread t = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(timerInterval * 1000);
						} catch (InterruptedException ex) {
						}
						cleanup();
					}
				}
			});

			t.setDaemon(true);
			t.start();
		}
	}

	public void put(K key, T value) {
		synchronized (crunchifyCacheMap) {
			crunchifyCacheMap.put(key, new CrunchifyCacheObject(value));
		}
	}

	@SuppressWarnings("unchecked")
	public T get(K key) {
		synchronized (crunchifyCacheMap) {
			CrunchifyCacheObject c = (CrunchifyCacheObject) crunchifyCacheMap
					.get(key);

			if (c == null)
				return null;
			else {
				c.lastAccessed = System.currentTimeMillis();
				return c.value;
			}
		}
	}

	public void remove(K key) {
		synchronized (crunchifyCacheMap) {
			crunchifyCacheMap.remove(key);
		}
	}

	public int size() {
		synchronized (crunchifyCacheMap) {
			return crunchifyCacheMap.size();
		}
	}

	@SuppressWarnings("unchecked")
	public void forceClearAll() {
		// clear the cache
		synchronized (crunchifyCacheMap) {
			crunchifyCacheMap.clear();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void cleanup() {

		long now = System.currentTimeMillis();
		ArrayList<K> deleteKey = null;

		synchronized (crunchifyCacheMap) {
			MapIterator itr = crunchifyCacheMap.mapIterator();

			deleteKey = new ArrayList<K>((crunchifyCacheMap.size() / 2) + 1);
			K key = null;
			CrunchifyCacheObject c = null;

			while (itr.hasNext()) {
				key = (K) itr.next();
				c = (CrunchifyCacheObject) itr.getValue();
				// mark for delete when one of the following 2 conditions happen:
				// 1.) Last access time is more than "timeToLive" or 
				// 2.) the item is atleast "maxTimeToLive" old (now > maxTimeToLive + c.birthTime)
				if (c != null && (now > (timeToLive + c.lastAccessed) || now > (maxTimeToLive + c.birthTime))) {
					deleteKey.add(key);
				}
			}
		}

		for (K key : deleteKey) {
			synchronized (crunchifyCacheMap) {
				crunchifyCacheMap.remove(key);
			}

			Thread.yield();
		}
	}
	/**
	 * @return the maxSize
	 */
	public int getMaxSize() {
		return crunchifyCacheMap.maxSize();
	}
	/**
	 * @return the size
	 */
	public int getCurrentSize() {
		return crunchifyCacheMap.size();
	}
	/**
	 * Note that this method clears the cache - no matter down-sizing or up-sizing.  Infact it will simply 
	 * replace the underlying LRUMap with a new one.
	 * @param maxSize the maxSize to set
	 */
	public void setMaxSize(int maxSize) {
		crunchifyCacheMap = new LRUMap(maxSize);
	}
	
}
