package sim.app.exploration.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import sim.app.exploration.agents.ExplorerAgent;
import sim.app.exploration.agents.MapperAgent;
import sim.app.exploration.env.SimEnvironment;
import sim.app.exploration.objects.Prototype;
import sim.app.exploration.objects.SimObject;
import sim.app.exploration.objects.KnnObject;
import sim.util.Int2D;

public class Utils {

	public static final double MAX_SIZE = 10.0;
	public static final double COLOR_DIST = (100*100)*3;
	
	
	public static double interestFunction(double prob){
		double interest;
		
		// 2 - multiplying factor to make it scale better =P
		interest = Math.min(1, Math.tanh(2*prob));
		
		return interest;
	}
	
	public static double entropy(Vector<Double> probs){
		double e = 0;
		
		for(double prob : probs){
			if (prob==0) prob = 0.0001;
			e += prob * Math.log10(prob);
		}
		
		return -e;
	}
	
	public static double saturate(double corr, int nOcurrs){
		double sat = (Math.tanh( (nOcurrs-5)/2.0 ) + 1.0 )/2;
		//new idea
		//double sat = (Math.tanh( (Math.log(nOcurrs)-5)/2.0 ) + 1.0 );
		corr = corr*sat;
		
		return corr;
	}
	
	public static double getDistance(ExplorerAgent agent, SimObject obj){
		double d;
		
		if(obj == null){
			System.out.println(obj);
		}
		
		d = agent.getLoc().distance(obj.getLoc());
		
		return d;
	}

	public static Class getHighestProb(Hashtable<Class, Double> probs) {
		double maxProb = 0;
		Class maxClass = null;
		
		for(Class c: probs.keySet()){
			if(probs.get(c) > maxProb && c != SimObject.class){
				maxProb = probs.get(c);
				maxClass = c;
			}
		}
		
		return maxClass;
	}

	public static double colorDistance(Color color, Color color2) {
		double r1 = color.getRed(); double g1 = color.getGreen(); double b1 = color.getBlue();
		double r2 = color2.getRed(); double g2 = color2.getGreen(); double b2 = color2.getBlue();
		
		double dist = (r1-r2)*(r1-r2)+(g1-g2)*(g1-g2)+(b1-b2)*(b1-b2);
		
		
		dist = dist/COLOR_DIST;
		
		if (dist>1) dist = 1;
		
		return dist;
	}

	public static double colorDistance(Color color, int r, int g, int b) {
		double r1 = color.getRed(); double g1 = color.getGreen(); double b1 = color.getBlue();
		
		double dist = (r1-r)*(r1-r)+(g1-g)*(g1-g)+(b1-b)*(b1-b);
		
		
		dist = dist/COLOR_DIST;
		
		if (dist>1) dist = 1;
		
		return dist;
	}

	public static Color avgColor(Color color, Color color2, int nOccurrs) {
		double r1 = color.getRed(); double g1 = color.getGreen(); double b1 = color.getBlue();
		double r2 = color2.getRed(); double g2 = color2.getGreen(); double b2 = color2.getBlue();
		
		Color avg = new Color((int)((r1*nOccurrs+r2)/(nOccurrs+1)), (int)((g1*nOccurrs+g2)/(nOccurrs+1)), (int)((b1*nOccurrs+b2)/(nOccurrs+1)));
				
		return avg;
	}
	
	public static int getRandomRange(int baseline, int delta) {
		if (delta == 0) return baseline;
		return (new Random()).nextInt(2*delta + 1) + (baseline - delta);
	}
	
	public static double getRandomRange(double baseline, double delta) {
		if (delta == 0) return baseline;
		return Math.random()*(2*delta) + (baseline - delta);
	}

	public static double getKNN(Class[][] identifiedObjects, Prototype prot, MapperAgent mapper, SimEnvironment env,int k, SimObject unknownObj) {
		double dist=0;
		SimObject obj;
		ArrayList<KnnObject> objects = new ArrayList();
		
		//create list of near objects to unknown object
		for(int i=0;i<400;i++){
			for(int j=0;j<300;j++){
				if(mapper.isIdentified(new Int2D(i,j))){
					obj=env.identifyObject(new Int2D(i,j));
					dist=0;
					dist += colorDistance(unknownObj.getColor(), obj.getColor()) * colorDistance(unknownObj.getColor(), obj.getColor());
					dist += Math.abs(obj.getSize() - unknownObj.getSize()) * Math.abs(obj.getSize() - unknownObj.getSize());
					if(objects.size()<10){
						objects.add(new KnnObject(dist, obj.getClass()));
						//sorting now so that we always start with a sorted list in the else
						Collections.sort(objects,(o1,o2)-> Double.compare(o1.getDist(),o2.getDist()));
					}
					else{
						if(dist < objects.get(k-1).getDist()){
							objects.set(k-1, new KnnObject(dist, obj.getClass()));
							Collections.sort(objects,(o1,o2)-> Double.compare(o1.getDist(),o2.getDist()));
						}						
					}					
				}
			}
		}
		
		//count how many objects of the prototype class are inside there
		double count=0;
		//System.out.println("=============");
		//System.out.println(prot.thisClass);
		for(int i=0;i<k;i++){
			//System.out.println(objects.get(i).getThisClass());
			if(objects.get(i).getThisClass()==prot.thisClass){
				count ++;
			}
		}
		//System.out.println(count/k);
		return count/k;
	}
}
