package diagnosis.algorithms.hittingsetalg;

/**
 * This class represents a BHS-tree(node). It can have left and/or right branches. At this level, it 
 * has 2 parts C and H. C is always a minimized cluster set. The subsets that
 * were pruned away by minimizing are kept internally. H is always one number or empty. 
 * 
 * @author fry
 */
abstract public class SubSetTree {

/************************************************
 * 			MEMBER VARIABLES
 ************************************************/
    
	private SubSetContainer C_;
	private Object H_ = EMPTY;
	private SubSetContainer removedSetsThroughMinimizing_;  //remove??
	
	protected SubSetTree 	left_branch_, 
										right_branch_;

	//for debug purposes only
	static private boolean debug = false;

	//constant indicating an empty H_
	static public final Object EMPTY = new String("");
	
	//for printing purposes
	static public final int NBLINES = 2;
	static protected boolean [] print_lines;
	

/************************************************
 * 		CONSTRUCTORS
 ************************************************/

	/*
	 * Dummy constructor. Does not initialize anything.
	 */
	protected SubSetTree(){
		removedSetsThroughMinimizing_=new SubSetContainer();
	};

	/*
	 * Create a new Tree object with both right- and lefttree set to null.
	 * C and are set to the given parameters.
	 * C is minimised in this step, i.e. the non minimal cs are removed.
	 */
	protected SubSetTree(SubSetContainer MCS, Object setClusters){
		this();
		//TODO: Maybe make a clone here?
		C_ = MCS;
		H_ = setClusters;
		C_.minimize(removedSetsThroughMinimizing_ );
		left_branch_ = null;
		right_branch_ = null;
	}

	/*
	 * Create new Tree with right- and lefttree set to null. H is set to EMPTY. 
	 * C has the given value.
	 */
	protected SubSetTree(SubSetContainer MCS){
		this(MCS, EMPTY);
	}

/************************************************
 * 			PRIMITIVE GET-SET
 ************************************************/

	/*
	 * Return a reference to C_.
	 */
	protected SubSetContainer getC(){
		return C_;
	}

	protected SubSetContainer getRemovedThroughMinimizing(){
		return removedSetsThroughMinimizing_;
	}

	protected Object getH(){
		return H_;
	}

/************************************************
 * 					METHODS
 ************************************************/

	/*
	 * Return a random element from the elements of the subsets of the container.
	 */
	static public Object getRandomElement(SubSetContainer ss){
		// For now, return first element of the last subset.
		return ss.getVector(ss.size() - 1).get(0); 
	}

	/*
	 * Return the depth of the tree.
	 */
	protected abstract int getDepth(int depth_of_parent);
	public abstract int getDepth();

	/*
	 * The actual minimal hitting set calculation method.
	 * returned is a SubSetContainer containing the minimal hitting sets ;)
	 */
	public abstract SubSetContainer getMinimalHittingSet();
	
/************************************************
 * 					PRINTING METHODS
 ************************************************/

	/*
	 * Print the treestucture
	 */
	protected void print(int depth){
		int indent = 4;
		for(int i=0; i<depth; i++) {
			if(print_lines[i]){
				System.out.print("|");
			}else{
				System.out.print(" ");
			}
			for(int j = 1;j<indent; j++){
				System.out.print(" ");
			}
		}
	}

	protected void print(){};

	static protected void init_printlines(int depth){
		print_lines = new boolean [depth];
	}

	static protected void set_printline(int nb, boolean value){
		print_lines[nb] = value;
	}

/************************************************
 *		 			HELP METHODS
 ************************************************/

	/*
	 * Set the debugflags for the Tree-methods.
	 */
	public static void setDebugEnabled(boolean enabled){
		debug=enabled;
	}

	protected void debugPrint(String s){
		if(debug){
			System.out.println(s);
		}
	}

}
