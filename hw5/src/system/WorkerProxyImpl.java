package system;

import java.rmi.RemoteException;
import api.Task;

public class WorkerProxyImpl extends Thread {
	final private Worker worker;
	final private SpaceImpl spaceImpl; 
	
	public WorkerProxyImpl(Worker worker, SpaceImpl spaceImpl){
		this.worker = worker;
		this.spaceImpl = spaceImpl;
	}
	public void exit() throws RemoteException{
		worker.exit();
	}
	
	public void run(){
		while(true){
			Closure closure;
			try {
				closure = spaceImpl.takeQ();
				try {
					//System.out.println("proxy started");	

					WorkerResult  wr = worker.execute(closure.t, closure.args);
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
				} catch (RemoteException e) {
					spaceImpl.putQ(closure);
					return;
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				spaceImpl.unRegister(this);
				return;
			}
		}
	}
}