

package sap_bi_restapi_package;


import java.awt.Color;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import java.util.ArrayList;


public class InfostoreEntry_Panel extends JPanel implements ActionListener {
	
	private CrashAndRunChecker			 crashruncheck;
	private SAP_BI_WebIntel_REST_helper  my_rest_caller;
	private GUI_Frame					 parent_frame;
	private WaitingFrame				 mywaitingframe;
	private InfostoreTree_ScrollPane	 treeScrollPane;
	private Infostore_Button_Panel       infostore_button_panel;
	
	private JLabel author_name_value;
	private JLabel author_uri_value;
	private JLabel link_value;
	private JLabel id_value;
	private JLabel cuid_value;
	private JLabel description_value;
	private JLabel name_value;
	private JLabel type_value;
	
	//make folder report stuff or refresh document stuff or show usergroups
	private JButton   folder_report_button;
	private JButton   refresh_document_button;
	private JButton   show_usergroups_button;
	
	private String    outputpathfilename;
	
	
	public InfostoreEntry_Panel(CrashAndRunChecker tempcrashruncheck,
								SAP_BI_WebIntel_REST_helper temprest,
			                    GUI_Frame tempguiframe,
			                    WaitingFrame tempwaitframe,
			                    InfostoreTree_ScrollPane temptreescrollpane,
			                    Infostore_Button_Panel tempbuttons) {
		
		super(null);
		
		crashruncheck          = tempcrashruncheck;
		my_rest_caller         = temprest; 
		parent_frame           = tempguiframe;
		mywaitingframe         = tempwaitframe;
		treeScrollPane         = temptreescrollpane;
		infostore_button_panel = tempbuttons;
		
    	this.setBorder(BorderFactory.createLineBorder(Color.black));
    	this.setBounds(30, 650, 1000, 300);
    	
    	int WIDTH_LABEL  = 100;
    	int WIDTH_VALUE  = 600;
    	int WIDTH_BUFFER = 5;
    	int HEIGHT = 20;
    	int HEIGHT_BUFFER = 5;
    	
    	int START_XX = 10;
    	int START_YY = 10;
    	
    	int xx = START_XX;
    	int yy = START_YY;
    	
    	//***** first column *****
    	
    	JLabel author_name_label = new JLabel("Author Name:");
    	author_name_label.setBounds(xx, yy, WIDTH_LABEL, HEIGHT);
    	this.add(author_name_label);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	JLabel author_uri_label  = new JLabel("Author URI:");
    	author_uri_label.setBounds(xx, yy, WIDTH_LABEL, HEIGHT);
    	this.add(author_uri_label);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	JLabel link_label = new JLabel("Link:");
    	link_label.setBounds(xx, yy, WIDTH_LABEL, HEIGHT);
    	this.add(link_label);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	JLabel id_label = new JLabel("ID:");
    	id_label.setBounds(xx, yy, WIDTH_LABEL, HEIGHT);
    	this.add(id_label);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	JLabel cuid_label = new JLabel("CUID:");
    	cuid_label.setBounds(xx, yy, WIDTH_LABEL, HEIGHT);
    	this.add(cuid_label);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	JLabel description_label = new JLabel("Description:");
    	description_label.setBounds(xx, yy, WIDTH_LABEL, HEIGHT);
    	this.add(description_label);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	JLabel name_label = new JLabel("Name:");
    	name_label.setBounds(xx, yy, WIDTH_LABEL, HEIGHT);
    	this.add(name_label);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	JLabel type_label = new JLabel("Type:");
    	type_label.setBounds(xx, yy, WIDTH_LABEL, HEIGHT);
    	this.add(type_label);
    	
    	
    	
    	//***** second column *****
    	
    	xx = START_XX + WIDTH_LABEL + WIDTH_BUFFER;
    	yy = START_YY;
    	
    	author_name_value = new JLabel("");
    	author_name_value.setBounds(xx, yy, WIDTH_VALUE, HEIGHT);
    	this.add(author_name_value);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	author_uri_value  = new JLabel("");
    	author_uri_value.setBounds(xx, yy, WIDTH_VALUE, HEIGHT);
    	this.add(author_uri_value);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	link_value        = new JLabel("");
    	link_value.setBounds(xx, yy, WIDTH_VALUE, HEIGHT);
    	this.add(link_value);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	id_value          = new JLabel("");
    	id_value.setBounds(xx, yy, WIDTH_VALUE, HEIGHT);
    	this.add(id_value);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	cuid_value        = new JLabel("");
    	cuid_value.setBounds(xx, yy, WIDTH_VALUE, HEIGHT);
    	this.add(cuid_value);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	description_value = new JLabel("");
    	description_value.setBounds(xx, yy, WIDTH_VALUE, HEIGHT);
    	this.add(description_value);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	name_value        = new JLabel("");
    	name_value.setBounds(xx, yy, WIDTH_VALUE, HEIGHT);
    	this.add(name_value);
    	
    	yy += (HEIGHT + HEIGHT_BUFFER);
    	
    	type_value        = new JLabel("");
    	type_value.setBounds(xx, yy, WIDTH_VALUE, HEIGHT);
    	this.add(type_value);
    	
    	
    	//***** buttons *****
    	/***** I do not think we need this anymore ..... maybe too much clutter on the screen
    	JButton closebutton = new JButton("CLOSE ME");
    	closebutton.setActionCommand("CLOSE");
    	closebutton.addActionListener(this);
    	closebutton.setBounds(xx + WIDTH_VALUE, START_YY, 250, 50);
    	this.add(closebutton);
    	*****/
    	
    	folder_report_button = new JButton("MAKE FOLDER REPORT");
    	folder_report_button.setActionCommand("MAKE_FOLDER_REPORT");
    	folder_report_button.addActionListener(this);
    	folder_report_button.setBounds(xx + WIDTH_VALUE, START_YY + 165, 250, 50);
    	
    	refresh_document_button = new JButton("<html><p>REFRESH DOCUMENT</p></html>");
    	refresh_document_button.setActionCommand("REFRESH_DOCUMENT");
    	refresh_document_button.addActionListener(this);
    	refresh_document_button.setBounds(xx + WIDTH_VALUE, START_YY + 165, 250, 50);
    	
    	show_usergroups_button = new JButton("<html><p>SHOW USER GROUPS BELONGED TO</p></html>");
    	show_usergroups_button.setActionCommand("SHOW_USERGROUPS");
    	show_usergroups_button.addActionListener(this);
    	show_usergroups_button.setBounds(xx + WIDTH_VALUE, START_YY + 165, 250, 50);
    }
	
	
	//button actions
	public void actionPerformed(ActionEvent ev) {
		
		Object myobj = ev.getSource();
		JButton mybutton = (JButton)myobj;
		String actstr = mybutton.getActionCommand();
		
		if (actstr.equals("MAKE_FOLDER_REPORT")) //MAKE FOLDER REPORT button in InfostoreEntryPanel
			Start_Folder_Report();
		else if (actstr.equals("REFRESH_DOCUMENT")) {  //REFRESH DOCUMENT button in InfostoreEntryPanel
			
			//***** document refresh stuff *****
			DocumentRefresh_ScrollPane documentRefreshScrollPane = new DocumentRefresh_ScrollPane(crashruncheck, my_rest_caller, parent_frame, mywaitingframe, treeScrollPane, infostore_button_panel, this);
			String docidstr = id_value.getText();
		
			mywaitingframe.ChangeVisibility("Please wait while the refresh document parameters are being retrieved", "Rest-api calls are being made, but we do not know how long it will take.", "This will close automatically when the parameters have been retrieved.", true);
			
			parent_frame.SetEnableRecursive(treeScrollPane, false);
			
			parent_frame.remove(this);
			parent_frame.remove(infostore_button_panel);
		
			parent_frame.add(documentRefreshScrollPane);
			parent_frame.SetEnableRecursive(documentRefreshScrollPane, false);
			
			crashruncheck.println("click REFRESH_DOCUMENT docidstr=" + docidstr);
			documentRefreshScrollPane.Start_1st_Thread(docidstr);
		}
		else if (actstr.equals("SHOW_USERGROUPS")) { //SHOW USERGROUPS BELONGED TO button in InfostoreEntryPanel
			String user_id_str = id_value.getText();
			Start_Show_UserGroups(user_id_str);
			
		}
		/***** I do not think we need this anymore maybe it just does not look good and too much clutter
		else if (actstr.equals("CLOSE")) { //CLOSE ME button in InfostoreEntryPanel
			
			this.remove(refresh_document_button);
	    	this.remove(folder_report_button);
	    	parent_frame.remove(this);
	    	
	    	parent_frame.revalidate();
	    	parent_frame.repaint();

		}
		*****/
			
	}//actionPerformed

    
    public void Show_Me(InfostoreTreeNode mynode) {
    	if (mynode != null) {
	    	if (mynode.type != null) { //root node has a type that is null currently
	    		
	    		author_name_value.setText(mynode.author_name);
	        	author_uri_value.setText(mynode.author_uri);
	        	link_value.setText(mynode.link);
	        	id_value.setText(mynode.id);
	        	cuid_value.setText(mynode.cuid);
	        	description_value.setText(mynode.description);
	        	name_value.setText(mynode.name);
	        	type_value.setText(mynode.type);
	    		
		    	if (mynode.type.equals("Webi")) {
		    		this.remove(show_usergroups_button);
		    		this.remove(folder_report_button);
		    		this.add(refresh_document_button);
		    	}
		    	else if (mynode.type.equals("Folder")) {
		    		this.remove(show_usergroups_button);
		    		this.remove(refresh_document_button);
		    		this.add(folder_report_button);
		    	}
		    	else if (mynode.type.equals("User")) {
		    		this.remove(folder_report_button);
		    		this.remove(refresh_document_button);
		    		this.add(show_usergroups_button);
		    	}
		    	else { //shortcut, hyperlink, .....
		    		this.remove(folder_report_button);
		    		this.remove(refresh_document_button);
		    		this.remove(show_usergroups_button);
		    	}
	    	}
	    	else { //assume root node
	    		this.remove(folder_report_button);
	    		this.remove(refresh_document_button);
	    		this.remove(show_usergroups_button);
	    		
	    		author_name_value.setText("");
	        	author_uri_value.setText("");
	        	link_value.setText("");
	        	id_value.setText("");
	        	cuid_value.setText("");
	        	description_value.setText("");
	        	name_value.setText("");
	        	type_value.setText("");
	    	}
    	}
    	else { //assume root node
    		this.remove(folder_report_button);
    		this.remove(refresh_document_button);
    		this.remove(show_usergroups_button);
    		
    		author_name_value.setText("");
        	author_uri_value.setText("");
        	link_value.setText("");
        	id_value.setText("");
        	cuid_value.setText("");
        	description_value.setText("");
        	name_value.setText("");
        	type_value.setText("");
    	}
    	
    	parent_frame.add(this);
    	
    	parent_frame.revalidate();
    	parent_frame.repaint();
    }
    
    
    private void Start_Folder_Report() {
		
		String tempstr = "Choose where to save your folder report text file";
		String tempfilename = "my_folder_report.txt";
		
		FileDialog fd = new FileDialog(parent_frame, tempstr , FileDialog.SAVE);
		fd.setDirectory(System.getProperty("user.dir"));
		fd.setFile(tempfilename);
		fd.setMultipleMode(false);
		fd.setVisible(true);
		String filename  = fd.getFile();
		String directory = fd.getDirectory();
		if (filename == null)
			crashruncheck.println("Start_Folder_Report: The user pressed cancelled in the FileDialog so no folder report created.");
		else {
			outputpathfilename = directory + filename;
			
			parent_frame.SetEnableRecursive(treeScrollPane, false);
			parent_frame.SetEnableRecursive(this, false);
			
			mywaitingframe.ChangeVisibility("Please wait while your folder report text file is being generated", "Your folder report text file is being created, but we do not know how long it will take.", "This will close automatically when your file is finished being created.", true);
			
			//********************
			//use thread
			//********************
			SwingWorker<Integer, Void> make_folder_report_worker = new SwingWorker<Integer, Void>() {
				    @Override
				    public Integer doInBackground() {
				    	
				    	//use JTree already built from rest-api calls, do not make fresh rest-api calls
				    	crashruncheck.println("make_folder_report_worker: outputpathfilename: " + outputpathfilename);

				    	
				    	Integer result = treeScrollPane.Write_Folder_Report(outputpathfilename);
				    	return result;
				    }
	
				    @Override
				    public void done() {
				    	try {
			                Integer endresult = get();
			                Finish_Writing_Folder_Report(endresult);
			                
			            }
			            catch (Throwable tt) {
			                crashruncheck.println("!!!!! make_folder_report_worker: THROWABLE CAUGHT in class InfostoreEntry_Panel: ", tt);
			            }
				    }
			};
				
			make_folder_report_worker.execute();
		}
		
	}//Start_Folder_Report
	
	
	private void Finish_Writing_Folder_Report(int endresult) {
				
		parent_frame.SetEnableRecursive(treeScrollPane, true);
		parent_frame.SetEnableRecursive(this, true);
		
		mywaitingframe.ChangeVisibility(null, null, null, false);
		
		if (endresult == 0)
        	JOptionPane.showMessageDialog(this, "Successfully generated folder report file to " + outputpathfilename, "Success", JOptionPane.PLAIN_MESSAGE);
		else if (endresult == -1)
			JOptionPane.showMessageDialog(this, "Failed to generate folder report file!  Could not create output file!  Please check console window or debug output text file for errors on why this happened and email it to us please.", "ERROR", JOptionPane.ERROR_MESSAGE);
        else if (endresult == -2)
        	JOptionPane.showMessageDialog(this, "Failed to generate folder report file!  Please check console window or debug output text file for errors on why this happened and email it to us please.", "ERROR", JOptionPane.ERROR_MESSAGE);
        else
        	JOptionPane.showMessageDialog(this, "VERY STRANGE RETURN CODE IN WRITING FOLDER REPORT FROM THREAD PLEASE TAKE SCREENSHOT AND EMAIL US", "ERROR", JOptionPane.ERROR_MESSAGE);
	}//Finish_Writing_Folder_Report
	
	
	
	
	
	
	private void Start_Show_UserGroups(String user_id_str) {
			
		parent_frame.SetEnableRecursive(treeScrollPane, false);
		parent_frame.SetEnableRecursive(this, false);
			
		mywaitingframe.ChangeVisibility("Please wait while we make a rest-api call to find USERGROUPS this USER belongs to.", "We do not know how long this rest-api call will take.", "This will close automatically.", true);
			
		//********************
		//use thread
		//********************
		SwingWorker<RestAPIResponse, Void> usergroups_worker = new SwingWorker<RestAPIResponse, Void>() {
			@Override
			public RestAPIResponse doInBackground() {
				Boolean token_success = my_rest_caller.logon();
				if (token_success) {
					RestAPIResponse response = my_rest_caller.Get_Relationship(user_id_str, "userGroups", null, true);
					my_rest_caller.logoff();
					return response;
				}
				else
					return null; //failed to rest-api logon and get token
			}
			
			@Override
			public void done() {
				try {
					RestAPIResponse endresult = get();
					Finish_Show_UserGroups(endresult);
				}
				catch (Throwable tt) {
					crashruncheck.println("!!!!! usergroups_worker: THROWABLE CAUGHT in class InfostoreEntry_Panel: ", tt);
				}
			}
		};
				
		usergroups_worker.execute();
		
	}//Start_Show_UserGroups
	
	
	private void Finish_Show_UserGroups(RestAPIResponse endresult) {
		
		mywaitingframe.ChangeVisibility(null, null, null, false);
		
		if (endresult == null)
			JOptionPane.showMessageDialog(this, "Failed to get USERGROUPS this user belongs to!  Is the server or internet down ?!?!?", "ERROR", JOptionPane.ERROR_MESSAGE);
		else if (endresult.responseCode != 200)
			JOptionPane.showMessageDialog(this, "Failed to get USERGROUPS for this user because the responseCode is NOT 200 ..... This is strange, perhaps a bad userid was given, please take a screenshot and email us the debugging output text file.", "ERROR", JOptionPane.ERROR_MESSAGE);
		else {
			XML_helper xmlhelp = new XML_helper(crashruncheck);
			ArrayList<String> usergroup_id_list = xmlhelp.Parse_UserGroup_Entry_Elements(endresult.response);
			if (usergroup_id_list == null)
				JOptionPane.showMessageDialog(this, "Failed to XML parse USERGROUP IDs for this user ..... This is very strange, please take a screenshot and email us the debugging output text file.", "ERROR", JOptionPane.ERROR_MESSAGE);
			else {
				int length = usergroup_id_list.size();
				if (length > 0) {
					String temp_message_str = "<html><body><p>This USER belongs to the following groups:</p>";
					for (int ii=0; ii < usergroup_id_list.size(); ii++) {
						String usergroup_id_str = usergroup_id_list.get(ii);
						String usergroup_name = treeScrollPane.Get_UserGroup_Name_From_ID(usergroup_id_str);
						if (usergroup_name != null)
							temp_message_str += String.format("<p>%s:%s</p>", usergroup_id_str, usergroup_name);
						else
							temp_message_str += String.format("<p>%s:(Could not find the name of this USERGROUP ID)</p>", usergroup_id_str);
					}
					temp_message_str += "</body></html>";
					JOptionPane.showMessageDialog(this, temp_message_str, "Success", JOptionPane.PLAIN_MESSAGE);
				}
				else
					JOptionPane.showMessageDialog(this, "This USER does not belong to any USERGROUPS.", "Success", JOptionPane.PLAIN_MESSAGE);
			}
		}
		
		parent_frame.SetEnableRecursive(treeScrollPane, true);
		parent_frame.SetEnableRecursive(this, true);
	}//Finish_Show_UserGroups
}//InfostoreEntry_Panel
