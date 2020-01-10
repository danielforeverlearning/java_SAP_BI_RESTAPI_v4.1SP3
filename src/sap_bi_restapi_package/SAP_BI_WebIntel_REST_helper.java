package sap_bi_restapi_package;

import java.util.ArrayList;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import java.io.BufferedReader;
import java.io.FileReader;

public class SAP_BI_WebIntel_REST_helper {
	
	private RestAPICaller rest;
	private String TOKEN;
	private String TOKEN_WITH_QUOTES;
	
	private CrashAndRunChecker crashruncheck;
	
	private String save_username;
	private String save_password;
	private String save_protocol_host_port;
	
	public SAP_BI_WebIntel_REST_helper(CrashAndRunChecker tempcrashruncheck) {
		crashruncheck = tempcrashruncheck;
		rest = new RestAPICaller(crashruncheck);
	}
	
	public Boolean Test_And_Save_Credentials(String username, String password, String protocol_host_port) {
		
		save_username = username;
		save_password = password;
		save_protocol_host_port = protocol_host_port;
		
		TOKEN = null;
		TOKEN_WITH_QUOTES = null;
		
		Boolean token_success = logon();
		if (token_success)
			logoff();
		else {
			save_username = null;
			save_password = null;
			save_protocol_host_port = null;
		}
		
		return token_success;
	}
	
	public Boolean logon() {
		
		try {

			RestAPIResponse myresponse = rest.sendGet(save_protocol_host_port + "biprws/logon/long");
			if (myresponse.responseCode == 200)
				crashruncheck.println(myresponse.response);
			
			//xml helper
			XML_helper auth_help = new XML_helper(crashruncheck);
			auth_help.parse(myresponse.response);
			auth_help.set_attr("userName", save_username);
			auth_help.set_attr("password", save_password);
			String authxml = auth_help.transform_to_String();
			
			crashruncheck.println();
			crashruncheck.println(authxml);
			
			
			//****** Try to authenticate and get token **********
			myresponse = rest.sendPost_XML(save_protocol_host_port + "biprws/logon/long", authxml);
			if (myresponse.responseCode == 200)
			{
				crashruncheck.println(myresponse.response);
			
				//xml helper
				XML_helper token_help = new XML_helper(crashruncheck);
				token_help.parse(myresponse.response);
				String token = token_help.get_attr("logonToken");
				
				crashruncheck.println("token = " + token);
				TOKEN = token;
				TOKEN_WITH_QUOTES = "\"" + token + "\"";
				
				return true;
			}
			else {
				TOKEN = null;
				TOKEN_WITH_QUOTES = null;
				
				return false;
			}
		}
		catch (Exception ex) {
			crashruncheck.println("EXCEPTION CAUGHT IN class SAP_BI_WebIntel_REST_helper method get_token !!!!!", ex);
			
			TOKEN = null;
			TOKEN_WITH_QUOTES = null;
			
			return false;
		}
	}//logon
	
	
	public Boolean logoff() {
		
		try {

			RestAPIResponse myresponse = rest.sendPost_token(save_protocol_host_port + "biprws/logoff", TOKEN);
			if (myresponse.responseCode == 200)
			{
				crashruncheck.println(myresponse.response);
				return true;
			}
			else
				return false;
		}
		catch (Exception ex) {
			crashruncheck.println("EXCEPTION CAUGHT IN class SAP_BI_WebIntel_REST_helper method logoff !!!!!", ex);
			
			return false;
		}
	}//logoff
	
	
	public void Make_Infostore_Call() {
		try {
			RestAPIResponse myresponse = rest.sendGet_token("http://erpwtinf01:6405/" + "biprws/infostore", TOKEN, false);
			
			if (myresponse.responseCode == 200)
				crashruncheck.println(myresponse.response);
		}
		catch (Throwable tt) {
			crashruncheck.println("class SAP_BI_WebIntel_REST_helper method Make_Infostore_Call: CAUGHT THROWABLE !!!!!", tt);
		}
	}
	
	
	public void  Get_Document_Properties(String docidstr) {
		try {
			RestAPIResponse myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/raylight/v1/documents/" + docidstr + "/properties", TOKEN_WITH_QUOTES, false);

			if (myresponse.responseCode == 200)
				crashruncheck.println(myresponse.response);
		}
		catch (Throwable tt) {
			crashruncheck.println("class SAP_BI_WebIntel_REST_helper method Get_Document_Properties: CAUGHT THROWABLE !!!!!", tt);
		}
	}
	
	
	public void  Get_Document_Schedules(String docidstr, String schedule_id_str) {
		try {
			RestAPIResponse myresponse = null;
			
			if (schedule_id_str == null)
				myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/raylight/v1/documents/" + docidstr + "/schedules", TOKEN_WITH_QUOTES, false);
			else
				myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/raylight/v1/documents/" + docidstr + "/schedules/" + schedule_id_str, TOKEN_WITH_QUOTES, false);
			
			
			if (myresponse.responseCode == 200)
				crashruncheck.println(myresponse.response);
		}
		catch (Throwable tt) {
			crashruncheck.println("class SAP_BI_WebIntel_REST_helper method Get_Document_Schedules: CAUGHT THROWABLE !!!!!", tt);
		}
	}//Get_Document_Schedules
	
	
	
