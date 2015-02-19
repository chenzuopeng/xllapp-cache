package org.xllapp.cache;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 此类提供一种可以为任意方法提供缓存支持的机制.此类是线程安全的.
 * 
 * @author dylan.chen Sep 7, 2013
 * 
 */
public class CacheTemplate {

	private final static Logger logger = LoggerFactory.getLogger(CacheTemplate.class);

	/**
	 * 默认的缓存过期时长:30分钟
	 */
	public final static int DEFAULT_EXPIRY = 30 * 60;

	/**
	 * 默认的缓存过期时长,单位:秒
	 */
	private int expiry = DEFAULT_EXPIRY;

	/**
	 * 在被缓存方法执行失败时,是否使用过期的缓存
	 */
	private boolean useOldCacheIfFail = true;

	private ThreadPoolExecutor threadPool;

	private CacheProvider cacheProvider;

	public CacheTemplate(CacheProvider cacheProvider) {
		this(cacheProvider, true);
	}

	public CacheTemplate(CacheProvider cacheProvider, boolean useOldCacheIfFail) {
		this(cacheProvider, useOldCacheIfFail, DEFAULT_EXPIRY, initDefaultThreadPool());
	}

	public CacheTemplate(CacheProvider cacheProvider, boolean useOldCacheIfFail, int expiry, ThreadPoolExecutor threadPool) {
		this.cacheProvider = cacheProvider;
		this.expiry = expiry;
		this.useOldCacheIfFail = useOldCacheIfFail;
		this.threadPool = threadPool;
	}

	public static ThreadPoolExecutor initDefaultThreadPool() {
		return new ThreadPoolExecutor(1, 2, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),Executors.defaultThreadFactory(),new ThreadPoolExecutor.CallerRunsPolicy());
	}

	/**
	 * 执行带有缓存的方法.
	 * 
	 * @param callback
	 *            CacheCallback接口实现
	 * @return 返回结果
	 * @throws Exception
	 *             执行带有缓存的方法失败时,抛出此异常
	 */
	public Object execute(CacheCallback callback) throws Exception {
		return execute(callback, this.expiry);
	}

	/**
	 * 
	 * @param callback
	 *            　CacheCallback接口实现
	 * @param expiry
	 *            缓存的过期时长,单位:秒
	 * @return　返回结果
	 * @throws Exception
	 *             　执行带有缓存的方法失败时,抛出此异常
	 */
	public Object execute(CacheCallback callback, int expiry) throws Exception {

		String key = callback.getKey();

		logger.debug("cache key:{}", key);

		if (null == key) {
			Object value = callback.getValue();
			logger.debug("key is null,directly return execution result value[{}]", value);
			return value;
		}

		CacheValue cacheValue = null;
		try {
			cacheValue = this.cacheProvider.getCache(key);
			logger.debug("fetch cache[key={},value={}]", key, cacheValue);
		} catch (Exception e) {
			Object value = callback.getValue();
			logger.warn("failure to fetch key[" + key + "] cache,directly return execution result value[" + value + "].caused by:" + e.getLocalizedMessage(), e);
			return value;
		}

		// 初始化缓存
		if (null == cacheValue) {
			Serializable initValue = callback.getValue();
			logger.debug("initialized cache value:{}", initValue);
			CacheValue initCacheValue = new CacheValue(initValue);
			setCache(key, initCacheValue, false);
			logger.debug("key[{}] cache is empty,return execution result value[{}] and added to cache[{}]", key, initValue, initCacheValue);
			return initValue;
		}

		// 验证缓存有效性
		if (!verifyCache(key, cacheValue, expiry)) {
			try {
				Serializable newlValue = callback.getValue();
				logger.debug("new cached value:{}", newlValue);
				CacheValue newCacheValue = new CacheValue(newlValue);
				setCache(key, newCacheValue, true);
				logger.debug("key[{}] cache[{}] is invalid,return re-execute result value[{}] and replaced by cache[{}]", key, cacheValue, newlValue, newCacheValue);
				return newlValue;
			} catch (Exception e) {
				logger.warn("re-execute failed when key[" + key + "] cache[" + cacheValue + "] is expired,return old cache value[" + cacheValue.getObject() + "].caused by:" + e.getLocalizedMessage(), e);
				if (!this.useOldCacheIfFail) {
					throw e;
				}
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("key[{}] cache[{}] is valid,return cached value[{}]", key, cacheValue, cacheValue.getObject());
			}
		}

		return cacheValue.getObject();
	}

	/**
	 * 删除缓存.
	 * 
	 * @param key
	 *            key
	 */
	public void deleteCache(String key) {
		this.cacheProvider.deleteCache(key);
	}

	private void setCache(final String key, final CacheValue cacheValue, final boolean isUpdated) {
		try {
			this.threadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						if (isUpdated) {
							CacheTemplate.this.cacheProvider.updateCache(key, cacheValue);
						} else {
							CacheTemplate.this.cacheProvider.addCache(key, cacheValue);
						}
					} catch (Exception e) {
						logger.warn("failure to set key[" + key + "] cache[" + cacheValue + "].caused by:" + e.getLocalizedMessage(), e);
					}
				}
			});
		} catch (RejectedExecutionException e) {
			logger.warn("failure to set key[" + key + "] cache[" + cacheValue + "].caused by:thread pool is full", e);
		}
	}

	private boolean verifyCache(String key,CacheValue cacheValue, int expiry) {
		
		//验证缓存是否有效
		if(!cacheValue.isValid()){
			logger.warn("key[{}] cache[{}] is broken", key,cacheValue);
			return false;
		}
		
		//验证缓存是否过期
		Date createDate=cacheValue.getCreateDate();
		Date currentDate = new Date();
		Date expiredDate = DateUtils.addSeconds(createDate, expiry);
		boolean expired=currentDate.after(expiredDate);
		if(expired){
			logger.debug("key[{}] cache[{}] is expired", key,cacheValue);
			return false;
		}
		
		return true;
	}

}
