package sap_bi_restapi_package;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;;

//***************************************************************************
//October 21, 2019
//We created this class because sometimes when the tree is made,
//some of the very long names of a node in the tree are displayed improperly,
//instead of seeing its long name, you will see the first part of its name
//followed by ...
//This happens because after we make a infostore rest-api call and we get 
//the XML list of entries, we add nodes to the tree.
//When we add a node to its parent node, the parent node needs to get expanded.
//
//The getPathForRow method will often now work the 1st time getTreeCellRendererComponent is called,
//so the renderer does not generate the text for the rendered object correctly.
//The component's size is determined based on the incorrect text, and this sometimes means the
//final (correct) text of the component gets cut off.
//
//The parent node needs to load its children from the treeModel and update
//their preferred sizes if this node has not previously been expanded.
//To update the preferred size of the node it is necessary to get its cell renderer.
//So, the 1st time the node has expanded
//the TreeCellRenderer::getTreeCellRendererComponent()
//is called 2x for each child of the parent node:
//(1)To get child's preferred size
//(2)To insert the child into the vector of visible nodes.
//Note: the node's preferred size should be updated before it becomes visible!
//(See javax.swing.tree.VariableHeightLayoutCache.TreeStateNode.expand())
//The doc says that JTree::getPathForRow(int row) method will return null if the row is not visible.
//
//https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4433885
//https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4433885
//
//So the workaround is to make this class which extends DefaultTreeCellRenderer
//and override getTreeCellRendererComponent.
//We just call the normal getTreeCellRendererComponent from DefaultTreeCellRenderer
//and then do setText again.

public class InfostoreTreeCellRenderer extends DefaultTreeCellRenderer {
	
	private ImageIcon closed_folder_icon;
	
	public InfostoreTreeCellRenderer() {
		super();
		
		closed_folder_icon = (ImageIcon)getClosedIcon();
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		
		try {
			
			super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
			
			String tempstr = value.toString();
			
			if (leaf) {
				if (value != null) {
					InfostoreTreeNode mynode = (InfostoreTreeNode) value;
					if (mynode != null) {
						if (mynode.type != null) {
							if (mynode.type.equals("Folder"))
								setIcon(closed_folder_icon);
						}
					}
				}
			}
			
			setText(tempstr);
		}
		catch (Throwable t) {
			System.out.println("InfostoreTreeCellRenderer: CAUGHT THROWABLE !!!!!");
			t.printStackTrace();
			
			String tempstr = value.toString();
			
			if (leaf) {
				if (value != null) {
					InfostoreTreeNode mynode = (InfostoreTreeNode) value;
					if (mynode != null) {
						if (mynode.type != null) {
							if (mynode.type.equals("Folder"))
								setIcon(closed_folder_icon);
						}
					}
				}
			}
			
			setText(tempstr);
		}
		return this;
	}

}
