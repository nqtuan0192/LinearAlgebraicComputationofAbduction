package diagnosis.algorithms.hittingsetalg;

import java.util.Vector;

/**
 * The interface for diagnosis systems to be used by the HS tree algorithm. 
 * All objects and diagnosis system that should make use of the HS tree
 * algorithm must implement this interface.
 *
 * @version 1.0, Date 15.03.2000
 * @author Franz Wotawa
 * @see modis.PDiagnosisSystem
 * @see modis.CollectionGraphSystem
 */
public interface DiagnosisGraphSystem {

    public Vector diagnosisComponents();
    public Vector computeConflicts(Vector hittingset);
    public int maxDiagnosisSize();
    public int maxNumberOfDiagnoses();
}
