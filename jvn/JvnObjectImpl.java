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

/**
 *
 * @author Arno
 */
public class JvnObjectImpl implements JvnObject {    
    private int id;
    private LockState lockState;
    private Serializable object;
    
    public JvnObjectImpl(int id, Serializable o, JvnLocalServer jvnRemoteServer) {
        super();
        this.id = id;
        this.lockState = LockState.W;
        this.object = o;
    }
    
    @Override
    public void jvnLockRead() throws JvnException {
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
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        switch (lockState) {
            case W:  
            case WC :
            case RWC :
                //I have the lock in write
                this.lockState = LockState.W;
                break;
            case R :  
            case RC : 
            case NL :  
                object = JvnServerImpl.jvnGetServer().jvnLockWrite(this.id);
                break;
        }
    }

    @Override
    public synchronized void jvnUnLock() throws JvnException {
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
        //notify to my threads waited for lock 
        notifyAll();
    }
    
    @Override
    public synchronized void jvnRemoveLock() throws JvnException {
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
    public synchronized void jvnInvalidateReader() throws JvnException {
        switch (lockState) {
            case W: 
            case WC :
            case RWC :
                throw new JvnException("JvnError : No read lock can be "
                        + "free while having Write lock");
            case R :    
                try {
                    wait();
                } catch (InterruptedException ie) {
                    throw new JvnException("JvnError : waiting for unlock never happend !");
                }
                lockState = LockState.NL;
                break;
            case RC :  
            case NL :
                lockState = LockState.NL;
                break;
        }
        notifyAll();
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
        switch (lockState) {
            case R : 
            case RWC :
            case W: 
                try {
                    wait();
                } catch (InterruptedException ie) {
                    throw new JvnException("JvnError : waiting for unlock never happend !");}
                lockState = LockState.NL;
                break;
            case RC :  
            case NL :
            case WC :
                lockState = LockState.NL;
                break;
        }
        //notify to my threads waited for lock 
        notifyAll();
        return this.object;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() 
            throws JvnException {
        switch (lockState) {
            case RWC :
            case R :  
                lockState = LockState.R;
                break;
            case W: 
                try {
                    wait();
                } catch (InterruptedException ie) {
                    throw new JvnException("JvnError : waiting for unlock never happend !");}
                lockState = LockState.RC;
                break;
            case RC :  
                break;
            case NL :
            case WC :
                lockState = LockState.NL;
                break;
        }
        //notify to my threads waited for lock 
        notifyAll();
        return this.object;
    }
       
}
