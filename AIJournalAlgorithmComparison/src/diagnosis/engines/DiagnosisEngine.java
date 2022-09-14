package diagnosis.engines;

import java.io.File;
import java.util.HashSet;

import diagnosis.data_structures.Diagnosis;

public interface DiagnosisEngine {

	public double getDeltaTime();

	public HashSet<Diagnosis> getDiag();

	public HashSet<HashSet<String>> getDiagnoses();

	public String getError();

	public String getStats();

	public boolean isTimeout();

	/**
	 * Computes the diagnoses for a given model.
	 * 
	 * @param model
	 *            File containing the representation of the model
	 * @param observations
	 *            Set of observations
	 * @param negatedObservations
	 *            Set of observations not observable
	 * @param timeout
	 *            Timeout in seconds (if < 0 then there is no timeout)
	 */
	public void startComputation(File model_file, HashSet<String> observations, HashSet<String> negatedObservations,
			long timeout);

	/**
	 * Computes the diagnoses for a given model.
	 * 
	 * @param model
	 *            String representation of the model
	 * @param observations
	 *            Set of observations
	 * @param negatedObservations
	 *            Set of observations not observable
	 * @param timeout
	 *            Timeout in seconds (if < 0 then there is no timeout)
	 */
	public void startComputation(String model, HashSet<String> observations, HashSet<String> negatedObservations,
			long timeout);
}
