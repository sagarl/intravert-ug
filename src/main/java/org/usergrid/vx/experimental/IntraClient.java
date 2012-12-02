package org.usergrid.vx.experimental;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

public class IntraClient implements Handler<HttpClientResponse> {
	private static Logger logger = LoggerFactory.getLogger(IntraClient.class);
	private static Vertx vertx;
	private HttpClient httpClient;
	
	ArrayBlockingQueue<IntraRes> q = new ArrayBlockingQueue<IntraRes>(1);

	public IntraClient() {
		vertx = Vertx.newVertx();
		this.httpClient = vertx.createHttpClient().setHost("localhost")
				.setPort(8080).setMaxPoolSize(1).setKeepAlive(true);
	}

	public IntraRes sendBlocking(IntraReq i) throws Exception {
		HttpClientRequest req = httpClient.request("POST", "/:appid/intrareq", this);
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		XMLEncoder e = new XMLEncoder(bo);
    	e.writeObject(i);
    	e.close();
    	System.out.println( new String(bo.toByteArray()));
    	/*
    	req.putHeader("content-length", value.length());
    	req.write(value);
    	
    	req.exceptionHandler(new Handler<Exception>(){
            public void handle(Exception arg0){
			
            }
    	});
    	req.end();
    	return q.poll(10, TimeUnit.SECONDS);
    	*/
    	return null;
	}
	
	@Override
	public void handle(HttpClientResponse arg0) {
		IntraRes ir = new IntraRes();
		q.add(ir);

	}

	public static void main(String[] args) throws Exception {
		IntraClient i = new IntraClient();
		IntraReq req = new IntraReq();
		req.add( IntraOp.setKeyspaceOp("myks") );
		req.add( IntraOp.setColumnFamilyOp("mycf") );
		req.add( IntraOp.setAutotimestampOp() );
		req.add( IntraOp.setOp("bob",  new Object [] { 4, "stuff" }, 10) );
		req.add( IntraOp.getOp("bob", new Object [] { 4, "stuff" }) );
		req.add( IntraOp.setKeyspaceOp("otherks") );
		req.add( IntraOp.setColumnFamilyOp("othercf") );
		req.add( IntraOp.getOp( IntraOp.getResRefOp(-3, IntraOp.VALUE),  "wantedcolumn") );
		i.sendBlocking(req);
	}
}
