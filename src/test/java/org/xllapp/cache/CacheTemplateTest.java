package org.xllapp.cache;

import java.lang.reflect.Field;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Assert;
import org.junit.Test;
import org.xllapp.cache.CacheCallback;
import org.xllapp.cache.CacheProvider;
import org.xllapp.cache.CacheTemplate;
import org.xllapp.cache.CacheValue;

/**
 * 
 * 
 * @Copyright: Copyright (c) 2008 FFCS All Rights Reserved
 * @Company: 北京福富软件有限公司
 * @author 陈作朋 Sep 8, 2013
 * @version 1.00.00
 * @history:
 * 
 */
public class CacheTemplateTest {

	public static class CacheValueEquals implements IArgumentMatcher {

		private String expected;

		private Date startTime;

		public CacheValueEquals(String expected, Date startTime) {
			this.expected = expected;
			this.startTime = startTime;
		}

		@Override
		public boolean matches(Object actual) {
			if (this.expected == null && actual == null) {
				return true;
			} else if (actual instanceof CacheValue) {
				CacheValue actualCacheValue = (CacheValue) actual;
				Date endTime = new Date();
				/**
				 * 过期时间验证规则: startTime(方法开始执行的时间) < expiryTime <
				 * endTime(方法执行完成的时间(此时过期时间已生成))
				 */
				boolean b = this.expected.equalsIgnoreCase((String) actualCacheValue.getObject()) && (this.startTime.before(actualCacheValue.getCreateDate()) || this.startTime.getTime() == actualCacheValue.getCreateDate().getTime()) && (endTime.getTime() == actualCacheValue.getCreateDate().getTime() || endTime.after(actualCacheValue.getCreateDate()));
				if (!b) {
					System.err.println("expected value:" + this.expected);
					System.err.println("actual value:" + actualCacheValue.getObject());
					System.err.println("startTime:" + DateFormatUtils.format(this.startTime, "yyyy-MM-dd hh:mm:ss"));
					System.err.println("endTime:" + DateFormatUtils.format(endTime, "yyyy-MM-dd hh:mm:ss"));
					System.err.println("CacheValue.getCreateDate():" + DateFormatUtils.format(actualCacheValue.getCreateDate(), "yyyy-MM-dd hh:mm:ss"));
				}
				return b;
			} else {
				return false;
			}
		}

		@Override
		public void appendTo(StringBuffer buffer) {
			buffer.append("CacheValueEquals fail");
		}

		public static CacheValue doEquals(String newValue, Date startDate) {
			EasyMock.reportMatcher(new CacheValueEquals(newValue, startDate));
			return null;
		}

	}

	/**
	 * 第一次调用,没有缓冲,直接返回方法执行的值并将值保存到缓存中
	 */
	// @Test
	public void test1() throws Exception {

		final String key = "key";
		final String value = "value";
		int expiry = 20;

		CacheCallback cacheCallback = EasyMock.createMock(CacheCallback.class);
		EasyMock.expect(cacheCallback.getKey()).andReturn(key).times(1);
		EasyMock.expect(cacheCallback.getValue()).andReturn(value).times(1);
		EasyMock.replay(cacheCallback);

		CacheProvider cacheProvider = EasyMock.createNiceMock(CacheProvider.class);
		EasyMock.expect(cacheProvider.getCache(key)).andReturn(null).times(1);

		cacheProvider.addCache(EasyMock.eq(key), CacheValueEquals.doEquals(value, new Date()));

		EasyMock.expectLastCall().times(1);

		EasyMock.replay(cacheProvider);

		String returnValue = (String) new CacheTemplate(cacheProvider).execute(cacheCallback, expiry);

		/**
		 * 由于CacheProvider.addCache()方法是异步调用的,为了确保调用到此方法,此处挂起线程一段时间
		 */
		Thread.sleep(1000);

		EasyMock.verify(cacheProvider, cacheCallback);
		Assert.assertEquals(value, returnValue);
	}

	/**
	 * 第一次调用,没有缓冲,并且执行被缓存的方法出现异常
	 */
	@Test(expected = Exception.class)
	public void test11() throws Exception {

		final String key = "key";

		CacheCallback cacheCallback = EasyMock.createMock(CacheCallback.class);
		EasyMock.expect(cacheCallback.getKey()).andReturn(key).times(1);
		EasyMock.expect(cacheCallback.getValue()).andThrow(new Exception()).times(1);
		EasyMock.replay(cacheCallback);

		CacheProvider cacheProvider = EasyMock.createNiceMock(CacheProvider.class);
		EasyMock.expect(cacheProvider.getCache(key)).andReturn(null).times(1);
		EasyMock.replay(cacheProvider);

		new CacheTemplate(cacheProvider).execute(cacheCallback);

		EasyMock.verify(cacheProvider, cacheCallback);
	}

