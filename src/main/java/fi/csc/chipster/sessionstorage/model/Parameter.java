package fi.csc.chipster.sessionstorage.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import fi.csc.microarray.description.SADLSyntax.ParameterType;

@Entity // db
public class Parameter {

	@Id // db
	private String id;
	private String displayName;
	private String description;
	private ParameterType type;
	private String value;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public ParameterType getType() {
		return type;
	}
	public void setType(ParameterType type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}	
}
