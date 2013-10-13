/***
 * JAVANAISE Implementation
 * JvnObjectImpl class
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
import java.util.concurrent.locks.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arno
 */
public class JvnObjectImpl implements JvnObject {    
    private int id;
    private LockState lockState;
    private Serializable object;
    private final Lock lockOnState = new ReentrantLock();
    private final Condition lockCondition = lockOnState.newCondition();
    
    public JvnObjectImpl(int id, Serializable o, JvnLocalServer jvnRemoteServer) {
        super();
        this.id = id;
        this.lockState = LockState.W;
        this.object = o;
    }
    
    @Override
    public void jvnLockRead() throws JvnException {
//        lockOnState.lock();
//        try {
            switch (lockState) {
                case W: 
                    throw new JvnException("JvnError : Writer can not ask for Read lock");
                case NL : 
                    object = JvnServerImpl.jvnGetServer().jvnLockRead(this.id);
                    this.lockState = LockState.R;
                    break;
                case RC : 
                case R :
                    //I have the lock in read
                    this.lockState = LockState.R;
                    break;
                case WC :
                case RWC :
                    //I have the lock in read (and in write)
                    this.lockState = LockState.RWC;
                    break;
            }
//        } finally {
//            lockOnState.unlock();
//        }
    }

    @Override
    public void jvnLockWrite() throws JvnException {
//        lockOnState.lock();
//        try {
            switch (lockState) {
                case W:  
                case WC :
                case RWC :
                    //I have the lock in write
                    this.lockState = LockState.W;
                    break;
                case R :  
                    throw new JvnException("JvnError : Reader can not ask for Write lock");
                case RC : 
                case NL :  
                    object = JvnServerImpl.jvnGetServer().jvnLockWrite(this.id);
                    this.lockState = LockState.W;
                    break;
            }
//        } finally {
//            lockOnState.unlock();
//        }
    }

    @Override
    public void jvnUnLock() throws JvnException {
        lockOnState.lock();
        try {
            switch (lockState) {
                case W: 
                case WC :
                case RWC :
                    this.lockState = LockState.WC;
                    break;
                case R :  
                case RC : 
                    this.lockState = LockState.RC;
                    break;  
                case NL : 
                    break;
            }
        } finally {
            lockCondition.signalAll();
            lockOnState.unlock();
        }
    }
    
    @Override
    public void jvnRemoveLock() throws JvnException {
        lockState = LockState.NL;
    }

    @Override
    public int jvnGetObjectId() 
            throws JvnException {
        return id;
    }

    @Override
    public Serializable jvnGetObjectState() 
            throws JvnException {
        return this.object;
    }
    
    @Override
    public void jvnSetObjectState(Serializable s)
	throws jvn.JvnException {
        this.object = s;
    }
    
    @Override
    public LockState jvnGetLockStatus()
	throws jvn.JvnException {
        return this.lockState;
    }

    @Override
    public void jvnInvalidateReader() throws JvnException {
        lockOnState.lock();
        try {
            switch(lockState) {
                case W: 
                case WC :
                case RWC :
                    throw new JvnException("JvnError : No read lock can be "
                            + "free while having Write lock");
                case R :    
                    lockCondition.await();
                    lockState = LockState.NL;
                    break;
                case RC :  
                case NL :
                    lockState = LockState.NL;
                    break;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(JvnObjectImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            lockOnState.unlock();
        }
    }

    @Override
    public  Serializable jvnInvalidateWriter() throws JvnException {
        lockOnState.lock();
        try {
            switch (lockState) {
                case R : 
                case RWC :
                case W: 
                    lockCondition.await();
                    lockState = LockState.NL;
                    break;
                case RC :  
                case NL :
                case WC :
                    lockState = LockState.NL;
                    break;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(JvnObjectImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            lockOnState.unlock();
        }
        return this.object;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() 
            throws JvnException {
        lockOnState.lock();
        try {
            switch (lockState) {
                case RWC :
                case R :  
                    lockState = LockState.R;
                    break;
                case W: 
                    lockCondition.await();
                    lockState = LockState.RC;
                    break;
                case RC :  
                    break;
                case NL :
                case WC :
                    lockState = LockState.NL;
                    break;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(JvnObjectImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            lockOnState.unlock();
        }
        return this.object;
    }
       
}
