package com.damiendallimore.fusion.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.damiendallimore.fusion.alexa.config.Configuration;
import com.damiendallimore.fusion.alexa.config.ResourceStringsUtil;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.amazon.ask.request.Predicates.intentName;

public class HelpIntentHandler implements RequestHandler {

	private Configuration configuration;
	
	protected static Logger logger = LoggerFactory.getLogger(HelpIntentHandler.class);
	
	public HelpIntentHandler(Configuration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("AMAZON.HelpIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		
		
		logger.info("HelpIntentHandler invoked");
		
		String speechText = ResourceStringsUtil.getResource(configuration,input,ResourceStringsUtil.HELP);
		String cardName = ResourceStringsUtil.getResource(configuration,input,ResourceStringsUtil.CARDNAME);
		
		return input.getResponseBuilder().withSpeech(speechText).withSimpleCard(cardName, speechText)
				.withReprompt(speechText).build();
	}
}