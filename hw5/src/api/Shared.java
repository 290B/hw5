package api;

public interface Shared extends Cloneable{

	public boolean isNewerThan(Shared input);
	
	public Object getShared();
	
	public Shared clone()throws CloneNotSupportedException;
}
