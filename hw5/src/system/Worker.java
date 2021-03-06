package system;

import java.rmi.Remote;
import java.rmi.RemoteException;

import api.Shared;
import api.Task;



public interface Worker extends Remote
{
    //Result execute( Task task ) throws RemoteException;
	public <T> T execute(Task t, Object[] args) throws RemoteException;
	//public WorkerResult start(Task t) throws RemoteException;
    void exit() throws RemoteException;
    
    public boolean put(Closure closure) throws RemoteException;
    public WorkerResult take() throws RemoteException, InterruptedException;
}