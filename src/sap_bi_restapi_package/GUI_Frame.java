
package sap_bi_restapi_package;


import java.awt.Component;
import java.awt.Container;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowStateListener;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JOptionPane;





public class GUI_Frame extends JFrame implements ActionListener,
                                                 WindowListener,
                                                 WindowFocusListener,
                                                 WindowStateListener  {
	
	//crash and run checking
	private SAP_BI_WebIntel_REST_helper		my_rest_caller;
	private CrashAndRunChecker				crashruncheck;
	private WaitingFrame					mywaitingframe;
	
	private Infostore_Button_Panel          infostore_button_panel;
	
	//login stuff
	private JLabel         userlabel;
	private JTextField     userfield;
	private JLabel         passlabel;
	private JPasswordField passfield;
	private JRadioButton   test_server_radio;
	private JRadioButton   prod_server_radio;
	private ButtonGroup    server_group;
	private JButton        credbutton;
	private String         myusername;
	private String         mypassword;
	private String         PROTOCOL_HOST_PORT;
	
	
	
	
	
	public GUI_Frame(WaitingFrame tempwaitframe, CrashAndRunChecker tempcrashruncheck) {
		
		super("GUI_Frame");
		
		mywaitingframe = tempwaitframe;
		crashruncheck = tempcrashruncheck;
		
		addWindowListener(this);
		addWindowFocusListener(this);
		addWindowStateListener(this);
		
		setSize(1100,1000);
		setLayout(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//***** login stuff *****
		userlabel = new JLabel("Please enter username:");
		userlabel.setBounds(10,10, 200, 20);
		
		userfield = new JTextField();
		userfield.setBounds(10, 50, 200, 20);
		
		passlabel = new JLabel("Please enter password:");
		passlabel.setBounds(10, 90, 200, 20);
		
		passfield = new JPasswordField();
		passfield.setBounds(10, 130, 200, 20);
		
		test_server_radio = new JRadioButton("TEST SERVER == test == http://test:6405/");
		test_server_radio.setSelected(true);
		test_server_radio.setBounds(10, 170, 400, 20);
		
		prod_server_radio = new JRadioButton("PROD SERVER == prod == http://prod:6405/");
		prod_server_radio.setSelected(false);
		prod_server_radio.setBounds(10, 210, 400, 20);
		
		server_group = new ButtonGroup();
		server_group.add(test_server_radio);
		server_group.add(prod_server_radio);
		
		credbutton = new JButton("ENTER");
		credbutton.setActionCommand("ENTER");
		credbutton.addActionListener(this);
		credbutton.setBounds(10, 250, 100, 20);
		
		this.add(userlabel);
		this.add(userfield);
		this.add(passlabel);
		this.add(passfield);
		this.add(test_server_radio);
		this.add(prod_server_radio);
		this.add(credbutton);

	}//GUI_Frame
	
	
	//button actions
	public void actionPerformed(ActionEvent ev) {
		
		Object myobj = ev.getSource();
		JButton mybutton = (JButton)myobj;
		String actstr = mybutton.getActionCommand();
		
		if (actstr.equals("ENTER")) { //login credentials enter
			
			myusername = userfield.getText();
			char[] pass_char_array = passfield.getPassword();
			mypassword = String.valueOf(pass_char_array);
			
			String server_type;
			if (prod_server_radio.isSelected()) {
				PROTOCOL_HOST_PORT = "http://prod:6405/";
				server_type = "PROD_prod_6405";
			}
			else {
				PROTOCOL_HOST_PORT = "http://test:6405/";
				server_type = "TEST_test_6405";
			}
			crashruncheck.println("Server choice is " + server_type + " == " + PROTOCOL_HOST_PORT);
			
			my_rest_caller = new SAP_BI_WebIntel_REST_helper(crashruncheck);
			Boolean token_success = my_rest_caller.Test_And_Save_Credentials(myusername, mypassword, PROTOCOL_HOST_PORT);
			if (token_success) {
				
				this.remove(userlabel);
				this.remove(userfield);
				this.remove(passlabel);
				this.remove(passfield);
				this.remove(test_server_radio);
				this.remove(prod_server_radio);
				this.remove(credbutton);
				
				infostore_button_panel = new Infostore_Button_Panel(crashruncheck, my_rest_caller, this, mywaitingframe);
				
				this.revalidate();
				this.repaint();
			}
			else {
				String errormsg = "Failed to get token ..... Did you type in correct username and password for the chosen url and port ?!?!?";
				crashruncheck.println(errormsg);
				JOptionPane.showMessageDialog(this, errormsg, "ERROR", JOptionPane.ERROR_MESSAGE);
				
				//DO NOT CLOSE APP
				//this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			}	
		}
	}//actionPerformed
    
	
	//**********************************************************************************
	//Save this method for now to disable or enable a container such as the treepanel.
	//The problem is that there is single-click and double-click
	//on a tree node and so in the past with the bad-api
	//i could not tell if i was single clicking or double-clicking on an
	//unknown folder id ..... i could have studied more about JTree but
	//i still need a method to disable or enable the tree panel so save it.
	//**********************************************************************************
	public void SetEnableRecursive(Component container, boolean enable){
	    container.setEnabled(enable);

	    try {
	        Component[] components= ((Container) container).getComponents();
	        for (int i = 0; i < components.length; i++) {
	        	SetEnableRecursive(components[i], enable);
	        }
	    } catch (ClassCastException ex) {
	    	crashruncheck.println("CAUGHT EXCEPTION IN class GUI_Frame method SetEnableRecursive !!!!!", ex);
	    }
	}//SetEnableRecursive
	
	
	
	
	
	
	
	
	
	
	
	
	public void windowClosed(WindowEvent e) {
		crashruncheck.println("windowClosed");
    }
	
	public void windowDeactivated(WindowEvent e) {
		crashruncheck.println("windowDeactivated");
	}
	
	public void windowActivated(WindowEvent e) {
		crashruncheck.println("windowActivated");
    }
	
	public void windowDeiconified(WindowEvent e) {
		crashruncheck.println("windowDeiconified");
    }
	
	public void windowIconified(WindowEvent e) {
		crashruncheck.println("windowIconified");
    }
	
	public void windowClosing(WindowEvent e) {
		crashruncheck.println("windowClosing");
		crashruncheck.println(myusername);
		crashruncheck.println(PROTOCOL_HOST_PORT);
		crashruncheck.Close();
    }
	
	public void windowOpened(WindowEvent e) {
		crashruncheck.println("windowOpened");
    }
	
	public void windowLostFocus(WindowEvent e) {
		crashruncheck.println("windowLostFocus");
    }
	
	public void windowGainedFocus(WindowEvent e) {
		crashruncheck.println("windowGainedFocus");
    }
	
	public void windowStateChanged(WindowEvent e) {
		crashruncheck.println("windowStateChanged");
    }
	
	
	
	
}//class
