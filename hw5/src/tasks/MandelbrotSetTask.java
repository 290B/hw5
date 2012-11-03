package tasks;

import java.io.Serializable;

import api.*;


/**
 * This class is a implementations that solves the Mandelbrot set.
 * @author orein
 *
 */

public class MandelbrotSetTask extends DAC implements java.io.Serializable{
	private static final long serialVersionUID = 227L;
	
	/**
	 * 
	 * @param setCornerX is the x coordinate describing the left bottom corner of the square to calculate, typical value -2
	 * @param setCornerY is the y coordinate describing the left bottom corner of the square to calculate, typical value -2
	 * @param setEdgeLength is the length of the sides of the square starting in the left bottom corner, typical value 4
	 * @param setN is the resolution of the calculation of the image of the Mandelbrot set. Typical value 256
	 * @param setIterLimit is the iteration limit for each pixel in the calculation. Exceeding this limit stops the calculation. Typical value 64
	 * @param taskCoordX is the coordinate describing where on the x axis this part of the solution will be placed in the complete picture
	 * @param taskCoordY is the coordinate describing where on the y axis this part of the solution will be placed in the complete picture
	 */

	
	
	/**
	 * To execute the calculations 
	 * @return MandlebrotReturn is an object which contains the int [][] array with the number of iterations it took for each pixel of the task and the two coordinates that describe where in the complete picture this piece will be placed. See MandelbrotReturn doc.
	 */
	
	public class Compute extends DAC implements Task, Serializable{
		private double cornerX;
		private double cornerY;
		private double edgeLength;
		private int n;
		private int iterLimit;
		private static final long serialVersionUID = 227L;
		
		public Compute(double setCornerX, double setCornerY, double setEdgeLength, int setN, int setIterLimit){
			this.cornerX = setCornerX;
			this.cornerY = setCornerY;
			this.edgeLength = setEdgeLength;
			this.n = setN;
			this.iterLimit = setIterLimit;
		}	
		
		public Object execute(){
			int[][] count = new int[n][n]; 
			for (int i = 0; i < n; i++){
				for (int j = 0; j < n; j++){
					double cRe = cornerX+(edgeLength/n)*i;
					double cIm = cornerY+(edgeLength/n)*j;
					double re = cRe;
					double im = cIm;
					double sqrRe;
					double sqrIm;
					int k = 0;
					while (re*re + im*im <= 4 && k < iterLimit){
						sqrRe = (re*re - im*im);
						sqrIm = (2*im*re);
						re = sqrRe + cRe;
						im = sqrIm + cIm;
						k++;
					}
					count[i][n-j-1] = k; 
				}
			}
		send_argument(count);
			return null;
		}
	}
	public class Split extends DAC implements Task, Serializable{
		private double cornerX;
		private double cornerY;
		private double edgeLength;
		private int n;
		private int iterLimit;
		int depth;
		private static final long serialVersionUID = 227L;
		
		public Split(double setCornerX, double setCornerY, double setEdgeLength, int setN, int setIterLimit, int depth){
			this.cornerX = setCornerX;
			this.cornerY = setCornerY;
			this.edgeLength = setEdgeLength;
			this.n = setN;
			this.iterLimit = setIterLimit;
			this.depth = depth;
		}		
		public Object execute(){
			if (depth > 0) {
				spawn(new Split(cornerX, cornerY, edgeLength/2, n, iterLimit, depth-1));
				spawn(new Split(cornerX+ edgeLength/2, cornerY, edgeLength/2, n, iterLimit, depth-1));
				spawn(new Split(cornerX, cornerY+edgeLength/2, edgeLength/2, n, iterLimit, depth-1));
				spawn(new Split(cornerX+edgeLength/2, cornerY+edgeLength/2, edgeLength/2, n, iterLimit, depth-1));
				spawn_next(new Compose(), 4);
			}else{
				spawn(new Compute(cornerX, cornerY, edgeLength, n, iterLimit));
				spawn_next(new Compose(), 1);
			}
			
			
			return null;
		}
	}
	public class Compose extends DAC implements Task, Serializable{
		private static final long serialVersionUID = 227L;
		
		Object args;
		public Object execute() {
			Object [] results = (Object [])args;
			int [][] lowerLeft = (int [][])results[0];
			int [][] lowerRight = (int [][])results[1];
			int [][] upperLeft = (int [][])results[2];
			int [][] upperRight = (int [][])results[3];
			
			int length = lowerLeft[0].length; 
			int [][] count = new int[length*2][length*2];
			for (int x = 0; x < length; x++){
				for (int y = 0; y < length; y++){
					count[x][y] = lowerLeft[x][y];
					count[x+length][y] = lowerRight[x][y];
					count[x][y+length] = upperLeft[x][y];
					count[x+length][y+length] = upperRight[x][y];
				}
			}
			send_argument(count);
			return null;
		}
		
	}
}




