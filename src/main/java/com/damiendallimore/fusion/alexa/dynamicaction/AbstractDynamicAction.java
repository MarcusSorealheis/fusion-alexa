package com.damiendallimore.fusion.alexa.dynamicaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.amazon.ask.model.Slot;

/**
 * Implement this abstract class to create a custom Dynamic Action. Then wire it
 * up in conf/configuration.json
 * 
 */
public abstract class AbstractDynamicAction {

	private Map<String, String> args = new HashMap<String, String>();
	private Map<String, Slot> slots = new HashMap<String, Slot>();
	private String responseTemplate = "";

	/**
	 * Implementation classes implement this method
	 * 
	 * @return A String representing the text output to be sent to Alexa
	 */
	public abstract String executeAction();

	public void setSlots(Map<String, Slot> slots) {

		this.slots = slots;
	}

	public void setArgs(Map<String, String> args) {

		this.args = args;
	}

	public void setResponseTemplate(String template) {

		this.responseTemplate = template;
	}

	protected String replaceResponse(String dynamicResponse) {

		String response = getResponseTemplate().replaceAll("\\$dynamic_response\\$", dynamicResponse);

		
		Set<String> slotKeys = slots.keySet();
		// search replace slots into response string
		for (String key : slotKeys) {

			String value = slots.get(key).getValue();

			response = response.replaceAll("\\$slot_" + key + "\\$", value);

		}
		
		return response;
	}

	protected String getSlot(String key) {

		return this.slots.get(key).getValue();

	}

	protected String getArg(String key) {

		return this.args.get(key);

	}

	protected String getResponseTemplate() {

		return this.responseTemplate;
	}

}