	/**
	 * 获取缓存时出现异常,执行被缓存的方法并直接返回执行的结果
	 */
	@Test
	public void test2() throws Exception {

		final String key = "key";
		final String value = "value";

		CacheCallback cacheCallback = EasyMock.createMock(CacheCallback.class);
		EasyMock.expect(cacheCallback.getKey()).andReturn(key).times(1);
		EasyMock.expect(cacheCallback.getValue()).andReturn(value).times(1);
		EasyMock.replay(cacheCallback);

		CacheProvider cacheProvider = EasyMock.createNiceMock(CacheProvider.class);
		EasyMock.expect(cacheProvider.getCache(key)).andThrow(new Exception()).times(1);
		EasyMock.replay(cacheProvider);

		String returnValue = (String) new CacheTemplate(cacheProvider).execute(cacheCallback);

		EasyMock.verify(cacheProvider, cacheCallback);
		Assert.assertEquals(value, returnValue);
	}

	/**
	 * 获取缓存时出现异常并且执行被缓存的方法也出现异常
	 */
	@Test(expected = Exception.class)
	public void test21() throws Exception {

		final String key = "key";

		CacheCallback cacheCallback = EasyMock.createMock(CacheCallback.class);
		EasyMock.expect(cacheCallback.getKey()).andReturn(key).times(1);
		EasyMock.expect(cacheCallback.getValue()).andThrow(new Exception()).times(1);
		EasyMock.replay(cacheCallback);

		CacheProvider cacheProvider = EasyMock.createNiceMock(CacheProvider.class);
		EasyMock.expect(cacheProvider.getCache(key)).andThrow(new Exception()).times(1);
		EasyMock.replay(cacheProvider);

		new CacheTemplate(cacheProvider).execute(cacheCallback);

		EasyMock.verify(cacheProvider, cacheCallback);
	}

	/**
	 * 缓存有效,返回缓存的值
	 */
	@Test
	public void test3() throws Exception {

		final String key = "key";

		String cachedValue = "cachedValue";

		int expiry = 10;

		CacheCallback cacheCallback = EasyMock.createMock(CacheCallback.class);
		EasyMock.expect(cacheCallback.getKey()).andReturn(key).times(1);
		EasyMock.replay(cacheCallback);

		CacheProvider cacheProvider = EasyMock.createMock(CacheProvider.class);
		EasyMock.expect(cacheProvider.getCache(key)).andReturn(new CacheValue(cachedValue, DateUtils.addSeconds(new Date(), 99999))).times(1);
		EasyMock.replay(cacheProvider);

		String returnValue = (String) new CacheTemplate(cacheProvider).execute(cacheCallback, expiry);

		EasyMock.verify(cacheProvider, cacheCallback);
		Assert.assertEquals(cachedValue, returnValue);

	}

	/**
	 * 缓存过期,执行被缓存的方法,返回执行的结果并更新缓存
	 */
	@Test
	public void test4() throws Exception {

		final String key = "key";
		final String newValue = "newValue";
		String cachedValue = "cachedValue";

		CacheCallback cacheCallback = EasyMock.createMock(CacheCallback.class);
		EasyMock.expect(cacheCallback.getKey()).andReturn(key).times(1);
		EasyMock.expect(cacheCallback.getValue()).andReturn(newValue).times(1);
		EasyMock.replay(cacheCallback);

		CacheProvider cacheProvider = EasyMock.createMock(CacheProvider.class);

		EasyMock.expect(cacheProvider.getCache(key)).andReturn(new CacheValue(cachedValue, DateUtils.addSeconds(new Date(), -60))).times(1);

		cacheProvider.updateCache(EasyMock.eq(key), CacheValueEquals.doEquals(newValue, new Date()));

		// cacheProvider.updateCache(EasyMock.eq(key),
		// EasyMock.anyObject(CacheValue.class));

		EasyMock.expectLastCall().times(1);

		EasyMock.replay(cacheProvider);

		String returnValue = (String) new CacheTemplate(cacheProvider).execute(cacheCallback, 10);

		/**
		 * 由于CacheProvider.setCache()方法是异步调用的,为了确保调用到此方法,此处挂起线程一段时间
		 */
		Thread.sleep(3000);

		EasyMock.verify(cacheProvider, cacheCallback);
		Assert.assertEquals(newValue, returnValue);
	}

	/**
	 * 缓存过期,但是执行被缓存的方法失败,返回过期的缓存值
	 */
	@Test
	public void test5() throws Exception {

		final String key = "key";

		String expiredCachedValue = "cachedValue";

		CacheCallback cacheCallback = EasyMock.createMock(CacheCallback.class);
		EasyMock.expect(cacheCallback.getKey()).andReturn(key).times(1);
		EasyMock.expect(cacheCallback.getValue()).andThrow(new Exception("TEST-5")).times(1);
		EasyMock.replay(cacheCallback);

		CacheProvider cacheProvider = EasyMock.createMock(CacheProvider.class);

		EasyMock.expect(cacheProvider.getCache(key)).andReturn(new CacheValue(expiredCachedValue, DateUtils.addSeconds(new Date(), -60))).times(1);

		EasyMock.replay(cacheProvider);

		String returnValue = (String) new CacheTemplate(cacheProvider).execute(cacheCallback, 10);

		EasyMock.verify(cacheProvider, cacheCallback);
		Assert.assertEquals(expiredCachedValue, returnValue);
	}

