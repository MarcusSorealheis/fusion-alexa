package com.damiendallimore.fusion.alexa.handlers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.ask.dispatcher.exception.ExceptionHandler;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.model.Response;


public class FusionExceptionHandler implements ExceptionHandler {
	
	protected static Logger logger = LoggerFactory.getLogger(FusionExceptionHandler.class);
	
    @Override
    public boolean canHandle(HandlerInput input, Throwable throwable) {
        return throwable instanceof AskSdkException;
    }

    @Override
    public Optional<Response> handle(HandlerInput input, Throwable throwable) {
    	
    	logger.info("FusionExceptionHandler invoked");
    	
    	logger.error("Error handling Alexa request",throwable);
    	
        return input.getResponseBuilder()
                    .withSpeech("An error was encountered while handling your request. Try again later.")
                    .build();
    }
}