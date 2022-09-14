package support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

public class Printer {
	
	public static <E> void printHashSet(HashSet<E> set){
		Iterator<E> i = set.iterator();
		System.out.print("{");
		while(i.hasNext()){		
			System.out.print(i.next());
			if(i.hasNext()){
				System.out.print(",");
			}
		}
		System.out.print("}");	
	}
	
	public static <E> void printHashSetHashSet(HashSet<HashSet<E>> set){
		Iterator<HashSet<E>> i = set.iterator();
		System.out.print("{");
		while(i.hasNext()){		
			printHashSet(i.next());
			if(i.hasNext()){
				System.out.print(",");
			}
		}
		System.out.println("}");	
	}
	
	public static <E> void printArrayList(ArrayList<E> list){
		Iterator<E> i = list.iterator();
		System.out.print("{");
		while(i.hasNext()){		
			System.out.print(i.next());
			if(i.hasNext()){
				System.out.print(",");
			}
		}
		System.out.print("}");	
	}
	
	public static <E> void printArrayListArrayList(ArrayList<ArrayList<E>> list){
		Iterator<ArrayList<E>> i = list.iterator();
		System.out.print("{");
		while(i.hasNext()){		
			printArrayList(i.next());
			if(i.hasNext()){
				System.out.print(",");
			}
		}
		System.out.println("}");	
	}
	
	public static <E> void printCollection(Collection<E> list){
		Iterator<E> i = list.iterator();
		
		while(i.hasNext()){		
			System.out.print(i.next());
			if(i.hasNext()){
				System.out.print(",");
			}
		}
		System.out.println();
	}
	
	public static <V, K> void printHashMap(HashMap<K,V> map){
		for(Entry<K, V> entry:map.entrySet()){
			System.out.println(entry.getKey() + " : "+entry.getValue());
		}
		System.out.println();
	}
	
	public static <T> void printCollectionCollection(Collection<Collection<T>> list){
		Iterator<Collection<T>> i = list.iterator();
		System.out.print("{");
		while(i.hasNext()){		
			printCollection(i.next());
			if(i.hasNext()){
				System.out.print(",");
			}
		}
		System.out.println("}");	
	}
	
	/**
	 * Prints content to a file
	 * @param file
	 * @param content
	 */
	public static void printToFile(File file,String content, boolean append_to_end) {

		try {
			if (!file.exists()) {
				file.createNewFile();
			} 
			BufferedWriter outputStream = new BufferedWriter(new FileWriter(file, append_to_end));
			outputStream.write(content);
			outputStream.close();

		} catch (IOException e) {
			System.out.println("Fault while handling the results file");
			e.printStackTrace();
		}
	}
	
	/**
	 * Prints content to a file
	 * @param directory
	 * @param filename
	 * @param content
	 */
	public static void printToFile(String directory, String filename,String content, boolean append_to_end) {
		File file = new File(directory+filename);	
		try {
			if (!file.exists()) {
				file.createNewFile();
			} 
			BufferedWriter outputStream = new BufferedWriter(new FileWriter(file, append_to_end));
			outputStream.write(content);
			outputStream.close();

		} catch (IOException e) {
			System.out.println("Fault while handling the results file");
			e.printStackTrace();
		}
	}
}
