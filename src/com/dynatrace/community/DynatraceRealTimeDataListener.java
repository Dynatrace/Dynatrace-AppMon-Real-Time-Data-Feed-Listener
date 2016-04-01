package com.dynatrace.community;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.dynatrace.diagnostics.core.realtime.export.Btexport;
import com.sun.net.httpserver.*;

public class DynatraceRealTimeDataListener {
	
	/**
	 * usage: [-port:4001] [-url:/test] [-aggregate] [-console] 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
        
		int port = 4001;
		String url = "/test";
		boolean bAggregate = false;
		boolean bConsole = true;
		
		// parse parameters to overwrite defaults!
		for(String arg : args) {
			if(arg.startsWith("-port:")) port = Integer.valueOf(arg.substring(6));
			else if(arg.startsWith("-url:")) url = arg.substring(5);
			else if(arg.equalsIgnoreCase("-aggregate"))	bAggregate = true;
			else if(arg.equalsIgnoreCase("-console")) bConsole = true;
		}
				
		// create our listeners!
		ArrayList<DynatraceResultCallback> callbacks = new ArrayList<DynatraceResultCallback>();
		if(bAggregate) callbacks.add(new DynatraceResultAggregatorCallback());
		if(bConsole) callbacks.add(new DynatraceResultConsoleCallback(System.out));
		if(callbacks.size() == 0) callbacks.add(new DynatraceResultConsoleCallback(System.out));

		// start with the first listener
		DynatraceRealTimeDataListener listener = new DynatraceRealTimeDataListener();
		listener.startListener(port, url, callbacks.get(0));
		
		// add more if there are more
		for(int i=1;i<callbacks.size();i++) {
			listener.addResultCallback(callbacks.get(i));
		}
				
        System.out.println("Press any key to close ...!");
        String nextLine = new BufferedReader(new InputStreamReader(System.in)).readLine();
        
        for(DynatraceResultCallback callback : callbacks) {
        	callback.flushResults(System.out);
        }
        
        // stop the listener
        listener.stopListener();
	}
	

	private HttpServer httpServerListener;
	private DynaTraceHttpHandler dynatraceHttpHandler;
	
	public void startListener(int port, String urlEndpoint, DynatraceResultCallback callback) throws IOException {
		dynatraceHttpHandler = new DynaTraceHttpHandler();
		dynatraceHttpHandler.addResultCallback(callback);
		
		httpServerListener = HttpServer.create(new InetSocketAddress(port), 0);
		httpServerListener.createContext(urlEndpoint, dynatraceHttpHandler);
		httpServerListener.setExecutor(null);
		httpServerListener.start();	
	}
	
	public void stopListener() {
		if(httpServerListener != null) {
			httpServerListener.stop(0);
			httpServerListener = null;
		}
	}
	
	public void addResultCallback(DynatraceResultCallback callback) {
		dynatraceHttpHandler.addResultCallback(callback);
	}
	
	
	static class DynaTraceHttpHandler implements HttpHandler {
		private static List<DynatraceResultCallback> resultCallbacks = new ArrayList<DynatraceResultCallback>();
	
		public void addResultCallback(DynatraceResultCallback callback) {
			resultCallbacks.add(callback);
		}
		
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            
            // parse the current list of BTs
            Btexport.BusinessTransactions transactionList = Btexport.BusinessTransactions.parseFrom(t.getRequestBody());

            // now iterate through all BTs and call our result callbacks
            for(Btexport.BusinessTransaction transaction : transactionList.getBusinessTransactionsList()) {
            	for(DynatraceResultCallback callback : resultCallbacks) {
            		callback.businessTransactionResult(transaction);
            	}
            }
           
            // send the response back
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }	
}
