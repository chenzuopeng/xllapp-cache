package org.xllapp.cache;

import java.io.Serializable;

/**
* 
* 实现此接口提供缓存的key和被缓存的value的具体生产逻辑.
*
* @author dylan.chen Sep 7, 2013
* 
*/
public interface CacheCallback{
	
	public String getKey();
	    	
	public Serializable getValue() throws Exception;
	
}