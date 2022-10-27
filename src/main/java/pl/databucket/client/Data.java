package pl.databucket.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Data {

	private Long id;
	private Boolean reserved;
	private String owner;
	private Integer tagId;
	private Map<String, Object> properties;
	private Date createdAt;
	private String createdBy;
	private Date modifiedAt;
	private String modifiedBy;

	public Data(Data data) {
		this.id = data.id;
		this.reserved = data.reserved;
		this.owner = data.owner;
		this.tagId = data.tagId;
		this.properties = data.properties;
		this.createdAt = data.createdAt;
        this.createdBy = data.createdBy;
		this.modifiedAt = data.modifiedAt;
		this.modifiedBy = data.modifiedBy;
	}

	public Data() {}
	
	public Data(Long id, Integer tagId, Boolean locked, String lockedBy, Map<String, Object> properties, Date createdAt, String createdBy, Date modifiedAt, String modifiedBy) {
		this.id = id;
		this.tagId = tagId;
		this.reserved = locked;
		this.owner = lockedBy;
		this.properties = properties;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.modifiedAt = modifiedAt;
		this.modifiedBy = modifiedBy;
	}
	
	public Long getId() {
		return id;
	}

	public Boolean getReserved() {
		return reserved;
	}
	
	public void setReserved(Boolean reserved) {
		this.reserved = reserved;
	}
	
	public String getOwner() {
		return owner;
	}

	public Integer getTagId() {
		return tagId;
	}

	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}
	
	public Map<String, Object> getProperties() {
		return this.properties;
	}
	
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	public void deleteProperty(String propertyPath) {
		if (this.properties != null) {
			removeValueByPath(this.properties, propertyPath.replace("$.", ""));
		}
	}
	
	public void setProperty(String propertyPath, Object value) {
		if (properties == null)
			properties = new HashMap<>();
		
		setValueByPath(this.properties, propertyPath.replace("$.", ""), value);
	}
	
	public Object getProperty(String propertyPath) {
		if (this.properties == null)
			return null;
		else
			return getValueByPath(this.properties, propertyPath.replace("$.", ""));
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public Date getModifiedAt() {
		return modifiedAt;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}	
	
	@Override
	public String toString() {
		String dataStr = "";
		dataStr += "id: " + this.getId() + "\n";
		dataStr += "tagId: " + this.getTagId() + "\n";
		dataStr += "reserved: " + this.getReserved() + "\n";
		dataStr += "owner: " + this.getOwner() + "\n";
		dataStr += "createdAt: " + this.getCreatedAt() + "\n";
		dataStr += "createdBy: " + this.getCreatedBy() + "\n";
		dataStr += "modifiedAt: " + this.getModifiedAt() + "\n";
		dataStr += "modifiedBy: " + this.getModifiedBy() + "\n";
		dataStr += "properties: " + this.getProperties().toString();
		return dataStr;
	}
	
	@SuppressWarnings("unchecked")
	private Object getValueByPath(Object source, String path) {
		if (path.contains(".")) {
			int pos = path.indexOf(".");
			String name = path.substring(0, pos);
			String restPath = path.substring(pos + 1);
			if (source instanceof Map && ((Map<String, Object>) source).containsKey(name))
				return getValueByPath(((Map<String, Object>) source).get(name), restPath);
			else
				return null;
		} else
			if (source instanceof Map && ((Map<String, Object>) source).containsKey(path))
				return ((Map<String, Object>) source).get(path);
			else
				return null;
				
	}
	
	@SuppressWarnings("unchecked")
	private void setValueByPath(Object source, String path, Object value) {
		if (path.contains(".")) {
			int pos = path.indexOf(".");
			String name = path.substring(0, pos);
			String restPath = path.substring(pos + 1);
			if (source instanceof Map && !((Map<String, Object>) source).containsKey(name))
				((Map<String, Object>) source).put(name, new HashMap<String, Object>());
			setValueByPath(((Map<String, Object>) source).get(name), restPath, value);			
		} else 
			((Map<String, Object>) source).put(path, value);
	}
	
	@SuppressWarnings("unchecked")
	private void removeValueByPath(Object source, String path) {
		if (path.contains(".")) {
			int pos = path.indexOf(".");
			String name = path.substring(0, pos);
			String restPath = path.substring(pos + 1);
			if (source instanceof Map && ((Map<String, Object>) source).containsKey(name))
				removeValueByPath(((Map<String, Object>) source).get(name), restPath);
		} else
			if (source instanceof Map && ((Map<String, Object>) source).containsKey(path))
				((Map<String, Object>) source).remove(path);

	}
}
