package system;

import java.rmi.RemoteException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import api.Task;

public class WorkerProxyImpl extends Thread {
	final private Worker worker;
	final private SpaceImpl spaceImpl;
	private static final BlockingDeque backupQueue = new LinkedBlockingDeque();
	
	public WorkerProxyImpl(Worker worker, SpaceImpl spaceImpl){
		this.worker = worker;
		this.spaceImpl = spaceImpl;
	}
	public void exit() throws RemoteException{
		worker.exit();
	}
	
//	public void run(){
//		while(true){
//			Closure closure;
//			try {
//				closure = spaceImpl.takeQ();
//				try {
//					//System.out.println("proxy started");	
//
//					WorkerResult  wr = worker.execute(closure.t, closure.args);
//					if (wr.spawn_next != null){
//						int spawnNextID = spaceImpl.getID();
//						Closure spawnNextClosure = new Closure(wr.spawn_next, wr.spawn_nextJoin, closure.cont, spawnNextID);
//						if (spawnNextClosure.joinCounter > 0){
//							spaceImpl.putWaitMap(spawnNextClosure);
//						}else{
//							spaceImpl.putQ(spawnNextClosure);
//						}
//						int argNumber = 0;
//						while(!wr.spawn.isEmpty()){
//							Continuation cont = new Continuation(spawnNextID, argNumber);
//							Closure spawnClosure = new Closure((Task) wr.spawn.pop(), 0, cont, spaceImpl.getID());
//							spaceImpl.putQ(spawnClosure);
//							argNumber++;
//						}
//					}
//					if (wr.send_argument != null){
//						spaceImpl.placeArgument(closure.cont, wr.send_argument);
//					}
//				} catch (RemoteException e) {
//					spaceImpl.putQ(closure);
//					return;
//				}
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//				spaceImpl.unRegister(this);
//				return;
//			}
//		}
//	}
	
	public void run(){
	while(true){
		Closure closure;
		while(true){
			try{
				closure = spaceImpl.takeQ();
				try{
					if (worker.put(closure)){
						backupQueue.addFirst(closure);
					}else{
						spaceImpl.putQ(closure);
						break;
					}
				}catch(RemoteException e) {
					System.out.println("Remote exeption : worker.put(closure)");
					spaceImpl.putQ(closure);
					while (!backupQueue.isEmpty()){
						spaceImpl.putQ((Closure)backupQueue.takeFirst());
					}
					spaceImpl.unRegister(this);
					return;
				}
			}catch (InterruptedException e) {
				System.out.println("WorkerProxyImpl - Space queue error");
				return;
			}
		}
			
		try {
			closure = (Closure)backupQueue.takeLast();
			try{
				WorkerResult wr = worker.take();
				if (wr.spawn_next != null){
					int spawnNextID = spaceImpl.getID();
					Closure spawnNextClosure = new Closure(wr.spawn_next, wr.spawn_nextJoin, closure.cont, spawnNextID);
					if (spawnNextClosure.joinCounter > 0){
						spaceImpl.putWaitMap(spawnNextClosure);
					}else{
						spaceImpl.putQ(spawnNextClosure);
					}
					int argNumber = 0;
					while(!wr.spawn.isEmpty()){
						Continuation cont = new Continuation(spawnNextID, argNumber);
						Closure spawnClosure = new Closure((Task) wr.spawn.pop(), 0, cont, spaceImpl.getID());
						spaceImpl.putQ(spawnClosure);
						argNumber++;
					}
				}
				if (wr.send_argument != null){
					spaceImpl.placeArgument(closure.cont, wr.send_argument);
				}
				
			}catch(RemoteException e){
				spaceImpl.putQ(closure);
				while (!backupQueue.isEmpty()){
					spaceImpl.putQ((Closure)backupQueue.takeFirst());
				}
				spaceImpl.unRegister(this);
				return;
			}
		}catch (InterruptedException e){
			System.out.println("WorkerProxyImpl - Space queue error");
			return;
		}
		
	}
	}
}