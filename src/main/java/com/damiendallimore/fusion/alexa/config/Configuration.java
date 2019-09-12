package com.damiendallimore.fusion.alexa.config;

import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Object to hold the parsed JSON configuration file (configuration.json)
 * 
 * @author damien
 *
 */
public class Configuration {

	private AlexaWebServiceSettings alexaWebServiceSettings;
	private FusionServerAPISettings fusionServerAPISettings;
	private Map<String, DynamicAction> dynamicActions;
	private Map<String, JSONResponseHandler> jsonResponseHandlers;
	private Map<String, IntentMapping> intentMappings;
	private Map<String,Map<String, String>> resourceStrings;

	private static Configuration instance;
	
	private ScriptEngine scriptEngine;
	
	private Configuration() {
		
		scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
	}
	

	public synchronized static Configuration getInstance() {
		
		if (instance == null) {
			instance = new Configuration();
		}
		
	    return instance;
	
	}

	public ScriptEngine getScriptEngine() {
		return scriptEngine;
	}


	public Map<String, JSONResponseHandler> getJsonResponseHandlers() {
		return jsonResponseHandlers;
	}




	public void setJsonResponseHandlers(Map<String, JSONResponseHandler> jsonResponseHandlers) {
		this.jsonResponseHandlers = jsonResponseHandlers;
	}




	public Map<String, Map<String, String>> getResourceStrings() {
		return resourceStrings;
	}


	public void setResourceStrings(Map<String, Map<String, String>> resourceStrings) {
		this.resourceStrings = resourceStrings;
	}


	public AlexaWebServiceSettings getAlexaWebServiceSettings() {
		return alexaWebServiceSettings;
	}

	public void setAlexaWebServiceSettings(AlexaWebServiceSettings alexaWebServiceSettings) {
		this.alexaWebServiceSettings = alexaWebServiceSettings;
	}

	public FusionServerAPISettings getFusionServerAPISettings() {
		return fusionServerAPISettings;
	}

	public void setFusionServerAPISettings(FusionServerAPISettings fusionServerAPISettings) {
		this.fusionServerAPISettings = fusionServerAPISettings;
	}

	public Map<String, DynamicAction> getDynamicActions() {
		return dynamicActions;
	}

	public void setDynamicActions(Map<String, DynamicAction> dynamicActions) {
		this.dynamicActions = dynamicActions;
	}

	public Map<String, IntentMapping> getIntentMappings() {
		return intentMappings;
	}

	public void setIntentMappings(Map<String, IntentMapping> intentMappings) {
		this.intentMappings = intentMappings;
	}

}
