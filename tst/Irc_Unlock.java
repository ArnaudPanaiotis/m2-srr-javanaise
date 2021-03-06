/***
 * JAVANAISE API
 * Irc_Unlock test class
 * Contacts: 
 *  nicolas.bouscarle@centraliens-lille.org
 *  arnaud.panaiotis@e.ujf-grenoble.fr
 *
 * Authors: 
 *  Bouscarle Nicolas
 *  Panaiotis Arnaud
 */
package tst;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import irc.Sentence;
import jvn.*;


 public class Irc_Unlock {
	public TextArea		text;
	public TextField	data;
	Frame 			frame;
	JvnObject               sentence;


  /**
  * main method
  * create a JVN object nammed IRC for representing the Chat application
  **/
	public static void main(String argv[]) {
	   try {
		   
		// initialize JVN
		JvnServerImpl js = JvnServerImpl.jvnGetServer();
		
		// look up the IRC object in the JVN server
		// if not found, create it, and register it in the JVN server
		JvnObject jo = js.jvnLookupObject("IRC");

		if (jo == null) {
			jo = js.jvnCreateObject((Serializable) new Sentence());
			// after creation, I have a write lock on the object
			jo.jvnUnLock();
			js.jvnRegisterObject("IRC", jo);
		}
		// create the graphical part of the Chat application
		 new Irc_Unlock(jo);
	   
	   } catch (Exception e) {
		   System.out.println("IRC problem : " + e.getMessage());
                   e.printStackTrace();
	   }
	}

  /**
   * IRC Constructor
   @param jo the JVN object representing the Chat
   **/
	public Irc_Unlock(JvnObject jo) {
		sentence = jo;
		frame=new Frame();
		frame.setLayout(new GridLayout(1,1));
		text=new TextArea(10,60);
		text.setEditable(false);
		text.setForeground(Color.red);
		frame.add(text);
		data=new TextField(40);
		frame.add(data);
		Button read_button = new Button("read");
		read_button.addActionListener(new readListener(this));
		frame.add(read_button);
		Button write_button = new Button("write");
		write_button.addActionListener(new writeListener(this));
		frame.add(write_button);
                Button unlock_button = new Button("unlock");
                unlock_button.addActionListener(new unlockListener(this));
		frame.add(unlock_button);
                frame.setSize(545,201);
		text.setBackground(Color.black); 
		frame.setVisible(true);
	}
}
/**
  * Internal class to manage user events (read) on the CHAT application
  **/
 class readListener implements ActionListener {
	Irc_Unlock irc;
  
	public readListener (Irc_Unlock i) {
		irc = i;
	}
   
 /**
  * Management of user events
  **/
        @Override
	public void actionPerformed (ActionEvent e) {
	 try {
		// lock the object in read mode
		irc.sentence.jvnLockRead();
		
		// invoke the method
		String s = ((Sentence)(irc.sentence.jvnGetObjectState())).read();
		// display the read value
		irc.data.setText(s);
		irc.text.append(s+"\n");
	   } catch (JvnException je) {
		   System.out.println("IRC problem : " + je.getMessage());
	   }
	}
}

 /**
  * Internal class to manage user events (write) on the CHAT application
  **/
 class writeListener implements ActionListener {
	Irc_Unlock irc;
  
	public writeListener (Irc_Unlock i) {
        	irc = i;
	}
  
  /**
    * Management of user events
   **/
        @Override
	public void actionPerformed (ActionEvent e) {
	   try {	
		// get the value to be written from the buffer
                String s = irc.data.getText();
        	
                // lock the object in write mode
		irc.sentence.jvnLockWrite();
		// invoke the method
		((Sentence)(irc.sentence.jvnGetObjectState())).write(s);
	 } catch (JvnException je) {
		   System.out.println("IRC problem  : " + je.getMessage());
	 }
	}
}


/**
 * Internal class to manage user events (unlock) on the CHAT application
 **/
class unlockListener implements ActionListener {
    Irc_Unlock irc;

    public unlockListener (Irc_Unlock i) {
            irc = i;
    }

    /**
    * Management of user events
    **/
    @Override
    public void actionPerformed (ActionEvent e) {
       try {
            // unlock the object
            irc.sentence.jvnUnLock();
        } catch (JvnException je) {
               System.out.println("IRC problem  : " + je.getMessage());
        }
    }
 }


