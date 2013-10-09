/***
 * JAVANAISE API
 * JvnObject interface
 * Contacts: 
 *  nicolas.bouscarle@centraliens-lille.org
 *  arnaud.panaiotis@e.ujf-grenoble.fr
 *
 * Authors: 
 *  Bouscarle Nicolas
 *  Panaiotis Arnaud
 */
 

package jvn;

import java.io.*;

/**
 * Interface of a JVN object. 
 * The serializable property is required in order to be able to transfer 
 * a reference to a JVN object remotely
 */

public interface JvnObject extends Serializable {

    
        public enum LockState {NL, RC, WC, R, W, RWC};
        
	/**
	* Get a Read lock on the object 
	* @throws JvnException
	**/
	public void jvnLockRead()
	throws jvn.JvnException; 

	/**
	* Get a Write lock on the object 
	* @throws JvnException
	**/
	public void jvnLockWrite()
     	throws jvn.JvnException; 

	/**
	* Unlock  the object 
	* @throws JvnException
	**/
	public void jvnUnLock()
	throws jvn.JvnException; 
        
        
        /**
	* Remove lock on the object 
	* @throws JvnException
	**/
	public void jvnRemoveLock()
	throws jvn.JvnException; 
	
	
	/**
	* Get the object identification
	* @throws JvnException
	**/
	public int jvnGetObjectId()
	throws jvn.JvnException; 
	
	/**
	* Get the object value
	* @throws JvnException
	**/
	public Serializable jvnGetObjectState()
	throws jvn.JvnException; 
        
        /**
	* Set the object value
	* @throws JvnException
	**/
	public void jvnSetObjectState(Serializable s)
	throws jvn.JvnException; 
        
        /**
	* Get the object lock state
	* @throws JvnException
	**/
	public LockState jvnGetLockStatus()
	throws jvn.JvnException; 
	
	/**
	* Invalidate the Read lock of the JVN object 
	* @throws JvnException
	**/
        public void jvnInvalidateReader()
	throws jvn.JvnException;
	    
	/**
	* Invalidate the Write lock of the JVN object  
	* @return the current JVN object state
	* @throws JvnException
	**/
        public Serializable jvnInvalidateWriter()
	throws jvn.JvnException;
	
	/**
	* Reduce the Write lock of the JVN object 
	* @return the current JVN object state
	* @throws JvnException
	**/
        public Serializable jvnInvalidateWriterForReader()
	throws jvn.JvnException;	
}
