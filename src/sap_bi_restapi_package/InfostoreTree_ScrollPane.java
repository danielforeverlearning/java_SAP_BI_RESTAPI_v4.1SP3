package sap_bi_restapi_package;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class InfostoreTree_ScrollPane extends JScrollPane implements TreeSelectionListener  {
	
	private InfostoreTreeNode top_node;
	private JTree mytree;
	private JPanel treepanel;
	
	private InfostoreTreeNode folder_report_node;
	
	private SAP_BI_WebIntel_REST_helper	my_rest_caller;
	private CrashAndRunChecker			crashruncheck;
	private GUI_Frame                   parent_frame;
	private Infostore_Button_Panel      infostore_button_panel;
	
	private String						my_tree_type;
	
	public InfostoreTree_ScrollPane(CrashAndRunChecker tempcrashruncheck,
									SAP_BI_WebIntel_REST_helper temprestcaller,
									GUI_Frame tempparentframe,
									Infostore_Button_Panel tempbuttonpanel) {
		super();
		
		crashruncheck			= tempcrashruncheck;
		my_rest_caller			= temprestcaller;
		parent_frame            = tempparentframe;
		infostore_button_panel  = tempbuttonpanel;
		
		//makes tree panel
		treepanel = new JPanel(new GridLayout(1,0));
		mytree = null;
						
		this.setViewportView(treepanel);
		this.setViewportBorder(BorderFactory.createLineBorder(Color.black));
		this.setBounds(30, 30, 1000, 500);
		parent_frame.add(this);
	}
	
	
	public void Make_Tree(String temptreetype, String[] filter_str_array) {
		
		my_tree_type = temptreetype;
		
		top_node = new InfostoreTreeNode();
		if (my_tree_type.equals("USERS_TREE"))
			top_node.name = "infostore";
		else if (my_tree_type.equals("ROOT_FOLDER_TREE"))
			top_node.name = "Root Folder";
						
		if (mytree != null)
			treepanel.remove(mytree);
		
		mytree = new JTree(top_node);
		mytree.setCellRenderer(new InfostoreTreeCellRenderer());
		treepanel.add(mytree);
		
		parent_frame.SetEnableRecursive(this, false);
			
		//****************************************************************************************************
		//Trying to prevent exceptions thrown due to array index bounds reading
		//by giving some time for the gui thread to wait for all the awt and swing events to finish 
		//is not working ..... perhaps it is this very thread that is throwing the exceptions
		//need a different work around.
		//(1) Thread.sleep(2000);     //THIS DID NOT WORK !!!!!
		//
		//https://docs.oracle.com/javase/tutorial/uiswing/concurrency/index.html
		//https://docs.oracle.com/javase/tutorial/uiswing/concurrency/index.html
		//
		//
		//(2) work around is to have this gui build the tree after rest-calls
		//AND THEN BELOW add the tree selection listener code below.
		//AGAIN THIS DID NOT WORK !!!!! Argh ..... ok keep reading docs :(
		//Getting thrown in between collapse all and expand root ..... it means the swing thread
		//not finished dispatching events from its eventqueue, it still aint dont.
		//
		//(3)try SwingWorker
		//https://docs.oracle.com/javase/tutorial/uiswing/concurrency/simple.html
		//https://docs.oracle.com/javase/tutorial/uiswing/concurrency/simple.html
		//
		//Ok this works solution is many-parts:
		//(a) use swing worker (separate thread) so "event dispatch thread" (which is what gui uses) does not get blocked.
		//(b) created class InfostoreTreeCellRenderer which extends DefaultTreeCellRenderer
		//    because as recursively build the tree adding kids to parents, the renderer gets messed up in display of text
		//    that is why we saw ... after some text displays.
		//*****************************************************************************************************
		SwingWorker<Boolean, Void> tree_worker = new SwingWorker<Boolean, Void>() {
		    @Override
		    public Boolean doInBackground() {
		    	Boolean result = my_rest_caller.logon();
		    	if (result) {
		    		if (my_tree_type.equals("USERS_TREE"))
		    			result = my_rest_caller.Make_Users_Tree(filter_str_array, mytree, top_node);
		    		else if (my_tree_type.equals("ROOT_FOLDER_TREE"))
		    			result = my_rest_caller.Root_Contains_Make_Infostore_Tree(filter_str_array, mytree, top_node);
		    	}
				return result;
		    }

		    @Override
		    public void done() {
		    	try {
	                Boolean endresultsuccess = get();
	                Finish_Making_Tree(endresultsuccess);
	            }
	            catch (Throwable tt) {
	                crashruncheck.println("!!!!! tree_worker: THROWABLE CAUGHT in class GUI_Frame: ", tt);
	            }
		    }
		};
		
		tree_worker.execute();
		
		Boolean is_swing_event_dispatch_thread =  javax.swing.SwingUtilities.isEventDispatchThread();
		if (is_swing_event_dispatch_thread)
			crashruncheck.println("TRUE == javax.swing.SwingUtilities.isEventDispatchThread");
		else
			crashruncheck.println("FALSE == javax.swing.SwingUtilities.isEventDispatchThread");
		
	}//Make_Tree
	
	private void Finish_Making_Tree(Boolean endresultsuccess) {
		
		Boolean logoff_success = my_rest_caller.logoff();
		if (logoff_success)
			crashruncheck.println("Finish_Making_Tree: logoff OK");
		else
			crashruncheck.println("Finish_Making_Tree: logoff BAD !!!!!"); //orphaned session in CMC till time limit expiration of session
		
		if (endresultsuccess) {
		
			crashruncheck.println("Finish_Making_Tree: endresultsuccess OK");
		
			//Listen for when the selection changes
			mytree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			mytree.addTreeSelectionListener(this);
			
			boolean checkscrollsonexpand = mytree.getScrollsOnExpand();
			if (checkscrollsonexpand)
				crashruncheck.println("Finish_Making_Tree: checkscrollsonexpand: TRUE");
			else
				crashruncheck.println("Finish_Making_Tree: checkscrollsonexpand: FALSE");
		
			Collapse_All_Just_Show_Root();
		
			parent_frame.SetEnableRecursive(this, true);
			
			infostore_button_panel.Set_Enabled_All_Buttons(true);
		}
		else {
			String errormsg = "Finish_Making_Tree: Failed to make rest-API calls over network or got a bad response code in a rest-API call (not 200) or some form of exception caught !!!!!";
			crashruncheck.println(errormsg);
			JOptionPane.showMessageDialog(this, errormsg, "ERROR", JOptionPane.ERROR_MESSAGE);
			parent_frame.dispatchEvent(new WindowEvent(parent_frame, WindowEvent.WINDOW_CLOSING));
			
		}
	}
	
	
	private void Collapse_All_Just_Show_Root() {
    	crashruncheck.println("FORCE COLLAPSE START");
		int rowcount = mytree.getRowCount();
		for (int rr = (rowcount - 1); rr >= 0;  rr--) {
			if (mytree.isExpanded(rr))
				mytree.collapseRow(rr);
		}
		crashruncheck.println("FORCE COLLAPSE END");
		mytree.expandRow(0);
    }
	
	
	public InfostoreTreeNode Enforce_Set_Selection_Path(String typepathstr) {
		
		Collapse_All_Just_Show_Root();
		
		DefaultTreeModel  model  = (DefaultTreeModel)mytree.getModel();
		InfostoreTreeNode mynode = top_node;
			
		int colonindex = typepathstr.indexOf(":");
		String mytype = typepathstr.substring(0, colonindex);
		String pathalone = typepathstr.substring(colonindex + 1).trim();
			
		String[] strarray = pathalone.split("/");
		
		if (strarray.length == 1) {
			TreePath mypath = new TreePath(top_node);
			mytree.setSelectionPath(mypath);
			return top_node; //Root Folder
		}
		
		int ii = 1;
		while (true) {
			int childcount = model.getChildCount(mynode);
			for (int cc=0; cc < childcount; cc++) {
				InfostoreTreeNode childnode = (InfostoreTreeNode)model.getChild(mynode, cc);
				if (childnode.name.equals(strarray[ii])) {
					mynode = childnode;
					break;
				}
			}
			
			if (ii == (strarray.length - 1))
				break;
			else
				ii++;
		}
		
		TreeNode[] treenodearray = mynode.getPath();
		TreePath mypath = new TreePath(treenodearray);
		mytree.setSelectionPath(mypath);
		mytree.scrollPathToVisible(mypath);
		return mynode;
	}//Enforce_Set_Selection_Path
	
	
	//TREE ACTIONS ONLY
	/** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent ev) {
        InfostoreTreeNode node = (InfostoreTreeNode) mytree.getLastSelectedPathComponent();
 
        if (node == null) return;
 
        if (node.isLeaf()) {
            crashruncheck.println("INFOSTORE_TREE: LEAF = " + node.name);
            infostore_button_panel.Show_Infostore_Stuff(node);
        } else {
            crashruncheck.println("INFOSTORE_TREE: NON-LEAF = " + node.name);
            infostore_button_panel.Show_Infostore_Stuff(node);
        }
        
        if (node.type != null) {
        	if (node.type.equals("Folder"))
        		folder_report_node = node;
        }
    }
    
    public InfostoreTreeNode  is_Node_Selected() {
    	InfostoreTreeNode node = (InfostoreTreeNode) mytree.getLastSelectedPathComponent();
    	return node;
    }
    
    
    public Integer Write_Folder_Report(String outputpathfilename) {
		
		PrintWriter mywriter;
		try {
			mywriter = new PrintWriter(outputpathfilename);
		}
		catch (Throwable tt) {
			crashruncheck.println("Write_Folder_Report: FAILED TO new PrintWriter !!!!!", tt);
			return -1;
		}
		
		//use JTree already built from rest-api calls, do not make fresh rest-api calls
		//folder_report_node set from JTree event listener
		try {
			DefaultTreeModel model = (DefaultTreeModel)mytree.getModel();
			TreeNode[] mypath = folder_report_node.getPath();
			
			String foldername = "";
			for (int ii=0; ii < mypath.length; ii++) {
				
				if (ii != 0)
					foldername += "/";
				
				InfostoreTreeNode tempnode = (InfostoreTreeNode)mypath[ii];
				foldername += tempnode.name;
			}
			mywriter.println("Folder Name: " + foldername);
			mywriter.println("ID:          " + folder_report_node.id);
			mywriter.println("CUID:        " + folder_report_node.cuid);
			mywriter.println("Reports:     ");
			mywriter.println("--------------------------------------------------------");
			
			mywriter.println(foldername);
			recursive_folder_report(model, folder_report_node, mywriter, foldername.length(), 0);
			
			mywriter.close();
			
			String debugstr = String.format("Write_Folder_Report: OK outputpathfilename=%s", outputpathfilename);
			crashruncheck.println(debugstr);
			return 0;
		}
		catch (Throwable tt) {
			crashruncheck.println("Write_Folder_Report: CAUGHT THROWABLE !!!!!", tt);
			return -2;
		}
	}//Write_Folder_Report
	
	
	private void recursive_folder_report(DefaultTreeModel model, InfostoreTreeNode folder_node, PrintWriter mywriter, int indentlength, int level)  {
		ArrayList<InfostoreTreeNode> printables = new ArrayList<InfostoreTreeNode>();
		ArrayList<InfostoreTreeNode> child_folders = new ArrayList<InfostoreTreeNode>();
		int childcount = model.getChildCount(folder_node);
		for (int cc=0; cc < childcount; cc++)  {
			InfostoreTreeNode childnode = (InfostoreTreeNode)model.getChild(folder_node, cc);
			if (childnode.type.equals("Folder"))
				child_folders.add(childnode);
			else if (childnode.type.equals("Folder") == false)
				printables.add(childnode);
		}
		
		//only print for this folder_level if you got printables
		if (printables.size() > 0) {
			
			//print folder 
			for (int indent=0; indent < indentlength; indent++)
				mywriter.print(" ");
			
			if (level == 0)
				mywriter.println("/");
			else {
				mywriter.print("/");
				mywriter.println(folder_node.name);
			}
			
			for (int pp=0; pp < printables.size(); pp++) {
				for (int indent=0; indent < indentlength; indent++)
					mywriter.print(" ");
				mywriter.println(printables.get(pp).type + ": " + printables.get(pp).name);
			}
			
			mywriter.println();
		}
			
		
		//recurse thru child folders
		for (int ii=0; ii < child_folders.size(); ii++) {
			if (level == 0)
				recursive_folder_report(model, child_folders.get(ii), mywriter, indentlength, level + 1);
			else
				recursive_folder_report(model, child_folders.get(ii), mywriter, indentlength + folder_node.name.length() + 1, level + 1);
		}
	}//recursive_folder_report
	
	
	public void recursive_search_string_non_case_sensitive(DefaultTreeModel model, InfostoreTreeNode folder_node, String searchstr, ArrayList<String> output)  {
		
		if (model == null || folder_node == null) { //start recursion
			model = (DefaultTreeModel)mytree.getModel();
			folder_node = top_node;
		}

		
		ArrayList<InfostoreTreeNode> child_folders = new ArrayList<InfostoreTreeNode>();
		int childcount = model.getChildCount(folder_node);
		for (int cc=0; cc < childcount; cc++) {
			InfostoreTreeNode childnode = (InfostoreTreeNode)model.getChild(folder_node, cc);
			
			if (childnode.type.equals("Folder"))
				child_folders.add(childnode);
			
			if (childnode.name.toLowerCase().contains(searchstr)) {
				TreeNode[] mypath = childnode.getPath();
				
				String pathname = "";
				for (int ii=0; ii < mypath.length; ii++) {
					
					if (ii != 0)
						pathname += "/";
					
					InfostoreTreeNode tempnode = (InfostoreTreeNode)mypath[ii];
					pathname += tempnode.name;
				}
				output.add(childnode.type + ": " + pathname);
			}
		}
		
		//recurse thru child folders
		for (int ii=0; ii < child_folders.size(); ii++)
			recursive_search_string_non_case_sensitive(model, child_folders.get(ii), searchstr, output);

	}//recursive_search_string_non_case_sensitive
	
	
	
	//******************************************************************************************************
	//This method assumes mytree is a JTree already filled with
	//top_node having a name called "infostore"
	//and under top_node are 2 nodes, the 1st node called "Users" and the 2nd node called "User Groups".
	//So we go down "User Groups" node trying to match ids
	//
	//Output:     null == not found
	//        non-null == found name String
	//******************************************************************************************************	
	public String Get_UserGroup_Name_From_ID(String usergroup_id_str) {
		DefaultTreeModel model = (DefaultTreeModel)mytree.getModel();
		int childcount = model.getChildCount(top_node);
		InfostoreTreeNode UserGroupsNode = null;
		for (int cc=0; cc < childcount; cc++) {
			InfostoreTreeNode childnode = (InfostoreTreeNode)model.getChild(top_node, cc);
			if (childnode.name.equals("User Groups")) {
				UserGroupsNode = childnode;
				break;
			}
		}
		
		childcount = model.getChildCount(UserGroupsNode);
		for (int cc=0; cc < childcount; cc++) {
			InfostoreTreeNode childnode = (InfostoreTreeNode)model.getChild(UserGroupsNode, cc);
			if (childnode.type.equals("UserGroup")) {
				if (childnode.id.equals(usergroup_id_str)) {
					return childnode.name;
				}
			}
		}
		
		return null;
	}//Get_UserGroup_Name_From_ID

}//InfostoreTree_ScrollPane

