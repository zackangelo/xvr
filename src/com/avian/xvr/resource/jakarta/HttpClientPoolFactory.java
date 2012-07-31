package com.avian.xvr.resource.jakarta;

public class HttpClientPoolFactory {
	private HttpClientPool poolSingleton;
	private static HttpClientPoolFactory factorySingleton;
	
	private HttpClientPoolFactory() { 
		poolSingleton = null;
	}
	
	public static HttpClientPoolFactory getInstance() { 
		if(factorySingleton == null) { 
			factorySingleton = new HttpClientPoolFactory();
		}
		
		return factorySingleton;
	}
	/**
	 * Returns the shared HttpClientPool instance.
	 * 
	 * We may change our pool allocation strategy in the future to 
	 * support different pools for different document types.
	 *
	 */
	public HttpClientPool createPool() { 
		if(poolSingleton == null) { 
			poolSingleton = new HttpClientPool();
		}
		
		return poolSingleton;
	}
}
