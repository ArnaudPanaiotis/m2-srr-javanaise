package tst;

/**
 * JAVANAISE TEST
 * Burst
 * 
 * Contacts: 
 *  nicolas.bouscarle@centraliens-lille.org
 *  arnaud.panaiotis@e.ujf-grenoble.fr
 *
 * Authors: 
 *  Bouscarle Nicolas
 *  Panaiotis Arnaud
 * 
 * This is a test to see the comportement of Javanaise when a high number of
 * client makes requests
 */

import java.util.Random;
import java.util.ArrayList;

import irc.Sentence;
import java.io.Serializable;
import java.util.List;
import java.util.Arrays;
import jvn.*;
import java.rmi.*;
import java.rmi.activation.*;

public class Burst {
    
    
    /*
     * Interval class Client which will be threaded to make burst test
     */
    private class Client /*extends Activatable*/{
        private List<String> WORD_LIST;
        private final int NUMBER_OF_QUESTION;  
        private Random random;
        
        
        public Client(int number, int id) {
            //super(id);
            WORD_LIST = new ArrayList(Arrays.asList(
                    "chat", "chien", "renard", "grenouille", "canard", 
                    "poney", "écureuil"));
            NUMBER_OF_QUESTION = number;
            this.random = new Random();
        }
        /*
        public Client(int id) {
            this(10, id);
        }*/

        
    }
}


