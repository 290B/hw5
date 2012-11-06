package client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import api.DAC;
import api.Space;
import system.SpaceImpl;
import system.WorkerImpl;
import tasks.FibTask;

public class FibClientImplStandalone {

	public static void main(String[] args) {
		if (System.getSecurityManager() == null ){ 
			   System.setSecurityManager(new java.rmi.RMISecurityManager() ); 
			}
			try{
				//String name = "Space";
	    		//Registry registry = LocateRegistry.getRegistry(args[0]);
	    		//Space space = (Space) registry.lookup(name);
	    		SpaceImpl space = new SpaceImpl();
				space.main(null);
				
				WorkerImpl worker = new WorkerImpl();
				String[] arg = new String[2];
				arg[0]="localhost";
				arg[1]= "1098";
				worker.main(arg);
				int [][] count = null;
	    		FibTask fibTask = new FibTask();
	    		FibTask.Fib fib = fibTask.new Fib(15);
	    		
	    		long start = System.currentTimeMillis();
	    		space.put(fib);
	    		int result = (Integer) space.take();
	    		long stop = System.currentTimeMillis();
	    		System.out.println("The result is: " + result);
	    		System.out.println("Job completion time is:   " + (stop-start) + " ms");
	    		try{
	    			space.exit();
	    			
	    		}catch(Exception e){
	    			System.out.println("Space terminated");
	    		}
	    	}catch (Exception e) {
				System.err.println("fib client exception:");
				e.printStackTrace();
			}
	}

}
