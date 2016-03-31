package com.dynatrace.community;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;

import com.dynatrace.diagnostics.core.realtime.export.Btexport;
import com.dynatrace.diagnostics.core.realtime.export.Btexport.BusinessTransaction;
import com.google.protobuf.LazyStringList;

public class DynatraceResultAggregatorCallback implements DynatraceResultCallback {

	private int totalBTCount = 0;
	private HashMap<BusinessTransactionEntryIdentifier, BTEntryAggregatedMeasures> aggregatedBTMeasures = new HashMap<DynatraceResultAggregatorCallback.BusinessTransactionEntryIdentifier, DynatraceResultAggregatorCallback.BTEntryAggregatedMeasures>();
	

	public void flushResults(PrintStream stream) {
	    System.out.println("Here are the aggregated results");
	    
	    try {
			writeStatusToStream(stream);
		} catch (IOException e) {
		}
	}
	
	@Override
	public void businessTransactionResult(BusinessTransaction transaction) {
		BusinessTransactionEntryIdentifier btLookupIdentifier = new BusinessTransactionEntryIdentifier();
		btLookupIdentifier.name = transaction.getName();
		btLookupIdentifier.app = transaction.getApplication();
		btLookupIdentifier.splitting = "";
		BTEntryAggregatedMeasures measuresForIdentifier = null;
		
		// iterate through all occurences 
		LazyStringList measureNames = transaction.getMeasureNamesList();
    	for(Btexport.BtOccurrence occ : transaction.getOccurrencesList()) {
    		totalBTCount++;
    		
    		// First we need to complete our btIdentifier for our HashMap Entry
    		String newSplitting = "";
    		for(int dim = 0; dim < occ.getDimensionsCount();dim++) {
    			if(dim > 0) newSplitting += ";";
    			newSplitting = occ.getDimensions(dim);
    		}

    		// we assume we have a lot of the same splittings - therefore we only lookup the splitting if it changed from the previous
    		if((newSplitting.compareToIgnoreCase(btLookupIdentifier.splitting) != 0) || (measuresForIdentifier == null)) {
    			btLookupIdentifier.splitting = newSplitting;
	    		// lets see if this entry is already in the HashMap
	    		measuresForIdentifier = aggregatedBTMeasures.get(btLookupIdentifier);
	    		if(measuresForIdentifier == null) {
	    			BusinessTransactionEntryIdentifier btIdentifier = new BusinessTransactionEntryIdentifier(btLookupIdentifier);
	    			measuresForIdentifier = new BTEntryAggregatedMeasures();
	    			aggregatedBTMeasures.put(btIdentifier, measuresForIdentifier);
	    		}
    		}
    		
    		// now lets update the measures
    		measuresForIdentifier.count++;
    		if(occ.getFailed()) measuresForIdentifier.failedCount++;
    		    		
    		// core Measures
    		measuresForIdentifier.execTime.addValue(occ.getExecTime());
    		measuresForIdentifier.respTime.addValue(occ.getResponseTime());
    		measuresForIdentifier.duration.addValue(occ.getDuration());
    		measuresForIdentifier.cpuTime.addValue(occ.getCpuTime());
    		measuresForIdentifier.syncTime.addValue(occ.getSyncTime());
    		measuresForIdentifier.waitTime.addValue(occ.getWaitTime());
    		measuresForIdentifier.suspTime.addValue(occ.getSuspensionTime());
    		

    		// custom result measures
    		for(int value=0;value<measureNames.size();value++) {
    			String measureName = measureNames.get(value);
    			BTEntryAggregatedMeasure customAggregatedMeasure = measuresForIdentifier.customMeasures.get(measureName);
    			if(customAggregatedMeasure == null) {
    				customAggregatedMeasure = new BTEntryAggregatedMeasure(measureName);
    				measuresForIdentifier.customMeasures.put(measureName, customAggregatedMeasure);
    			}
    			customAggregatedMeasure.addValue(occ.getValues(value));
    		}    		
    	}
	}
	
	public void writeStatusToStream(java.io.OutputStream oStream) throws IOException {
		BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(oStream));
		
