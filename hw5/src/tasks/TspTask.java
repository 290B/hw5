package tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.Serializable;

import api.DAC;
import api.Task;
import api.Shared;

import tasks.TspReturn;
import tasks.SharedTsp;

/**
 * This class implements a Traveling Salesman Problem solver as a task
 * which fits into the RMI framework implemented in the API.
 * 
 * The solver simply brute forces and finds all possible routes in both 
 * directions and then evaluates them all to find the most efficient one.
 * 
 * this class contains the less general TspTaks and TspCompose
 * 
 * @author torgel
 *
 */
public class TspTask implements Serializable{
	private static final long serialVersionUID = 227L;		
	private static final double inf = 10000; 
	
	public SharedTsp sharedTsp;
	public TspReturn currentBestValues = new TspReturn(new ArrayList<Integer>() , inf);
	public Shared sharedLocal;
	
	/**
	 * Takes a TspTask containing a TspInputArg which can be executed
	 * @author torgel
	 *
	 */
	public class TspExplorer extends DAC implements Task, Serializable{
		private static final long serialVersionUID = 227L;		
		public Object [] args;
		public TspExplorer(Object... args){this.args = args;}
		public TspExplorer(){}
	
	    public ArrayList<Integer> path;
	    public double [][] distances;
	    public double sumPathLength;
	    public ArrayList<Integer> allTowns;
	    public int levelToSplitAt;
	    public double currentShortestPathLength = 1000000;
	    public ArrayList<Integer> currentShortestPath = new ArrayList<Integer>();
	    

		/**
		 * Executes the TspTask which deals with splitting the task up and calculating
		 * shortest paths of sufficiently small sub-trees. uses spawn and spawn_next to 
		 * spawn other smaller tasks and composer tasks.
		 * 
		 * @return returns null, but uses send_argument to distribute TspResults to the 
		 * composer tasks via the space
		 * 
		 */
		public Object execute() {

			sharedTsp = (SharedTsp)getShared();
			
			//System.out.println("shared is " + sharedTsp.getShared());
			
			//currentBestValues.settSumPathLength((Double) sharedTsp.getShared());

			TspInputArg in = (TspInputArg)args[0];

				

			path = in.getPath();
		    distances = in.getDistances();
		    sumPathLength = in.getSumPathLength() ;
		    allTowns = in.getAllTowns();
		    levelToSplitAt = in.getLevelToSplitAt() ;    
		  		
		    
		  if (path.size() == 1){
				setShared(findInitialShortPath());
				System.out.println("Should be less than 30...  :  " + getLeastRemaning(path, allTowns, distances));
				
			}
		   
		    if (path.size() < levelToSplitAt){ //The tree is still too big to be computed localy, try to split
		    	
		    	if (path.size() == distances.length){  //path at maximum length, compute that single path locally
		    	   	TspReturn res = localTsp(in);
					send_argument(res);
		    	}
		    	
		    	else { 
			    	//Explore more of the tree, that is add more elements to path and ant split the task up. 
			    	//Also add the traversed Length so far
			    		    	
					int numComposeArguments = 0;

					//for every child not on the travelled path
					for (Integer town : allTowns){
						if (!path.contains(town)){
							ArrayList<Integer> newPath = new ArrayList<Integer>();
							newPath.addAll(path);
							newPath.add(town);	    	

							double newSumPath = sumPathLength+(distances[path.get(path.size()-1)][newPath.get(newPath.size()-1)]);  //distance between the next town to visit and the previous one
							//System.out.println("newPath" +newPath+" with length " + newSumPath);	
							
							//if (newSumPath < currentBestValues.getSumPathLength()){
							
							if (newSumPath + getLeastRemaning(newPath, allTowns, distances) <= (Double) sharedTsp.getShared()){ //TODO HERE
							
								//currentBestValues.settSumPathLength(newSumPath);
								currentBestValues.setPath(newPath);
								
								spawn(new TspExplorer((Object)new TspInputArg(newPath, distances, newSumPath, allTowns ,levelToSplitAt)));
								//TODO check if dynamic matters
								numComposeArguments++;
							}
							//spawn(new TspExplorer((Object)new TspInputArg(newPath, distances, newSumPath, allTowns ,levelToSplitAt)));
							//numComposeArguments++;
						}
					}
			    
					if (numComposeArguments == 0){
						//System.out.println("MISTENKT");
						send_argument(new TspReturn(null,inf));
						
					}else {
						spawn_next(new TspComposer(), numComposeArguments); 
					}
					numComposeArguments=0;
		    	}
		    	

		    	
		    }
		    else { // Compute the given task locally
		    	TspReturn res = localTsp(in);
				send_argument(res);
		    }
			return null;
		}
		
