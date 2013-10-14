/***
 * JAVANAISE API
 * Irc_Burst test class
 * Contacts: 
 *  nicolas.bouscarle@centraliens-lille.org
 *  arnaud.panaiotis@e.ujf-grenoble.fr
 *
 * Authors: 
 *  Bouscarle Nicolas
 *  Panaiotis Arnaud
 */
package tst;

import java.io.*;

import irc.Sentence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jvn.*;


 public class Irc_Burst {
	static JvnObject               sentence;
        private static java.util.List<String> WORD_LIST;
        private static final int NUMBER_OF_QUESTION = 100;  
        private static Random random;


  /**
  * main method
  * create a JVN object nammed IRC for representing the Chat application
  **/
	public static void main(String argv[]) throws InterruptedException{
	   try {
		// initialize JVN
		JvnServerImpl js = JvnServerImpl.jvnGetServer();
		
		// look up the IRC object in the JVN server
		// if not found, create it, and register it in the JVN server
		JvnObject jo = js.jvnLookupObject("IRC");

		if (jo == null) {
			jo = js.jvnCreateObject((Serializable) new Sentence());
			// after creation, I have a write lock on the object
                        ((Sentence)
                                (jo.jvnGetObjectState()))
                                .write("Evil Bunny is ready");
			jo.jvnUnLock();
			js.jvnRegisterObject("IRC", jo);
		}
                
                sentence = jo;
	   
	   } catch (Exception e) {
		   System.out.println("IRC problem : " + e.getMessage());
                   e.printStackTrace();
	   }
           
           WORD_LIST = new ArrayList(Arrays.asList(
                    "chat", "chien", "renard", "grenouille", "canard", 
                    "poney", "écureuil"));
           random = new Random();
           
           
           
           /*
            * For loop to generate random comportement
            */
           for (int i = 0; i<NUMBER_OF_QUESTION; i++) {
               try {
                    Thread.sleep(10 * random.nextInt(10));
                    if (random.nextInt(5) == 0) {
                        sentence.jvnLockWrite();
                        ((Sentence)
                                (sentence.jvnGetObjectState()))
                                .write(WORD_LIST.get(
                                random.nextInt(WORD_LIST.size())));
                        System.out.println("Writed : "+
                                ((Sentence)(sentence
                                .jvnGetObjectState())).read());
                    } else {
                        sentence.jvnLockRead();
                        System.out.println("Readed : "+
                                ((Sentence)(sentence
                                .jvnGetObjectState())).read());
                    }
                    Thread.sleep(10 * random.nextInt(5));
                    sentence.jvnUnLock();
               } catch (JvnException ex) {
                    System.out.println("Error while attemping to get a lock");
               }
           }
            System.out.println("Finished!");
            try {
                JvnServerImpl.jvnGetServer().jvnTerminate();
            } catch (JvnException ex) {
                Logger.getLogger(Irc_Burst.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.exit(0);
	}


}

