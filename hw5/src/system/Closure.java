package system;
import api.Task;

public class Closure {
	public Task t;
	public int joinCounter;
	public Object [] args; // is an array
	public int ID;
	public Continuation cont;
	
	public <T> Closure(Task t, int joinCounter, Continuation cont, int ID){
		this.t = (Task) t;
		this.joinCounter = joinCounter;
		this.args = new Object [joinCounter];
		this.ID = ID;
		this.cont = cont;
	}

}


