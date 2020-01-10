


//https://stackoverflow.com/questions/11736878/display-calendar-to-pick-a-date-in-java
//https://stackoverflow.com/questions/11736878/display-calendar-to-pick-a-date-in-java
//https://stackoverflow.com/questions/11736878/display-calendar-to-pick-a-date-in-java
//https://stackoverflow.com/questions/11736878/display-calendar-to-pick-a-date-in-java
//https://stackoverflow.com/questions/11736878/display-calendar-to-pick-a-date-in-java


package sap_bi_restapi_package;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;


public class Parameter_Date_Button extends JButton {

    private static String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
    private static final int DIALOG_WIDTH = 300;
    private static final int DIALOG_HEIGHT = 200;

    private SimpleDateFormat dateFormat;
    private DatePanel datePanel = null;
    private JDialog dateDialog = null;
    
    private String savelabel;
    private String mydatestring;
    private CrashAndRunChecker crashruncheck;

    public Parameter_Date_Button(String mylabel, CrashAndRunChecker tempcrashruncheck) {
        this(new Date(), mylabel, tempcrashruncheck);
    }

    public Parameter_Date_Button(String mylabel, CrashAndRunChecker tempcrashruncheck, Date date) {
        this(date, mylabel, tempcrashruncheck);
    }

    private Parameter_Date_Button(Date date, String mylabel, CrashAndRunChecker tempcrashruncheck) {
    	savelabel = mylabel;
        crashruncheck = tempcrashruncheck;
        
        setDate(date);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent paramMouseEvent) {
            	if (datePanel == null)
            		datePanel = new DatePanel();
            	
                Point point = getLocationOnScreen();
                point.y = point.y + 30;
                showDateDialog(datePanel, point);
            }
        });
        
        setActionCommand("PRIVATE CONSTRUCTOR");
        addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ev) {
        		Object myobj = ev.getSource();
        		JButton mybutton = (JButton)myobj;
        		String actstr = mybutton.getActionCommand();
        		if (actstr.equals("PRIVATE CONSTRUCTOR"))
        			crashruncheck.println("Parameter_Date_Button: CLICK_OPEN savelabel=" + savelabel);
            }
        });
    }
    

    private void showDateDialog(DatePanel dateChooser, Point position) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(Parameter_Date_Button.this);
        if (dateDialog == null || dateDialog.getOwner() != owner) {
            dateDialog = createDateDialog(owner, dateChooser);
        }
        dateDialog.setLocation(getAppropriateLocation(owner, position));
        dateDialog.setVisible(true);
    }

    private JDialog createDateDialog(Frame owner, JPanel contentPanel) {
        JDialog dialog = new JDialog(owner, "Date Selected", true);
        dialog.setUndecorated(true);
        dialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        return dialog;
    }

    private Point getAppropriateLocation(Frame owner, Point position) {
        Point result = new Point(position);
        Point p = owner.getLocation();
        int offsetX = (position.x + DIALOG_WIDTH) - (p.x + owner.getWidth());
        int offsetY = (position.y + DIALOG_HEIGHT) - (p.y + owner.getHeight());

        if (offsetX > 0) {
            result.x -= offsetX;
        }

        if (offsetY > 0) {
            result.y -= offsetY;
        }

        return result;
    }

    private SimpleDateFormat getDefaultDateFormat() {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        }
        return dateFormat;
    }

    

    public void setDate(Date date) {
    	
    	String tempstr = null;
    	if (date != null) {
    		mydatestring = getDefaultDateFormat().format(date);
    		tempstr = "<html><center>" + savelabel + "</center><br/><center style=\"color:Blue;\">" + mydatestring + "</center></html>";
    	}
    	else {
    		mydatestring = "";
    		tempstr = "<html><center>" + savelabel + "</center><br/><center style=\"color:Blue;\">&nbsp;</center></html>";
    	}
        setText(tempstr);
    }

    public Date getDate() {
        try {
            return getDefaultDateFormat().parse(mydatestring);
        } catch (ParseException ex) {
        	return null;
        }
    }
    
    public String getDateAsString() {
    	return mydatestring;
    }

    private class DatePanel extends JPanel implements ChangeListener {
        int startYear = 1;
        int lastYear  = 9999;

        Color backGroundColor = Color.gray;
        Color palletTableColor = Color.white;
        Color todayBackColor = Color.orange;
        Color weekFontColor = Color.blue;
        Color dateFontColor = Color.black;
        Color weekendFontColor = Color.red;

        Color controlLineColor = Color.pink;
        Color controlTextColor = Color.white;

        JSpinner yearSpin;
        JSpinner monthSpin;
        JButton[][] daysButton = new JButton[6][7];
        JButton  clearButton;

        DatePanel() {
            setLayout(new BorderLayout());
            setBorder(new LineBorder(backGroundColor, 2));
            setBackground(backGroundColor);

            JPanel topYearAndMonth = createYearAndMonthPanal();
            add(topYearAndMonth, BorderLayout.NORTH);
            JPanel centerWeekAndDay = createWeekAndDayPanal();
            add(centerWeekAndDay, BorderLayout.CENTER);

            reflushWeekAndDay();
        }

        private JPanel createYearAndMonthPanal() {
        	
            Calendar cal = getCalendar();
            int currentYear = cal.get(Calendar.YEAR);
            int currentMonth = cal.get(Calendar.MONTH) + 1;
            
            String debugstr = String.format("createYearAndMonthPanal year=%d month=%d", currentYear, currentMonth);
            crashruncheck.println(debugstr);

            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.setBackground(controlLineColor);

            yearSpin = new JSpinner(new SpinnerNumberModel(currentYear,
                    startYear, lastYear, 1));
            yearSpin.setPreferredSize(new Dimension(56, 20));
            yearSpin.setName("Year");
            yearSpin.setEditor(new JSpinner.NumberEditor(yearSpin, "####"));
            yearSpin.addChangeListener(this);
            panel.add(yearSpin);

            JLabel yearLabel = new JLabel("Year");
            yearLabel.setForeground(controlTextColor);
            panel.add(yearLabel);

            monthSpin = new JSpinner(new SpinnerNumberModel(currentMonth, 1,
                    12, 1));
            monthSpin.setPreferredSize(new Dimension(35, 20));
            monthSpin.setName("Month");
            monthSpin.addChangeListener(this);
            panel.add(monthSpin);

            JLabel monthLabel = new JLabel("Month");
            monthLabel.setForeground(controlTextColor);
            panel.add(monthLabel);
            
            clearButton = new JButton("CLEAR");
            clearButton.setPreferredSize(new Dimension(80, 20));
            clearButton.setActionCommand("CLEAR");
            clearButton.addActionListener(new ActionListener() {
            	public void actionPerformed(ActionEvent ev) {
            		Object myobj = ev.getSource();
            		JButton mybutton = (JButton)myobj;
            		String actstr = mybutton.getActionCommand();
            		if (actstr.equals("CLEAR")) {
            			dateDialog.setVisible(false);
            			dayColorUpdate(true);
            			setDate(null);
            			
            			String debugstr = String.format("Parameter_Date_Button: CLEAR_CLOSE  savelabel=%s mydatestring=%s", savelabel, mydatestring);
            			crashruncheck.println(debugstr);
            		}
                }
            });
            panel.add(clearButton);
            

            return panel;
        }

        private JPanel createWeekAndDayPanal() {
            String colname[] = { "S", "M", "T", "W", "T", "F", "S" };
            JPanel panel = new JPanel();
            panel.setFont(new Font("Arial", Font.PLAIN, 10));
            panel.setLayout(new GridLayout(7, 7));
            panel.setBackground(Color.white);

            for (int i = 0; i < 7; i++) {
                JLabel cell = new JLabel(colname[i]);
                cell.setHorizontalAlignment(JLabel.RIGHT);
                if (i == 0 || i == 6) {
                    cell.setForeground(weekendFontColor);
                } else {
                    cell.setForeground(weekFontColor);
                }
                panel.add(cell);
            }

            int actionCommandId = 0;
            for (int i = 0; i < 6; i++)
                for (int j = 0; j < 7; j++) {
                    JButton numBtn = new JButton();
                    numBtn.setBorder(null);
                    numBtn.setHorizontalAlignment(SwingConstants.RIGHT);
                    numBtn.setActionCommand(String
                            .valueOf(actionCommandId));
                    numBtn.setBackground(palletTableColor);
                    numBtn.setForeground(dateFontColor);
                    numBtn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                            JButton source = (JButton) event.getSource();
                            if (source.getText().length() == 0) {
                                return;
                            }
                            dayColorUpdate(true);
                            source.setForeground(todayBackColor);
                            int newDay = Integer.parseInt(source.getText());
                            Calendar cal = getCalendar();
                            cal.set(Calendar.DAY_OF_MONTH, newDay);
                            
                            cal.set(Calendar.YEAR, getSelectedYear());
                            cal.set(Calendar.MONTH, getSelectedMonth() - 1);
                            
                            setDate(cal.getTime());

                            String debugstr = String.format("Parameter_Date_Button: CLICK_CLOSE  savelabel=%s  mydatestring=%s ", savelabel, mydatestring);
                            crashruncheck.println(debugstr);
                            dateDialog.setVisible(false);
                        }
                    });

                    if (j == 0 || j == 6)
                        numBtn.setForeground(weekendFontColor);
                    else
                        numBtn.setForeground(dateFontColor);
                    daysButton[i][j] = numBtn;
                    panel.add(numBtn);
                    actionCommandId++;
                }

            return panel;
        }

        private Calendar getCalendar() {
            Calendar calendar = Calendar.getInstance();
            calendar.setLenient(false);
            Date mydate = getDate();
            if (mydate != null)
            	calendar.setTime(mydate);
            else {
            	Date today = new Date();
            	calendar.setTime(today);
            }
            return calendar;
        }

        private int getSelectedYear() {
            return ((Integer) yearSpin.getValue()).intValue();
        }

        private int getSelectedMonth() {
            return ((Integer) monthSpin.getValue()).intValue();
        }

        private void dayColorUpdate(boolean isOldDay) {
            Calendar cal = getCalendar();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            int actionCommandId = day - 2 + cal.get(Calendar.DAY_OF_WEEK);
            int i = actionCommandId / 7;
            int j = actionCommandId % 7;
            if (isOldDay) {
                daysButton[i][j].setForeground(dateFontColor);
            } else {
                daysButton[i][j].setForeground(todayBackColor);
            }
        }

        private void reflushWeekAndDay() {
            Calendar cal = getCalendar();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            int maxDayNo = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int dayNo = 2 - cal.get(Calendar.DAY_OF_WEEK);
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 7; j++) {
                    String s = "";
                    if (dayNo >= 1 && dayNo <= maxDayNo) {
                        s = String.valueOf(dayNo);
                    }
                    daysButton[i][j].setText(s);
                    dayNo++;
                }
            }
            dayColorUpdate(false);
        }

        public void stateChanged(ChangeEvent ev) {
        	dayColorUpdate(true);

            JSpinner source = (JSpinner) ev.getSource();
            Calendar cal = getCalendar();
            if (source.getName().equals("Year")) {
                cal.set(Calendar.YEAR, getSelectedYear());
            } else {
            	try {
                cal.set(Calendar.MONTH, getSelectedMonth() - 1); //Because JANUARY is java int 0, but spinner value is 1 for january
            	}
            	catch (Throwable tt) {
            		crashruncheck.println("Parameter_Date_Button: stateChanged CAUGHT THROWABLE 1 ..... its ok continue", tt);
            	}
            }
            
            
	        Date mydate = null;
	        try {
	        	mydate = cal.getTime();
	        }
	        catch (Throwable tt) {
	        	crashruncheck.println("Parameter_Date_Button: stateChanged CAUGHT THROWABLE 2 !!!!! For example if on Oct31 and you month spinner to Sept but there is no Sept31 because we set leniency to false ..... solution is to force day of month to 1");
	            cal.set(Calendar.DAY_OF_MONTH, 1);
	            mydate = cal.getTime();
	        }
	        
	        //leap year bug check
	        int spinner_month_value  = getSelectedMonth();
            int calendar_month_value = cal.get(Calendar.MONTH) + 1; //because JANUARY is java int 0 so add 1
            if (spinner_month_value != calendar_month_value) { //got leap year bug
            	crashruncheck.println("Parameter_Date_Button: leap year bug caught ..... forcing day of month to 1 ..... syncing spinner and calendar month");
            	cal.set(Calendar.DAY_OF_MONTH, 1);
            	cal.set(Calendar.MONTH, getSelectedMonth() - 1); //whatever month spinner says
	            mydate = cal.getTime();
            }
            
            setDate(mydate);
            reflushWeekAndDay();
        }//stateChanged
    }//private class DatePanel
}//public class Parameter_Date_Button
