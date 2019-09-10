package com.damiendallimore.fusion.alexa.handlers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.ask.dispatcher.exception.ExceptionHandler;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.model.Response;
import com.damiendallimore.fusion.alexa.config.Configuration;
import com.damiendallimore.fusion.alexa.config.ResourceStringsUtil;


public class FusionExceptionHandler implements ExceptionHandler {
	
	private Configuration configuration;
	
	protected static Logger logger = LoggerFactory.getLogger(FusionExceptionHandler.class);
	
	public FusionExceptionHandler(Configuration configuration) {
		this.configuration = configuration;
	}
	
    @Override
    public boolean canHandle(HandlerInput input, Throwable throwable) {
        return throwable instanceof AskSdkException;
    }

    @Override
    public Optional<Response> handle(HandlerInput input, Throwable throwable) {
    	
    	logger.info("FusionExceptionHandler invoked");
    	
    	logger.error("Error handling Alexa request",throwable);
    	
    	String speechText = ResourceStringsUtil.getResource(configuration,input,ResourceStringsUtil.ERROR);
		
		
        return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .build();
    }
}