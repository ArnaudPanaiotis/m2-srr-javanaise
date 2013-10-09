/***
 * JAVANAISE Implementation
 * JvnServerImpl class
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
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;



public class JvnServerImpl 	
                extends UnicastRemoteObject 
                implements JvnLocalServer, JvnRemoteServer{
    // A JVN server is managed as a singleton 
    private static JvnServerImpl js = null;
    private HashMap<Integer, JvnObject> objects;
    private JvnRemoteCoord jrc = null;

    /**
    * Default constructor
    * @throws JvnException
    **/
    private JvnServerImpl() throws Exception {
        super();
        objects = new HashMap();
        jrc = (JvnRemoteCoord)Naming.lookup("//localhost:2020/JvnCoord/");
    }
	
 
    /**
    * Static method allowing an application to get a reference to 
    * a JVN server instance
    * @throws JvnException
    **/
    public static JvnServerImpl jvnGetServer() {
        if (js == null){
            try {
                js = new JvnServerImpl();
            } catch (Exception e) {
                return null;
            }
        }
        return js;
    }
	
    /**
    * The JVN service is not used anymore
    * @throws JvnException
    **/
    @Override
    public  void jvnTerminate()
            throws jvn.JvnException {
        try {
            jrc.jvnTerminate(this);
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new JvnException("JvnError : Connection failed");
        }
    } 
	
    /**
    * creation of a JVN object
    * @param o : the JVN object state
    * @throws JvnException
    **/
    @Override
    public  JvnObject jvnCreateObject(Serializable o)
            throws jvn.JvnException {
        int id = -1;
        try {
            id = jrc.jvnGetObjectId();
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new JvnException("JvnError : Connection failed");
        }
        JvnObject jo = new JvnObjectImpl(id, o, js);
        this.objects.put(id, jo);
        return jo; 
    }
	
    /**
    *  Associate a symbolic name with a JVN object
    * @param jon : the JVN object name
    * @param jo : the JVN object 
    * @throws JvnException
    **/
    @Override
    public  void jvnRegisterObject(String jon, JvnObject jo)
            throws jvn.JvnException {
        try {
            jrc.jvnRegisterObject(jon, jo, js);
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new JvnException("JvnError : Connection failed");
        }
    }
	
    /**
    * Provide the reference of a JVN object beeing given its symbolic name
    * @param jon : the JVN object name
    * @return the JVN object 
    * @throws JvnException
    **/
    @Override
    public JvnObject jvnLookupObject(String jon)
            throws jvn.JvnException {
        try {
            JvnObject tmp = jrc.jvnLookupObject(jon, this);
            if (tmp != null) {
                this.objects.put(tmp.jvnGetObjectId(), tmp);
            }
            return tmp;
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new JvnException("JvnError : Connection failed");
        }
    }	
	
    /**
    * Get a Read lock on a JVN object 
    * @param joi : the JVN object identification
    * @return the current JVN object state
    * @throws  JvnException
    **/
    @Override
    public Serializable jvnLockRead(int joi)
            throws JvnException {
        try {
            return jrc.jvnLockRead(joi, this);
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new JvnException("JvnError : Connection failed");
        }
    }
    
    
    /**
    * Get a Write lock on a JVN object 
    * @param joi : the JVN object identification
    * @return the current JVN object state
    * @throws  JvnException
    **/
    @Override
    public Serializable jvnLockWrite(int joi)
            throws JvnException {
        try {
            return jrc.jvnLockWrite(joi, js);
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new JvnException("JvnError : Connection failed");
        }
    }	

	
    /**
    * Invalidate the Read lock of the JVN object identified by id 
    * called by the JvnCoord
    * @param joi : the JVN object id
    * @return void
    * @throws java.rmi.RemoteException,JvnException
    **/
    @Override
    public void jvnInvalidateReader(int joi)
            throws java.rmi.RemoteException,jvn.JvnException {
	objects.get(joi).jvnInvalidateReader();
    }
	    
    /**
    * Invalidate the Write lock of the JVN object identified by id 
    * @param joi : the JVN object id
    * @return the current JVN object state
    * @throws java.rmi.RemoteException,JvnException
    **/
    @Override
    public Serializable jvnInvalidateWriter(int joi)
            throws java.rmi.RemoteException,jvn.JvnException { 
	return objects.get(joi).jvnInvalidateWriter(); 
    }
	
    /**
    * Reduce the Write lock of the JVN object identified by id 
    * @param joi : the JVN object id
    * @return the current JVN object state
    * @throws java.rmi.RemoteException,JvnException
    **/
    @Override
    public Serializable jvnInvalidateWriterForReader(int joi)
            throws java.rmi.RemoteException,jvn.JvnException { 
	return objects.get(joi).jvnInvalidateWriterForReader();
    }

}