		bWriter.write(String.format("Total Number of Analyzed Business Transaction Feed Entries: %d", totalBTCount));
		bWriter.write("\n========================================================================\n");
		bWriter.flush();
		
		Object[] btIdentifierArray = aggregatedBTMeasures.keySet().toArray();
		Object[] btMeasuresArray = aggregatedBTMeasures.values().toArray();
		
		for(int i=0;i<btMeasuresArray.length;i++) {
			BusinessTransactionEntryIdentifier btIdentifier = (BusinessTransactionEntryIdentifier)btIdentifierArray[i];
			BTEntryAggregatedMeasures btMeasures = (BTEntryAggregatedMeasures)btMeasuresArray[i];
			
			bWriter.write(btIdentifier.toString());bWriter.write("\n");
			bWriter.write(btMeasures.toString());bWriter.write("\n");
		}
		bWriter.flush();
	}
	
	public class BusinessTransactionEntryIdentifier
	{
		public String name;
		public String app;
		public String splitting;

		public BusinessTransactionEntryIdentifier()	{
			
		}
		
		public BusinessTransactionEntryIdentifier(BusinessTransactionEntryIdentifier clone) {
			this.name = clone.name;
			this.app = clone.app;
			this.splitting = clone.splitting;
		}
		
		@Override
		public String toString() {
			return String.format("%s;%s;%s", name, app, splitting);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof BusinessTransactionEntryIdentifier) {
				BusinessTransactionEntryIdentifier compareObj = (BusinessTransactionEntryIdentifier)obj;
				return (compareObj.name.compareToIgnoreCase(this.name) == 0) && (compareObj.app.compareToIgnoreCase(this.app) == 0) && (compareObj.splitting.compareToIgnoreCase(this.splitting) == 0);
			}
			
			return super.equals(obj);
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
	}

	public class BTEntryAggregatedMeasures
	{
		public int count = 0, failedCount = 0;
		public BTEntryAggregatedMeasure respTime = new BTEntryAggregatedMeasure("Response Time");
		public BTEntryAggregatedMeasure execTime = new BTEntryAggregatedMeasure("Execution Time");
		public BTEntryAggregatedMeasure duration = new BTEntryAggregatedMeasure("Duration");
		public BTEntryAggregatedMeasure cpuTime = new BTEntryAggregatedMeasure("CPU Time");
		public BTEntryAggregatedMeasure waitTime = new BTEntryAggregatedMeasure("Wait Time");
		public BTEntryAggregatedMeasure syncTime = new BTEntryAggregatedMeasure("Sync Time");
		public BTEntryAggregatedMeasure suspTime = new BTEntryAggregatedMeasure("Susp Time");
		
		public HashMap<String, BTEntryAggregatedMeasure> customMeasures = new HashMap<String, DynatraceResultAggregatorCallback.BTEntryAggregatedMeasure>();
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(respTime.toString());sb.append("\n");
			sb.append(execTime.toString());sb.append("\n");
			sb.append(duration.toString());sb.append("\n");
			sb.append(cpuTime.toString());sb.append("\n");
			sb.append(waitTime.toString());sb.append("\n");
			sb.append(syncTime.toString());sb.append("\n");
			sb.append(suspTime.toString());sb.append("\n");
			
			for(BTEntryAggregatedMeasure customMeasure : customMeasures.values()) {
				sb.append(customMeasure);sb.append("\n");
			}
			
			return sb.toString();
		}
	}
	
	public class BTEntryAggregatedMeasure
	{
		public String measureName;
		public int count;
		public double min, max, avg;

		public BTEntryAggregatedMeasure(String name) {
			this.measureName = name;
			this.count = 0;
			this.min = this.max = this.avg = 0.0;
		}
		
		public void addValue(double value) {
			avg = (avg*count + value);
			count++;
			if(avg != 0) avg = avg / count;
			if(value < min || (count == 1)) min = value;
			if(value > max || (count == 1)) max = value;
		}
		
		@Override
		public String toString() {
			return String.format("%s: CNT=%d, MIN=%.2f, AVG=%.2f, MAX=%.2f", measureName, count, min, avg, max);
		}
	}
}