package sap_bi_restapi_package;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Component;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;


import java.util.ArrayList;

public class Search_ScrollPane extends JScrollPane implements ActionListener, ListSelectionListener {
	
	private CrashAndRunChecker			crashruncheck;
	private GUI_Frame               	parent_frame;
	private InfostoreTree_ScrollPane	tree_scroll;
	private Infostore_Button_Panel      infostore_button_panel;
	
	private JPanel					mainpanel;
	private JLabel					search_label;
	private JTextField              search_textfield;
	private JButton                 search_button;
	private JButton					close_button;
	
	private JScrollPane				list_scroll;
	private JList<String>           list;
	private int                     last_selected_index;
	private InfostoreTreeNode       last_selected_node;
	
	private int hoverIndex = -1;

	private class MyListCellRenderer extends JLabel implements ListCellRenderer {
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			String str = value.toString();
	        setText(str);
	        if (isSelected)
	        	setBackground(Color.red);
	        else {
	        	if (index == hoverIndex)
	        		setBackground(Color.green);
	        	else
	        		setBackground(Color.white);
	        }
	        setOpaque(true);
	        return this;
		}
	}

	
	public Search_ScrollPane(CrashAndRunChecker tempcrashruncheck,
							 GUI_Frame tempparentframe,
							 InfostoreTree_ScrollPane temptreescroll,
							 Infostore_Button_Panel tempbuttonpanel) {
		
		super();
		
		crashruncheck          = tempcrashruncheck;
		parent_frame           = tempparentframe;
		tree_scroll            = temptreescroll;
		infostore_button_panel = tempbuttonpanel;
		
		mainpanel = new JPanel(null);
		
		search_label = new JLabel("Please enter string to search the current tree for any nodes containing the string (case-insensitive search):");
		search_label.setBounds(10,10, 757, 20);
		mainpanel.add(search_label);
		
		search_textfield = new JTextField();
		search_textfield.setBounds(10, 50, 656, 20);
		mainpanel.add(search_textfield);
		
		search_button = new JButton("SEARCH");
		search_button.setActionCommand("SEARCH");
    	search_button.addActionListener(this);
		search_button.setBounds(670, 50, 250, 50);
		mainpanel.add(search_button);
		
		close_button = new JButton("<html><p>CLOSE ME AND REENABLE TREE</p></html>");
		close_button.setActionCommand("CLOSE");
		close_button.addActionListener(this);
		close_button.setBounds(670, 120, 250, 50);
		mainpanel.add(close_button);
		
		list_scroll = null;
		
		this.setViewportView(mainpanel);
		this.setViewportBorder(BorderFactory.createLineBorder(Color.black));
		this.setBounds(30, 550, 1000, 400);
		
		parent_frame.add(this);
		
		parent_frame.revalidate();
		parent_frame.repaint();
		
	}
	
	
	public void actionPerformed(ActionEvent ev) {
		
		Object myobj = ev.getSource();
		JButton mybutton = (JButton)myobj;
		String actstr = mybutton.getActionCommand();
		
		if (actstr.equals("SEARCH"))
			CreateList();
		else if (actstr.equals("CLOSE"))
			infostore_button_panel.Close_Search_ScrollPane(this, last_selected_node);
		
	}
	
	
	//when we instantiated list we set to SINGLE_SELECTION not multiple
	//Ok ..... when you mouse click, this event will be fired 2x, 1st time because a row gets unselected and 2nd time because a row gets selected.
	//But ..... when you you use keyboard up or down arrows after something is selected, this event only gets fired 1x.
	//So the solution is to force code execution to only execute 1x using variable last_selected_index
	public void valueChanged(ListSelectionEvent ev) {

		ListSelectionModel lsm = (ListSelectionModel)ev.getSource();		
		int anchor_index = lsm.getAnchorSelectionIndex();
		
		if (last_selected_index != anchor_index) {
			last_selected_index = anchor_index;
			
			String typepathstr = list.getSelectedValue();
			
			String debugstr = String.format("Search_ScrollPane: last_selected_index=%d typepathstr=%s", last_selected_index, typepathstr);
			crashruncheck.println(debugstr);
			
			last_selected_node = tree_scroll.Enforce_Set_Selection_Path(typepathstr);
			
		}
	}
	
	private void setHoverIndex(int index) {
		
		if (hoverIndex == index)
			return;
		
		hoverIndex = index;
		list.repaint();
	}
	
	private void CreateList() {
		
		if (list_scroll != null)
			mainpanel.remove(list_scroll);
		
		last_selected_index = -555;
		last_selected_node = null;
		
		String tempstr = search_textfield.getText();
		
		ArrayList<String> output = new ArrayList<String>();
		tree_scroll.recursive_search_string_non_case_sensitive(null, null, tempstr.trim().toLowerCase(), output);
		
		String[] data = output.toArray(new String[output.size()]);
		
		list = new JList<String>(data) {
            //Subclass JList to workaround bug 4832765, which can cause the
            //scroll pane to not let the user easily scroll up to the beginning
            //of the list.  An alternative would be to set the unitIncrement
            //of the JScrollBar to a fixed value. You wouldn't get the nice
            //aligned scrolling, but it should work.
            public int getScrollableUnitIncrement(Rectangle visibleRect,
                                                  int orientation,
                                                  int direction) {
                int row;
                if (orientation == SwingConstants.VERTICAL &&
                      direction < 0 && (row = getFirstVisibleIndex()) != -1) {
                    Rectangle r = getCellBounds(row, row);
                    if ((r.y == visibleRect.y) && (row != 0))  {
                        Point loc = r.getLocation();
                        loc.y--;
                        int prevIndex = locationToIndex(loc);
                        Rectangle prevR = getCellBounds(prevIndex, prevIndex);
 
                        if (prevR == null || prevR.y >= r.y) {
                            return 0;
                        }
                        return prevR.height;
                    }
                }
                return super.getScrollableUnitIncrement(visibleRect, orientation, direction);
            }
        };
 
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.getSelectionModel().addListSelectionListener(this);
        
        
        list.setCellRenderer(new MyListCellRenderer());
        
        list.addMouseListener(new MouseAdapter() {
        	public void mouseExited(MouseEvent me) {
        		setHoverIndex(-1);
        	}
        });
        
        list.addMouseMotionListener(new MouseAdapter() {
        	public void mouseMoved(MouseEvent me) {
        		Point pp = me.getPoint();
        		int cellindex = list.locationToIndex(pp);
        		Rectangle cellrect = list.getCellBounds(cellindex, cellindex);
        		if (cellrect.contains(pp))
        			setHoverIndex(cellindex);
        		else
        			setHoverIndex(-1);
        	}
        });
        
        
        list_scroll = new JScrollPane(list);
        list_scroll.setBounds(10, 90, 656, 300);
        
        mainpanel.add(list_scroll);
        
        parent_frame.revalidate();
        parent_frame.repaint();
	}

}//Search_ScrollPane
