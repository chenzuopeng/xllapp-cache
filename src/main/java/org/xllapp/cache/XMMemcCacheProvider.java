package org.xllapp.cache;

import java.io.IOException;
import java.util.Arrays;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于memcached的实现.使用的Client是Memcached-Java-Client.
 * 
 * @author dylan.chen Sep 11, 2013
 * 
 */
public class XMMemcCacheProvider implements CacheProvider {

	public static class Config {
		/**
		 * 服务地址
		 */
		private String servers;

		/**
		 * 服务器负载量
		 */
		private int[] weights;

		/**
		 * 操作超时,单位:毫秒
		 */
		private long opTimeout = MemcachedClient.DEFAULT_OP_TIMEOUT;

		/**
		 * 连接超时,单位:毫秒
		 */
		private int connectTimeout = MemcachedClient.DEFAULT_CONNECT_TIMEOUT;

		public String getServers() {
			return this.servers;
		}

		public void setServers(String servers) {
			this.servers = servers;
		}

		public int[] getWeights() {
			return this.weights;
		}

		public void setWeights(int... weights) {
			this.weights = weights;
		}

		public long getOpTimeout() {
			return this.opTimeout;
		}

		public void setOpTimeout(long opTimeout) {
			this.opTimeout = opTimeout;
		}

		public int getConnectTimeout() {
			return this.connectTimeout;
		}

		public void setConnectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}

	private final static Logger logger = LoggerFactory.getLogger(XMMemcCacheProvider.class);

	private MemcachedClient memcachedClient;

	private int expiry = Integer.MAX_VALUE;

	public XMMemcCacheProvider(Config config) {

		logger.debug("loaded Config[{}]", config);

		String[] servers = config.getServers().split(",");
		int[] weights = config.getWeights();
		if (ArrayUtils.isEmpty(weights)) {
			weights = new int[servers.length];
			Arrays.fill(weights, 1);
		}
		String serverList = formatServers(servers);

		if (logger.isDebugEnabled()) {
			logger.debug("servers:{}", serverList);
			logger.debug("weights:{}", Arrays.toString(weights));
		}

		MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(serverList), weights);
		builder.setConnectTimeout(config.getConnectTimeout());
		builder.setOpTimeout(config.getOpTimeout());
		try {
			this.memcachedClient = builder.build();
		} catch (IOException e) {
			throw new RuntimeException("failure to build MemcachedClient", e);
		}
	}

	private String formatServers(String[] input) {
		String[] array = new String[input.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = input[i].trim();
		}
		return StringUtils.join(array, " ");
	}

	@Override
	public CacheValue getCache(String key) throws Exception {
		CacheValue value = (CacheValue) this.memcachedClient.get(key);
		logger.debug("getted cache[{}] by key[{}]", value, key);
		return value;
	}

	@Override
	public void addCache(String key, CacheValue value) {
		setCache(key, value);
		logger.debug("added key[{}] cache[{}]", key, value);
	}

	@Override
	public void updateCache(String key, CacheValue value) {
		setCache(key, value);
		logger.debug("updated key[{}] cache[{}]", key, value);
	}

	private void setCache(String key, CacheValue value) {
		try {
			if(!value.isValid()){
				logger.warn("invalid key[{}] cache[{}]",key, value);
				return;
			}
			this.memcachedClient.set(key, this.expiry, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void deleteCache(String key) {
		try {
			this.memcachedClient.delete(key);
			logger.debug("deleted key[{}] cache", key);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
