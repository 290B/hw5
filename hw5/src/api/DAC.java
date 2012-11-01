package api;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import system.WorkerImpl;

/**
 * The interface to the Divide and conquer framework which is used
 * to compute distributed recursive tasks
 * 
 * @author torgel
 *
 */
public class DAC {
	public Object[] args;
	public Task spawn_next;
	public int spawn_nextJoin;
	public LinkedList spawn = new LinkedList();
	
	private WorkerImpl worker;
	
	public Object send_argument;
	
	/**
	 * spawn starts a task and immediately puts it on the ready queue for execution
	 * 
	 * @param t the task that will be spawned
	 */
	protected <T> void spawn(Task<T> t){
		spawn.add(t);
	}
	
	/**
	 * spawn_next starts a task and puts it in wait queue for input arguments
	 * after getting all the arguments it needs it will execute() the task
	 * 
	 * @param t the task that will be spawned, should be a "compose" task
	 * @param joinCounter is the number of arguments the task will wait for before 
	 * executing
	 */
	protected <T> void spawn_next(Task<T> t, int joinCounter){
		spawn_next = t;
		spawn_nextJoin = joinCounter;
	}
	
	/**
	 * send argument is the method that will move output from the regular task executions
	 * to the waiting tasks which were started with spawn_next
	 * 
	 * @param value an Object of any type which will be sent to the collector tasks that is
	 * linked with the task which the value is coming from
	 */
	protected void send_argument(Object value){
		send_argument = value;
	}
	protected Shared getShared(){ return worker.getShared();}
	
	protected void setShared(Shared proposedShared){ worker.setShared(proposedShared); }
	
	public void setWorker(WorkerImpl worker){ this.worker = worker; }
	
	
	//public void execute(){}
	
//	public WorkerResult start(){
//		this.execute();
//		return new WorkerResult(spawn, spawnArgs, spawn_next, spawn_nextArgs, send_argument);
//	}
}