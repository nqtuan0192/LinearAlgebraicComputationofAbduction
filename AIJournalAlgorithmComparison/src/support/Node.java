package support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Node<T> {
	    private T data;
	    private Node<T> parent;
	    private List<Node<T>> children;
	       
	    public Node() {
			super();
		}
	    
	    public Node(T data) {
			super();
			this.data = data;
			this.parent = new Node<T>();
			this.children = new ArrayList<Node<T>>();
		}
	    
	    public Node(T data, Node<T> parent) {
			super();
			this.data = data;
			this.parent = parent;
			this.children = new ArrayList<Node<T>>();
		}
	    
	    public Node(T data, Node<T> parent, List<Node<T>> children) {
			super();
			this.data = data;
			this.parent = parent;
			this.children = children;
		}

	    
	    public int getNumberOfChildren() {
	        if (this.children != null) {
	            return this.children.size();
	        }
	        return 0;
	    }
		
		public void addChild(Node<T> child){
			if (this.children != null) {
				this.children.add(child);
	        }
			else
			{
				this.children = new ArrayList<Node<T>>();
			}
		}
		
		public void addParent(Node<T> parent){
			this.parent = parent;
	   
		}
		
		
	    public void removeChildAt(T data){
	    	Iterator<Node<T>> iterator = this.children.iterator();
	        while(iterator.hasNext()){
	        	Node<T> child = iterator.next();
	        	if(child.getData().equals(data)){
	        		this.children.remove(child);
	        	}
	        }
	    }
		
		public T getData() {
			return data;
		}
		public void setData(T data) {
			this.data = data;
		}
		public List<Node<T>> getChildren() {
			return children;
		}
		public void setChildren(List<Node<T>> children) {
			this.children = children;
		}
		public Node<T> getParent() {
			return parent;
		}
		public void setParent(Node<T> parent) {
			this.parent = parent;
		}
		
		 public String toString() {
		        StringBuilder sb = new StringBuilder();
		        sb.append("{").append(getData().toString()).append(",[");
		        int i = 0;
		        for (Node<T> e : getChildren()) {
		            if (i > 0) {
		                sb.append(",");
		            }
		            sb.append(e.getData().toString());
		            i++;
		        }
		        sb.append("]").append("}");
		        return sb.toString();
		    }
	
}
