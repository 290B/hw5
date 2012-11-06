package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import api.DAC;
import api.Space;
import api.Task;
import tasks.MandelbrotSetTask;

public class MandelbrotClient {
	private static final int N_PIXELS = 1024;
	private static final int ITERATION_LIMIT = 4096;
	private static final double CORNER_X = -0.7510975859375;
	private static final double CORNER_Y = 0.1315680625;
	private static final double EDGE_LENGTH = 0.01611;
	private static final int DEPTH = 2;
	
	public static void main(String[] args) {
		if (System.getSecurityManager() == null ){ 
			   System.setSecurityManager(new java.rmi.RMISecurityManager() ); 
			}
			try{
				String name = "Space";
	    		Registry registry = LocateRegistry.getRegistry(args[0]);
	    		Space space = (Space) registry.lookup(name);
	    		MandelbrotSetTask temp = new MandelbrotSetTask();
	    		Task split = temp.new Split(CORNER_X, CORNER_Y, EDGE_LENGTH, N_PIXELS, ITERATION_LIMIT, DEPTH);
	    		long start = System.currentTimeMillis();
	    		space.put(split);
	    		int [][] count = (int[][]) space.take();
	    		long stop = System.currentTimeMillis();
	    		System.out.println("Job completion time is:   " + (stop-start) + " ms");
	    		
	    		JLabel mandelbrotLabel = displayMandelbrotSetTaskReturnValue( count );
				JFrame frame = new JFrame( "Result Visualizations" );
				frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
				Container container = frame.getContentPane();
				container.setLayout( new BorderLayout() );
				container.add( new JScrollPane( mandelbrotLabel ), BorderLayout.WEST );
				frame.pack();
				frame.setVisible( true );
	    		
	    		try{
	    			space.exit();
	    			
	    		}catch(Exception e){
	    			System.out.println("Space terminated");
	    		}
	    	}catch (Exception e) {
				System.err.println("fib client exception:");
				e.printStackTrace();
			}
	}
	
	private static JLabel displayMandelbrotSetTaskReturnValue( int[][] counts )
	{
	    Image image = new BufferedImage( N_PIXELS, N_PIXELS, BufferedImage.TYPE_INT_ARGB );
	    Graphics graphics = image.getGraphics();
	    for ( int i = 0; i < counts.length; i++ )
	    for ( int j = 0; j < counts.length; j++ )
	    {
	        graphics.setColor( getColor( counts[i][j] ) );
	        graphics.fillRect(i, j, 1, 1);
	    }
	    ImageIcon imageIcon = new ImageIcon( image );
	    return new JLabel( imageIcon );
	}

	private static Color getColor( int i )
	{
	    if ( i == ITERATION_LIMIT )
	        return Color.BLACK;
	    return Color.getHSBColor((float)i/ITERATION_LIMIT, 1F, 1F);
	}

}