	public RestAPIResponse Get_Users(int page, int page_size) {
		
		String urlstr = save_protocol_host_port + "biprws/infostore/Users/children?";
		urlstr += String.format("page=%d&pageSize=%d", page, page_size);
		
		RestAPIResponse myresponse = null;
		
		try {
			myresponse = rest.sendGet_token(urlstr, TOKEN, false);
		} catch (Exception ex) {
			crashruncheck.println("Get_Users: Exception caught in restful API call !!!!!", ex);
			return null;
		}
		
		if (myresponse.responseCode == 200)
			System.out.println(myresponse.response);
		
		return myresponse;
	}//Get_Users
	
	
	public RestAPIResponse Get_User_Groups(int page, int page_size) {
		
		String urlstr = save_protocol_host_port + "biprws/infostore/User%20Groups/children?";
		urlstr += String.format("page=%d&pageSize=%d", page, page_size);
		
		RestAPIResponse myresponse = null;
		
		try {
			myresponse = rest.sendGet_token(urlstr, TOKEN, false);
		} catch (Exception ex) {
			crashruncheck.println("Get_User_Groups: Exception caught in restful API call !!!!!", ex);
			return null;
		}
		
		if (myresponse.responseCode == 200)
			System.out.println(myresponse.response);
		
		return myresponse;
	}//Get_User_Groups
	
	
	public Boolean Make_Users_Tree(String[] tempstrarray, JTree myTree, InfostoreTreeNode myNode)  {
		
		InfostoreTreeNode Users_node = new InfostoreTreeNode();
		Users_node.author_name = "";
		Users_node.author_uri  = "";
		Users_node.link        = "";
		Users_node.id          = "";
		Users_node.cuid        = "";
		Users_node.description = "";
		Users_node.name        = "Users";
		Users_node.type        = "Folder";
		
		InfostoreTreeNode User_Groups_node = new InfostoreTreeNode();
		User_Groups_node.author_name = "";
		User_Groups_node.author_uri  = "";
		User_Groups_node.link        = "";
		User_Groups_node.id          = "";
		User_Groups_node.cuid        = "";
		User_Groups_node.description = "";
		User_Groups_node.name        = "User Groups";
		User_Groups_node.type        = "Folder";
		
		DefaultTreeModel tempmodel = (DefaultTreeModel) myTree.getModel();
		tempmodel.insertNodeInto(User_Groups_node, myNode, myNode.getChildCount());
		tempmodel.insertNodeInto(Users_node, myNode, myNode.getChildCount());
		
		int page_size = 50;
		int page=1;
		Boolean done = false;
		while (done == false) {
			
			RestAPIResponse myresponse = Get_Users(page, page_size);
			if (myresponse == null)
				return false;
			else if (myresponse.responseCode != 200) {
				crashruncheck.println("class SAP_BI_WebIntel_REST_helper method Make_Users_Tree: Users - Got a bad responseCode not 200 !!!!!");
				String tempstr = String.format("responseCode=%d  page=%d  page_size=%d", myresponse.responseCode, page, page_size);
				crashruncheck.println(tempstr);
				return false;
			}
			else { //responseCode == 200
				
				XML_helper xmlhelp = new XML_helper(crashruncheck);
				xmlhelp.parse(myresponse.response);
				ArrayList<InfostoreTreeNode> mylist = new ArrayList<InfostoreTreeNode>();
				
				//mylist gets added with entries filtered by tempstrarray, entry_count is all entries in XML nothing to do with tempstrarray
				int entry_count = xmlhelp.infostore_subfolders_page_parse(false, tempstrarray, mylist);
				
				//go thru xml list of entries
				//add node to tree
				for (int ii=0; ii < mylist.size(); ii++) {
					InfostoreTreeNode new_tree_node = mylist.get(ii);
					DefaultTreeModel model = (DefaultTreeModel) myTree.getModel();
					
					model.insertNodeInto(new_tree_node, Users_node, Users_node.getChildCount());
					myTree.scrollPathToVisible(new TreePath(new_tree_node.getPath()));
				}
				
				if (entry_count < page_size)
					done = true;
				else
					page++;
			}
		}
		
		page=1;
		done = false;
		while (done == false) {
			
			RestAPIResponse myresponse = Get_User_Groups(page, page_size);
			if (myresponse == null)
				return false;
			else if (myresponse.responseCode != 200) {
				crashruncheck.println("class SAP_BI_WebIntel_REST_helper method Make_Users_Tree: User_Groups - Got a bad responseCode not 200 !!!!!");
				String tempstr = String.format("responseCode=%d  page=%d  page_size=%d", myresponse.responseCode, page, page_size);
				crashruncheck.println(tempstr);
				return false;
			}
			else { //responseCode == 200
				
				XML_helper xmlhelp = new XML_helper(crashruncheck);
				xmlhelp.parse(myresponse.response);
				ArrayList<InfostoreTreeNode> mylist = new ArrayList<InfostoreTreeNode>();
				
				//for USER GROUPS we do not care about tempstrarray, we try to get all USER GROUPS
				String[] everything_filter_array = new String[1];
				everything_filter_array[0] = "*";
				int entry_count = xmlhelp.infostore_subfolders_page_parse(false, everything_filter_array, mylist);
				
				//go thru xml list of entries
				//add node to tree
				for (int ii=0; ii < mylist.size(); ii++) {
					InfostoreTreeNode new_tree_node = mylist.get(ii);
					DefaultTreeModel model = (DefaultTreeModel) myTree.getModel();
					
					model.insertNodeInto(new_tree_node, User_Groups_node, User_Groups_node.getChildCount());
					myTree.scrollPathToVisible(new TreePath(new_tree_node.getPath()));
				}
				
				if (entry_count < page_size)
					done = true;
				else
					page++;
			}
		}
		
		return true;
	}//Make_Users_Tree
	
	
	//****************************************************************************
	//From "Root%20Folder" get all folders containing substring in tempstrarray
	//****************************************************************************
	public Boolean Root_Contains_Make_Infostore_Tree(String[] tempstrarray, JTree myTree, InfostoreTreeNode myNode)  {
		
		int page_size = 50;
		int page=1;
		Boolean done = false;
		while (done == false) {
			
			String urlstr = save_protocol_host_port + "biprws/infostore/Root%20Folder/children?";
			urlstr += String.format("page=%d&pageSize=%d", page, page_size);
			
			RestAPIResponse myresponse = null;
			try {
				myresponse = rest.sendGet_token(urlstr, TOKEN, false);
			} catch (Exception ex) {
				crashruncheck.println("Root_Contains_Make_Infostore_Tree: Exception caught in restful API call !!!!!", ex);
				return false;
			}
			if (myresponse.responseCode == 200) {
				
				XML_helper xmlhelp = new XML_helper(crashruncheck);
				xmlhelp.parse(myresponse.response);
				ArrayList<InfostoreTreeNode> mylist = new ArrayList<InfostoreTreeNode>();
				
				//mylist gets added with entries filtered by tempstrarray, entry_count is all entries in XML nothing to do with tempstrarray
				int entry_count = xmlhelp.infostore_subfolders_page_parse(true, tempstrarray, mylist);
				
				//go thru xml list of entries
				//add node to tree and recurse down
				for (int ii=0; ii < mylist.size(); ii++) {
					InfostoreTreeNode new_tree_node = mylist.get(ii);
					DefaultTreeModel model = (DefaultTreeModel) myTree.getModel();
					
					model.insertNodeInto(new_tree_node, myNode, myNode.getChildCount());
					myTree.scrollPathToVisible(new TreePath(new_tree_node.getPath()));
					
					if (new_tree_node.type.equals("Folder")) {
						Boolean tempresult = Recursive_Make_Infostore_Tree(myTree, new_tree_node, new_tree_node.id);
						if (tempresult == false)
							return false;
					}
				}
				
				
				if (entry_count < page_size)
					done = true;
				else
					page++;
			}
			else {
				crashruncheck.println("class SAP_BI_WebIntel_REST_helper method Root_Contains_Make_Infostore_Tree: Got a bad responseCode not 200 !!!!!");
				String tempstr = String.format("responseCode = %d url = %s", myresponse.responseCode, urlstr);
				crashruncheck.println(tempstr);
				return false;
			}
		}
		
		return true;
	}//Root_Contains_Make_Infostore_Tree
	
	

