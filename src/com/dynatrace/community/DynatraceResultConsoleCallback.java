package com.dynatrace.community;

import java.io.PrintStream;

import com.dynatrace.diagnostics.core.realtime.export.Btexport;
import com.dynatrace.diagnostics.core.realtime.export.Btexport.BusinessTransaction;
import com.google.protobuf.LazyStringList;

public class DynatraceResultConsoleCallback implements DynatraceResultCallback {
	
	public void flushResults(PrintStream stream) {
		// nothing to write as we write everything out as it comes in!
	}
	
	@Override
	public void businessTransactionResult(BusinessTransaction transaction) {
		StringBuilder sb = new StringBuilder();
		sb.append(transaction.getName());
		sb.append("\n");
		
		// iterate through all occurences and print out all measures
		LazyStringList measureNames = transaction.getMeasureNamesList();
    	for(Btexport.BtOccurrence occ : transaction.getOccurrencesList()) {
    		
    		sb.append("-- ;PPID=");sb.append(occ.getPurePathId());
    		sb.append(";TS=");sb.append(occ.getStartTime());
    		
    		// all BT Splittings
    		sb.append(";Splittings=");
    		for(int dim = 0; dim <occ.getDimensionsCount();dim++) {
    			if(dim > 0) sb.append(";");
    			sb.append(occ.getDimensions(0));
    		}
    		
    		// core Measures
    		sb.append(";FAIL=");sb.append(occ.getFailed());
    		sb.append(";RESPT=");sb.append(occ.getResponseTime());
    		sb.append(";EXECT=");sb.append(occ.getExecTime());
    		sb.append(";DUR=");sb.append(occ.getDuration());
    		sb.append(";CPU=");sb.append(occ.getCpuTime());
    		sb.append(";SYNC=");sb.append(occ.getSyncTime());
    		sb.append(";WAIT=");sb.append(occ.getWaitTime());
    		sb.append(";SUSP=");sb.append(occ.getSuspensionTime());
    		

    		// custom result measures
    		for(int value=0;value<measureNames.size();value++) {
    			sb.append(";");sb.append(measureNames.get(value));
    			sb.append("=");sb.append(occ.getValues(value));
    		}
    		
    		sb.append("\n");
    	}
		System.out.println(sb.toString());
	}

}
