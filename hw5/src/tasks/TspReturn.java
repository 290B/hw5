package tasks;
import java.io.Serializable;

import java.util.ArrayList;  

/**
 * The return class that the workers and space use to communicate
 * results to each other
 * 
 * @param path ArrayList of integers that indicates the sequence of towns
 * that were visited for this result
 * @param sumPathLength is the euclidean length of the path, the distance travelled
 */
public class TspReturn implements Serializable{

    static final long serialVersionUID = 227L; // Was missing 
	private double sumPathLength;
	private ArrayList<Integer> path;


    public TspReturn( ArrayList<Integer> path, double sumPathLength) 
    {
    	this.sumPathLength = sumPathLength;
    	this.path = path;
    }

    public  ArrayList<Integer> getPath() { return path; }
    
    public double getSumPathLength(){ return sumPathLength;}
    
    public void setPath (ArrayList<Integer> inPath){
    	this.path = inPath;
    }
    
    public void settSumPathLength(double inSum){
    	this.sumPathLength = inSum;
    }

    

}