package sap_bi_restapi_package;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Infostore_Button_Panel extends JPanel implements ActionListener {
	
	private CrashAndRunChecker			crashruncheck;
	private SAP_BI_WebIntel_REST_helper my_rest_caller;
	private GUI_Frame					parent_frame;
	private WaitingFrame				mywaitingframe;
	
	private InfostoreTree_ScrollPane	treeScrollPane;
	private InfostoreEntry_Panel		InfostoreEntryPanel;
	
	private JButton						search_tree_button;
	private JButton                     rebuild_tree_users_button;
	private JButton                     rebuild_tree_rootfolder_button;
	
	public Infostore_Button_Panel(CrashAndRunChecker tempcrashruncheck,
								  SAP_BI_WebIntel_REST_helper temprest,
			                      GUI_Frame tempguiframe,
			                      WaitingFrame tempwaitframe) { 
		
		super(null);
		
		crashruncheck  = tempcrashruncheck;
		my_rest_caller = temprest;
		parent_frame   = tempguiframe;
		mywaitingframe = tempwaitframe;
		
		search_tree_button = new JButton("<html><p>SEARCH TREE</p></html>");
		search_tree_button.setActionCommand("SEARCH_TREE");
		search_tree_button.addActionListener(this);
		search_tree_button.setBounds(10, 10, 320, 80);
		search_tree_button.setEnabled(false);
		this.add(search_tree_button);
		
		rebuild_tree_users_button = new JButton("<html><p>REBUILD USERS TREE</p></html>");
		rebuild_tree_users_button.setActionCommand("REBUILD_USERS_TREE");
		rebuild_tree_users_button.addActionListener(this);
		rebuild_tree_users_button.setBounds(340, 10, 320, 80);
		rebuild_tree_users_button.setEnabled(true);
		this.add(rebuild_tree_users_button);
		
		rebuild_tree_rootfolder_button = new JButton("<html><p>REBUILD ROOT FOLDER TREE</p></html>");
		rebuild_tree_rootfolder_button.setActionCommand("REBUILD_ROOT_FOLDER_TREE");
		rebuild_tree_rootfolder_button.addActionListener(this);
		rebuild_tree_rootfolder_button.setBounds(670, 10, 320, 80);
		rebuild_tree_rootfolder_button.setEnabled(true);
		this.add(rebuild_tree_rootfolder_button);
		
		this.setBorder(BorderFactory.createLineBorder(Color.black));
    	this.setBounds(30, 540, 1000, 100);
    	
    	parent_frame.add(this);
    	
    	//***** instantiate treeScrollPane and InfostoreEntryPanel *****
    	treeScrollPane = new InfostoreTree_ScrollPane(crashruncheck, 
    			                                     my_rest_caller,
    			                                     parent_frame,
    			                                     this);
    	
    	InfostoreEntryPanel = new InfostoreEntry_Panel(crashruncheck,
    												   my_rest_caller,
    												   parent_frame,
    												   mywaitingframe,
    												   treeScrollPane,
    												   this);
		
	}
	
	
	//button actions
	public void actionPerformed(ActionEvent ev) {
		
		Object myobj = ev.getSource();
		JButton mybutton = (JButton)myobj;
		String actstr = mybutton.getActionCommand();
		
		if (actstr.equals("SEARCH_TREE")) {
			
			parent_frame.remove(this);
			parent_frame.remove(InfostoreEntryPanel);
			parent_frame.SetEnableRecursive(treeScrollPane, false);
			
			
			Search_ScrollPane search_scrollpane = new Search_ScrollPane(crashruncheck, parent_frame, treeScrollPane, this);
		}
		else if (actstr.equals("REBUILD_USERS_TREE")) {
			parent_frame.remove(InfostoreEntryPanel);
			Set_Enabled_All_Buttons(false);
			parent_frame.revalidate();
			parent_frame.repaint();
		
			String tempstr = (String)JOptionPane.showInputDialog(parent_frame, "Please enter strings to filter from USERS folder separated by commas (case-insensitive search), or * for everything under USERS folder:",
														   "REBUILD USERS TREE", JOptionPane.PLAIN_MESSAGE,
														   null, null, "john,doe,mary,smith");
			String[] filter_str_array = null;
			if (tempstr == null) { //they pressed cancel
				InfostoreTreeNode node = treeScrollPane.is_Node_Selected();
				if (node != null)
					parent_frame.add(InfostoreEntryPanel);
				
				Set_Enabled_All_Buttons(true);
				parent_frame.revalidate();
				parent_frame.repaint();
				return;
			}
			else if (tempstr.length() == 0) //get everything
				filter_str_array = new String[] { "*" };
			else
				filter_str_array = tempstr.split(",");
			
			treeScrollPane.Make_Tree("USERS_TREE", filter_str_array);
			
		}
		else if (actstr.equals("REBUILD_ROOT_FOLDER_TREE")) {
			parent_frame.remove(InfostoreEntryPanel);
			Set_Enabled_All_Buttons(false);
			parent_frame.revalidate();
			parent_frame.repaint();
			
			String tempstr = (String)JOptionPane.showInputDialog(parent_frame, "Please enter strings to filter from ROOT FOLDER separated by commas (case-sensitive search), or * for everything under ROOT FOLDER:",
					   "REBUILD ROOT FOLDER TREE", JOptionPane.PLAIN_MESSAGE,
					   null, null, "ABS,HRM,FIN");
			
			String[] filter_str_array = null;
			if (tempstr == null) { //they pressed cancel
				InfostoreTreeNode node = treeScrollPane.is_Node_Selected();
				if (node != null)
					parent_frame.add(InfostoreEntryPanel);
				
				Set_Enabled_All_Buttons(true);
				parent_frame.revalidate();
				parent_frame.repaint();
				return;
			}
			else if (tempstr.length() == 0) //get everything
				filter_str_array = new String[] { "*" };
			else
				filter_str_array = tempstr.split(",");
			
			treeScrollPane.Make_Tree("ROOT_FOLDER_TREE", filter_str_array);
		}
	}//actionPerformed
	
	public void Set_Enabled_All_Buttons(boolean enable) {
		this.search_tree_button.setEnabled(enable);
		this.rebuild_tree_users_button.setEnabled(enable);
		this.rebuild_tree_rootfolder_button.setEnabled(enable);
	}
	
	public void Show_Infostore_Stuff(InfostoreTreeNode node) {
		InfostoreEntryPanel.Show_Me(node);
		Set_Enabled_All_Buttons(true);
	}
	
	public void Close_Search_ScrollPane(Search_ScrollPane search_scroll, InfostoreTreeNode node) {
		parent_frame.remove(search_scroll);
		parent_frame.SetEnableRecursive(treeScrollPane, true);
		InfostoreEntryPanel.Show_Me(node);
		
		//infostore_button_panel already exists because it has the search tree button which was just clicked ..... it is not null
		parent_frame.add(this);
		
		parent_frame.revalidate();
		parent_frame.repaint();
	}
	
}//class

