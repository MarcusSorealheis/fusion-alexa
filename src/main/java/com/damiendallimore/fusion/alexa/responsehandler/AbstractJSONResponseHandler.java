package com.damiendallimore.fusion.alexa.responsehandler;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.damiendallimore.fusion.alexa.config.Configuration;

public abstract class AbstractJSONResponseHandler {
	
	public static final String DEFAULT = "com.damiendallimore.fusion.alexa.responsehandler.DefaultResponseHandler";
	
	private Map<String, String> args = new HashMap<String, String>();
	
	public abstract String processResponse(String response,JSONObject json,Configuration configuration,HandlerInput input);
	
	public void setArgs(Map<String, String> args) {

		this.args = args;
	}
	
	protected String getArg(String key) {

		return this.args.get(key);

	}

}
