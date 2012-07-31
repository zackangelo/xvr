package com.avian.xvr.resource.jakarta;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

/**
 * HTTP Client pool class. Spawns a pool of HTTP requestor threads and
 * distributes HTTP requests among them. Calls to queueRequestAndBlock will
 * block until the data is available for reading.
 * 
 * @author zangelo
 *
 */
public class HttpClientPool {
	/**
	 * This is a wrapper class we use to communicate with and block against the queue. If an error occurs in one
	 * of the threads we also use this class as a vessel for conveying that exception back to the parent thread
	 * so that it may be handled accordingly.
	 * 
	 * @author zangelo
	 *
	 */
	static class HttpRequestQueueEntry { 
		public HttpRequestQueueEntry() { 
			monitor = new Object();
			e = null;
		}
		
		String url;
		Object monitor;
		Exception e;
		
		byte[] responseData;
	}
	
	static class HttpRequestorThread extends Thread { 
		private BlockingQueue<HttpRequestQueueEntry> q;
		private boolean running;
		private static int threadIndex = 0;
		private Logger logger;
		private HttpClient client;
		
		public HttpRequestorThread(BlockingQueue<HttpRequestQueueEntry> q,HttpClient client) { 
			super("HttpRequestor-"+(threadIndex++));
			this.q = q;
			this.client = client;
			logger = Logger.getLogger(getClass());

			running = true;
		}
		
		public byte[] fetchData(String url) throws HttpException, IOException { 
			GetMethod get = new GetMethod(url);
			get.setFollowRedirects(true);
			
			client.executeMethod(get);
			return get.getResponseBody();
		}
		
		public synchronized void destroy() { 
			this.running = false;
			this.interrupt();
		}
		
		public void run() { 
			logger.debug("Hello.");
			while(running) {
				HttpRequestQueueEntry entry;
				
				try {
					 entry = q.take(); /* take() blocks if empty */
				} catch (InterruptedException e) { /* ignore */
					e.printStackTrace(); continue;
				}
				
				logger.debug("Fetching " + entry.url + "...");
				
				try {
					entry.responseData = fetchData(entry.url);
				} catch (HttpException e) {
					entry.e = e;
				} catch (IOException e) {
					entry.e = e;
				}
				
				logger.debug("Received " + entry.responseData.length + " bytes from " + entry.url);
				
				synchronized(entry.monitor) {
					entry.monitor.notifyAll();
				}
			}
		}
	}
	
	public HttpClientPool() { 
		requestQueue = new LinkedBlockingQueue<HttpRequestQueueEntry>();
		
		/**
		 * We'll use the internal threading HttpClient threading mechanisms for now, but we might 
		 * try using an individual HttpClient for each thread in the future.
		 */
		sharedClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		logger = Logger.getLogger(getClass());
		
		buildPool(DEFAULT_THREAD_COUNT, requestQueue,sharedClient);
	}
	
	BlockingQueue<HttpRequestQueueEntry> requestQueue;
	HttpClient sharedClient;
	Logger logger;
	HttpRequestorThread[] pool;
	public static final int DEFAULT_THREAD_COUNT = 5;
	
	private void buildPool(int numThreads,BlockingQueue<HttpRequestQueueEntry> q,HttpClient client) { 
		logger.debug("Initializing HTTP Requestor pool...");
		pool = new HttpRequestorThread[numThreads];
		for(int i=0;i<numThreads;i++) { 
			pool[i] = new HttpRequestorThread(q,client);
			pool[i].start();
		}
	}
	
	private void destroyPool() { 
		for(HttpRequestorThread t : pool) { 
			t.destroy();
		}
	}
	
	private void queueRequest(HttpRequestQueueEntry entry) {
		requestQueue.offer(entry);
	}
	
	/**
	 * 
	 * @param url URL to submit request to
	 * @return HTTP Response body data
	 */
	public byte[] queueRequestAndBlock(String url) throws HttpException,IOException  { 
		HttpRequestQueueEntry entry = new HttpRequestQueueEntry();
		
		entry.url = url;
		
		queueRequest(entry);
		
		synchronized(entry.monitor) {
			try {
				entry.monitor.wait();
			} catch (InterruptedException e) {
			} /* implement request timeout (either by exception in jakarta HttpClient or here */ 
		}
		
		if(entry.e != null) { /* did an exception occur in the thread? */
			/* a bit of a hack here... we know that only two exceptions can't be thrown, fix this in the future */
			if(entry.e instanceof HttpException) { 
				throw (HttpException)entry.e;
			} else if (entry.e instanceof IOException) { 
				throw (IOException) entry.e;
			} else {
				logger.warn("Unknown error occurred while trying to fetch " + url);
			}
		}
		
		return entry.responseData;
	}
}
