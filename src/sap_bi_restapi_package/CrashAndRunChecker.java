package sap_bi_restapi_package;

import java.io.PrintWriter;
import java.time.LocalDateTime;

public class CrashAndRunChecker {
	
	private String outputfilename;
	private PrintWriter mywriter;
	
	//constructors can not return null
	//better to but file open stuff in another method
	public CrashAndRunChecker() {
		LocalDateTime nowtime = LocalDateTime.now();
		outputfilename = String.format("sap_bi_restapi_debug_output_%d_%d_%d_%d_%d_%d_%d.txt", 
										nowtime.getYear(), nowtime.getMonth().getValue(), nowtime.getDayOfMonth(), 
										nowtime.getHour(), nowtime.getMinute(), nowtime.getSecond(), nowtime.getNano());
	}
	
	public boolean Open() {
		try {
			mywriter = new PrintWriter(outputfilename);
			if (mywriter != null) {
				this.println(outputfilename);
				return true;
			}
			else
				return false; //maybe access privileges can not create folder in o.s. level
		}
		catch (Throwable tt) {
			return false;
		}
	}
	
	public void Close() {
		mywriter.close();
	}
	
	public void println() {
		
		mywriter.println();
		
		System.out.println();
		
	}
	
	public void println(String str) {
		
		mywriter.println(str);
		
		System.out.println(str);
	}
	
	public void println(String titlestr, Throwable tt) {
		
		mywriter.println(titlestr);
		System.out.println(titlestr);
		
		String throwmsg = tt.getMessage();
		if (throwmsg != null) {
			mywriter.println("CrashAndRunChecker: tt.getMessage()");
			mywriter.println(throwmsg);
			System.out.println("CrashAndRunChecker: tt.getMessage()");
			System.out.println(throwmsg);
		}
		
		mywriter.println("CrashAndRunChecker: tt.printStackTrace()");
		tt.printStackTrace(mywriter);
		
		tt.printStackTrace();
		
		
		
		Throwable causethrow = tt.getCause();
		if (causethrow != null) {
			String causethrowmsg = causethrow.getMessage();
			if (causethrowmsg != null) {
				mywriter.println("CrashAndRunChecker: causethrow.getMessage()");
				mywriter.println(causethrowmsg);
				System.out.println("CrashAndRunChecker: causethrow.getMessage()");
				System.out.println(causethrowmsg);
			}
		}
		
		mywriter.println("CrashAndRunChecker: causethrow.printStackTrace()");
		causethrow.printStackTrace(mywriter);
		
		causethrow.printStackTrace();
	}

}//CrashAndRunChecker
