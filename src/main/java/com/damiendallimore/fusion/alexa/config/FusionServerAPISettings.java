package com.damiendallimore.fusion.alexa.config;

public class FusionServerAPISettings {

	private String fusionScheme = "http";
	private String fusionHost = "";
	private int fusionPort = 8764;
	private String fusionUser = "";
	private String fusionPassword = "";

	public FusionServerAPISettings() {
	}

	
	public String getFusionScheme() {
		return fusionScheme;
	}


	public void setFusionScheme(String fusionScheme) {
		this.fusionScheme = fusionScheme;
	}


	public String getFusionHost() {
		return fusionHost;
	}

	public void setFusionHost(String fusionHost) {
		this.fusionHost = fusionHost;
	}

	public int getFusionPort() {
		return fusionPort;
	}

	public void setFusionPort(int fusionPort) {
		this.fusionPort = fusionPort;
	}

	public String getFusionUser() {
		return fusionUser;
	}

	public void setFusionUser(String fusionUser) {
		this.fusionUser = fusionUser;
	}

	public String getFusionPassword() {
		return fusionPassword;
	}

	public void setFusionPassword(String fusionPassword) {
		this.fusionPassword = fusionPassword;
	}

}
