package com.damiendallimore.fusion.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.model.slu.entityresolution.Resolution;
import com.amazon.ask.model.slu.entityresolution.Resolutions;
import com.amazon.ask.model.slu.entityresolution.ValueWrapper;
import com.damiendallimore.fusion.alexa.config.Configuration;
import com.damiendallimore.fusion.alexa.config.DynamicAction;
import com.damiendallimore.fusion.alexa.config.FusionServerAPISettings;
import com.damiendallimore.fusion.alexa.config.IntentMapping;
import com.damiendallimore.fusion.alexa.config.ResourceStringsUtil;
import com.damiendallimore.fusion.alexa.dynamicaction.AbstractDynamicAction;


import java.io.IOException;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FusionIntentHandler implements IntentRequestHandler {

	private Configuration configuration;
	
	protected static Logger logger = LoggerFactory.getLogger(FusionIntentHandler.class);
	
	public FusionIntentHandler(Configuration configuration) {
		this.configuration = configuration;
	}
	public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().startsWith("FUSION_");
    }

	@Override
	public Optional<Response> handle(HandlerInput input,IntentRequest intentRequest) {
		
		Intent intent = intentRequest.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;
		
		logger.info("FusionIntentHandler invoked for intent "+intentName);
		
		IntentMapping im = configuration.getIntentMappings().get(intentName);
		
		if (im == null) {
			logger.error("No mapping exists for " + intentName);
			
			String noIntent = ResourceStringsUtil.getResource(configuration,input,ResourceStringsUtil.NOINTENT);
			
			
			return input.getResponseBuilder()
				    .withSimpleCard("Fusion", noIntent)
				    .withSpeech(noIntent)
				    .build();
		} else {
			
			logger.info("Processing intent "+intentName);
			
			String response = im.getResponse();
			String dynamicAction = im.getDynamicAction();
			String dynamicActionArgs = im.getDynamicActionArgs();
			String solrQuery = im.getSolrQuery();
			String filterQuery = im.getFilterQuery();
			Map<String, Slot> slots = intent.getSlots();
			if(slots == null) {
				slots = new HashMap<String, Slot>();
			}
			
			if (dynamicAction != null && dynamicAction.length() > 0) {
				
				logger.info("This intent is a dynamic action");
				DynamicAction dam = configuration.getDynamicActions().get(dynamicAction);
				if (dam == null) {
					logger.error("No dynamic action mapping exists for " + dynamicAction);
				}
				try {
					
					logger.info("Executing dynamic action");
					AbstractDynamicAction instance = (AbstractDynamicAction) (Class.forName(dam.getClassName()).newInstance());
					instance.setArgs(getParamMap(dynamicActionArgs));
					instance.setSlots(slots);
					instance.setResponseTemplate(response);
					response = instance.executeAction();
				} catch (Exception e) {
					logger.error("Error executing dynamic action " + dynamicAction + " : " + e.getMessage());
				}
			}
			else if(solrQuery != null && solrQuery.length() > 0)  {
				
				logger.info("This intent is a Fusion REST call");
				
				Set<String> slotKeys = slots.keySet();
        		StringBuffer totalResponse = new StringBuffer();
        		// search replace slots into query params and response strings
        		for (String key : slotKeys) {

        			Slot slot = slots.get(key);
        			
        			String value = slot.getValue();
        			
        			//override value with a resolution
        			Resolutions resolutions = slot.getResolutions();
        			if(resolutions != null) {
        				List<Resolution> resolutionsPerAuthority = resolutions.getResolutionsPerAuthority();
        				for (Resolution resolution : resolutionsPerAuthority) {
        					List<ValueWrapper> resolutionValues = resolution.getValues();
        					for(ValueWrapper vw : resolutionValues) {
        						String valueName = vw.getValue().getName();
        						if(valueName.equalsIgnoreCase(value)) {
        							value=valueName;
        						}
        					}
        					
        				}
        			}
        			
        			
        			response = response.replaceAll("\\$" + key + "\\$", value);
        			solrQuery = solrQuery.replaceAll("\\$" + key + "\\$", value);
        			filterQuery = filterQuery.replaceAll("\\$" + key + "\\$", value);
        		}
        		
				FusionServerAPISettings fusionSettings = configuration.getFusionServerAPISettings();
				
				CloseableHttpResponse httpResponse = null;
				CloseableHttpClient httpclient = null;
				try {
					
					CredentialsProvider credsProvider = new BasicCredentialsProvider();	
					credsProvider.setCredentials(
							AuthScope.ANY,
			                new UsernamePasswordCredentials(fusionSettings.getFusionUser(),fusionSettings.getFusionPassword()));
					
					HttpHost targetHost = new HttpHost(fusionSettings.getFusionHost(), fusionSettings.getFusionPort(), fusionSettings.getFusionScheme());
					AuthCache authCache = new BasicAuthCache();
					authCache.put(targetHost, new BasicScheme());
					
					HttpClientContext context = HttpClientContext.create();
					context.setCredentialsProvider(credsProvider);
					context.setAuthCache(authCache);
					
					
					TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
				    SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
				    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, 
				      NoopHostnameVerifier.INSTANCE);
				     
				    Registry<ConnectionSocketFactory> socketFactoryRegistry = 
				      RegistryBuilder.<ConnectionSocketFactory> create()
				      .register("https", sslsf)
				      .register("http", new PlainConnectionSocketFactory())
				      .build();
				 
				    BasicHttpClientConnectionManager connectionManager = 
				      new BasicHttpClientConnectionManager(socketFactoryRegistry);
				    
					
					httpclient = HttpClients.custom()
			                .setSSLSocketFactory(sslsf)
			                .setConnectionManager(connectionManager)
			                .build();
						
					
					StringBuffer requestPath = new StringBuffer();
					requestPath.append("/api/apollo/apps/")
					           .append(im.getApp())
					           .append("/query-pipelines/")
					           .append(im.getPipelineId())
					           .append("/collections/")
					           .append(im.getCollection())
					           .append("/")
					           .append(im.getRequestHandler());
					
					
					URIBuilder builder = new URIBuilder();
					builder.setScheme(fusionSettings.getFusionScheme())
					.setHost(fusionSettings.getFusionHost())
					.setPort(fusionSettings.getFusionPort())
					.setPath(requestPath.toString())
					    .setParameter("q", solrQuery)
					    .setParameter("fq", filterQuery)
					    .setParameter("fl", im.getFieldList())
					    .setParameter("sort", im.getSortFieldDirection())
						.setParameter("rows", String.valueOf(im.getMaxResultsPerPage()))
						.setParameter("df", im.getDefaultField())
						.setParameter("wt", "json")
						.setParameter("start", "0");
					URI uri = builder.build();
					HttpGet httpGet = new HttpGet(uri);
					
					logger.info("Performing GET request to : "+uri.toString());
					
					List<HashMap<String, String>> docs = new ArrayList<HashMap<String, String>>();
					httpResponse = httpclient.execute(httpGet,context);
				  
					
				    int status = httpResponse.getStatusLine().getStatusCode();
				    
				    logger.info("Received HTTP response with code : "+status);
				    
                    if (status >= 200 && status < 300) {
                    	
                        HttpEntity entity = httpResponse.getEntity();
                        String jsonStr =  entity != null ? EntityUtils.toString(entity) : "{}";
                        
                        logger.debug("Received response JSON : "+jsonStr);
                        
                        JSONObject json = new JSONObject(jsonStr);
                        		       
                        docs = processJSON(json);	
                        
                        
                    } else {
                    	
                    	logger.error("Unexpected HTTP response status: " + status);
                    	
                    }
                    
					
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
            		
            		response = totalResponse.toString();
            		
					
				} catch (Exception e) {
					logger.error("Error performing GET request to Fusion",e);
				}
                finally {
					try {
						if(httpResponse != null)
						  httpResponse.close();
						if(httpclient != null)
						  httpclient.close();
						
					} catch (IOException e) {}
				}
				
			}
			else {
				logger.info("This intent is a static response");
			}
			
			
			if (response.startsWith("<speak>")) {
				response = response.replaceAll("\\\\", "");	
			}
				
			String cardName = ResourceStringsUtil.getResource(configuration,input,ResourceStringsUtil.CARDNAME);
			
			return input.getResponseBuilder()
		    .withSimpleCard(cardName, response)
		    .withSpeech(response)
		    .build();
						
		}
		
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
	/**
	 * Helper method to roll out a key=value string to a Map
	 * 
	 * @param keyValString
	 * @return
	 */
	private Map<String, String> getParamMap(String keyValString) {

		Map<String, String> map = new HashMap<String, String>();

		try {
			StringTokenizer st = new StringTokenizer(keyValString, ",");
			while (st.hasMoreTokens()) {
				StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
				while (st2.hasMoreTokens()) {
					map.put(st2.nextToken(), st2.nextToken());
				}
			}
		} catch (Exception e) {
			logger.error("Error rolling out param string into a Map : " + e.getMessage());
		}

		return map;

	}
	

}