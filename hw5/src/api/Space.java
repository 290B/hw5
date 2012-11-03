package api;

import java.rmi.Remote;
import java.rmi.RemoteException;

import tasks.SharedTsp;
/**
 * The space takes tasks from the client and distributes them to works, it then returns results to the client
 * @author torgel
 *
 */
public interface Space extends Remote
{
    public static String SERVICE_NAME = "Space";
	/**
	 * 
	 * @param task the task that will be distributed to the workers
	 * @throws RemoteException
	 */
    void put( Task task, Shared shared ) throws RemoteException;
    
    void put( Task task) throws RemoteException;
/**
 * 
 * @return a result object that has been compute by one of the workers
 * @throws RemoteException something went wrong with the RMI connection
 * @throws InterruptedException something was interupter while running
 */
    Object take() throws RemoteException, InterruptedException; // 

    void exit() throws RemoteException;
}
