package model.logic.converter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class Rule.
 */
public class Rule implements Comparable<Rule>{

	/** The head. */
	private Set<String> head;
	
	/** The body. */
	private Set<String> body;
	
	/** The representation. */
	private String representation;

	/**
	 * Gets the head.
	 *
	 * @return the head
	 */
	public Set<String> getHead() {
		return head;
	}

	/**
	 * Gets the representation.
	 *
	 * @return the representation
	 */
	public String getRepresentation() {
		return representation;
	}

	/**
	 * Sets the representation.
	 *
	 * @param representation the new representation
	 */
	public void setRepresentation(String representation) {
		this.representation = representation;
	}

	/**
	 * Instantiates a new rule.
	 */
	public Rule(){
		this.body = new HashSet<String>();
		this.head = new HashSet<String>();
	}

	/**
	 * Retrieve head.
	 *
	 * @return the sets the
	 */
	public Set<String> retrieveHead() {
		return head;
	}
	
	/**
	 * Sets the head.
	 *
	 * @param head the new head
	 */
	public void setHead(Set<String> head) {
		this.head = head;
	}
	
	/**
	 * Gets the body.
	 *
	 * @return the body
	 */
	public Set<String> getBody() {
		return body;
	}
	
	/**
	 * Sets the body.
	 *
	 * @param body the new body
	 */
	public void setBody(Set<String> body) {
		this.body = body;
	}

	/**
	 * Adds the to body.
	 *
	 * @param antecedent the antecedent
	 */
	public void addToBody(String antecedent){
		this.body.add(antecedent);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return representation;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Rule arg0) {
		return this.toString().compareTo(arg0.toString());

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((head == null) ? 0 : head.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rule other = (Rule) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (head == null) {
			if (other.head != null)
				return false;
		} else if (!head.equals(other.head))
			return false;
		return true;
	}



}