		/**
		 * Performs the local tsp when the job is sufficiently divided
		 * @param inn is the input to the subtask that is to be executed locally
		 * @return a TspReturn object with the best path found and the length of it
		 */
		public TspReturn localTsp(TspInputArg inn){
			
			sharedTsp = (SharedTsp)getShared();
		    ArrayList<Integer> path = inn.getPath();
		    double [][] distances = inn.getDistances();
		    double sumPathLength = inn.getSumPathLength();
		    ArrayList<Integer> allTowns = inn.getAllTowns();
			
		
			if (path.size() < distances.length){
				
				//for every child on path, that is every town except those visited on the path so far				
				for (Integer town : allTowns){
					if (!path.contains(town)){
						ArrayList<Integer> newPath = new ArrayList<Integer>();
						newPath.addAll(path);
						newPath.add(town);	    	

						double newSumPath = sumPathLength+(distances[path.get(path.size()-1)][newPath.get(newPath.size()-1)]);  //distance between the next town to visit and the previous one
						//System.out.println("newPath" +newPath+" with length " + newSumPath);	
						//TspExplorer localTask = new TspExplorer((Object)new TspInputArg(newPath, distances, newSumPath, allTowns ,levelToSplitAt));
						//System.out.println("Upper bound:   " + (newSumPath + getLeastRemaning(newPath, allTowns, distances)));
						
						if (newSumPath + getLeastRemaning(newPath, allTowns, distances) < (Double) sharedTsp.getShared()){
							localTsp(new TspInputArg(newPath, distances, newSumPath, allTowns ,levelToSplitAt));
						}
						
						//localTask.execute();
						

						//return new TspReturn(currentShortestPath, currentShortestPathLength);
					}
				}				
			}
			else if (path.size() == distances.length){
				sumPathLength += (distances[path.get(path.size()-1)][0]); //adding the length back to town -
				
				if (sumPathLength <= (Double) sharedTsp.getShared()){
					currentShortestPathLength = sumPathLength;
					ArrayList<Integer> tempPath = new ArrayList<Integer>();
					tempPath.addAll(path);
					currentShortestPath = tempPath;
					setShared(new SharedTsp(sumPathLength));
				}
			}			
			return new TspReturn(currentShortestPath, currentShortestPathLength);
		}
		
		public double getLeastRemaning(ArrayList<Integer> path, ArrayList<Integer> allTowns, double[][] distances){
			double edgeSum = 0;
			for (int i= 0; i < allTowns.size(); i++){ // Finds two shortest edges from each unvisited edge
				if (!path.contains(allTowns.get(i))){
					double least1 = 20000;
					double least2 = 10000;
					for (int j = 0; j < allTowns.size(); j++){
						
						if (i!=j && ((!path.contains(allTowns.get(j)) || (allTowns.get(j) == 0 || (allTowns.get(j) == path.get(path.size()-1)))))){
							if (distances[i][j] < least1){
								System.out.println("First shortest");
								least2 = least1;
								least1 = distances[i][j];
							}
						}
					}
					edgeSum += least1 + least2;
				}
				
			}
			double least = 30000;
			int i = 0; // first node
			for (int j = 0; j < allTowns.size(); j++){ // edge from start edge to edge not visited
				
				if (!path.contains(allTowns.get(j))){
					if (distances[i][j] < least){
						System.out.println("Second shortest");
						least = distances[i][j];
					}
				}
			}
			edgeSum += least;
			least = 1000;
			i = path.size();
			for (int j = 0; j < allTowns.size(); j++){ // edge from start edge to edge not visited
				if (!path.contains(allTowns.get(j))){
					if (distances[i][j] < least){
						System.out.println("Third shortest");
						least = distances[i][j];
					}
				}
			}
			edgeSum += least;
			
			
			edgeSum = edgeSum/2;
			return edgeSum;	
		}
			
		
		public Shared findInitialShortPath (){
			
			ArrayList<Integer> newPath = new ArrayList<Integer>();
			newPath.add(0);
			double totalDistance = 0;
			double distanceToClosestTown = inf;
			Integer closestTown = 0;
			double tempDistance;
			
			for(int i = 0 ; i < distances.length-1;i++){ // TODO, should the -1 be there?
				for (Integer town : allTowns){
					if (!newPath.contains(town)){
						tempDistance = distances[town][newPath.get(newPath.size()-1)];  										
						if (tempDistance < distanceToClosestTown){
							distanceToClosestTown = tempDistance;
							closestTown = town;						
						}
					}
				}
				newPath.add(closestTown);
				totalDistance += distanceToClosestTown;
				distanceToClosestTown = inf;
			}
			
			totalDistance += distances[newPath.get(newPath.size()-1)][newPath.get(0)]; // add length back to root town
			//System.out.println("distances " + totalDistance);
			newPath.add(0);
			//System.out.println(newPath);
			
			System.out.println("initial distance: "+totalDistance);
			
			//currentBestValues.settSumPathLength(totalDistance);
			//currentBestValues.setPath(newPath);
			
			
			
			return new SharedTsp(totalDistance);
		}
	}

	
	
	/**
	 * The Tsp Compose task takes an array of objects which is actually
	 * TspReturn objects and finds the shortest path amongst the input paths.
	 * It then sends this result to the next composer task by using 
	 * send_argument. This continues until the root composer task receives
	 * all it's input. 
	 * 
	 * @author torgel
	 *
	 */
	public class TspComposer extends DAC implements Task, Serializable{
		private static final long serialVersionUID = 227L;		
		public TspComposer(Object ... args){this.args = args;}
		public TspComposer(){}
		/**
		 * Executes the compose task and sends arguments to other compose tasks
		 * 
		 * @return returns null, but uses send_arguments to distribute TspResult objects
		 * to the other composer tasks
		 */
		public Object execute(){			
			TspReturn inputVal;
			ArrayList<Integer> currentShortestPath = new ArrayList<Integer>();
			double currentShortestPathLength = inf;
			
			for (int i = 0; i < args.length ; i++){
				inputVal = (TspReturn)args[i];
				if (inputVal.getSumPathLength() < currentShortestPathLength){
					currentShortestPathLength = inputVal.getSumPathLength();
					currentShortestPath = inputVal.getPath();
				}
			}
			
			//System.out.println("short path  " + currentShortestPath);
			//System.out.println(" short path len " + currentShortestPathLength);
			
			TspReturn ret = new TspReturn(currentShortestPath,currentShortestPathLength);
			send_argument(ret);
			return null;
		}
	}
	
	
}

/*
s



*/