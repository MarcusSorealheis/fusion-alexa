package com.damiendallimore.fusion.alexa.dynamicaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FooAction extends AbstractDynamicAction {

	protected static Logger logger = LoggerFactory.getLogger(FooAction.class);

	@Override
	public String executeAction() {

		logger.info("Executing FooAction");
		
		String response = "";
		try {

			// DO SOMETHING
			// getArg("somarg");
			// getSlot("someslot");
			
			response = replaceResponse("this is the foo action");

		} catch (Exception e) {
			
			logger.error("Error executing FooAction :" + e);
		}
		return response;
	}

}
