package system;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import api.DAC;
import api.Shared;
import api.Task;

public class WorkerImpl implements Worker, Serializable {
    public Shared shared;
	private static Worker2Space space;
	private static final long serialVersionUID = 227L;
	private final BlockingQueue sharedQ = new LinkedBlockingQueue();
	private final BlockingDeque localQ = new LinkedBlockingDeque(); // Contains closure
	private final BlockingDeque doneQ = new LinkedBlockingDeque();  // Contains WorkerResults
	private static final int LOCAL_QUEUE_LENGTH = 2;
	
	public static void main(String[] args) {
		String spaceHost = args[0];
		String name = "Worker";
		String spaceName = "Space";
		if (System.getSecurityManager() == null ) 
		{ 
		   System.setSecurityManager(new java.rmi.RMISecurityManager()); 
		}
		try{
			Worker worker = new WorkerImpl();
			
			Worker stub = (Worker) UnicastRemoteObject.exportObject(worker, 0);
			//Registry registry = LocateRegistry.createRegistry( 1093 );
			//registry.rebind(name, stub);
			
			if (spaceHost.equals("spacemode")){
				System.out.println("spaceWorker started");
				SharedProxy sharedProxy = ((WorkerImpl)worker).new SharedProxy();
				sharedProxy.start();
				Registry registrySpace = LocateRegistry.getRegistry("localhost");
				space = (Worker2Space)registrySpace.lookup(spaceName);
				
			}else{
				//Worker worker = new WorkerImpl();
				System.out.println("Connecting to space: " + spaceHost);
				Registry registrySpace = LocateRegistry.getRegistry(spaceHost);
				System.out.println("Looking up service: " + spaceName);
				space = (Worker2Space)registrySpace.lookup(spaceName);
			
				space.register(worker);
				System.out.println("WorkerImpl bound");
				SharedProxy sharedProxy = ((WorkerImpl)worker).new SharedProxy();
				sharedProxy.start();
				Executer executer = ((WorkerImpl)worker).new Executer((WorkerImpl)worker);
				executer.start();
			}
		} catch (Exception e) {
            System.err.println("WorkerImpl exception:");
            e.printStackTrace();
        }
	}
	
	public WorkerResult execute(Task task, Object [] args){
			DAC t = (DAC) task;
			if (t.args == null) t.args = args;
			t.setWorker(this);
			try {
				Shared sharedTemp = space.getShared(); 
				if (sharedTemp != null){
					this.shared = sharedTemp.clone();
				}
			} catch (RemoteException e) {
				System.out.println("Space could not send Shared to worker");
			} catch(CloneNotSupportedException e){
				System.out.println("Could not clone...");
			}
			task.execute();
			return new WorkerResult(t.spawn, t.spawn_next, t.spawn_nextJoin , t.send_argument);
	}
	public void exit() throws RemoteException {
		System.out.println("Remote call to exit()");
		System.exit(0);
		
	}
	public Shared getShared(){
		try {
			return shared.clone(); 
		} catch (CloneNotSupportedException e) {
			
			e.printStackTrace();
			System.exit(0);
		}
			return null;
	}
	
	public void setShared(Shared proposedShared){
		try {
			sharedQ.add(proposedShared.clone());
		} catch (CloneNotSupportedException e) {
			System.out.println("proposedShared not clonable");
			e.printStackTrace();
			System.exit(0);
		}
	}
	public class Executer extends Thread{
		Closure closure;
		WorkerImpl myWorker;
		public Executer(WorkerImpl myWorker){
			this.myWorker = myWorker;
		}
		public void run(){
			while(true){
				try {
					closure = (Closure)localQ.takeFirst();
					Task task = closure.t;
					Object [] args  = closure.args;
					DAC t = (DAC) task;
					if (t.args == null) t.args = args;
					t.setWorker(myWorker);
					try {
						Shared sharedTemp = space.getShared(); 
						if (sharedTemp != null){
							myWorker.shared = sharedTemp.clone();
						}
					} catch (RemoteException e) {
						System.out.println("Space could not send Shared to worker");
						e.printStackTrace();
					} catch(CloneNotSupportedException e){
						System.out.println("Could not clone...");
						e.printStackTrace();
					}
					task.execute();
					doneQ.addLast(new WorkerResult(t.spawn, t.spawn_next, t.spawn_nextJoin , t.send_argument));
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(0);
				}
				
			}
			
		}
		
	}
	
	
	public class SharedProxy extends Thread{
		public SharedProxy(){}
		@Override
		public void run() {
			while(true){
			try {
				Shared proposedShared = (Shared)sharedQ.take();
				if (proposedShared.isNewerThan(shared)){
					Shared sharedFromSpace;
					try {
						sharedFromSpace = space.getShared();
						if (proposedShared.isNewerThan(sharedFromSpace)){
							shared = proposedShared;
							space.setShared(shared);
						}else{
							shared = sharedFromSpace;
						}
					} catch (RemoteException e) {
						System.out.println("SharedProxy: Remote exception");
						e.printStackTrace();
					} catch (CloneNotSupportedException e) {
						System.out.println("SharedProxy: Clone exception");
						e.printStackTrace();
					}
					
				}
				
			} catch (InterruptedException e) {
				System.out.println("Error while executing sharedQ.take()");
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		}
		
		
	}
	public boolean put(Closure closure) throws RemoteException {
		try {
			//System.out.println("remote call on put");
			if (localQ.size() < LOCAL_QUEUE_LENGTH){
				localQ.putLast(closure);
				return true;
			}else{
				return false;
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}
	public WorkerResult take() throws RemoteException, InterruptedException {
		return (WorkerResult) doneQ.take();
	}
}
