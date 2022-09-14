package model.source.data_structures;

import java.util.Set;


/**
 * The Class FaultMode represents a simple fault mode.
 */
public class FaultMode {

	/** The id. */
	// Mode(C,M)
	private String id;
	
	/** The mode. */
	//M
	private String mode;

	/** The original_mode. */
	private String original_mode;

	/** The original_component. */
	private String original_component;
	
	/** The component. */
	//C
	private String component;
	
	/** The effects. */
	//E
	private Set<String> effects;

	/** The observations_logic. */
	private String observations_logic;

	/** The detection_methods. */
	private Set<String> detection_methods;

	/**
	 * Instantiates a new fault mode.
	 *
	 * @param id the id
	 * @param mode the mode
	 * @param component the component
	 * @param effects the effects
	 * @param detection_methods the detection_methods
	 */
	public FaultMode(String id, String mode, String component,
			Set<String> effects, Set<String> detection_methods) {
		super();
		this.id = id;
		this.mode = mode;
		this.component = component;
		this.effects = effects;
		this.detection_methods = detection_methods;
	}

	/**
	 * Instantiates a new fault mode.
	 */
	public FaultMode() {
		// TODO Auto-generated constructor stu
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets the mode.
	 *
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * Sets the mode.
	 *
	 * @param mode the new mode
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * Gets the component.
	 *
	 * @return the component
	 */
	public String getComponent() {
		return component;
	}

	/**
	 * Sets the component.
	 *
	 * @param component the new component
	 */
	public void setComponent(String component) {
		this.component = component;
	}

	/**
	 * Gets the effects.
	 *
	 * @return the effects
	 */
	public Set<String> getEffects() {
		return effects;
	}

	/**
	 * Sets the effects.
	 *
	 * @param effects the new effects
	 */
	public void setEffects(Set<String> effects) {
		this.effects = effects;
	}

	/**
	 * Gets the detection_methods.
	 *
	 * @return the detection_methods
	 */
	public Set<String> getDetection_methods() {
		return detection_methods;
	}

	/**
	 * Sets the detection_methods.
	 *
	 * @param detection_methods the new detection_methods
	 */
	public void setDetection_methods(Set<String> detection_methods) {
		this.detection_methods = detection_methods;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return id;
	}

	/**
	 * Gets the original_mode.
	 *
	 * @return the original_mode
	 */
	public String getOriginal_mode() {
		return original_mode;
	}

	/**
	 * Sets the original_mode.
	 *
	 * @param original_mode the new original_mode
	 */
	public void setOriginal_mode(String original_mode) {
		this.original_mode = original_mode;
	}

	/**
	 * Gets the original_component.
	 *
	 * @return the original_component
	 */
	public String getOriginal_component() {
		return original_component;
	}

	/**
	 * Sets the original_component.
	 *
	 * @param original_component the new original_component
	 */
	public void setOriginal_component(String original_component) {
		this.original_component = original_component;
	}

	/**
	 * Gets the observations_logic.
	 *
	 * @return the observations_logic
	 */
	public String getObservations_logic() {
		return observations_logic;
	}

	/**
	 * Sets the observations_logic.
	 *
	 * @param observations_logic the new observations_logic
	 */
	public void setObservations_logic(String observations_logic) {
		this.observations_logic = observations_logic;
	}


}
