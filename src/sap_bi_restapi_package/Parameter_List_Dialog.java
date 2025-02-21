/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package sap_bi_restapi_package;

import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import java.util.ArrayList;
import java.util.List;

 
public class Parameter_List_Dialog extends JDialog
                        implements ActionListener {
    private static Parameter_List_Dialog dialog;
    private static ArrayList<String> values;
    private JList<String> list;
 
    /**
     * Set up and show the dialog.  The first Component argument
     * determines which frame the dialog depends on; it should be
     * a component in the dialog's controlling frame. The second
     * Component argument should be null if you want the dialog
     * to come up with its left corner in the center of the screen;
     * otherwise, it should be the component on top of which the
     * dialog should appear.
     */
    public static ArrayList<String> showDialog(Component frameComp, Component locationComp,
    		                                   String labelText, String title, String[] possibleValues, boolean is_single_select) {
    	
    	values = null;
        Frame frame = JOptionPane.getFrameForComponent(frameComp);
        dialog = new Parameter_List_Dialog(frame, locationComp,
                                           labelText, title, possibleValues, is_single_select);
        dialog.setVisible(true);
        return values;
    }
 
    
 
    private Parameter_List_Dialog(Frame frame, Component locationComp,
								  String labelText, String title, String[] data, boolean is_single_select) {
        super(frame, title, true);
 
        //Create and initialize the buttons.
        JButton cancelButton = new JButton("CLEAR");
        cancelButton.setActionCommand("CLEAR");
        cancelButton.addActionListener(this);
        getRootPane().setDefaultButton(cancelButton);
        
        
        final JButton setButton = new JButton("SET");
        setButton.setActionCommand("SET");
        setButton.addActionListener(this);
        
 
        //main part of the dialog
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
 
        if (is_single_select)
        	list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        else
        	list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        
        //if (longValue != null) {
        //    list.setPrototypeCellValue(longValue); //get extra space
        //}
        
        
		//list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setLayoutOrientation(JList.VERTICAL);
		
        
		list.setVisibleRowCount(-1);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    setButton.doClick(); //emulate button click
                }
            }
        });
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(500, 500));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);
 
        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel(labelText);
        label.setLabelFor(list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 
        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);
 
        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(listPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);
 
        //Initialize values
        //list.setSelectedValue(initialValue, true);
        
        pack();
        setLocationRelativeTo(locationComp);
    }
 
    //Handle clicks on the Set and Cancel buttons.
    public void actionPerformed(ActionEvent e) {
        if ("SET".equals(e.getActionCommand())) {
        	List<String> templist = list.getSelectedValuesList();
        	if (templist.isEmpty())
        		Parameter_List_Dialog.values = null;
        	else
        		Parameter_List_Dialog.values = (ArrayList<String>)templist;
        }
        else
        	Parameter_List_Dialog.values = null;
        
        Parameter_List_Dialog.dialog.setVisible(false);
    }
}//class