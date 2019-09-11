package com.damiendallimore.fusion.alexa.config;

import java.util.Map;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;

public class ResourceStringsUtil {

	public static final String GOODBYE = "goodbye";
	public static final String CARDNAME = "cardname";
	public static final String FALLBACK = "fallback";
	public static final String ERROR = "error";
	public static final String HELP = "help";
	public static final String LAUNCH = "launch";
	public static final String NOINTENT = "nointent";
	public static final String NORESULTS = "noresults";
	
	public static final String DEFAULT = "";
	
	
	
	public static String getResource(Configuration configuration,HandlerInput input, String key) {
		
		String locale = input.getRequest().getLocale();
		
		Map<String,String> localResources =  configuration.getResourceStrings().get(locale);
		
		if(localResources != null && !localResources.isEmpty()) {
			
			String message =  localResources.get(key);
			if(message != null && message.length() > 0) {
				return message;
			}
		}
		
		return DEFAULT;
		
	}

}
