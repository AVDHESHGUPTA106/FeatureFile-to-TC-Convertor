package com.lex.FeatureClassification.model;

public class FeatureDetail {

	private String folderPath;

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	private String module;

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getFeatureFile() {
		return featureFile;
	}

	public void setFeatureFile(String featureFile) {
		this.featureFile = featureFile;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	private String featureFile;
	private String status;

	private int scenerioCount;

	public int getScenerioCount() {
		return scenerioCount;
	}

	public void setScenerioCount(int scenerioCount) {
		this.scenerioCount = scenerioCount;
	}

}
