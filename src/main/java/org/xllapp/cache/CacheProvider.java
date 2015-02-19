package org.xllapp.cache;

/**
 * 实现此接口提供具体的缓存实现.
 *
 * @author dylan.chen Sep 8, 2013
 * 
 */
public interface CacheProvider {
	
	/**
	 * 获取缓存.
	 * 
	 * @param key key
	 * @return 缓存的值
	 * @exception 如果获取缓存失败时,抛出此异常
	 */
	public CacheValue getCache(String key) throws Exception;
	
	/**
	 * 添加缓存.
	 * 
	 * @param key key 
	 * @param value 缓存的值
	 */
	public void addCache(String key,CacheValue value);
	
	/**
	 * 更新缓存.
	 * 
	 * @param key key 
	 * @param value 缓存的值
	 */
	public void updateCache(String key,CacheValue value);
	
	/**
	 * 删除缓存.
	 * 
	 * @param key key 
	 */
	public void deleteCache(String key);
	
}
