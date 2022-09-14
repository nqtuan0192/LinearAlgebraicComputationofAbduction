package support;

import java.util.ArrayList;
import java.util.Comparator;

public class StatisticBasics {

	    public static double getMean(ArrayList<Double> data)
	    {
	        double sum = 0.0;
	        for(double a : data)
	            sum += a;
	        return sum/data.size();
	    }

	    public static double getVariance(ArrayList<Double> data)
	    {
	        double mean = getMean(data);
	        double temp = 0;
	        for(double a :data)
	            temp += (mean-a)*(mean-a);
	        return temp/data.size();
	    }

	    public static double getStdDev(ArrayList<Double> data)
	    {
	        return Math.sqrt(getVariance(data));
	    }

	    
	    public static double getMax(ArrayList<Double> data){
	    	data.sort(new Comparator<Double>() {
	    	    public int compare(Double c1, Double c2) {
	    	        return Double.compare(c1, c2);
	    	    }
	    	});
	    	
	    	return data.get(data.size()-1);
	    }
	    
	    public static  double median(ArrayList<Double> data) 
	    {
	    
	       data.sort(new Comparator<Double>() {
	    	    public int compare(Double c1, Double c2) {
	    	        return Double.compare(c1, c2);
	    	    }
	    	});
	       
	       if (data.size() % 2 == 0) 
	       {
	          return (data.get((data.size() / 2) - 1) + data.get(data.size() / 2)) / 2.0;
	       } 
	       else 
	       {
	          return data.get(data.size() / 2);
	       }
	    }
	
}