	//**************************************************************************************************************************************
	//Recursive method
	//
	//(example)
	//idstr == "Root%20Folder"
	//idstr == 23
	//
	//Returns: true = success
	//         false = maybe got a bad response code (not 200)
	//         false = exception caught trying to make restcall
	//****************************************************************************************************************************************
	private Boolean Recursive_Make_Infostore_Tree(JTree myTree, InfostoreTreeNode myNode, String idstr)  {
		
		int page_size = 50;
		int page=1;
		Boolean done = false;
		while (done == false) {
			
			String urlstr = String.format("%sbiprws/infostore/%s/children?page=%d&pageSize=%d", save_protocol_host_port, idstr, page, page_size);
			
			RestAPIResponse myresponse = null;
			try {
				myresponse = rest.sendGet_token(urlstr, TOKEN, false);
			} catch (Exception ex) {
				crashruncheck.println("Recursive_Make_Infostore_Tree: Exception caught in restful API call !!!!!", ex);
				return false;
			}
			if (myresponse.responseCode == 200) {
				
				XML_helper xmlhelp = new XML_helper(crashruncheck);
				xmlhelp.parse(myresponse.response);
				ArrayList<InfostoreTreeNode> mylist = new ArrayList<InfostoreTreeNode>();
				int entry_count = xmlhelp.infostore_page_parse(mylist); //mylist gets added with entry
				
				//go thru xml list of entries
				//add node to tree and recurse down
				for (int ii=0; ii < mylist.size(); ii++) {
					InfostoreTreeNode new_tree_node = mylist.get(ii);
					DefaultTreeModel model = (DefaultTreeModel) myTree.getModel();
					
					model.insertNodeInto(new_tree_node, myNode, myNode.getChildCount());
					myTree.scrollPathToVisible(new TreePath(new_tree_node.getPath()));
					
					if (new_tree_node.type.equals("Folder")) {
						Boolean tempresult = Recursive_Make_Infostore_Tree(myTree, new_tree_node, new_tree_node.id);
						if (tempresult == false)
							return false;
					}
				}
				
				
				if (entry_count < page_size)
					done = true;
				else
					page++;
			}
			else {
				crashruncheck.println("class SAP_BI_WebIntel_REST_helper method Recursive_Make_Infostore_Tree: Got a bad responseCode not 200 !!!!!");
				String tempstr = String.format("responseCode = %d url = %s", myresponse.responseCode, urlstr);
				crashruncheck.println(tempstr);
				return false;
			}
		}
		
		return true;
	}//Recursive_Make_Infostore_Tree
	
	
	public boolean DEBUG_Get_All_WebIntelligence_Documents(ArrayList<String> pathlist, ArrayList<String> docidlist, String folder_id, String path_so_far) {
		int page_size = 50;
		int page=1;
		Boolean done = false;
		while (done == false) {
			
			String urlstr = String.format("%sbiprws/infostore/%s/children?page=%d&pageSize=%d", save_protocol_host_port, folder_id, page, page_size);
			
			RestAPIResponse myresponse = null;
			try {
				myresponse = rest.sendGet_token(urlstr, TOKEN, false);
			} catch (Exception ex) {
				crashruncheck.println("DEBUG_Get_All_WebIntelligence_Documents: Exception caught in restful API call !!!!!", ex);
				return false;
			}
			if (myresponse.responseCode == 200) {
				
				XML_helper xmlhelp = new XML_helper(crashruncheck);
				xmlhelp.parse(myresponse.response);
				
				ArrayList<InfostoreTreeNode> mylist = new ArrayList<InfostoreTreeNode>();
				int entry_count = xmlhelp.infostore_page_parse(mylist); //mylist gets added with entry
				
				String debugstr = null;
				if (folder_id.equals("Root%20Folder"))
					debugstr = String.format("FOLDER: %s/  entry_count=%d  page=%d", path_so_far, entry_count, page);
				else
					debugstr = String.format("FOLDER: %s/  entry_count=%d  page=%d  folder_id=%s", path_so_far, entry_count, page, folder_id);
				crashruncheck.println(debugstr);
				
				//go thru xml list of entries
				//add node to tree and recurse down
				for (int ii=0; ii < mylist.size(); ii++) {
					InfostoreTreeNode new_tree_node = mylist.get(ii);
					
					if (new_tree_node.type.equals("Folder")) {
						Boolean tempresult = DEBUG_Get_All_WebIntelligence_Documents(pathlist, docidlist, new_tree_node.id, path_so_far + "/" + new_tree_node.name);
						if (tempresult == false)
							return false;
					}
					else if (new_tree_node.type.equals("Webi")) {
						pathlist.add(path_so_far + "/" + new_tree_node.name);
						docidlist.add(new_tree_node.id);
					}
				}
				
				if (entry_count < page_size)
					done = true;
				else
					page++;
			}
			else {
				crashruncheck.println("class SAP_BI_WebIntel_REST_helper method DEBUG_Get_All_WebIntelligence_Documents: Got a bad responseCode not 200 !!!!!");
				String tempstr = String.format("responseCode = %d url = %s", myresponse.responseCode, urlstr);
				crashruncheck.println(tempstr);
				return false;
			}
		}
		
		return true;
	}//DEBUG_Get_All_WebIntelligence_Documents
	

	
	//*****************************************************************************************
	//sbo41_webi_restful_ws_en.pdf
	//3.8.1 Getting the document refresh parameters before refreshing a document
	//3.8.2 Refreshing a document
	//*****************************************************************************************
	public XML_helper Get_Document_Refresh_Parameters_XML(String docidstr) {
		try {
			String urlstr;
			RestAPIResponse response;
			
			urlstr = String.format("%sbiprws/raylight/v1/documents/%s/parameters", save_protocol_host_port, docidstr);
			response = rest.sendGet_token(urlstr, TOKEN_WITH_QUOTES, false);
			
			if (response.responseCode == 200) {
				
				crashruncheck.println(response.response);
				
				XML_helper parsed_xml = new XML_helper(crashruncheck);
				parsed_xml.parse(response.response);
				
				return parsed_xml;
			}
			else {
				return null;
			}
		}
		catch (Throwable tt) {
			crashruncheck.println("class SAP_BI_WebIntel_REST_helper method Get_Document_Refresh_Parameters_XML: CAUGHT THROWABLE !!!!!", tt);
			return null;
		}
	}//Get_Document_Refresh_Parameters_XML
	
	
	//****************************************************************************************************************
	//sbo41_webi_restful_ws_en.pdf
	//3.8.2.4 Example 4: Specifying how LoV values are returned
	//
	//A query may be given to specify how LOV values will be returned, 
	//and/or refreshed (if the LOV allows it).
	//
	//Request:
	//PUT http://<serverName>:6405/biprws/raylight/v1/documents/{documentId}/parameters
	//
	//Header:                         Value:
	//-----------------               --------------------------------------
	//Content-Type                    application/xml or application/json
	//Accept                          application/xml or application/json
	//
	//Parameter:                      Value:
	//-----------------               --------------------------------------
	//documentId                      Mandatory. Integer. The identifier of the Web Intelligence document.
	//context                         Optional or Mandatory. Contexts are objects that are used in the query such as Reservations or Sales. Contexts can be constrained and can be single or multiple values.
	//prompt                          Optional or Mandatory. Text, numeric, dateTime
	//name                            Name of the document
	//answer type                     Text, numeric, or dateTime
	//
	//Body: (example)                                                           Description:
	//----------------------------------------------------------------------    --------------------------------------
	//<parameters>
	//   .....
	//<parameter>                                                               Optional.  Where:
	//    <id>1</id>
	//    <answer>                                                              intervalId: (type=integer, optional) specifies which values interval should be returned. An error is returned if this index is out of range (depending on values count).
	//        <info>
	//            <lov>                                                         intervalSize: (type=integer, optional) specifies how many (and which) values should be returned
	//                <query intervalId="2" intervalSize="6" refresh="true">    
	//                    <sort order="Descending" />                           refresh: (type=boolean, optional) refreshes the LOV values.  An error is returned if the LOV does not allow refreshing.
	//                    <search>pattern</search>
	//                </query>                                                  sort: (type=string, values="None" or "Ascending", default="Descending", optional)
	//            </lov>
	//        </info>                                                           search pattern: (type=string, optional) the following wildcard characters may be used in the pattern string: "?" for 0 or 1 character, and "*" for 0 or n characters.  For example, "M?Gregor" yields to the value McGregor, and "M*Gregor" to the values McGregor and MacGregor.
	//    </answer>
	//</parameter>
	//</parameters>
	//
	//
	//Response:
	//
	//Header:                          Value:
	//-------------------              -------------------------------------------------
	//Status code                      HTTP response code
	//Content-Type                     application/xml or application/json
	//Content-Length                   Length of content in the response body
	//
	//Note:
	//(1) Refreshing a Web Intelligence document with multi-columns parameters has to be done the same
	//    way as for single-column values, by giving only the cell(s) whose index matches the mappingId.
	//
	//(2) partial="true" in LOV information means that only a part of the LOV values can be returned
	//    because the LOV size is limited by server settings or the universe's query limit.
	//****************************************************************************************************************
	public RestAPIResponse  Get_LOV_Interval(String docidstr, String paramId, String intervalId) {
		try {
			String urlstr;
			RestAPIResponse response = null;
			
			urlstr = String.format("%sbiprws/raylight/v1/documents/%s/parameters", save_protocol_host_port, docidstr);
			
			String xmlbodystr = "<parameters><parameter>";
			xmlbodystr += String.format("<id>%s</id>", paramId);
			xmlbodystr += "<answer><info><lov>";
			xmlbodystr += String.format("<query intervalId=\"%s\">", intervalId);
			xmlbodystr += "<search>*</search>";
			xmlbodystr += "</query></lov></info></answer></parameter></parameters>";
			
			
			response = rest.sendPut_XML_token(urlstr, xmlbodystr, TOKEN_WITH_QUOTES);
			return response;
		}
		catch (Throwable tt) {
			crashruncheck.println("class SAP_BI_WebIntel_REST_helper method Get_LOV_Interval: CAUGHT THROWABLE !!!!!", tt);
			return null; //caught a throwable exception need to check crashruncheck text file to debug
		}
	}//Get_LOV_Interval
	
	
	//XML_helper parsedXML, ArrayList<DocumentRefresh_Parameter> param_definitions, List<String>[] list_params, Parameter_Date_Button[] date_params
	public RestAPIResponse  Do_Document_Refresh(String docidstr, String xmlbodystr)  {
		try {
			String urlstr = String.format("%sbiprws/raylight/v1/documents/%s/parameters", save_protocol_host_port, docidstr);
			
			
			crashruncheck.println(xmlbodystr);
			
			RestAPIResponse response = rest.sendPut_XML_token(urlstr, xmlbodystr, TOKEN_WITH_QUOTES);
			
			return response;
		}
		catch (Throwable tt) {
			crashruncheck.println("class SAP_BI_WebIntel_REST_helper method Do_Document_Refresh: CAUGHT THROWABLE !!!!!", tt);
			return null;
		}
	}//Do_Document_Refresh
	
	
	public RestAPIResponse Run_Report_And_Get_File(boolean want_excel, String docidstr, String outputfilename) {
		
		//*********************************************************************************************************************
		//NOTE !!!!!
		//This is not same thing as "refreshing with prompt values" or "scheduling".
		//rest-api contains rest calls for "refreshing with prompt values" and "scheduling".
		//This method just exports a document to .pdf or .xlsx assuming it has already been provided with
		//data from a previous query with given prompt values.
		//Inside the thin-client you have to click the refresh button and then enter prompt values
		//and then it querys and gets fresh data and then generates the document which you can then export to .pdf or .xlsx.
		//
		//Maybe later i can add code that does refresh and stuff.
		//sbo41_webi_restful_ws_en.pdf
		//3.8.1 Getting the document refresh parameters before refreshing a document
		//3.8.2 Refreshing a document
		//*********************************************************************************************************************
		
		//*******************************************************************************************************************
		// sbo41_webi_restful_ws_en.pdf
		//
		//3.2.5 Exporting documents
		//
		//You can export a document in two ways: the entire document, or in paginated mode. You use the two
		//following urls:
		//• Export a document (GET -s <url>/documents/{documentId}[?parameters])
		//• Export a document in paginated mode (GET -s <url>/documents/{documentId}/pages)
		//
		//The output format can be:
		//• XML
		//• PDF
		//• Excel 2003
		//• Excel 2007
		//
		//Related Topics
		//• Exporting an entire document
		//• Exporting a document in paginated mode
		//
		//3.2.5.1 Exporting an entire document
		//You can export documents in the following formats:
		//• XML
		//• PDF
		//• Excel 2003
		//• Excel 2007
		//
		//Exporting a document
		//Note:
		//If HTML output is chosen, image links will be generated by Raylight: thus, the logon token must still be
		//valid when HTML output is displayed (to be able to get images from the generated links)
		//Request:
		//GET http://<serverName>:6405/biprws/raylight/vx/documents/{documentId}[?parameters]
		//*******************************************************************************************************************
		
		try {
				String urlstr;
				RestAPIResponse report_response = null;
				
				if (want_excel) {
					urlstr = String.format("%sbiprws/raylight/v1/documents/%s", save_protocol_host_port, docidstr);
					report_response = rest.sendGet_token_file_output(urlstr,
						                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
						                                                outputfilename, TOKEN_WITH_QUOTES);
				}
				else {
					urlstr = String.format("%sbiprws/raylight/v1/documents/%s/pages", save_protocol_host_port, docidstr);
					report_response = rest.sendGet_token_file_output(urlstr, "application/pdf", outputfilename, TOKEN_WITH_QUOTES);
				}
				if (report_response.responseCode == 200) {
					String tempstr = String.format("Run_Report_And_Get_File: responseCode=%d  outputfilename=%s  urlstr=%s", report_response.responseCode, outputfilename, urlstr);
					crashruncheck.println(tempstr);
					return report_response;
				}
				else {
					String tempstr = String.format("Run_Report_And_Get_File: responseCode=%d  outputfilename=%s NOT GENERATED!!!!!  urlstr=%s", report_response.responseCode, outputfilename, urlstr);
					crashruncheck.println(tempstr);
					crashruncheck.println(report_response.response);
					return report_response;
				}
		}
		catch (Throwable tt) {
			crashruncheck.println("class SAP_BI_WebIntel_REST_helper method Run_Report_And_Get_File: CAUGHT THROWABLE !!!!!", tt);
			return null;
		}
	}//Run_Report_And_Get_File
	
	
	public RestAPIResponse Get_Relationship(String id_str, String type_str, String another_id_str, boolean use_token_with_quotes) {
		RestAPIResponse myresponse = null;
		String urlstr = null;
		if (another_id_str != null)
			urlstr = String.format("%sbiprws/infostore/%s/relationships/%s/%s", this.save_protocol_host_port, id_str, type_str, another_id_str);
		else
			urlstr = String.format("%sbiprws/infostore/%s/relationships/%s", this.save_protocol_host_port, id_str, type_str);
		
		try {
			if (use_token_with_quotes)
				myresponse = rest.sendGet_token(urlstr, this.TOKEN_WITH_QUOTES, false);
			else
				myresponse = rest.sendGet_token(urlstr, this.TOKEN, false);
		} catch (Exception ex) {
			crashruncheck.println("Get_Relationship: Exception caught in restful API call !!!!!", ex);
			return null;
		}
			
		if (myresponse.responseCode == 200)
			crashruncheck.println(myresponse.response);
		else {
			String debugstr = String.format("responseCode = %d", myresponse.responseCode);
			crashruncheck.println(debugstr);
		}
		
		return myresponse;
	}//Get_Relationship
	
	
	//*********************************************************************************************************
	//THIS ASSUMES YOU CALLED logon BEFORE SO YOU CAN GET TOKEN AND TOKEN_WITH_QUOTES
	//AND IT ASSUMED YOU WILL CALL logoff AFTER so you do not have orphaned sessions inside SAP BI !!!!!
	//its just a demo
	//*********************************************************************************************************
	public void Demo_good() throws Exception {
		
		RestAPIResponse myresponse = rest.sendGet(save_protocol_host_port + "biprws/raylight/v1/about");
		if (myresponse.responseCode == 200)
			crashruncheck.println(myresponse.response);
		
		
		myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/infostore/Root%20Folder/children?page=1", TOKEN, false);
		if (myresponse.responseCode == 200)
			crashruncheck.println(myresponse.response);
		
		
		myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/infostore/Root%20Folder/children?page=2", TOKEN, false);
		if (myresponse.responseCode == 200)
			crashruncheck.println(myresponse.response);
		
		//id=23 should still be root folder
		myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/infostore/23/children?page=1", TOKEN, false);
		if (myresponse.responseCode == 200)
			crashruncheck.println(myresponse.response);
		
		//ok try to find difference between link and author uri from entry "Development" under rootfolder
		myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/infostore/14792/children?page=1&pageSize=50", TOKEN, false);
		if (myresponse.responseCode == 200)
		{
			crashruncheck.println("ROOTFOLDER ==> Development ==> AUTHOR URI");
			crashruncheck.println("IN REALITY SINCE AUTHOR IS andy.huynh IT TAKES YOU TO HIS STUFF");
			crashruncheck.println(myresponse.response);
		}
		
		myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/infostore/27944/children?page=1&pageSize=50", TOKEN, false);
		if (myresponse.responseCode == 200)
		{
			crashruncheck.println("ROOTFOLDER ==> Development ==> link");
			crashruncheck.println(myresponse.response);
		}
		
		//ok what happens if you try to do it not on a "Folder" but a "Webi" ..... 465810
		myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/infostore/465810/children?page=1&pageSize=50", TOKEN, false);
		if (myresponse.responseCode == 200)
		{
			crashruncheck.println("NOT A Folder but a Webi test ..... 465810");
			crashruncheck.println(myresponse.response);
		}
		
	}
	
	
	
