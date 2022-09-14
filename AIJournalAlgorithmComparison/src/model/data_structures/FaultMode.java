package model.data_structures;

import java.util.Set;

public class FaultMode {
	
	// Mode(C,M)
	private String id;
	//M
	private String mode;
	
	private String original_mode;
	
	private String original_component;
	//C
	private String component;
	//E
	private Set<String> effects;
	
	private Set<String> detection_methods;
	
	public FaultMode(String id, String mode, String component,
			Set<String> effects, Set<String> detection_methods) {
		super();
		this.id = id;
		this.mode = mode;
		this.component = component;
		this.effects = effects;
		this.detection_methods = detection_methods;
	}

	public FaultMode() {
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public Set<String> getEffects() {
		return effects;
	}

	public void setEffects(Set<String> effects) {
		this.effects = effects;
	}

	public Set<String> getDetection_methods() {
		return detection_methods;
	}

	public void setDetection_methods(Set<String> detection_methods) {
		this.detection_methods = detection_methods;
	}

	
	@Override
	public String toString(){
		return id;
	}

	public String getOriginal_mode() {
		return original_mode;
	}

	public void setOriginal_mode(String original_mode) {
		this.original_mode = original_mode;
	}

	public String getOriginal_component() {
		return original_component;
	}

	public void setOriginal_component(String original_component) {
		this.original_component = original_component;
	}
	

}
