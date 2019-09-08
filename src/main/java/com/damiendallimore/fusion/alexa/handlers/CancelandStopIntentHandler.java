package com.damiendallimore.fusion.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.amazon.ask.request.Predicates.intentName;

public class CancelandStopIntentHandler implements RequestHandler {
	
	protected static Logger logger = LoggerFactory.getLogger(CancelandStopIntentHandler.class);

	
	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("AMAZON.StopIntent").or(intentName("AMAZON.CancelIntent")));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		
		logger.info("CancelandStopIntentHandler invoked");
		
		String speechText = "Goodbye, thankyou for talking to Fusion";
		return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("Fusion", speechText).build();
	}
}
