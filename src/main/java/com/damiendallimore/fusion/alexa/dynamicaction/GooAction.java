package com.damiendallimore.fusion.alexa.dynamicaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GooAction extends AbstractDynamicAction {

	protected static Logger logger = LoggerFactory.getLogger(GooAction.class);

	@Override
	public String executeAction() {

		logger.info("Executing GooAction");
		String response = "";
		try {

			// DO SOMETHING
			// getArg("somarg");
			// getSlot("someslot");
			response = replaceResponse("this is the goo action");

		} catch (Exception e) {
			logger.error("Error executing GooAction :" + e.getMessage());
		}
		return response;
	}

}
