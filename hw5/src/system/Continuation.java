package system;

import java.io.Serializable;

public class Continuation implements Serializable {
	private static final long serialVersionUID = 227L;
	public int ID;
	public int argNumber;
	public Continuation(int ID, int argNumber){
		this.ID = ID;
		this.argNumber = argNumber;
	}
}
