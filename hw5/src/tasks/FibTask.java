package tasks;

import java.io.Serializable;

import system.Continuation;
import api.DAC;
import api.Task;


/**
 * The task that contains the less general tasks Fib and Sum
 * @author torgel
 *
 */
public class FibTask implements Serializable{
	/**
	 * The task that splits up the fib tasks and spawns the fib sum tasks
	 * 
	 */
	public class Fib extends DAC implements Task, Serializable{
		public Object [] args;
		/**
		 * Takes an array of objects which are casted to integers
		 * 
		 */
		public Fib(Object... args){this.args = args;}
		
		public Fib(){}
		/**
		 * execute uses spawn and spawn_next to spawn new "smaller" tasks
		 * and sends them to the DAC
		 * 
		 * @return returns null but distributes results to the DAC by calling send_arguments
		 */
		public Object execute(){
			//System.out.println("Fib");
			int n = (Integer) args[0];
			if (n < 2) {
				send_argument(n);
			}else{
				spawn_next(new Sum(), 2);
				spawn(new Fib(n-1));
				spawn(new Fib(n-2));
			}
			return null;
		}
		
	}
	/**
	 * Sum is the task that sums up the leaf calculations of a fib task
	 * 
	 * @author torgel
	 *
	 */
	public class Sum extends DAC implements Task, Serializable{
		public Sum(){}
		/**
		 * @return returns null, but arguments are distributed to the DAC
		 * via the send_arguments call.
		 */
		public Object execute(){
			int x = (Integer)(args[0]);
			int y = (Integer)(args[1]);
			send_argument( x + y );
			return null;
		}
		
	}	
}

