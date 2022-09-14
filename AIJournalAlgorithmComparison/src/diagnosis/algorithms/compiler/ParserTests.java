/**
 * 
 */
package diagnosis.algorithms.compiler;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author fwotawa
 *
 */
public class ParserTests extends TestCase {

	/**
	 * @param name
	 */
	public ParserTests(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public static Test suite() {
	    TestSuite suite= new TestSuite();
	    suite.addTest(
	    		new ParserTests("Test 1") {
	            	protected void runTest() { testConfig1(); }
	        	});
	    suite.addTest(
	    		new ParserTests("Test 2") {
	            	protected void runTest() { testConfig2(); }
	        	});	 
	    return suite;
	}	
	
	public void testConfig1() {
		LogicParser parser = new LogicParser();
		boolean result = parser.parse("a->b.\n b :- c. c. a,b -> false. a(b). a(f(X)),b-> false.");
		Assert.assertEquals(result, true);
	}
	
	public void testConfig2() {
		LogicParser parser = new LogicParser();
		boolean result = parser.parse("a->b.\n b :- c. c a,b -> false.");
		Assert.assertEquals(result, false);
	}
}
