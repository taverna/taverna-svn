package org.myexp_whip_plugin;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Workflow extends Resource {
	
	private int id;
	
	private int version;
	
	private String title;
	
	private String description;
	
	private User uploader;
	
	private Date createdAt;
	
	private Date updatedAt;
	
	private URI preview;
	
	private URI thumbnail;
	
	private URI thumbnailBig;
	
	private URI svg;
	
	private License license;
	
	private URI contentUri;
	
	private String contentType;
	
	private List<Tag> tags = new ArrayList<Tag>();
	
	private List<User> credits = new ArrayList<User>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public User getUploader() {
		return uploader;
	}

	public void setUploader(User uploader) {
		this.uploader = uploader;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public URI getPreview() {
		return preview;
	}

	public void setPreview(URI preview) {
		this.preview = preview;
	}

	public URI getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(URI thumbnail) {
		this.thumbnail = thumbnail;
	}

	public URI getSvg() {
		return svg;
	}

	public void setSvg(URI svg) {
		this.svg = svg;
	}

	public License getLicense() {
		return license;
	}

	public void setLicense(License license) {
		this.license = license;
	}

	public URI getContentUri() {
		return contentUri;
	}

	public void setContentUri(URI contentUri) {
		this.contentUri = contentUri;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public List<User> getCredits() {
		return credits;
	}

	public URI getThumbnailBig() {
		return thumbnailBig;
	}

	public void setThumbnailBig(URI thumbnailBig) {
		this.thumbnailBig = thumbnailBig;
	}
	
}
