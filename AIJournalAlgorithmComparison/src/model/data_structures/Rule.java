package model.data_structures;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Rule implements Comparable<Rule>{
	
	private String head;
	private Set<String> body;
	
	public Rule(){
		this.body = new HashSet<String>();
	}
	
	public String retrieveHead() {
		return head;
	}
	public void setHead(String head) {
		this.head = head;
	}
	public Set<String> getBody() {
		return body;
	}
	public void setBody(Set<String> body) {
		this.body = body;
	}
	
	public void addToBody(String antecedent){
		this.body.add(antecedent);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		Iterator<String> i = body.iterator();
		while(i.hasNext()){
			sb.append(i.next());
			if(i.hasNext())
			sb.append(",");
		}	
		if(!head.equals(""))
			sb.append("->");
		sb.append(head);
		sb.append(".");
		
		return sb.toString().replace("\n", "");
	}

	@Override
	public int compareTo(Rule arg0) {
		return this.toString().compareTo(arg0.toString());
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((head == null) ? 0 : head.hashCode());
		return result;
	}

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
