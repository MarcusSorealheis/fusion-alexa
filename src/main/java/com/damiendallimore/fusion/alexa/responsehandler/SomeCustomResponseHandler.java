package com.damiendallimore.fusion.alexa.responsehandler;

import org.json.JSONObject;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.damiendallimore.fusion.alexa.config.Configuration;

public class SomeCustomResponseHandler extends AbstractJSONResponseHandler {

	@Override
	public String processResponse(String response,JSONObject json,Configuration configuration,HandlerInput input) {
		// do something with the json
		// format a response string
		return "some response string";
	}

}
