package org.xllapp.cache;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
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
 * @author 陈作朋 Sep 20, 2013
 * @version 1.00.00
 * @history:
 * 
 */
@Ignore
public class MemcachedTest {

	@Test
	public void test1() throws Exception {
		Config config = new Config();
		config.setServers("127.0.0.1:11211,  127.0.0.1:11212");
		XMMemcCacheProvider memcCacheProvider = new XMMemcCacheProvider(config);
		byte[] date=new byte[10*1024*1024];
		Arrays.fill(date,(byte)1);
		int m=99999999;
		for (int i = 0; i < m; i++) {
			try {
				String key = i+"";
//				CacheValue value = new CacheValue(date, DateUtils.addYears(new Date(), 999));
				CacheValue value = new CacheValue("value"+i, DateUtils.addYears(new Date(), 999));
				memcCacheProvider.addCache(key,value);
//				Thread.sleep(1000);
				System.out.println("-----:"+i);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	@Test
	public void test2() throws Exception {
		Config config = new Config();
		config.setServers("127.0.0.1:11211,127.0.0.1:11212");
		XMMemcCacheProvider memcCacheProvider = new XMMemcCacheProvider(config);
		for (int i = 0; i < 29; i++) {
			String key = i+"";
			System.out.println(key+":"+memcCacheProvider.getCache(key));
		}

	}
	
	@Test
	public void test3() throws Exception {
		Config config = new Config();
		config.setServers("127.0.0.1:11211");
		XMMemcCacheProvider memcCacheProvider = new XMMemcCacheProvider(config);
		System.out.println(memcCacheProvider.getCache("com.ffcs.icity.api.menuservice.controller.MoreMenuServiceController.3501_400_android_700_icity_ver"));

	}
	
	@Test
	public void test4() throws Exception {
		Config config = new Config();
		config.setServers("127.0.0.1:11211");
		XMMemcCacheProvider memcCacheProvider = new XMMemcCacheProvider(config);
		String key="com.ffcs.icity.api.info.controller.HomePageInfoController.3501_18965915170_android_1";
		/*System.out.println("before:"+memcCacheProvider.getCache(key));
		memcCacheProvider.updateCache(key, new CacheValue("afsafsafsafd", new Date()));
		System.out.println("after:"+memcCacheProvider.getCache(key));*/
		memcCacheProvider.deleteCache(key);

	}
}
