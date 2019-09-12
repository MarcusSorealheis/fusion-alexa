package com.damiendallimore.fusion.alexa.config;

public class JSONResponseHandler {

	private String name = "";
	// fully qualified classname
	private String className = "";
	
	private String javascript = "";

	public JSONResponseHandler() {
	}
		

	public String getJavascript() {
		return javascript;
	}

	public void setJavascript(String javascript) {
		this.javascript = javascript;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

}

