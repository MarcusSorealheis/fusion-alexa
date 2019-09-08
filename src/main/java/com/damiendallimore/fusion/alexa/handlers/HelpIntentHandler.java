package com.damiendallimore.fusion.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.amazon.ask.request.Predicates.intentName;

public class HelpIntentHandler implements RequestHandler {

	protected static Logger logger = LoggerFactory.getLogger(HelpIntentHandler.class);
	
	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("AMAZON.HelpIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		
		logger.info("HelpIntentHandler invoked");
		
		String speechText = "You can ask Fusion something";
		return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("Fusion", speechText)
				.withReprompt(speechText).build();
	}
}