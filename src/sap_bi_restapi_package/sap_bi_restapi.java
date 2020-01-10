


//*****************************************************************************************************************************************
//(1)  WARNING!!!!!
//     If you use eclipse IDE to Export... ==> Java ==> Runnable JAR file
//     Then it will not include the *.java source code files into the .jar file so future programmers/developers/software engineers can not see the java source code.
//
//(2)  You can run the file "NO_ECLIPSE_IDE_WINDOWS_JAR_BUILDER.bat" if you want to build the executable .jar file outside of eclipse-IDE,
//     or you can run the following commands from a windows command prompt:
//
//     (a) The swing app consists of the following .java and the following .jar files:
//         CrashAndRunChecker.java
//         DocumentRefresh_Parameter.java
//         DocumentRefresh_ScrollPane.java
//         DocumentRefresh_Thread_Result.java
//         GUI_Frame.java
//         Infostore_Button_Panel.java
//         InfostoreEntry_Panel.java
//         InfostoreTree_ScrollPane.java
//         InfostoreTreeCellRenderer.java
//         InfostoreTreeNode.java
//         JSON_helper.java
//         Parameter_Date_Button.java
//         Parameter_List_Dialog.java
//         Parameter_Multiple_Text_Dialog.java
//         RestAPICaller.java
//         RestAPIResponse.java
//         sap_bi_restapi.java     (this has public static void Main)
//         SAP_BI_WebIntel_REST_helper.java
//         Search_ScrollPane.java
//         WaitingFrame.java
//         XML_helper.java
//
//         json_simple.jar
//
//     (b) If not using eclipse IDE, you can get the package compiled from windows command prompt
//        assuming all *.java and *.gif and *.bat and *.txt files are in the current directory:
//        "C:\Program Files\java\jdk1.8.0_144\bin\javac.exe" -d . -classpath json-simple.jar  *.java
//
//     (c) Make sure the animated-gif files are inside the package folder just created, otherwise during run, you will not see them:
//        copy *.gif sap_bi_restapi_package
//
//     (d) Create the executable jar file:
//        "C:\Program Files\java\jdk1.8.0_144\bin\jar.exe" cmf  sap_bi_restapi.mf  sap_bi_restapi.jar  *.java *.gif  *.pdf  *.mf  NO_ECLIPSE_IDE_WINDOWS_JAR_BUILDER.bat  json-simple.jar  README.txt sap_bi_restapi_package
//
//(3)  To check the contents of the new jar file:
//     "C:\Program Files\java\jdk1.8.0_144\bin\jar.exe" tf  sap_bi_restapi.jar
//
//(4)  To run the executable jar file you can do the below command or just double-click on it from inside windows-explorer window:
//     "C:\Program Files\java\jdk1.8.0_144\bin\java.exe" -jar sap_bi_restapi.jar
//
//(7)  If all you have is the .jar file and you do not have the source code .java files or .gif files, you can extract the contents of the jar file:
//     "C:\Program Files\java\jdk1.8.0_144\bin\jar.exe"  xvf  sap_bi_restapi.jar
//
//(8)  All java source code and documentation found in "Microsoft Visual Source Safe" at
//     $/ERPDocumentations/Java_SAP_WebIntel_RESTful_web_service_console_app
//
//(9)  If you really want to use eclipse IDE, no problem, just make a java project and in the src folder make a package called
//     sap_bi_restapi_package and then add all the .java files and .jar files and .gif files and .bat file and .mf file and this README.txt file.
//
//(10) All SAP Business Intelligence REST-api documentation can be found in the 2 .pdf files
//     sbo41_bip_rest_ws_en.pdf
//     sbo41_webi_restful_ws_en.pdf
//
//(11) As of october1, 2019 when this code was started we were using SAP Business Intelligence v4.1SP3 ..... 
//     perhaps in the future servers will be upgraded to a later version in which case the v4.1 SP3 REST-api used in this source code may change or become deprecated or stay the same or include new functionality.
//     
//*****************************************************************************************************************************************

package sap_bi_restapi_package;


import javax.swing.SwingUtilities;


public class sap_bi_restapi {
	
	
	public static void main(String[] args) throws Exception {
		
		CrashAndRunChecker crashruncheck = new CrashAndRunChecker();
		boolean crashruncheck_file_created = crashruncheck.Open();
		if (crashruncheck_file_created == false) {
			System.out.println("CrashAndRunChecker: FAILED TO CREATE OUTPUT TEXT FILE ..... WILL NOT SHOW GUI !!!!!");
			System.out.println("MAIN DONE");
			return;
		}
		
		
		//Schedule this for the event dispatch thread (edt)
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				WaitingFrame export_pdf_excel_waitframe = new WaitingFrame(crashruncheck);
				boolean gifs_found = export_pdf_excel_waitframe.Load_Animated_Gifs();
				if (gifs_found == false)
					crashruncheck.println("FAILED TO FIND GIFS ..... STILL RUNNING BUT NO ANIMATED GIFS IN WAITINGFRAME WHEN EXPORTING EXCEL OR PDF FILES !!!!!");
					
				GUI_Frame gui = new GUI_Frame(export_pdf_excel_waitframe, crashruncheck);
			}
		});
		
		crashruncheck.println("MAIN DONE");
	}//main

}//class
