package tasks;

import java.io.Serializable;

import api.Shared;
/**This class is used for sharing the best pathlength found between workers and space in the DAC system
 * 
 * @author torgel
 *
 */
public class SharedTsp implements Shared, Serializable{
    static final long serialVersionUID = 227L; // Was missing 
    private double tspShared;
	
    /** constructs the shared object, that is the path length
     * 
     * @param input is the inital value of the shared object, that is the initial best path length or cost
     */
	public SharedTsp(double input){
		tspShared = input;
	}
	
	public SharedTsp(){
		
	}
	/**Returns the shared object from the space, that is the newst one
	 * 
	 */
	public Object getShared(){
		return (double)tspShared;
	}
	/**Detects if the path found is shorter than the shortest path found.
	 * 
	 */
	public boolean isNewerThan(Shared input) {
		if ( (Double) input.getShared() <= this.tspShared){
			return false;
		}else{
			return true;
		}
	}
	
	public Shared clone() throws CloneNotSupportedException{
		return (Shared) super.clone();
	}
	
}
