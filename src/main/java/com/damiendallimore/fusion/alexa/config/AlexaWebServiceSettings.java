package com.damiendallimore.fusion.alexa.config;

public class AlexaWebServiceSettings {

	private int httpsListenPort = 443;
	private String httpsListenScheme = "https";
	private String httpsListenEndpoint = "";
	// absolute path or filename relative to the crypto directory
	private String keystorePath = "";
	private String keystorePassword = "";
	private String skillID;

	public AlexaWebServiceSettings() {
	}

	public String getHttpsListenScheme() {
		return httpsListenScheme;
	}

	public void setHttpsListenScheme(String httpsListenScheme) {
		this.httpsListenScheme = httpsListenScheme;
	}

	public int getHttpsListenPort() {
		return httpsListenPort;
	}

	public void setHttpsListenPort(int httpsListenPort) {
		this.httpsListenPort = httpsListenPort;
	}

	public String getHttpsListenEndpoint() {
		return httpsListenEndpoint;
	}

	public void setHttpsListenEndpoint(String httpsListenEndpoint) {
		this.httpsListenEndpoint = httpsListenEndpoint;
	}

	public String getKeystorePath() {
		return keystorePath;
	}

	public void setKeystorePath(String keystorePath) {
		this.keystorePath = keystorePath;
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	public String getSkillID() {
		return skillID;
	}

	public void setSkillID(String skillID) {
		this.skillID = skillID;
	}

}