	//*********************************************************************************************************
	//THIS ASSUMES YOU CALLED logon BEFORE SO YOU CAN GET TOKEN AND TOKEN_WITH_QUOTES
	//AND IT ASSUMED YOU WILL CALL logoff AFTER so you do not have orphaned sessions inside SAP BI !!!!!
	//its just a demo
	//*********************************************************************************************************
	public void Demo_BAD_API() throws Exception {
		
		//String gittest = rest.sendGet_https("https://github.com");
		//crashruncheck.println(gittest);
		
		RestAPIResponse myresponse = rest.sendGet(save_protocol_host_port + "biprws/raylight/v1/about");
		if (myresponse.responseCode == 200)
			crashruncheck.println(myresponse.response);
		
		
		//****************************************************************************
		//sbo41_bip_rest_ws_en.pdf
		//
		//Business Intelligence Platform RESTful Web Service
		//
		//The Business Intelligence platform RESTful web service SDK lets you access the BI platform using the
		//HTTP protocol. You can use this SDK to log on to the BI platform, navigate the BI platform repository,
		//access resources, and perform basic resource scheduling. You can access this SDK by writing
		//applications that use any programming language that supports the HTTP protocol, or by using any tool
		//that supports making HTTP requests. Both XML and JSON (JavaScript Object Notation) request and
		//response formats are supported.
		//
		//Use the RESTful web services SDK under the following conditions:
		//	• You want to access BI platform repository objects or perform basic scheduling.
		//	• You want to use a programming language that is not supported by other BI platform SDKs.
		//	• You do not want to download and install BI platform libraries as a part of your application.
		//	If you want to programmatically access the advanced functionality of the BI platform, including server
		//	administration, security configuration, and modifying the repository, use one of the BI platform SDKs
		//	that support these features. For example, use the SAP BusinessObjects Business Intelligence platform
		//	Java SDK, the SAP BusinessObjects Business Intelligence platform .NET SDK, or the SAP
		//	BusinessObjects Business Intelligence platform Web Services SDK to access the advanced features
		//	of the BI platform.
		//****************************************************************************
		myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/infostore", TOKEN, false);
		if (myresponse.responseCode == 200)
			crashruncheck.println(myresponse.response);
		
		//*************************************************************************************
		//sbo41_webi_restful_ws_en.pdf
		//
		//SAP Web Intelligence RESTful Web Service
		//
		//The Web Intelligence RESTful web service SDK is an API used for manipulating the folIowing:
		//	• manipulating Web Intelligence documents and reports
		//	• retrieving data from a dataprovider
		//	• retrieving a list of available universes and details of an universes
		//	• scheduling documents
		//	It cannot be used to edit/create SAP Web Intelligence documents.
		//	The Web Intelligence RESTful web service SDK relies on the BI platform RESTful web services API
		//	for session management and repository access. Before starting with the Web Intelligence RESTful web
		//	service SDK, we strongly recommend that you to read the Business Intelligence Platform RESTful Web
		//	Service Developer Guide
		//******************************************************************************************************
		
		//******************************************************************************************************
		//3.2 Managing Documents
		//
		//Below are the main operations available to manage Web Intelligence documents.
		//You can get information about:
		//• Configuration formats
		//• Custom formats
		//• Documents
		//• Font mappings
		//• Functions
		//• Operators
		//• Report skins
		//You can manage
		//• Alerters
		//• Attachments
		//• Change tracking
		//• Documents, including exporting documents
		//• Links
		//• Styles
		//• Stylesheets (CSS)
		//The default URL to request Web Intelligence RESTful web services is the following:
		//http://<serverName>:6405/biprws/raylight/vx
		//Note:
		//Management of auto save & auto recovery configuration is currently not supported.
		//
		//(Skipping copy and paste about localization ..... assuming everybody reads/writes english
		//
		//******************************************************************************************************
		
		//****************************************************************************************
		//3.2.2 Document: retrieving, copying, or creating
		//
		//Use this URL to:
		//• Get the document list from the CMS ( GET <url>/documents).
		//• Copy a document (POST <url>/documents).
		//• Create an empty document (POST <url>/documents).
		//
		//Getting the Web Intelligence document list from the CMS:
		//This retrieves the list of document stored in the CMS. The documents are sorted by name. The list
		//depends on user access rights. You can also specify the number of documents to return for the list and
		//the first document to be used as the start document in the document list that you want to retrieve.
		//
		//Request:
		//GET http://<serverName>:6405/biprws/raylight/vx/documents
		//****************************************************************************************
		
		
		//shoot this call took 202426 milliseconds or a little over 3 minutes
		
		//long start_time = System.currentTimeMillis();
		//myresponse = rest.sendGet_token(PROTOCOL_HOST_PORT + "biprws/raylight/v1/documents", TOKEN_WITH_QUOTES, false);
		//long end_time   = System.currentTimeMillis();
		//if (myresponse.responseCode == 200)
		//{
		//	System.out.println(myresponse.response);
		//}
		//long timeElapsed = end_time - start_time;
		//String timestr = String.format("timeElased = %d milliseconds", timeElapsed);
		//System.out.println(timestr);
		
		
		//**************************************************************************************************************************
		//3.2.4 Document properties
		//
		//Use these functions to list or edit the document properties that are visible in the "Document Summary".
		//Certain settings are attributed automatically and cannot be set manually (for example, the last refresh
		//time).
		//
		//Related Topics
		//• Getting the properties of a document
		//• Update the properties of a document
		//
		//3.2.4.1 Getting the properties of a document
		//
		//Get the properties of a document referenced by its documentId parameter. (GET <url>documents/{documentId}/properties
		//
		//Note:
		//{documentId}: The identifier of the Web Intelligence document is retrieved in the document list by:
		//GET http://<serverName>:6405/biprws/raylight/vx/documents
		//
		//Retrieves the properties of a document
		//
		//Request:
		//GET http://<serverName>:6405/biprws/raylight/vx/documents/{documentId}/properties
		//*****************************************************************************************************************************
		
		myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/raylight/v1/documents/592369/properties", TOKEN_WITH_QUOTES, false);
		if (myresponse.responseCode == 200)
			crashruncheck.println(myresponse.response);
		
		
		//*******************************************************************************************************************
		//3.2.5 Exporting documents
		//
		//You can export a document in two ways: the entire document, or in paginated mode. You use the two
		//following urls:
		//• Export a document (GET -s <url>/documents/{documentId}[?parameters])
		//• Export a document in paginated mode (GET -s <url>/documents/{documentId}/pages)
		//
		//The output format can be:
		//• XML
		//• PDF
		//• Excel 2003
		//• Excel 2007
		//
		//Related Topics
		//• Exporting an entire document
		//• Exporting a document in paginated mode
		//
		//3.2.5.1 Exporting an entire document
		//You can export documents in the following formats:
		//• XML
		//• PDF
		//• Excel 2003
		//• Excel 2007
		//
		//Exporting a document
		//Note:
		//If HTML output is chosen, image links will be generated by Raylight: thus, the logon token must still be
		//valid when HTML output is displayed (to be able to get images from the generated links)
		//Request:
		//GET http://<serverName>:6405/biprws/raylight/vx/documents/{documentId}[?parameters]
		//*******************************************************************************************************************
		
		RestAPIResponse report_response = rest.sendGet_token_file_output(save_protocol_host_port + "biprws/raylight/v1/documents/592369",
				                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
				                                                "myoutputfile.xlsx",
				                                                TOKEN_WITH_QUOTES);
		if (report_response.responseCode == 200)
			crashruncheck.println("Created output file called myoutputfile.xlsx");
		else {
			crashruncheck.println("Error trying to create output file myoutputfile.xlsx");
			crashruncheck.println(report_response.response);
		}
		
		
		
		//Creating an output file can take a very long time, sometimes over 5 minutes
		
		//long start_time = System.currentTimeMillis();
		//report_response = rest.sendGet_token_file_output(PROTOCOL_HOST_PORT + "biprws/raylight/v1/documents/592369/pages",
        //        															"application/pdf",
        //        															"myoutputfile.pdf",
		//																	TOKEN_WITH_QUOTES);
		//long end_time = System.currentTimeMillis();
		//long timeElapsed = end_time - start_time;
		//String timestr = String.format("timeElased = %d milliseconds", timeElapsed);
		//System.out.println(timestr);
		//if (report_response.responseCode == 200)
		//	System.out.println("Created output file called myoutputfile.pdf");
		//else {
		//	crashruncheck.println("Error trying to create output file myoutputfile.pdf");
		//	crashruncheck.println(report_response.response);
		//}
		
		//***********************************************************************************************************
		//3.2.6 Managing styles, formats, fonts, skins, and charsets ..... (skip for now)
		//***********************************************************************************************************
		
		//***********************************************************************************************************
		//3.2.7 Managing functions, operators, and variables
		//
		//This section describes the different methods you can use for managing formula engine functions and
		//operators, and managing variables.
		//• Get the list of available formula engine functions (GET <url>/configuration/functions)
		//• Get the list of available formula engine operators (GET <url>/configuration/operators)
		//• Get the content of a documents variables dictionary (GET <url>/documents/{documentId}/variables)
		//• Add a new expression to a documents variables dictionary (POST <url>/documents/{documentId}/variables)
		//• Get the definition of a variable from a documents variable dictionary (GET <url>/documents/{documentId}/variables/{variableId})
		//• Modify the definition of an variable from a documents variable dictionary (PUT <url>/documents/{documentId}/variables/{variableId})
		//• Delete a variable from a documents variable dictionary (DELETE <url>/documents/{documentId}/variables/{variableId})
		//
		//Related Topics
		//• Getting the list of available formula engine functions
		//• Getting the list of available formula engine operators
		//• Getting the list of variables
		//• Adding a variable definition
		//• Getting the definition of a variable
		//• Modifying a variable definition
		//• Deleting a variable definition
		//***********************************************************************************************************
		
		//***********************************************************************************************************
		//3.2.7.1 Getting the list of available formula engine functions
		//Gets all functions of the available formula engine. This can be used to create formulas in the Report
		//Specification or define variables in the document dictionary.
		//
		//Request URL
		//GET http://<serverName>:6405/biprws/raylight/vx/configuration/functions
		//***********************************************************************************************************
		
		myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/raylight/v1/configuration/functions", TOKEN_WITH_QUOTES, false);
		if (myresponse.responseCode == 200)
			crashruncheck.println(myresponse.response);
		
		//********************************************************************************************************************
		//3.2.7.2 Getting the list of available formula engine operators
		//Gets all operators of the formula engine. This can be used to create formulas in the Report Specification
		//or define variables in the document dictionary.
		//
		//Request URL
		//GET http://<serverName>:6405/biprws/raylight/vx/configuration/operators
		//********************************************************************************************************************
		myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/raylight/v1/configuration/operators", TOKEN_WITH_QUOTES, false);
		if (myresponse.responseCode == 200)
			crashruncheck.println(myresponse.response);
		
		//**************************************************************************************************************
		//3.2.7.3 Getting the list of variables
		//Use this report to:
		//	• Get the content of a documents variables dictionary (GET <url>/documents/{documentId}/variables)
		//
		//Getting the content of a documents variables dictionary
		//
		//Request:
		//	GET http://<serverName>:6405/biprws/raylight/vx/documents/{documentId}/variables
		//***************************************************************************************************************
		
		myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/raylight/v1/documents/592369/variables", TOKEN_WITH_QUOTES, false);
		if (myresponse.responseCode == 200)
			crashruncheck.println(myresponse.response);
		
		//*******************************************************************************************************************
		//3.2.7.4 Adding a variable definition
		//Adding a new expression to the documents variables dictionary
		//You define the expression in the body which is defined in an .xml file saved in the current path (usually
		//the same path as the cURL tool). For example; variable2.xml.
		//
		//Note:
		//• The formula must be valid.
		//• You can only create a measure, an attribute or a dimension.
		//• When you create an attribute, the associated dimension is mandatory.
		//
		//Request:
		//POST http://<serverName>:6405/biprws/raylight/vx/documents/{documentId}/variables
		//********************************************************************************************************************
		
		//********************************************************************************************************************************************
		//3.2.7.5 Getting the definition of a variable
		//Use this to:
		//	• Get the definition of a variable from a documents variable dictionary (GET <url>/documents/{documentId}/variables/{variableId} )
		//
		//Note:
		//	{documentId}: The identifier of the Web Intelligence document retrieved in the document list 
		//by: GET http://<serverName>:6405/biprws/raylight/vx/documents
		//
		//Note:
		//  {variableId}: The identifier of the Web Intelligence variable retrieved in the document's variable list
		//by: GET http://<serverName>:6405/biprws/raylight/vx/documents/documentId/variables
		//
		//Getting the definition of a variable used by a document
		//
		//Request:
		//	GET http://<serverName>:6405/biprws/raylight/vx/documents/{documentId}/variables/{variableId}
		//*********************************************************************************************************************************************
		
		myresponse = rest.sendGet_token(save_protocol_host_port + "biprws/raylight/v1/documents/592369/variables/L2", TOKEN_WITH_QUOTES, false);
		if (myresponse.responseCode == 200)
			crashruncheck.println(myresponse.response);
	}//Demo_BAD_API

	
	public void DEBUGGING_TEST_GET_CHILDREN(String USERNAME, String PASSWORD, String PROTOCOL_HOST_PORT, String id_str, int page, int pageSize) {
		
		save_username = USERNAME;
		save_password = PASSWORD;
		save_protocol_host_port = PROTOCOL_HOST_PORT;
		
		if (logon()) {
			String urlstr = String.format("%sbiprws/infostore/%s/children?page=%d&pageSize=%d", PROTOCOL_HOST_PORT, id_str, page, pageSize);
			RestAPIResponse myresponse = null;
			
			try {
				myresponse = rest.sendGet_token(urlstr, TOKEN, false);
			} catch (Exception ex) {
				crashruncheck.println("DEBUGGING_TEST_GET_CHILDREN: Exception caught in restful API call !!!!!", ex);
				return;
			}
			
			if (myresponse.responseCode == 200)
				crashruncheck.println(myresponse.response);
			else {
				String debugstr = String.format("responseCode = %d", myresponse.responseCode);
				crashruncheck.println(debugstr);
			}
			
			logoff();
		}
	}//DEBUGGING_TEST_GET_CHILDREN
	
	
	
	
	
