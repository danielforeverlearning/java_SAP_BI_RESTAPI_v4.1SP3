package sap_bi_restapi_package;

import java.net.URL;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;


public class WaitingFrame extends JFrame implements ActionListener {
    private JPanel contentPane;
    
    private JLabel headerLabel;
    private JLabel headerLabel2;
    
    private JLabel imageLabel;
	private JLabel imageLabel2;
	private JLabel imageLabel3;
	private CrashAndRunChecker crashruncheck;
    
    public WaitingFrame(CrashAndRunChecker tempcrashruncheck) {
    	
        	super();
        	
        	crashruncheck = tempcrashruncheck;
        	
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			setBackground(Color.orange);
			
			setSize(new Dimension(700, 130));
			setLocationRelativeTo(null);
            setVisible(false);
			
            contentPane = (JPanel) getContentPane();
            contentPane.setBackground(Color.orange);
            contentPane.setLayout(null);
			
			
			headerLabel = new JLabel();
			headerLabel.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 16));
			headerLabel.setBounds(75, 10, 600, 20);
			contentPane.add(headerLabel);
			
            
			headerLabel2 = new JLabel();
			headerLabel2.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 16));
			headerLabel2.setBounds(75, 40, 600, 20);
			contentPane.add(headerLabel2);	
    }//WaitingFrame
    
    
    public void ChangeVisibility(String titlestr, String headerstr, String headerstr2, boolean is_visible) {
    	if (is_visible) {
    		setTitle(titlestr);
    		headerLabel.setText(headerstr);
    		headerLabel2.setText(headerstr2);
    		this.setVisible(true);
    	}
    	else
    		this.setVisible(false);
    }//ChangeVisibility
    
    
    public boolean Load_Animated_Gifs() {
    	
    	try {
    		
    		setSize(new Dimension(700, 700));
    	
	    	//button group
			JRadioButton gif1 = new JRadioButton("animated gif 1");
			gif1.setSelected(true);
			gif1.setBackground(Color.orange);
			gif1.setBounds(275, 80, 200, 15);
			gif1.addActionListener(this);
			contentPane.add(gif1);
	
			JRadioButton gif2 = new JRadioButton("animated gif 2");
			gif2.setSelected(false);
			gif2.setBackground(Color.orange);
			gif2.setBounds(275, 100, 200, 15);
			gif2.addActionListener(this);
			contentPane.add(gif2);
			
			JRadioButton gif3 = new JRadioButton("animated gif 3");
			gif3.setSelected(false);
			gif3.setBackground(Color.orange);
			gif3.setBounds(275, 120, 200, 15);
			gif3.addActionListener(this);
			contentPane.add(gif3);
		
			ButtonGroup gif_group = new ButtonGroup();
			gif_group.add(gif1);
			gif_group.add(gif2);
	        gif_group.add(gif3);
			
			// add the image label
	        
	        URL resource = this.getClass().getResource("giphy.gif");
	        if (resource == null) {
	        	resource = this.getClass().getResource("../giphy.gif");
	        	if (resource == null)
	        		return false;
	        }
	        ImageIcon icon = new ImageIcon(resource);
	        int GIF_WIDTH  = icon.getIconWidth();
			int GIF_HEIGHT = icon.getIconHeight();
			imageLabel = new JLabel();
	        imageLabel.setIcon(icon);
			imageLabel.setBounds((700 - GIF_WIDTH)/2, 150, GIF_WIDTH, GIF_HEIGHT);
			
			contentPane.add(imageLabel);
			
			resource = this.getClass().getResource("giphy2.gif");
	        if (resource == null) {
	        	resource = this.getClass().getResource("../giphy2.gif");
	        	if (resource == null)
	        		return false;
	        }
	        ImageIcon icon2 = new ImageIcon(resource);
	        int GIF_WIDTH_2  = icon2.getIconWidth();
			int GIF_HEIGHT_2 = icon2.getIconHeight();
			imageLabel2 = new JLabel();
			imageLabel2.setIcon(icon2);
			imageLabel2.setBounds((700 - GIF_WIDTH_2)/2, 150, GIF_WIDTH_2, GIF_HEIGHT_2);
			
			
			resource = this.getClass().getResource("giphy3.gif");
	        if (resource == null) {
	        	resource = this.getClass().getResource("../giphy3.gif");
	        	if (resource == null)
	        		return false;
	        }
	        ImageIcon icon3 = new ImageIcon(resource);
	        int GIF_WIDTH_3  = icon3.getIconWidth();
			int GIF_HEIGHT_3 = icon3.getIconHeight();
			imageLabel3 = new JLabel();
			imageLabel3.setIcon(icon3);
			imageLabel3.setBounds((700 - GIF_WIDTH_3)/2, 150, GIF_WIDTH_3, GIF_HEIGHT_3);
			
			return true;
    	}
    	catch (Throwable tt) {
    		crashruncheck.println("EXCEPTION CAUGHT IN class WaitingFrame method Load_Animated_Gifs !!!!!", tt);
    		return false;
    	}
    }//Load_Animated_Gifs
	
	public void  actionPerformed(ActionEvent ev) {
		
		Object myobj = ev.getSource();
		JRadioButton mybutton = (JRadioButton)myobj;
		String actstr = mybutton.getActionCommand();

		if (actstr.equals("animated gif 2")) {
			contentPane.remove(imageLabel);
			contentPane.remove(imageLabel3);
			contentPane.add(imageLabel2);
		}
		else if (actstr.equals("animated gif 3")) {
			contentPane.remove(imageLabel);
			contentPane.remove(imageLabel2);
			contentPane.add(imageLabel3);
		}
		else {
			contentPane.remove(imageLabel2);
			contentPane.remove(imageLabel3);
			contentPane.add(imageLabel);
		}
		
		revalidate();
		repaint();
	}


}//class
