package system;

import system.Closure;
import api.Task;

public interface proxy {
	public void putQ(Closure closure); // 
	public void putWaitMap(Closure closure); // In case of remote exception
	public Closure takeQ() throws InterruptedException;
	public Closure tryTakeQ()  throws InterruptedException;
	public void placeArgument(Continuation cont, Object obj);
	public int getID();
	public void unRegister(WorkerProxyImpl workerProxyImpl);
}