	/**
	 * 缓存过期,但是执行被缓存的方法失败,抛出异常
	 */
	@Test(expected = Exception.class)
	public void test55() throws Exception {

		final String key = "key";

		String expiredCachedValue = "cachedValue";

		CacheCallback cacheCallback = EasyMock.createMock(CacheCallback.class);
		EasyMock.expect(cacheCallback.getKey()).andReturn(key).times(1);
		EasyMock.expect(cacheCallback.getValue()).andThrow(new Exception()).times(1);
		EasyMock.replay(cacheCallback);

		CacheProvider cacheProvider = EasyMock.createMock(CacheProvider.class);

		EasyMock.expect(cacheProvider.getCache(key)).andReturn(new CacheValue(expiredCachedValue, DateUtils.addSeconds(new Date(), -60))).times(1);

		EasyMock.replay(cacheProvider);

		new CacheTemplate(cacheProvider, false).execute(cacheCallback, 10);

		EasyMock.verify(cacheProvider, cacheCallback);
	}

	/**
	 * 使用全局过期设置
	 */
	@Test
	public void test6() throws Exception {

		final String key = "key";
		final String newValue = "newValue";
		String cachedValue = "cachedValue";

		CacheCallback cacheCallback = EasyMock.createMock(CacheCallback.class);
		EasyMock.expect(cacheCallback.getKey()).andReturn(key).times(1);
		EasyMock.expect(cacheCallback.getValue()).andReturn(newValue).times(1);
		EasyMock.replay(cacheCallback);

		CacheProvider cacheProvider = EasyMock.createMock(CacheProvider.class);

		EasyMock.expect(cacheProvider.getCache(key)).andReturn(new CacheValue(cachedValue, DateUtils.addSeconds(new Date(), -60))).times(1);

		cacheProvider.updateCache(EasyMock.eq(key), CacheValueEquals.doEquals(newValue, new Date()));

		EasyMock.expectLastCall().times(1);

		EasyMock.replay(cacheProvider);

		String returnValue = (String) new CacheTemplate(cacheProvider, true, 10, CacheTemplate.initDefaultThreadPool()).execute(cacheCallback);

		/**
		 * 由于CacheProvider.setCache()方法是异步调用的,为了确保调用到此方法,此处挂起线程一段时间
		 */
		Thread.sleep(1000);

		EasyMock.verify(cacheProvider, cacheCallback);
		Assert.assertEquals(newValue, returnValue);

	}

	/**
	 * key为null
	 */
	@Test
	public void test7() throws Exception {

		String value = "value";

		CacheCallback cacheCallback = EasyMock.createMock(CacheCallback.class);
		EasyMock.expect(cacheCallback.getKey()).andReturn(null).times(1);
		EasyMock.expect(cacheCallback.getValue()).andReturn(value).times(1);
		EasyMock.replay(cacheCallback);

		CacheProvider cacheProvider = EasyMock.createMock(CacheProvider.class);
		EasyMock.replay(cacheProvider);

		String returnValue = (String) new CacheTemplate(cacheProvider).execute(cacheCallback);

		EasyMock.verify(cacheProvider, cacheCallback);
		Assert.assertEquals(value, returnValue);

	}

	@Test
	public void test8() {

		String key = "key";

		CacheProvider cacheProvider = EasyMock.createMock(CacheProvider.class);
		cacheProvider.deleteCache(key);
		EasyMock.expectLastCall().times(1);
		EasyMock.replay(cacheProvider);

		new CacheTemplate(cacheProvider).deleteCache(key);

		EasyMock.verify(cacheProvider);
	}

	@Test
	public void test9() throws Exception {

		String key = "key";
		String cachedValue = "cachedValue";
		String newValue = "newValue";

		CacheCallback cacheCallback = EasyMock.createMock(CacheCallback.class);
		EasyMock.expect(cacheCallback.getKey()).andReturn(key).times(1);
		EasyMock.expect(cacheCallback.getValue()).andReturn(newValue).times(1);
		EasyMock.replay(cacheCallback);

		CacheProvider cacheProvider = EasyMock.createNiceMock(CacheProvider.class);
		CacheValue newCacheValue = new CacheValue(cachedValue);

		Field field = CacheValue.class.getDeclaredField("createDate");
		field.setAccessible(true);
		field.set(newCacheValue, null);

		EasyMock.expect(cacheProvider.getCache(key)).andReturn(newCacheValue).times(1);
		cacheProvider.updateCache(EasyMock.eq(key), CacheValueEquals.doEquals(newValue, new Date()));
		EasyMock.expectLastCall().times(1);
		EasyMock.replay(cacheProvider);

		String returnValue = (String) new CacheTemplate(cacheProvider).execute(cacheCallback, Integer.MAX_VALUE);

		/**
		 * 由于CacheProvider.addCache()方法是异步调用的,为了确保调用到此方法,此处挂起线程一段时间
		 */
		Thread.sleep(1000);

		EasyMock.verify(cacheProvider, cacheCallback);
		Assert.assertEquals(newValue, returnValue);
	}

}
