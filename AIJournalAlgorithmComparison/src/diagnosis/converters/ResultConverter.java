package diagnosis.converters;

import java.util.HashSet;

import diagnosis.data_structures.Diagnosis;

public interface ResultConverter {
	
	
	public HashSet<Diagnosis> returnDiagnoses();
	public  HashSet<HashSet<String>> returnResultAsStrings();

}
