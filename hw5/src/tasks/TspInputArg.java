package tasks;
import java.io.Serializable;

import java.util.ArrayList;
/**
 * This is the class which is used by workers, clients and space to communicate
 * input parameters to Tsp tasks. Mostly used to get a clean interface with the
 * generics used in the DAC framework.
 * 
 * @param path is the sequence of cities that have been traversed so far
 * @param distances is the array of distances between pairs of towns
 * @param sumPathLength is the euclidean distance traveled by the path
 * @param allTowns list of all the towns (indexes) , 1...N
 * @param levelToSplitAt is an identifier that tells the task executor the
 * level at which to stop splitting the tasks into more tasks. This int counts
 * "from the top", that is a level of 0 will spawn only ONE task. a level of 10
 * will spawn a -huge- amount of tasks.
 */
public class TspInputArg implements Serializable{

    static final long serialVersionUID = 227L; // Was missing 
    double[][] distances; 
	private ArrayList<Integer> path;
	private double sumPathLength;
	private ArrayList<Integer> allTowns;
	private int levelToSplitAt;


    public TspInputArg( ArrayList<Integer> path, double [][] distances, double sumPathLength, ArrayList<Integer> allTowns, int levelToSplitAt)
    {
    	this.distances = distances;
    	this.path = path;
    	this.sumPathLength = sumPathLength;
    	this.allTowns = allTowns;
    	this.levelToSplitAt = levelToSplitAt;
    	
    }

    public ArrayList<Integer> getPath() { return path; }
    
    public double [][] getDistances(){ return distances;}
    
    public double getSumPathLength() {return sumPathLength;}
    
    public ArrayList<Integer> getAllTowns(){ return allTowns; }
    
    public int getLevelToSplitAt() {return levelToSplitAt; }

    

}