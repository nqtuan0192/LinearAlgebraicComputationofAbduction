package support;
/**
 * Debugging class for creating debug output
 *
 */
public class Debugger {
		private static int verbosity = 0; 

	    public static void log(int verbosity_level, String text){
	    	if(verbosity_level < 0){
	    		//error message
	    		System.err.print("Error: " + text);
	    	}
	    	else if(verbosity_level <= verbosity){
	        	System.out.print(text);
	        }
	    }
	    
	    public static void setVerbosity(int v){
	    	verbosity = v;
	    }
}
