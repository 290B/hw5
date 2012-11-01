package system;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import api.Task;

public class WorkerResult implements Serializable {
    static final long serialVersionUID = 227L;
    public Task spawn_next;
	public int spawn_nextJoin;
	public LinkedList a = new LinkedList();
	
	
	public LinkedList spawn = new LinkedList();
	public LinkedList spawnArgs = new LinkedList();
	public LinkedList spawnJoin = new LinkedList();
	
	public Object send_argument;
	
	public WorkerResult(LinkedList spawn, Task spawn_next, int spawn_nextJoin, Object send_argument){
		this.spawn = spawn;
		this.spawn_next = spawn_next;
		this.spawn_nextJoin = spawn_nextJoin;
		this.send_argument = send_argument;
	}
}
