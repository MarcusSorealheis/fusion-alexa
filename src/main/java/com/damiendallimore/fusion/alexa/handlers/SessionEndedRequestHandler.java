package com.damiendallimore.fusion.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.SessionEndedRequest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.amazon.ask.request.Predicates.requestType;

public class SessionEndedRequestHandler implements RequestHandler {

	protected static Logger logger = LoggerFactory.getLogger(SessionEndedRequestHandler.class);
	
	
	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(requestType(SessionEndedRequest.class));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		
		logger.info("SessionEndedRequestHandler invoked");
		
		// any cleanup logic goes here
		return input.getResponseBuilder().build();
	}

}