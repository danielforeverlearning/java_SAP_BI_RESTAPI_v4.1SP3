package sap_bi_restapi_package;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.FileDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;


public class DocumentRefresh_ScrollPane extends JScrollPane implements ActionListener {
	
	private CrashAndRunChecker			crashruncheck;
	private SAP_BI_WebIntel_REST_helper	my_rest_caller;
	private GUI_Frame					parent_frame;
	private WaitingFrame				mywaitingframe;
	private InfostoreTree_ScrollPane    treeScrollPane;
	private Infostore_Button_Panel      infostore_button_panel;
	private InfostoreEntry_Panel        InfostoreEntryPanel;
	
	private String						mydocid;
	
	
	private JPanel									documentRefreshPanel;
	
	private DocumentRefresh_Thread_Result			thread_result;
	
	private Parameter_Date_Button[]					param_date_buttons;
	private List<String>[]							chosen_list_params;
	private List<String>[]							chosen_text_params;							
	
	
	public DocumentRefresh_ScrollPane(CrashAndRunChecker tempcrashruncheck,
			                          SAP_BI_WebIntel_REST_helper temprest,
			                          GUI_Frame tempparentframe,
			                          WaitingFrame tempwaitframe,
			                          InfostoreTree_ScrollPane temptree,
			                          Infostore_Button_Panel tempbuttons,
			                          InfostoreEntry_Panel tempentry) {
		super();
		
		crashruncheck          = tempcrashruncheck;
		my_rest_caller         = temprest;
		parent_frame           = tempparentframe;
		mywaitingframe         = tempwaitframe;
		treeScrollPane         = temptree;
		infostore_button_panel = tempbuttons;
		InfostoreEntryPanel    = tempentry;
		
		documentRefreshPanel      = new JPanel(null);
		
		thread_result             = null;
		
		param_date_buttons        = null;
		chosen_list_params        = null;
		chosen_text_params        = null;
		
		this.setViewportView(documentRefreshPanel);
    	this.setViewportBorder(BorderFactory.createLineBorder(Color.black));
    	this.setBounds(30, 550, 1000, 300);
	}

	
	//button actions
	public void actionPerformed(ActionEvent ev) {
		
		Object myobj = ev.getSource();
		JButton mybutton = (JButton)myobj;
		String actstr = mybutton.getActionCommand();
		
		if (actstr.startsWith("LIST_PARAMETER_")) {
			String param_num_str = actstr.replace("LIST_PARAMETER_", "");
			Integer param_num = Integer.parseInt(param_num_str);
			String debugstr = String.format("DocumentRefresh_ScrollPane: actionPerformed=%s mydocid=%s param_name=%s", actstr, mydocid, thread_result.documentRefresh_SaveParameters.get(param_num).parameter_name); 
			crashruncheck.println(debugstr);
			List_Param_Click(actstr, mybutton);
		}
		else if (actstr.startsWith("TEXT_PARAMETER_")) {
			String param_num_str = actstr.replace("TEXT_PARAMETER_", "");
			Integer param_num = Integer.parseInt(param_num_str);
			
			DocumentRefresh_Parameter param = thread_result.documentRefresh_SaveParameters.get(param_num);
			
			String debugstr = String.format("DocumentRefresh_ScrollPane: actstr=%s mydocid=%s param_name=%s", actstr, mydocid, param.parameter_name); 
			crashruncheck.println(debugstr);
			
			if (param.info_cardinality.trim().toLowerCase().equals("single")) {
				String response = JOptionPane.showInputDialog(parent_frame, param.parameter_name);
				// get the user's input. note that if they press Cancel, response will be null
				if (response == null) {
					debugstr = String.format("DocumentRefresh_ScrollPane: %s (single) response = null", actstr);
					response = "";
				}
				else
					debugstr = String.format("DocumentRefresh_ScrollPane: %s (single) response = %s", actstr, response);
				crashruncheck.println(debugstr);
				
				this.chosen_text_params[param_num] = new ArrayList<String>();
				this.chosen_text_params[param_num].add(response);
				
				//change button label
			    boolean optional_param = true;
				if (param.parameter_optional.trim().toUpperCase().equals("FALSE"))
					optional_param = false; //mandatory, necessary, required, or can not do refresh
				
				String labelstr = null;
				if (optional_param)
					labelstr = "(OPTIONAL) " + param.parameter_name;
				else
					labelstr = "(REQUIRED) " + param.parameter_name;
				
				String tempbuttonstr = "<html><center>" + labelstr + "</center><br/><center style=\"color:Blue;\">" + response + "</center></html>";
				mybutton.setText(tempbuttonstr);
			}
			else { //multiple cardinality 
				
				String[] options = null;
				if (this.chosen_text_params[param_num] != null)
					options = this.chosen_text_params[param_num].toArray(new String[this.chosen_text_params[param_num].size()]);
				
				ArrayList<String> selections = Parameter_Multiple_Text_Dialog.showDialog(parent_frame, null, "Please select 1 or more values and press SET or press CLEAR to select no values:", param.parameter_name, options);
				
				String selections_str = "&nbsp;";
				if (selections == null)
					this.chosen_text_params[param_num].clear();
				else {
					this.chosen_text_params[param_num] = selections;
					for (int ii=0; ii < selections.size(); ii++)
						selections_str += (selections.get(ii) + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
				}
				
				//change button label
			    boolean optional_param = true;
				if (param.parameter_optional.trim().toUpperCase().equals("FALSE"))
					optional_param = false; //mandatory, necessary, required, or can not do refresh
				
				String labelstr = null;
				if (optional_param)
					labelstr = "(OPTIONAL) " + param.parameter_name;
				else
					labelstr = "(REQUIRED) " + param.parameter_name;
				
				String tempbuttonstr = "<html><center>" + labelstr + "</center><br/><center style=\"color:Blue;\">" + selections_str + "</center></html>";
				mybutton.setText(tempbuttonstr);						
			}
		}
		else if (actstr.equals("DO_REFRESH")) {
			crashruncheck.println("DocumentRefresh_ScrollPane: clicked DO_REFRESH");
			Start_2nd_Thread();
		}
		else if (actstr.equals("CANCEL")) {
			crashruncheck.println("DocumentRefresh_ScrollPane: clicked CANCEL");
			Talk_To_Parent(1); //error_code == 1 means they pressed CANCEL
		}
	}//actionPerformed
	
	
	
	
	
	//integer_code: string
	//(example:)
	//5: Got a bad response code NOT 200 trying to do Document Refresh mydocid=%d ..... Please take a screenshot and email us the crashruncheck text file dump
	private void Finish_2nd_Thread(String end_code_str) {
		
		mywaitingframe.ChangeVisibility(null, null, null, false);
		parent_frame.revalidate();
		parent_frame.repaint();
		
		if (end_code_str.startsWith("7:")) {
			JOptionPane.showMessageDialog(parent_frame, end_code_str.substring(2), "SUCCESS", JOptionPane.OK_OPTION);
			Talk_To_Parent(7); //error_code == 7 means they pressed DO_REFRESH we logged in, did rest-api call and got a good 200 responseCode, no logoff till after export pdf or excel
		}
		else if (end_code_str.startsWith("6:")) {
			JOptionPane.showMessageDialog(parent_frame, end_code_str.substring(2), "ERROR", JOptionPane.ERROR_MESSAGE);
			Talk_To_Parent(6); //error_code == 6 means DocumentRefresh_ScrollPane caught a throwable inside refresh_worker (2nd thread) !!!!! ..... Please take a screenshot and email us the crashruncheck text file dump
		}
		else if (end_code_str.startsWith("5:")) {
			JOptionPane.showMessageDialog(parent_frame, end_code_str.substring(2), "ERROR", JOptionPane.ERROR_MESSAGE);
			Talk_To_Parent(5); //error_code == 5 means they pressed DO_REFRESH we logged in, we did rest-api call but got a non-200 responseCode
		}
		else if (end_code_str.startsWith("4:")) {
			JOptionPane.showMessageDialog(parent_frame, end_code_str.substring(2), "ERROR", JOptionPane.ERROR_MESSAGE);
			Talk_To_Parent(4); //error_code == 4 means they pressed DO_REFRESH but we failed to get logon token
		}
		else if (end_code_str.startsWith("3:")) {
			JOptionPane.showMessageDialog(parent_frame, end_code_str.substring(2), "ERROR", JOptionPane.ERROR_MESSAGE);
			Talk_To_Parent(3); //error_code == 3 means they pressed DO_REFRESH but code failed to create XML body string before making rest-api call to do Document Refresh
		}
		else if (end_code_str.startsWith("2:")) {
			JOptionPane.showMessageDialog(parent_frame, end_code_str.substring(2), "ERROR", JOptionPane.ERROR_MESSAGE);
			
			parent_frame.SetEnableRecursive(this, true);
			parent_frame.revalidate();
			parent_frame.repaint();
			
			//error_code == 2 means they pressed DO_REFRESH but forgot to set all REQUIRED parameters ..... DO NOT CLOSE THIS DocumentRefresh_ScrollPane !!!!! KEEP IT OPEN !!!!!
			//we do not need to tell parent frame
		}
	}//Finish_2nd_Thread
	
	
	private void Start_2nd_Thread() {
		
		mywaitingframe.ChangeVisibility("Please wait while we call rest-api to refresh document", "A Rest-api call is made, but we do not know how long it will take.", "This will close automatically when the refresh has been completed.", true);
		parent_frame.SetEnableRecursive(this, false);
		parent_frame.revalidate();
		parent_frame.repaint();
		
		SwingWorker<String, Void> refresh_worker = new SwingWorker<String, Void>() {
		    @Override
		    public String  doInBackground() {
		    	crashruncheck.println("DocumentRefresh_ScrollPane  Start_2nd_Thread: running.....");
		    	if (my_rest_caller == null)
		    		crashruncheck.println("DocumentRefresh_ScrollPane  Start_2nd_Thread: my_rest_caller is null !!!!!");
		    	else
		    		crashruncheck.println("DocumentRefresh_ScrollPane  Start_2nd_Thread: my_rest_caller OK");
		    	
		    	
		    	boolean check_ok = Check_Required_Parameters();
				if (check_ok) {
					String xmlbodystr = thread_result.documentRefresh_ParsedXML.Write_Document_Refresh_Parameters(thread_result.documentRefresh_SaveParameters, chosen_list_params, param_date_buttons, chosen_text_params); //fills xml body with parameters chosen by user
					
					if (xmlbodystr.length() > 0) {
						Boolean token_success = my_rest_caller.logon();
				    	if (token_success) {
				    		RestAPIResponse response = my_rest_caller.Do_Document_Refresh(mydocid, xmlbodystr);
				    		String debugstr = String.format("clicked DO_REFRESH: responsecode=%d  response=%s", response.responseCode, response.response);
				    		crashruncheck.println(debugstr);
				    		
				    		if (response.responseCode == 200)
				    			return("7:" + response.response); //error_code == 7 means they pressed DO_REFRESH we logged in, did rest-api call and got a good 200 responseCode, no logoff till after export pdf or excel
				    		else {
				    			String badcodestr = String.format("Got a bad response code NOT 200 trying to do Document Refresh mydocid=%s ..... Please take a screenshot and email us the crashruncheck text file dump", mydocid);
				    			return("5:" + badcodestr); //error_code == 5 means they pressed DO_REFRESH we logged in, we did rest-api call but got a non-200 responseCode
				    		}
				    	}
				    	else {
				    		String failedlogon = String.format("Failed to logon trying to do Document Refresh mydocid=%s ..... Did the server just go down or something ?!?!?  Please take a screenshot and email us the crashruncheck text file dump", mydocid);
			    			return("4:" + failedlogon); //error_code == 4 means they pressed DO_REFRESH but we failed to get logon token
				    	}
					}
					else {
						String failedxmlbody = String.format("Failed to create XML body string before rest-api call to do Document Refresh mydocid=%s ..... Please take a screenshot and email us the crashruncheck text file dump", mydocid);
		    			return("3:" + failedxmlbody); //error_code == 3 means they pressed DO_REFRESH but code failed to create XML body string before making rest-api call to do Document Refresh
					}
				}
				else {
					return("2:One or more REQUIRED parameters have not been set ..... Please set all REQUIRED parameters."); //error_code == 2 means they pressed DO_REFRESH but forgot to set all REQUIRED parameters ..... DO NOT CLOSE THIS DocumentRefresh_ScrollPane !!!!! KEEP IT OPEN !!!!!
				}
		    }//doInBackground

		    @Override
		    public void done() {
		    	String end_code_str = null;
		    	try {
	                end_code_str = get();
	            }
	            catch (Throwable tt) {
	                crashruncheck.println("!!!!! refresh_worker: THROWABLE CAUGHT in class DocumentRefresh_ScrollPane: ", tt);
	                end_code_str = "6:DocumentRefresh_ScrollPane caught a throwable inside refresh_worker (2nd thread) !!!!! ..... Please take a screenshot and email us the crashruncheck text file dump";
	            }
		    	Finish_2nd_Thread(end_code_str);
		    }
		};
		
		refresh_worker.execute();
		
	}//Start_2nd_Thread
	
	
	//*******************************************************************************
	//sbo41_webi_restful_ws_en.pdf
	//3.8.1 Getting the document refresh parameters before refreshing a document
	//3.8.2 Refreshing a document
	//*******************************************************************************
	public void Start_1st_Thread(String docidstr) {
		
		mydocid = docidstr;
		
		SwingWorker<DocumentRefresh_Thread_Result, Void> params_worker = new SwingWorker<DocumentRefresh_Thread_Result, Void>() {
			    @Override
			    public DocumentRefresh_Thread_Result  doInBackground() {
			    	crashruncheck.println("DocumentRefresh_ScrollPane  Start_1st_Thread: running.....");
			    	if (my_rest_caller == null)
			    		crashruncheck.println("DocumentRefresh_ScrollPane  Start_1st_Thread: my_rest_caller is null !!!!!");
			    	else
			    		crashruncheck.println("DocumentRefresh_ScrollPane  Start_1st_Thread: my_rest_caller OK");
			    	Boolean token_success = my_rest_caller.logon();
			    	if (token_success) {
			    		DocumentRefresh_Thread_Result  myresult = new DocumentRefresh_Thread_Result(my_rest_caller, crashruncheck, docidstr);
			    		myresult.documentRefresh_ParsedXML = my_rest_caller.Get_Document_Refresh_Parameters_XML(docidstr);
			    		myresult.documentRefresh_SaveParameters = myresult.documentRefresh_ParsedXML.document_refresh_parameters_parse();
			    		
			    		// 0 = success, found the list of string values for this intervalID of this paramID
						//-1 = found this list of string values for this intervalID of this paramID but the start and end values are not matching
						//-2 = could not find the list of string values for this intervalID of this paramID this code needs to be stepped thru
						//-3 = responseCode that is not 200 from rest-api call
						//-4 = caught a throwable exception need to check crashruncheck text file to debug
			    		myresult.Get_All_List_Param_Choices();
			    		
			    		String debugcheck = String.format("DocumentRefresh_ScrollPane  Start_1st_Thread: list_params_get_choices=%d", myresult.list_params_get_choices);
			    		crashruncheck.println(debugcheck);
			    		
			    		return myresult;
			    	}
			    	else
			    		return null; //Could not get logon token, perhaps server is down or credentials are bad but we got this far into gui
			    }

			    @Override
			    public void done() {
			    	DocumentRefresh_Thread_Result endresult = null;
			    	try {
		                endresult = get();
		            }
		            catch (Throwable tt) {
		                crashruncheck.println("!!!!! params_worker: THROWABLE CAUGHT in class DocumentRefresh_ScrollPane: ", tt);
		                endresult = null;
		            }
			    	Finish_1st_Thread(endresult);
			    }
		};
			
		params_worker.execute();
		
	}//Start_1st_Thread

	
	private void Finish_1st_Thread(DocumentRefresh_Thread_Result endresult) {
		
		thread_result = endresult;
		
		if (endresult != null) {
			if (endresult.list_params_get_choices == 0) {
				boolean ok = RebuildPanel(endresult.documentRefresh_SaveParameters);
				if (ok)
					Talk_To_Parent(0); //success
				else
					Talk_To_Parent(-1); //will not show parameters panel because of multiple cardinality for DateTime parameter
			}
			else if (endresult.list_params_get_choices == -1) //-1 = Get_All_List_Param_Choices: found this list of string values for an intervalID of a paramID, but the start and end values are not matching
				Talk_To_Parent(-3);
			else if (endresult.list_params_get_choices == -2) //-2 = Get_All_List_Param_Choices: could not find the list of string values for an intervalID of a paramID this code needs to be stepped thru
				Talk_To_Parent(-4);
			else if (endresult.list_params_get_choices == -3) //-3 = Get_All_List_Param_Choices: responseCode that is not 200 from rest-api call
				Talk_To_Parent(-5);
			else
				Talk_To_Parent(-6); //-4 = Get_All_List_Param_Choices: caught a throwable exception need to check crashruncheck text file to debug
		}
		else
			Talk_To_Parent(-2); //could not get list of parameters from rest-api calls for some reason, need to check crashruncheck text file dump
    }//Finish_1st_Thread

	
	private void Talk_To_Parent(int error_code) {
		
		//-6 == Get_All_List_Param_Choices: caught a throwable exception need to check crashruncheck text file to debug
		//-5 == Get_All_List_Param_Choices: responseCode that is not 200 from rest-api call
		//-4 == Get_All_List_Param_Choices: could not find the list of string values for an intervalID of a paramID this code needs to be stepped thru
		//-3 == Get_All_List_Param_Choices: found this list of string values for an intervalID of a paramID, but the start and end values are not matching
		//-2 == could not get list of parameters from rest-api calls for some reason, need to check crashruncheck text file dump
		//-1 == will not show parameters panel because of multiple cardinality for DateTime parameter
		//0  == success on refresh_document_worker thread ..... document refresh panel should be showing in parent gui frame ..... user has to press CANCEL or DO_REFRESH to do final rest-api refresh call
		//1  == user pressed CANCEL
		//2  == user pressed DO_REFRESH but forgot to set all REQUIRED parameters ..... DO NOT CLOSE THIS DocumentRefresh_ScrollPane !!!!! KEEP IT OPEN !!!!! Talk_To_Parent will never get this code==2
		//3  == user pressed DO_REFRESH but code failed to create XML body string before making rest-api call to do Document Refresh
		//4  == user pressed DO_REFRESH but we failed to get logon token
		//5  == user pressed DO_REFRESH we logged in, we did rest-api call inside thread but got a non-200 responseCode
		//6  == user pressed DO_REFRESH we logged in, we did rest-api call inside thread but caught a throwable inside refresh_worker (2nd thread) !!!!! ..... Please take a screenshot and email us the crashruncheck text file dump
		//7  == user pressed DO_REFRESH we logged in, did rest-api call and got a good 200 responseCode, DO NOT logoff till after export pdf or excel here from GUI side
		
		String debugstr = String.format("DocumentRefresh_ScrollPane  Talk_To_Parent: error_code=%d", error_code);
		crashruncheck.println(debugstr);
		
		if (error_code != 0 && error_code != 2) {
			documentRefreshPanel.removeAll();
			revalidate();
			repaint();
			
			thread_result             = null; //force garbage collection
			
			param_date_buttons        = null; //force garbage collection
			chosen_list_params        = null; //force garbage collection
			chosen_text_params        = null; //force garbage collection
		}
		
		
		
		//parent_frame.Doc_Refresh_ScrollPane_Finished(error_code);
		//-5 == for parameters that are specifically lists, Get_All_List_Param_Choices: caught a throwable trying to make rest-api call, need to check crashruncheck text file dump
		//-4 == for parameters that are specifically lists, Get_All_List_Param_Choices: got a bad response code not 200 after rest-api call, need to check crashruncheck text file dump
		//-3 == for parameters that are specifically lists, Get_All_List_Param_Choices: rest-api call OK with 200 but failed to parse the XML response, need to check crashruncheck text file dump
		//-2 == could not get list of parameters from rest-api calls for some reason, need to check crashruncheck text file dump
		//-1 == will not show parameters panel because of multiple cardinality for DateTime parameter
		//0  == success on refresh_document_worker thread ..... document refresh panel should be showing in parent gui frame ..... user has to press CANCEL or DO_REFRESH to do final rest-api refresh call
		//1  == user pressed CANCEL
		//2  == user pressed DO_REFRESH but forgot to set all REQUIRED parameters ..... DO NOT CLOSE THIS DocumentRefresh_ScrollPane !!!!! KEEP IT OPEN !!!!! Technically this method will never get this code==2
		//3  == user pressed DO_REFRESH but code failed to create XML body string before making rest-api call to do Document Refresh
		//4  == user pressed DO_REFRESH but we failed to get logon token
		//5  == user pressed DO_REFRESH we logged in, we did rest-api call inside thread but got a non-200 responseCode
		//6  == user pressed DO_REFRESH we logged in, we did rest-api call inside thread but caught a throwable inside refresh_worker (2nd thread) !!!!! ..... Please take a screenshot and email us the crashruncheck text file dump
		//7  == user pressed DO_REFRESH we logged in, did rest-api call and got a good 200 responseCode, DO NOT logoff till after export pdf or excel here from GUI side
				
		//logoff check ..... if user pressed CANCEL then no logon was done
		if (error_code != 7 && error_code != 1 && error_code != 2) {
			Boolean logoff_success = my_rest_caller.logoff();
			if (logoff_success)
				debugstr = String.format("Doc_Refresh_ScrollPane_Finished: error_code==%d  logoff OK", error_code);
			else
				debugstr = String.format("Doc_Refresh_ScrollPane_Finished: error_code==%d  logoff BAD !!!!!", error_code); //orphaned session in CMC till time limit expiration of session
			crashruncheck.println(debugstr);
		}
				
		if (error_code == 0)  { //successfully got parameters, enable the scroll pane wait for DO_REFRESH or CANCEL click
					
			parent_frame.SetEnableRecursive(this, true);
			parent_frame.revalidate();
			parent_frame.repaint();
					
			mywaitingframe.ChangeVisibility(null, null, null, false);
			return;
		}
		else if (error_code == 7) { //DO_REFRESH was success
					
			//logoff done after exporting pdf or excel file
					
			parent_frame.remove(this);
					
			parent_frame.revalidate();
			parent_frame.repaint();
					
			Object[] possibleValues = { "EXCEL", "PDF" };
			Object selectedValue = JOptionPane.showInputDialog(parent_frame, "Please choose EXCEL (.xlsx) or PDF (.pdf) file output", "Excel or PDF", JOptionPane.QUESTION_MESSAGE, null, possibleValues, possibleValues[0]);
					
			if (selectedValue != null) {
				if (selectedValue.equals("EXCEL"))
					Start_Exporting_Output_File(true);
				else
					Start_Exporting_Output_File(false);
			}
			else { //they pressed cancel they did not want output file
						
				//show infostore stuff
				parent_frame.add(InfostoreEntryPanel);
				parent_frame.add(infostore_button_panel);
						
				parent_frame.SetEnableRecursive(treeScrollPane, true);
						
				parent_frame.revalidate();
				parent_frame.repaint();
			}
			
			return;
		}
				
		//below 0 means mywaitingframe still showing and we can not show the DocumentRefresh_ScrollPane because we could not get parameters
		if (error_code < 0) {
			
			mywaitingframe.ChangeVisibility(null, null, null, false);
			if (error_code == -1)
				debugstr = "(code -1) Not building document refresh parameters scrollpane because of multiple-cardinality DateTime encountered ..... please take screenshot and email us that crashandrun text file dump!!!!!";
			else {
				debugstr = String.format("(code %d) Failed to get document refresh parameters!  Maybe could not get logon token (perhaps the server is down) or a bug in code ..... please take screenshot and email us that crashandrun text file dump!!!!!", error_code);
			}
			JOptionPane.showMessageDialog(parent_frame, debugstr, "ERROR", JOptionPane.ERROR_MESSAGE);
		}
		
		//dialog pop-up already showing		
		//1  == user pressed CANCEL
		//2  == user pressed DO_REFRESH but forgot to set all REQUIRED parameters ..... DO NOT CLOSE THIS DocumentRefresh_ScrollPane !!!!! KEEP IT OPEN !!!!! Technically this method will never get this code==2
		//3  == user pressed DO_REFRESH but code failed to create XML body string before making rest-api call to do Document Refresh
		//4  == user pressed DO_REFRESH but we failed to get logon token
		//5  == user pressed DO_REFRESH we logged in, we did rest-api call inside thread but got a non-200 responseCode
		//6  == user pressed DO_REFRESH we logged in, we did rest-api call inside thread but caught a throwable inside refresh_worker (2nd thread) !!!!! ..... Please take a screenshot and email us the crashruncheck text file dump

		if (error_code != 2) {
			parent_frame.remove(this);
					
			//show InfostoreEntryPanel, infostore_button_panel, treeScrollPane
			parent_frame.add(InfostoreEntryPanel);
			parent_frame.add(infostore_button_panel);
					
			parent_frame.SetEnableRecursive(treeScrollPane, true);
					
			parent_frame.revalidate();
			parent_frame.repaint();
		}
	}//Talk_To_Parent
	
	
	private boolean RebuildPanel(ArrayList<DocumentRefresh_Parameter> myparams) {
		
		documentRefreshPanel.removeAll();
		
		int gridsize = myparams.size() + 2; //GO and CANCEL buttons;
		documentRefreshPanel.setLayout(new GridLayout(gridsize, 1));
		
		chosen_list_params        = new List[thread_result.documentRefresh_SaveParameters.size()];
		chosen_text_params        = new List[thread_result.documentRefresh_SaveParameters.size()];
		param_date_buttons        = new Parameter_Date_Button[thread_result.documentRefresh_SaveParameters.size()];
		
		for (int ii=0; ii < myparams.size(); ii++) {
			
			if (myparams.get(ii).answer_type.equals("DateTime")) {
				boolean ok = Create_DateTime_Button(myparams.get(ii), ii);
				if (ok == false)
					return false;
			}
			else if (myparams.get(ii).is_text_button_parameter()) {
				String actionstr = String.format("TEXT_PARAMETER_%d", ii);
				Create_Text_Button(actionstr, myparams.get(ii), ii);
			}
			else {
				String actionstr = String.format("LIST_PARAMETER_%d", ii);
				Create_ListDialog_Button(actionstr, myparams.get(ii), ii);
			}
		}
		
		JButton do_refresh_button = new JButton("DO_REFRESH");
		do_refresh_button.setActionCommand("DO_REFRESH");
		do_refresh_button.addActionListener(this);
		documentRefreshPanel.add(do_refresh_button);
		
		JButton cancel_button = new JButton("CANCEL");
		cancel_button.setActionCommand("CANCEL");
		cancel_button.addActionListener(this);
		documentRefreshPanel.add(cancel_button);
		
		return true;
	}//RebuildPanel
	
	
	//returns: false = error such as wanting multiple_cardinality date picking
	//returns: true  = everything was ok
	private boolean Create_DateTime_Button(DocumentRefresh_Parameter param, int index) {
		boolean multiple_cardinality = false;
		if (param.info_cardinality.trim().toUpperCase().equals("MULTIPLE"))
			multiple_cardinality = true;
		
		//For now only single selection date ..... throw up JOptionPane if multiple selection wanted
		if (multiple_cardinality) {
			JOptionPane.showMessageDialog(parent_frame, "Multiple DateTime selections parameter encountered trying to refresh a document but we did not java code for this yet, PLEASE TAKE SCREENSHOT AND EMAIL US", "ERROR", JOptionPane.ERROR_MESSAGE);
			return false;
		}
			
		boolean optional_param = true;
		if (param.parameter_optional.trim().toUpperCase().equals("FALSE"))
			optional_param = false; //mandatory, necessary, required, or can not do refresh
		
		String labelstr = null;
		if (optional_param)
			labelstr = "(OPTIONAL) " + param.parameter_name;
		else
			labelstr = "(REQUIRED) " + param.parameter_name;
		
		//default values or not
		Parameter_Date_Button datebutton = null;
		if (param.info_values != null) { //just take 1st value as default value
			String temp = param.info_values.get(0);
			String[] strarray = temp.split("-");
			Integer year = Integer.parseInt(strarray[0]);
			Integer month = Integer.parseInt(strarray[1]);
			int Tindex = strarray[2].indexOf('T');
			Integer day = Integer.parseInt(strarray[2].substring(0, Tindex));
			
			//fix for date
			//month is 0-based not 1 based ..... JANUARY is defined as 0
			month--;
			
			GregorianCalendar cal = new GregorianCalendar(year, month, day);
			Date mydate = cal.getTime();
			
			datebutton = new Parameter_Date_Button(labelstr, crashruncheck, mydate);
		}
		else //no default values
			datebutton = new Parameter_Date_Button(labelstr, crashruncheck);
		
		param_date_buttons[index] = datebutton;
		documentRefreshPanel.add(datebutton);
		return true;
	}//Create_DateTime_Button
	
	
	private void Create_Text_Button(String actionstr, DocumentRefresh_Parameter param, int index) {

		boolean optional_param = true;
		if (param.parameter_optional.trim().toUpperCase().equals("FALSE"))
			optional_param = false; //mandatory, necessary, required, or can not do refresh
		
		String labelstr = null;
		if (optional_param)
			labelstr = "(OPTIONAL) " + param.parameter_name;
		else
			labelstr = "(REQUIRED) " + param.parameter_name;
		
		//default values checking
		String selections_str = "&nbsp;";
		if (param.info_lov_values != null) {
			
			//make copy
			ArrayList<String> mycopy = new ArrayList<String>();
			for (int ii=0; ii < param.info_lov_values.size(); ii++) {
				mycopy.add(param.info_lov_values.get(ii));
				selections_str += (param.info_lov_values.get(ii) + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			}
			
			chosen_text_params[index] = mycopy;
		}
		
		String tempbuttonstr = "<html><center>" + labelstr + "</center><br/><center style=\"color:Blue;\">" + selections_str + "</center></html>";
		JButton tempbutton = new JButton(tempbuttonstr);
		
		tempbutton.setActionCommand(actionstr);
		tempbutton.addActionListener(this);
		documentRefreshPanel.add(tempbutton);
	}//Create_Text_Button
	
	
	private void Create_ListDialog_Button(String actionstr, DocumentRefresh_Parameter param, int index) {
		
		boolean optional_param = true;
		if (param.parameter_optional.trim().toUpperCase().equals("FALSE"))
			optional_param = false; //mandatory, necessary, required, or can not do refresh
		
		String labelstr = null;
		if (optional_param)
			labelstr = "(OPTIONAL) " + param.parameter_name;
		else
			labelstr = "(REQUIRED) " + param.parameter_name;
		
		//default values checking
		String selections_str = "&nbsp;";
		if (param.answer_values != null) {
			
			//make copy
			ArrayList<String> mycopy = new ArrayList<String>();
			for (int ii=0; ii < param.answer_values.size(); ii++) {
				mycopy.add(param.answer_values.get(ii));
				selections_str += (param.answer_values.get(ii) + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			}
			
			chosen_list_params[index] = mycopy;
		}
		
		String tempbuttonstr = "<html><center>" + labelstr + "</center><br/><center style=\"color:Blue;\">" + selections_str + "</center></html>";
		JButton tempbutton = new JButton(tempbuttonstr);
		
		tempbutton.setActionCommand(actionstr);
		tempbutton.addActionListener(this);
		documentRefreshPanel.add(tempbutton);
	}//Create_ListDialog_Button
	
	
	
	
	
	private void List_Param_Click(String actstr, JButton mybutton) {
		String indexstr = actstr.replace("LIST_PARAMETER_", "");
		Integer   index = Integer.parseInt(indexstr);
		
		DocumentRefresh_Parameter param = thread_result.documentRefresh_SaveParameters.get(index);
		
		boolean is_single_select = true;
		if (param.info_cardinality.toUpperCase().trim().equals("MULTIPLE"))
			is_single_select = false;
		
		boolean optional_param = true;
		if (param.parameter_optional.trim().toUpperCase().equals("FALSE"))
			optional_param = false; //mandatory, necessary, required, or can not do refresh
		
		boolean intervals = false;
		if (param.info_lov_intervals != null)
			intervals = true;
		
		ArrayList<String> selections = Parameter_List_Dialog.showDialog(parent_frame, null, thread_result.list_param_labels[index], param.parameter_name, thread_result.list_param_choices[index], is_single_select);
		chosen_list_params[index] = selections;
		
		if (selections != null) {
			String debug_str = "";
			String selections_str = "";
			
			if (intervals) {
				selections_str = "(Too many to print here)";
				debug_str      = "(Too many to print here) (lov intervals values)";
			}
			else {
				for (int ii=0; ii < selections.size(); ii++) {
					selections_str += (selections.get(ii) + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
					debug_str      += (selections.get(ii) + "     ");
				}
			}
			
			if (optional_param)
				mybutton.setText("<html><center>(OPTIONAL)&nbsp;" + param.parameter_name + "</center><br/><center style=\"color:Blue;\">" + selections_str + "</center></html>");
			else
				mybutton.setText("<html><center>(REQUIRED)&nbsp;" + param.parameter_name + "</center><br/><center style=\"color:Blue;\">" + selections_str + "</center></html>");
			crashruncheck.println("List_Param_Click: " + param.parameter_name + " " + debug_str);
		}
		else {
			if (optional_param)
				mybutton.setText("<html><center>(OPTIONAL)&nbsp;" + param.parameter_name + "</center><br/>&nbsp;</html>");
			else
				mybutton.setText("<html><center>(REQUIRED)&nbsp;" + param.parameter_name + "</center><br/>&nbsp;</html>");
			crashruncheck.println("List_Param_Click: " + param.parameter_name + " null");
		}
	}//List_Param_Click
	
	
	public boolean  Check_Required_Parameters() {
		for (int ii=0; ii < thread_result.documentRefresh_SaveParameters.size(); ii++) {
			DocumentRefresh_Parameter param_definition = thread_result.documentRefresh_SaveParameters.get(ii);
			if (param_definition.parameter_optional.trim().toUpperCase().equals("FALSE")) {
				if (param_definition.answer_type.equals("DateTime")) { //class Parameter_Date_Button object
					Parameter_Date_Button datebutton = param_date_buttons[ii];
					if (datebutton == null)
						return false;
					else if (datebutton.getDateAsString().equals(""))
						return false;
				}
				else if (param_definition.is_text_button_parameter()) {
					//button that popped dialog for single text input
					if (chosen_text_params[ii] == null)
						return false;
					else if (chosen_text_params[ii].size() == 0)
						return false;
				}
				else {  //button that popped Parameter_List_Dialog object
					
					if (chosen_list_params[ii] == null)
						return false;
					else if (chosen_list_params[ii].size() == 0)
						return false;
				}
			}
		}
		return true;
	}//Check_Required_Parameters
	
	
	
	
	private void Start_Exporting_Output_File(boolean want_excel) {
		
		//*******************************************************************************
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
		//*********************************************************************************
		
		String tempfilename;
		String tempstr;
		if (want_excel) {
			tempstr = "Choose where to save your excel file";
			tempfilename = "my_output_file.xlsx";
		}
		else {
			tempstr = "Choose where to save your PDF file";
			tempfilename = "my_output_file.pdf";
		}
		
		FileDialog fd = new FileDialog(parent_frame, tempstr , FileDialog.SAVE);
		fd.setDirectory(System.getProperty("user.dir"));
		fd.setFile(tempfilename);
		fd.setMultipleMode(false);
		fd.setVisible(true);
		String filename  = fd.getFile();
		String directory = fd.getDirectory();
		if (filename == null)
			crashruncheck.println("Start_Exporting_Output_File: The user pressed cancelled in the FileDialog so no file export.");
		else {
		
			if (want_excel)
				mywaitingframe.ChangeVisibility("Please wait while your EXCEL file is being generated", "Your EXCEL file is being created, but we do not know how long it will take.", "This will close automatically when your file is finished being created.", true);
			else
				mywaitingframe.ChangeVisibility("Please wait while your PDF file is being generated", "Your PDF file is being created, but we do not know how long it will take.", "This will close automatically when your file is finished being created.", true);
		
			
			String tempoutputpathfilename = directory + filename;
						
			//********************
			//use thread
			//********************
			SwingWorker<RestAPIResponse, Void> export_worker = new SwingWorker<RestAPIResponse, Void>() {
				    @Override
				    public RestAPIResponse doInBackground() {
				    	crashruncheck.println("export_worker: tempoutputpathfilename: " + tempoutputpathfilename);
				    	
				    	//DO NOT do logon because we need to use same token after we did refresh document rest-api call
				    	
				    	RestAPIResponse response = my_rest_caller.Run_Report_And_Get_File(want_excel, mydocid, tempoutputpathfilename);
				    	return response;
				    }
	
				    @Override
				    public void done() {
				    	try {
			                RestAPIResponse endresult = get();
			                Finish_Exporting_Output_File(endresult);
			            }
			            catch (Throwable tt) {
			                crashruncheck.println("!!!!! export_worker: THROWABLE CAUGHT in class GUI_Frame: ", tt);
			            }
				    }
			};
				
			export_worker.execute();
		}
	}//Start_Exporting_Output_File
	
		
	private void Finish_Exporting_Output_File(RestAPIResponse endresult) {
		
		Boolean logoff_success = my_rest_caller.logoff();
		if (logoff_success)
			crashruncheck.println("Finish_Exporting_Output_File: logoff OK");
		else
			crashruncheck.println("Finish_Exporting_Output_File: logoff BAD !!!!!"); //orphaned session in CMC till time limit expiration of session
		
		mywaitingframe.ChangeVisibility(null, null, null, false);
		
		if (endresult.responseCode == 200)
        	JOptionPane.showMessageDialog(parent_frame, "Successfully generated document for id=" + mydocid, "Success", JOptionPane.PLAIN_MESSAGE);
		else if (endresult.responseCode == 500)
			JOptionPane.showMessageDialog(parent_frame, "<html><body><p style='width: 555px;'>Failed to generate document ..... SAP BI v4.1SP3 rest-api call to export document responseCode=500 means 'RESTful web service internal error - An unclassified error occurred. See the response body for more information' ..... but sometimes we do not get a response body back ..... This is an error in SAP BI web-service side in regards to exporting certain documents ..... Please take a screenshot and tell us which document you were trying to refresh and please email us back the debugging crash and run text file.</p></body></html>" ,"ERROR", JOptionPane.ERROR_MESSAGE);
        else
        	JOptionPane.showMessageDialog(parent_frame, "<html><body><p style='width: 555px;'>Failed to generate document!  Please check console window or debug text file for errors on why this happened and please email the debugging crash and run text file to us.</p></body></html>", "ERROR", JOptionPane.ERROR_MESSAGE);
		
		//show infostore stuff
		parent_frame.add(InfostoreEntryPanel);
		parent_frame.add(infostore_button_panel);
		
		parent_frame.SetEnableRecursive(treeScrollPane, true);
		
		parent_frame.revalidate();
		parent_frame.repaint();
	}//Finish_Exporting_Output_File


}//class DocumentRefresh_ScrollPane
