package com.damiendallimore.fusion.alexa.responsehandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.damiendallimore.fusion.alexa.config.Configuration;
import com.damiendallimore.fusion.alexa.config.ResourceStringsUtil;


public class DefaultResponseHandler extends AbstractJSONResponseHandler {

	protected static Logger logger = LoggerFactory.getLogger(DefaultResponseHandler.class);
	
	@Override
	public String processResponse(String response,JSONObject json,Configuration configuration,HandlerInput input) {
		
		
		StringBuffer totalResponse = new StringBuffer();
		List<HashMap<String, String>> docs = new ArrayList<HashMap<String, String>>();
				
        docs = processJSON(json);
		
        boolean multirow = false;
		if (docs.size() > 1) {
			multirow = true;
			totalResponse.append("<speak>");
			response = response.replaceAll("<speak>", "");
			response = response.replaceAll("</speak>", "");
		}
		// oops , no search results
		if (docs.isEmpty()) {
			
			response = ResourceStringsUtil.getResource(configuration,input,ResourceStringsUtil.NORESULTS);
			
			totalResponse.append(response);
		} else {

			for (HashMap<String, String> outputKeyVal : docs) {
				String copyResponse = response;
				for (String key : outputKeyVal.keySet()) {
					// interpolate fields from response row into response
					// textual output
					copyResponse = copyResponse.replaceAll("\\$resultfield_" + key + "\\$", outputKeyVal.get(key));

				}
				if (multirow)
					totalResponse.append("<s>");
				totalResponse.append(copyResponse).append("  ");
				if (multirow)
					totalResponse.append("</s>");
			}
		}
		if (multirow)
			totalResponse.append("</speak>");
		
		return totalResponse.toString();
	}
	
	/**
	 * Process response JSON from Fusion
	 * 
	 * @param jsonStr
	 * @return
	 */
	private List<HashMap<String, String>> processJSON(JSONObject json) {
		
		logger.info("Processing response JSON from Fusion");
		
		
		List<HashMap<String, String>> docs = new ArrayList<HashMap<String, String>>();
		
		if(json == null) {
			return docs;
		}
			
		JSONArray docsArray = json.getJSONObject("response").getJSONArray("docs");
		
		for (int i = 0; i < docsArray.length(); i++) {
			
			HashMap<String, String> doc = new HashMap<String, String>();
			
			JSONObject jsonDoc = docsArray.getJSONObject(i);
			
			for (String keyStr : jsonDoc.keySet()) {
				Object keyvalue = jsonDoc.get(keyStr);
				String keyvalueStr = "";
				
				//roll out multi value field
		        if(keyvalue instanceof JSONArray) {
		        	
		        	List<Object> arrayItems = ((JSONArray)keyvalue).toList();
		        	for(Object item : arrayItems) {
		        		keyvalueStr += "<s>"+item.toString()+"</s>";
		        	}
		        	
		        }
		        //single value field
		        else {
		        	keyvalueStr = keyvalue.toString();
		        }
		        
		        
		        doc.put(keyStr, keyvalueStr);
			}

			docs.add(doc);
						
		}
	
		logger.info("Processed response JSON from Fusion, list contains "+docs.size()+" docs");
		
		return docs;
		
		
	}

}
