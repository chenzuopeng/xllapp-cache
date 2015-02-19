package org.xllapp.cache;

import java.util.Date;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xllapp.cache.CacheValue;
import org.xllapp.cache.XMMemcCacheProvider;
import org.xllapp.cache.XMMemcCacheProvider.Config;

/**
 * 
 * 
 * @Copyright: Copyright (c) 2013 FFCS All Rights Reserved
 * @Company: 北京福富软件有限公司
 * @author 陈作朋 Sep 11, 2013
 * @version 1.00.00
 * @history:
 * 
 */
@Ignore
public class XMMemcCachedProviderTest {

	/**
	 * 添加
	 */
	@Test
	public void test1() throws Exception {
		Config config = new Config();
		config.setServers("127.0.0.1:11211");
		XMMemcCacheProvider memcCacheProvider = new XMMemcCacheProvider(config);
		String key = "key";
		CacheValue value = new CacheValue("value", new Date());
		memcCacheProvider.addCache(key,value);
		Assert.assertEquals(value, memcCacheProvider.getCache(key));
	}
	
	/**
	 * 更新
	 */
	@Test
	public void test2() throws Exception {
		Config config = new Config();
		config.setServers("127.0.0.1:11211");
		XMMemcCacheProvider memcCacheProvider = new XMMemcCacheProvider(config);
		String key = "key";
		CacheValue value = new CacheValue("value", new Date());
		memcCacheProvider.addCache(key,value);
		CacheValue newValue = new CacheValue("你好呀ａｆｓｆｄａ224", new Date());
		memcCacheProvider.updateCache(key,newValue);
		Assert.assertEquals(newValue, memcCacheProvider.getCache(key));
	}
	
	/**
	 * 外部指定weights
	 */
	@Test
	public void test3() throws Exception {
		Config config = new Config();
		config.setServers("127.0.0.1:11211");
		config.setWeights(1);
		XMMemcCacheProvider memcCacheProvider = new XMMemcCacheProvider(config);
		String key = "key";
		CacheValue value = new CacheValue("value", new Date());
		memcCacheProvider.addCache(key,value);
		CacheValue newValue = new CacheValue("你好呀ａｆｓｆｄａ224", new Date());
		memcCacheProvider.updateCache(key,newValue);
		Assert.assertEquals(newValue, memcCacheProvider.getCache(key));
	}
	
	/**
	 * 连接异常
	 */
	@Test(expected=Exception.class)
	public void test4(){
		Config config = new Config();
		config.setServers("99.0.0.1:11212");
		config.setConnectTimeout(1);
		XMMemcCacheProvider memcCacheProvider = new XMMemcCacheProvider(config);
		String key = "key";
		CacheValue value = new CacheValue("value", new Date());
		memcCacheProvider.addCache(key,value);
	}
	
	/**
	 * 集群1
	 * @throws Exception 
	 */
	@Test
	public void test51() throws Exception{
		Config config = new Config();
		config.setServers("127.0.0.1:11211,127.0.0.1:11212");
		XMMemcCacheProvider memcCacheProvider = new XMMemcCacheProvider(config);
		String key1 = "key1";
		CacheValue value1 = new CacheValue("value1", new Date());
		memcCacheProvider.addCache(key1,value1);
		String key2 = "key2";
		CacheValue value2 = new CacheValue("value2", new Date());
		memcCacheProvider.addCache(key2,value2);
		
		Assert.assertEquals(value1, memcCacheProvider.getCache(key1));
		Assert.assertEquals(value2, memcCacheProvider.getCache(key2));
	}
	
	/**
	 * 集群2,服务器地址中包含前后引导空格
	 * @throws Exception 
	 */
	@Test
	public void test52() throws Exception{
		Config config = new Config();
		config.setServers("127.0.0.1:11211 ,  127.0.0.1:11212 ");
		XMMemcCacheProvider memcCacheProvider = new XMMemcCacheProvider(config);
		String key1 = "key1";
		CacheValue value1 = new CacheValue("value1", new Date());
		memcCacheProvider.addCache(key1,value1);
		String key2 = "key2";
		CacheValue value2 = new CacheValue("value2", new Date());
		memcCacheProvider.addCache(key2,value2);
		
		Assert.assertEquals(value1, memcCacheProvider.getCache(key1));
		Assert.assertEquals(value2, memcCacheProvider.getCache(key2));
	}
	
	/**
	 * 删除
	 */
	@Test
	public void test6() throws Exception{
		Config config = new Config();
		config.setServers("127.0.0.1:11211");
		XMMemcCacheProvider memcCacheProvider = new XMMemcCacheProvider(config);
		String key = "key";
		CacheValue value = new CacheValue("value", new Date());
		memcCacheProvider.addCache(key,value);
		Assert.assertEquals(value, memcCacheProvider.getCache(key));
		memcCacheProvider.deleteCache(key);
		Assert.assertEquals(null, memcCacheProvider.getCache(key));
	}
	
}
