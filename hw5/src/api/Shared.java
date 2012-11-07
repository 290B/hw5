package api;
/**Creates an interface for the compute system to share objects for the purpose 
 * of conveying the best cost found in the branch and bound system
 * 
 * @author torgel
 *
 */
public interface Shared extends Cloneable{

	
	/** This function will check wheter an object is newer than the one in the space
	 * @param input is the object that will be checked against the version that is stored in the space
	 * @return is true if the space has the newest version, false otherwise
	 */
	public boolean isNewerThan(Shared input);
	/** this is called by the worker to get a fresh version of the shared object from the space
	 * 
	 * @return is the newest object availbe to the space
	 */
	public Object getShared();
	/** clones an object before sending it to the space for the purpose of mutability
	 * 
	 * @return the close
	 * @throws CloneNotSupportedException is called if the object does not implement cloning.
	 */
	public Shared clone()throws CloneNotSupportedException;
}