	public void DEBUGGING_EXPERIMENT_TRYING_TO_FIND_USERGROUP_USER_RELATIONSHIP(String username, String password, String protocol_host_port) {
		//"546"    == relationship "UserGroup-User" in page2 of pagesize50 of children of relationships
		//"1"      == User-Group Eveyone
		//"588532" == User maxwell.lee
		RestAPIResponse myresponse = null;
		if (this.Test_And_Save_Credentials(username, password, protocol_host_port)) {
			
			this.logon();
			
			crashruncheck.println("(1) 546 UserGroup-User 1");
			myresponse = this.Get_Relationship("546", "UserGroup-User", "1", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(2) 546 UserGroup-User 588532");
			myresponse = this.Get_Relationship("546", "UserGroup-User", "588532", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			
			crashruncheck.println("(3) 546 UserGroup 1");
			myresponse = this.Get_Relationship("546", "UserGroup", "1", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(4) 546 UserGroup 588532");
			myresponse = this.Get_Relationship("546", "UserGroup", "588532", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(5) 546 User 1");
			myresponse = this.Get_Relationship("546", "User", "1", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(6) 546 User 588532");
			myresponse = this.Get_Relationship("546", "User", "588532", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			//*************************************************************************
			
			crashruncheck.println("(7) 1 UserGroup-User 588532");
			myresponse = this.Get_Relationship("1", "UserGroup-User", "588532", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(8) 588532 UserGroup-User 1");
			myresponse = this.Get_Relationship("588532", "UserGroup-User", "1", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			
			crashruncheck.println("(9) 1 UserGroup 588532");
			myresponse = this.Get_Relationship("1", "UserGroup", "588532", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(10) 588532 UserGroup 1");
			myresponse = this.Get_Relationship("588532", "UserGroup", "1", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(11) 1 User 588532");
			myresponse = this.Get_Relationship("1", "User", "588532", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(12) 588532 User 1");
			myresponse = this.Get_Relationship("588532", "User", "1", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			//****************************************************************************
			
			crashruncheck.println("(13) 1 UserGroup-User 546");
			myresponse = this.Get_Relationship("1", "UserGroup-User", "546", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(14) 588532 UserGroup-User 546");
			myresponse = this.Get_Relationship("588532", "UserGroup-User", "546", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			
			crashruncheck.println("(15) 1 UserGroup 546");
			myresponse = this.Get_Relationship("1", "UserGroup", "546", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(16) 588532 UserGroup 546");
			myresponse = this.Get_Relationship("588532", "UserGroup", "546", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(17) 1 User 546");
			myresponse = this.Get_Relationship("1", "User", "546", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(18) 588532 User 546");
			myresponse = this.Get_Relationship("588532", "User", "546", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			//*****************************************************
			//*****************************************************
			//*****************************************************
			
			crashruncheck.println("(19) 546 UserGroup-User 1");
			myresponse = this.Get_Relationship("546", "UserGroup-User", "1", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(20) 546 UserGroup-User 588532");
			myresponse = this.Get_Relationship("546", "UserGroup-User", "588532", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			
			crashruncheck.println("(21) 546 UserGroup 1");
			myresponse = this.Get_Relationship("546", "UserGroup", "1", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(22) 546 UserGroup 588532");
			myresponse = this.Get_Relationship("546", "UserGroup", "588532", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(23) 546 User 1");
			myresponse = this.Get_Relationship("546", "User", "1", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(24) 546 User 588532");
			myresponse = this.Get_Relationship("546", "User", "588532", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			//*************************************************************************
			
			crashruncheck.println("(25) 1 UserGroup-User 588532");
			myresponse = this.Get_Relationship("1", "UserGroup-User", "588532", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(26) 588532 UserGroup-User 1");
			myresponse = this.Get_Relationship("588532", "UserGroup-User", "1", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			
			crashruncheck.println("(27) 1 UserGroup 588532");
			myresponse = this.Get_Relationship("1", "UserGroup", "588532", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(28) 588532 UserGroup 1");
			myresponse = this.Get_Relationship("588532", "UserGroup", "1", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(29) 1 User 588532");
			myresponse = this.Get_Relationship("1", "User", "588532", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(30) 588532 User 1");
			myresponse = this.Get_Relationship("588532", "User", "1", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			//****************************************************************************
			
			crashruncheck.println("(31) 1 UserGroup-User 546");
			myresponse = this.Get_Relationship("1", "UserGroup-User", "546", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(32) 588532 UserGroup-User 546");
			myresponse = this.Get_Relationship("588532", "UserGroup-User", "546", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			
			crashruncheck.println("(33) 1 UserGroup 546");
			myresponse = this.Get_Relationship("1", "UserGroup", "546", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(34) 588532 UserGroup 546");
			myresponse = this.Get_Relationship("588532", "UserGroup", "546", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(35) 1 User 546");
			myresponse = this.Get_Relationship("1", "User", "546", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(36) 588532 User 546");
			myresponse = this.Get_Relationship("588532", "User", "546", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			//****************************************************************************
			//****************************************************************************
			
			crashruncheck.println("(37) 588532 546 1");
			myresponse = this.Get_Relationship("588532", "546", "1", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(38) 588532 546 1");
			myresponse = this.Get_Relationship("588532", "546", "1", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(39) 1 546 588532");
			myresponse = this.Get_Relationship("1", "546", "588532", false);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			crashruncheck.println("(40) 1 546 588532");
			myresponse = this.Get_Relationship("1", "546", "588532", true);
			if (myresponse.responseCode==200) {
				crashruncheck.println("YAY I FOUND IT !!!!!");
				this.logoff();
				return;
			}
			
			//*****************************************************************************
			//Ok everything above does not work, but the following works good
			//*****************************************************************************
			
			//this works good
			crashruncheck.println("(WORKS GOOD 1) 588532 userGroups null");
			myresponse = this.Get_Relationship("588532", "userGroups", null, true);
			if (myresponse.responseCode==200)
				crashruncheck.println("YAY WORKS GOOD 1 !!!!!");
			
			//this works but it does not show any real useful information ..... 
			//i was hoping it would show all users of userGroup=1 which is userGroup=Everyone but it does not do that
			crashruncheck.println("(WORKS GOOD 2) 1 users null");
			myresponse = this.Get_Relationship("1", "users", null, true);
			if (myresponse.responseCode==200)
				crashruncheck.println("YAY WORKS GOOD 2 !!!!!");
			
			//this works good
			crashruncheck.println("(WORKS GOOD 3) 1 users 588532");
			myresponse = this.Get_Relationship("1", "users", "588532", true);
			if (myresponse.responseCode==200)
				crashruncheck.println("YAY WORKS GOOD 3 !!!!!");
				
			this.logoff();
		}
	}//DEBUGGING_EXPERIMENT_TRYING_TO_FIND_USERGROUP_USER_RELATIONSHIP
	
	
	public void TESTING_DEBUGGING_ONLY_Document_Refresh_From_XML_File(String docidstr, String xml_input_file_path) {
		
		try {
			String urlstr;
			RestAPIResponse response;
			
			urlstr = String.format("%sbiprws/raylight/v1/documents/%s/parameters", save_protocol_host_port, docidstr);
			
			String xmlbodystr = "";
			
			BufferedReader in = new BufferedReader(new FileReader(xml_input_file_path));
			String myline = in.readLine();
			while (myline != null) {
				xmlbodystr += myline;
				myline = in.readLine();
			}
			in.close();
			
			response = rest.sendPut_XML_token(urlstr, xmlbodystr, TOKEN_WITH_QUOTES);
			
			if (response.responseCode == 200) {
				crashruncheck.println(response.response);
			}
		}
		catch (Throwable tt) {
			crashruncheck.println("class SAP_BI_WebIntel_REST_helper method TESTING_DEBUGGING_ONLY_Document_Refresh_From_XML_File: CAUGHT THROWABLE !!!!!", tt);
		}
		
	}//TESTING_DEBUGGING_ONLY_Document_Refresh_From_XML_File
	
	
	//************************************************************
	//This method below proved XML reordering of attributes
	//had nothing to do with non-200 response code.
	//The truth was that the oracle database view
	//got clobbered and as a result the document could
	//not be refreshed successfully because it 
	//depended on that oracle database view.
	//************************************************************
	public void DEBUGGING_EXPERIMENT_REORDERED_XML_ATTRIBUTES_CAUSE_400_RESPONSE_NOT_200(String username, String password, String protocol_host_port) {
		
		String xml_str = null;
		try {
			StringBuilder contentBuilder = new StringBuilder();
			FileReader fileread = new FileReader("19576_answer.xml");
			BufferedReader br = new BufferedReader(fileread);
			String sCurrentLine = br.readLine();
			while (sCurrentLine != null) {
				contentBuilder.append(sCurrentLine);
				
				sCurrentLine = br.readLine();
			}
			xml_str = contentBuilder.toString();
		}
		catch (Throwable tt) {
			crashruncheck.println("Can not read xml file", tt);
			return;
		}
		
		crashruncheck.println(xml_str);
		
		
		if (this.Test_And_Save_Credentials(username, password, protocol_host_port)) {
			this.logon();
			
			//drop the XMLHelper object  we do not care, just want to see text
			this.Get_Document_Refresh_Parameters_XML("19576");
			
			RestAPIResponse response = this.Do_Document_Refresh("19576", xml_str);
			String debugstr = String.format("responsecode=%d", response.responseCode);
			crashruncheck.println(debugstr);
			crashruncheck.println(response.response);
			
			response = this.Run_Report_And_Get_File(true, "19576", "my_output_19576.xlsx");
			
			this.logoff();
		}
		
	}//DEBUGGING_EXPERIMENT_REORDERED_XML_ATTRIBUTES_CAUSE_400_RESPONSE_NOT_200
	
	
	
	
	public void DEBUGGING_TEST_CHECK_ALL_WEBI_PARAMETERS_UNDER_ROOT_FOLDER(String USERNAME, String PASSWORD, String PROTOCOL_HOST_PORT) {
		
		save_username = USERNAME;
		save_password = PASSWORD;
		save_protocol_host_port = PROTOCOL_HOST_PORT;
		
		if (logon()) {

			ArrayList<String> pathlist  = new ArrayList<String>();
			ArrayList<String> docidlist = new ArrayList<String>();
			boolean got_all_webi = DEBUG_Get_All_WebIntelligence_Documents(pathlist, docidlist, "Root%20Folder", "Root%20Folder");
			if (got_all_webi) {
				String debugstr = String.format("pathlist size=%d  docidlist size=%d", pathlist.size(), docidlist.size());
				crashruncheck.println(debugstr);
			}
			
			if (pathlist.size() != docidlist.size())
				crashruncheck.println("ERROR: pathlist.size() != docidlist.size()");
			else {
				int ii=-1;
				for (ii=0; ii < pathlist.size(); ii++) {
					String mypath = pathlist.get(ii);
					String mydocid = docidlist.get(ii);
					
					String debugstr = String.format("WORKING ON %d/%d mypath=%s mydocid=%s", ii, pathlist.size(), mypath, mydocid);
					crashruncheck.println(debugstr);
					
					DocumentRefresh_Thread_Result  myresult = new DocumentRefresh_Thread_Result(this, crashruncheck, mydocid);
		    		myresult.documentRefresh_ParsedXML      = Get_Document_Refresh_Parameters_XML(mydocid);							//rest call to xml
		    		myresult.documentRefresh_SaveParameters = myresult.documentRefresh_ParsedXML.document_refresh_parameters_parse();	//xml to ArrayList<DocumentRefresh_Parameter>
		    		
		    		int pp=-1;
		    		for (pp=0; pp < myresult.documentRefresh_SaveParameters.size(); pp++) {
		    			DocumentRefresh_Parameter param = myresult.documentRefresh_SaveParameters.get(pp);
		    			if (param.answer_type.equals("DateTime")) {
		    				if (param.info_cardinality.trim().toLowerCase().equals("single")) {
		    					String debugparamstr = String.format("param %d/%d is DateTime OK", pp, myresult.documentRefresh_SaveParameters.size());
		    					crashruncheck.println(debugparamstr);
		    				}
		    				else {
		    					String debugparamstr = String.format("FIX: param %d/%d is multiple pick DateTime ..... we need to fix source code to handle this", pp, myresult.documentRefresh_SaveParameters.size());
			    				crashruncheck.println(debugparamstr);
			    				break;
		    				}
		    			}
		    			else if (param.is_text_button_parameter()) {
		    				String debugparamstr = String.format("param %d/%d is single/multiple text_button_parameter OK", pp, myresult.documentRefresh_SaveParameters.size());
		    				crashruncheck.println(debugparamstr);
		    			}
		    			else if (param.answer_type.equals("Numeric") || param.answer_type.equals("Text")) {
		    				
		    				if (param.info_lov_hierarchical != null && param.info_lov_hierarchical.trim().toLowerCase().equals("true")) {
	    						String debugparamstr = String.format("FIX: param %d/%d is hierarchical unhandled area ..... we need to fix source code to handle this", pp, myresult.documentRefresh_SaveParameters.size());
			    				crashruncheck.println(debugparamstr);
			    				break;
	    					}
		    				else if (param.info_lov_partial != null && param.info_lov_partial.equals("false")) {
		    					
		    					
		    					if (param.info_lov_cvalues == null && param.info_values == null && param.info_lov_intervals == null) {
		    						String debugparamstr = String.format("FIX: param %d/%d is unhandled area BBB ..... we need to fix source code to handle this", pp, myresult.documentRefresh_SaveParameters.size());
				    				crashruncheck.println(debugparamstr);
				    				break;
		    					}
		    					else {
		    						String debugparamstr = String.format("param %d/%d is list param OK", pp, myresult.documentRefresh_SaveParameters.size());
				    				crashruncheck.println(debugparamstr);
		    					}
		    					
		    				}
		    				else {
		    					String debugparamstr = String.format("FIX: param %d/%d is unhandled area AAA ..... we need to fix source code to handle this", pp, myresult.documentRefresh_SaveParameters.size());
			    				crashruncheck.println(debugparamstr);
			    				break;
		    				}
		    			}
		    			else {
		    				String debugparamstr = String.format("FIX: param %d/%d is NOT DateTime and NOT Numeric and NOT Text ..... we need to fix source code to handle this", pp, myresult.documentRefresh_SaveParameters.size());
		    				crashruncheck.println(debugparamstr);
		    				break;
		    			}
		    		}
		    		
		    		if (pp != myresult.documentRefresh_SaveParameters.size())
		    			break;
				}
				
				if (ii != pathlist.size())
					crashruncheck.println("NOT A GOOD FINISH !!!!! AN ERROR HAPPENED !!!!!");
				else
					crashruncheck.println("GOOD FINISH");
			}
			logoff();
		}
	}//DEBUGGING_TEST_CHECK_ALL_WEBI_PARAMETERS_UNDER_ROOT_FOLDER

}//class
