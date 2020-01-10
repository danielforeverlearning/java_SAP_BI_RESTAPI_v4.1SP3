package sap_bi_restapi_package;

import javax.swing.tree.DefaultMutableTreeNode;

public class InfostoreTreeNode extends DefaultMutableTreeNode {
	String author_name;
	String author_uri;
	
	String link;    //If entry is type=="Folder", this takes you to its sub-folders
	                //If entry is type=="Webi", this takes you to its report, you can run the query or whatever
	
	String id;
	String cuid;
	String description;
	String name;
	String type;	//Folder or Webi
	
	public InfostoreTreeNode() {
		super();
	}
	
	@Override
	public String toString() {
		return name;
	}
}
