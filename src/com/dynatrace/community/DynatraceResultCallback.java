package com.dynatrace.community;

import java.io.PrintStream;

import com.dynatrace.diagnostics.core.realtime.export.Btexport;

public interface DynatraceResultCallback {
	void businessTransactionResult(Btexport.BusinessTransaction transaction);
	void flushResults(PrintStream stream);
}
