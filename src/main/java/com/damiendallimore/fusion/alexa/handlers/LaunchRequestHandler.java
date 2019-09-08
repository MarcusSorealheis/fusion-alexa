package com.damiendallimore.fusion.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.amazon.ask.request.Predicates.requestType;

public class LaunchRequestHandler implements RequestHandler {

	protected static Logger logger = LoggerFactory.getLogger(LaunchRequestHandler.class);
	
	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(requestType(LaunchRequest.class));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		
		logger.info("LaunchRequestHandler invoked");
				
		String speechText = "Welcome to the LucidWorks Fusion Skill";
		return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("Fusion", speechText)
				.withReprompt(speechText).build();
	}

}