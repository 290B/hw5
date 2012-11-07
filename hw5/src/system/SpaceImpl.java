package system;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.*;

import tasks.SharedTsp;
import tasks.TspReturn;

import api.DAC;
import api.Shared;
import api.Space;
import api.Task;

import tasks.SharedTsp;

public class SpaceImpl implements Space, Worker2Space, proxy{
	
	private static final Map waitMap = new ConcurrentHashMap();
	private static final BlockingDeque readyQ = new LinkedBlockingDeque();
	private static final BlockingQueue result = new LinkedBlockingQueue();
	private static final BlockingQueue proxyList = new LinkedBlockingQueue();
	private static final BlockingDeque spaceTaskQ = new LinkedBlockingDeque();
	private static int nextID = 1;
	private static final long serialVersionUID = 227L;
	private static Shared shared;
	private static boolean prefetching;
	private static boolean spaceTask;
	
	public static void main(String[] args) {
		if (System.getSecurityManager() == null ) 
		{ 
		   System.setSecurityManager(new java.rmi.RMISecurityManager() ); 
		}
		if (((String)args[0]).equals("prefetching")){
			prefetching = true;
			System.out.println("Prefetching enabled");
		}
		try {
			Space space = new SpaceImpl();
			Space stub = (Space)UnicastRemoteObject.exportObject(space, 0);
			Registry registry = LocateRegistry.createRegistry( 1099 );
			registry.rebind(SERVICE_NAME, stub);
			System.out.println("SpaceImpl bound");
			WorkerImpl spaceWorker = new WorkerImpl();
			String[] arg = new String[1];
			arg[0]="spacemode";
			spaceWorker.main(arg);
			SpaceWorkerProxy spaceWorkerProxy = ((SpaceImpl)space).new SpaceWorkerProxy((SpaceImpl)space, spaceWorker);
			spaceWorkerProxy.start();
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
public void put(Task task, Shared sharedIn) throws RemoteException {
		
		//TODO create a shared variable here somewhere
		try {
			this.shared = sharedIn.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println("SharedTSP not clonable");
			e.printStackTrace();
		}
		System.out.println("Getting Shared from space...2");
		
		
		//  Create a Closure object and insert to readyQ
		System.out.println("Task recieved...");
		Continuation finalResult = new Continuation(0,0);
		Closure root = new Closure(task, 0, finalResult, getID());
		readyQ.add(root);
	}
public void put(Task task) throws RemoteException {
		//  Create a Closure object and insert to readyQ
		System.out.println("Task recieved...");
		Continuation finalResult = new Continuation(0,0);
		Closure root = new Closure(task, 0, finalResult, getID());
		readyQ.add(root);
	}
	
	@Override
	public Object take() throws RemoteException, InterruptedException {
		return result.take();
	}
	@Override
	public void exit() {
		while(!proxyList.isEmpty()){
			try {
				WorkerProxyImpl cpl = (WorkerProxyImpl)proxyList.take();
				cpl.exit();
				cpl.stop();
			} catch (Exception e) {
		
			}
			
		}
		System.exit(0);
		
		
	}
	synchronized public void putWaitMap(Closure closure) {
		//System.out.println("Placing closure in wait map...");
		waitMap.put(closure.ID, closure);
	}
	
	public void putQ(Closure closure) {
		//System.out.println("Putting closure to queue...");
		readyQ.addFirst(closure);
	}
	
	synchronized public void placeArgument(Continuation cont, Object argument){
		//System.out.println("Placing argument to map key (ID) :" + cont.ID);
		if (cont.ID == 0){
			try {
				result.put(argument);
			} catch (InterruptedException e) {
				System.out.println("Could not return result");
			}
		}else{
			
			Closure closure = (Closure) waitMap.remove(cont.ID);
			Object [] temp =  (Object[])closure.args;
			temp[cont.argNumber] = argument;
			closure.joinCounter--;
			if (closure.joinCounter == 0){
				if (spaceTask){
					spaceTaskQ.addFirst(closure);
				}else{
					putQ(closure);
				}
			}else{
			putWaitMap(closure);
			}
		}
		
	}
	
	public Closure takeQ() throws InterruptedException {
			Closure closure = (Closure)readyQ.take();
			return closure;
	}
	
	synchronized public Closure tryTakeQ() throws InterruptedException {
		
		Closure closure = (Closure)readyQ.poll();
		return closure;
}
	
	public void register(Worker worker) throws RemoteException, InterruptedException {
		WorkerProxyImpl workerProxy = new WorkerProxyImpl(worker, this, prefetching);
		workerProxy.start();
		proxyList.add(workerProxy);
		System.out.println("Computer registered");
	}
	
	public void unRegister(WorkerProxyImpl workerProxy) {
		proxyList.remove(workerProxy);
		System.out.println("Computer unregistered");
	}
	
	public synchronized int getID() { return nextID++; }
	
	public synchronized boolean setShared(Shared proposedShared){
		System.out.println("set shared called");
		if (proposedShared.isNewerThan(shared)){
			try {
				this.shared = proposedShared.clone();
				/*
				SharedTsp tmp = (SharedTsp)shared;
				TspReturn lol2 = (TspReturn)tmp.getShared();
				System.out.println("Min path updated: " + lol2.getSumPathLength());
				*/
			} catch (CloneNotSupportedException e) {
				System.out.println("Coult not clone...");
			}
			return true;
		}	
		return false;
		
	}

	@Override
	public Shared getShared() throws RemoteException, CloneNotSupportedException {
		if (shared != null){
			return shared.clone();
		}
		return null;
	}
	
	public class SpaceWorkerProxy extends Thread{
		SpaceImpl sp;
		Worker spaceWorker;
		SpaceWorkerProxy(SpaceImpl sp, Worker spaceWorker){
			this.sp = sp;
			this.spaceWorker = spaceWorker;
		}
		public void run(){
			while(true){
				try {
					Closure closure = (Closure) spaceTaskQ.take();
					try {
						WorkerResult wr = spaceWorker.execute(closure.t, closure.args);
						if (wr.spawn_next != null){
							int spawnNextID = sp.getID();
							Closure spawnNextClosure = new Closure(wr.spawn_next, wr.spawn_nextJoin, closure.cont, spawnNextID);
							if (spawnNextClosure.joinCounter > 0){
								sp.putWaitMap(spawnNextClosure);
							}else{
								sp.putQ(spawnNextClosure);
							}
							int argNumber = 0;
							while(!wr.spawn.isEmpty()){
								Continuation cont = new Continuation(spawnNextID, argNumber);
								Closure spawnClosure = new Closure((Task) wr.spawn.pop(), 0, cont, sp.getID());
								sp.putQ(spawnClosure);
								argNumber++;
							}
						}
						if (wr.send_argument != null){
							sp.placeArgument(closure.cont, wr.send_argument);
						}
			
					} catch (RemoteException e) {
						System.out.println("Major SpaceWorkerProxy error");
						e.printStackTrace();
						return;
					}
					
						} catch (InterruptedException e) {
					System.out.println("Error while taking closure from spaceTaskQ");
					e.printStackTrace();
				}
			}
		}
	}
}
