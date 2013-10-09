/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * Contacts: 
 *  nicolas.bouscarle@centraliens-lille.org
 *  arnaud.panaiotis@e.ujf-grenoble.fr
 *
 * Authors: 
 *  Bouscarle Nicolas
 *  Panaiotis Arnaud 
 */

package jvn;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class JvnCoordImpl 	
                extends UnicastRemoteObject 
                implements JvnRemoteCoord{
    private static int freeId;
    private HashMap<Integer, JvnObject> objects;
    private HashMap<String, Integer> symbolicNames;
    private HashMap<Integer, JvnRemoteServer> lockW;
    private HashMap<Integer, ArrayList<JvnRemoteServer>> lockR;
        
    private final Lock lockOnLockR = new ReentrantLock();
    private final Lock lockOnLockW = new ReentrantLock();
    private final Lock lockOnId = new ReentrantLock();

    /**
    * Default constructor
    * @throws JvnException
    **/
    private JvnCoordImpl() throws Exception {
        super();
        freeId = 0;
        objects = new HashMap();
        symbolicNames = new HashMap();
        lockW = new HashMap();
        lockR = new HashMap();
    }
    
    public static void main(String[] args)
       {
             try
             {
                 LocateRegistry.createRegistry(2020);
                 JvnCoordImpl cs = new JvnCoordImpl();
                 Naming.rebind("//localhost:2020/JvnCoord/", cs);
             }
             catch(Exception e)
             {
                 e.printStackTrace();
             }
       }

    /**
    *  Allocate a NEW JVN object id (usually allocated to a 
    *  newly created JVN object)
    * @throws java.rmi.RemoteException,JvnException
    **/
    @Override
    public int jvnGetObjectId()
            throws java.rmi.RemoteException,jvn.JvnException {
        lockOnId.lock();
        int tmp = -1;
        try {
            freeId++;
            tmp = freeId;
        } finally {
            lockOnId.unlock();
        }
        return tmp;
    }
  
    /**
    * Associate a symbolic name with a JVN object
    * @param jon : the JVN object name
    * @param jo  : the JVN object 
    * @param joi : the JVN object identification
    * @param js  : the remote reference of the JVNServer
    * @throws java.rmi.RemoteException,JvnException
    **/
    @Override
    public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
        throws java.rmi.RemoteException,jvn.JvnException{
        if (symbolicNames.containsKey(jon)) {
            throw new JvnException("This symbolic name is already taken");
        }
        objects.put(jo.jvnGetObjectId(), jo);
        symbolicNames.put(jon, jo.jvnGetObjectId());
        ArrayList<JvnRemoteServer> al;

        switch (jo.jvnGetLockStatus()) {
            case NL : 
                break;
            case RC : 
            case R :
                al = new ArrayList();
                al.add(js);
                lockR.put(jo.jvnGetObjectId(), al);
                break;
            case RWC :
                al = new ArrayList();
                al.add(js);
                lockR.put(jo.jvnGetObjectId(), al);
            case WC :
            case W :
                lockOnLockW.lock();
                if (lockW.containsKey(jo.jvnGetObjectId())) {
                    //get the write lock which were on previous registred object
                    Serializable o = lockW.get(jo.jvnGetObjectId()).
                            jvnInvalidateWriter(jo.jvnGetObjectId());
                    objects.get(jo.jvnGetObjectId()).jvnSetObjectState(o);
                } else {
                    lockW.put(jo.jvnGetObjectId(), js);
                }
                lockOnLockW.unlock();
                break;
        } 
    }
  
    /**
    * Get the reference of a JVN object managed by a given JVN server 
    * @param jon : the JVN object name
    * @param js : the remote reference of the JVNServer
    * @throws java.rmi.RemoteException,JvnException
    **/
    @Override
    public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
    throws java.rmi.RemoteException,jvn.JvnException{
        if (objects.get(symbolicNames.get(jon)) != null) {
            objects.get(symbolicNames.get(jon)).jvnRemoveLock();
        }
        return objects.get(symbolicNames.get(jon));
    }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
  @Override
  public Serializable jvnLockRead(int joi, JvnRemoteServer js)
  throws java.rmi.RemoteException, JvnException{
    Serializable object = objects.get(joi).jvnGetObjectState();
      // if we have a writer, remove it
    lockOnLockW.lock();
        try {
            if (lockW.containsKey(joi)) {
                object = lockW.get(joi).jvnInvalidateWriterForReader(joi);
                objects.get(joi).jvnSetObjectState(object);
                lockW.remove(joi);
            }
        } finally {
            lockOnLockW.unlock();
        }
      // update the Reads locks
      lockOnLockR.lock();
        try {
            if (! lockR.containsKey(joi)) {
                lockR.put(joi, new ArrayList());
            }
        } finally {
            lockOnLockR.unlock();
        }
      lockR.get(joi).add(js);
      return object;
  }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
    @Override
    public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException{
        Serializable object = objects.get(joi).jvnGetObjectState();
        // if we have a writer, remove it
        lockOnLockW.lock();
        try {
            if (lockW.containsKey(joi)) {
                object = lockW.get(joi).jvnInvalidateWriter(joi);
                objects.get(joi).jvnSetObjectState(object);
                lockW.remove(joi);
            }
        } finally {
            lockOnLockW.unlock();
        }
        // if we have readers, remove all of them
        lockOnLockR.lock();
        try {
            if (lockR.get(joi) != null){
                for (JvnRemoteServer server : lockR.get(joi)){
                    server.jvnInvalidateReader(joi);
                }
                lockR.remove(joi);
            }
        } finally {
            lockOnLockR.unlock();
        }
        // update lock
        lockW.put(joi, js);
        return object;
    }
    
    @Override
    public void jvnNotifyUnlock(int joi, JvnRemoteServer js) 
            throws RemoteException, JvnException {
        lockW.remove(joi);
        if (lockR.get(joi) != null) {
            lockR.get(joi).remove(js);
        }
    }
    
    @Override
    public void jvnNotifyUnlockWrite(int joi, JvnRemoteServer js) 
            throws RemoteException, JvnException {
        lockW.remove(joi);
    }

    
    /**
    * A JVN server terminates
    * @param js  : the remote reference of the server
    * @throws java.rmi.RemoteException, JvnException
    **/
    @Override
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
        ArrayList<Integer> tmp = new ArrayList();
        //Get and remove all WRITE locks of js
        for (Integer i : lockW.keySet()){
            if (lockW.get(i).equals(js)) {
                tmp.add(i);
            }
        }
        for (Integer i : tmp) {
            lockW.remove(i);
        }
        //Get and remove all READ locks of js
        for (ArrayList servers : lockR.values()) {
            servers.remove(js);
        }
    }
}