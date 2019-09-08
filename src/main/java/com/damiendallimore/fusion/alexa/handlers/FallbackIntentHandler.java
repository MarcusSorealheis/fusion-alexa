package com.damiendallimore.fusion.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.amazon.ask.request.Predicates.intentName;

public class FallbackIntentHandler implements RequestHandler {

	protected static Logger logger = LoggerFactory.getLogger(FallbackIntentHandler.class);

	
	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("AMAZON.FallbackIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		
		logger.info("FallbackIntentHandler invoked");
		
		
		String speechText = "Sorry, I don't know that. You can say try saying help!";
		return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("Fusion", speechText)
				.withReprompt(speechText).build();
	}
}
