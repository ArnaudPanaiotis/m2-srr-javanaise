/***
 * JAVANAISE API
 * JvnException Class
 * Contacts: 
 *  nicolas.bouscarle@centraliens-lille.org
 *  arnaud.panaiotis@e.ujf-grenoble.fr
 *
 * Authors: 
 *  Bouscarle Nicolas
 *  Panaiotis Arnaud
 */

package jvn;

/**
 * Interface of a JVN Exception. 
 */

public class JvnException extends Exception {
	String message;
  
	public JvnException() {
	}
	
	public JvnException(String message) {
		this.message = message;
	}	
  
	public String getMessage(){
		return message;
	}
}
