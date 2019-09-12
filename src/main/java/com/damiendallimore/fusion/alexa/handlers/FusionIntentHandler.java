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
import com.damiendallimore.fusion.alexa.config.JSONResponseHandler;
import com.damiendallimore.fusion.alexa.config.ResourceStringsUtil;
import com.damiendallimore.fusion.alexa.dynamicaction.AbstractDynamicAction;
import com.damiendallimore.fusion.alexa.responsehandler.AbstractJSONResponseHandler;

import java.io.IOException;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.script.Bindings;
import javax.script.ScriptEngine;

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
			
			String response = replacei18NTokens(im.getResponse(),input);
			
			String responseHandler = im.getJsonResponseHandler();
			String responseHandlerArgs = im.getJsonResponseHandlerArgs();
			
			String dynamicAction = im.getDynamicAction();
			String dynamicActionArgs = im.getDynamicActionArgs();
			String solrQuery = im.getSolrQuery();
			String filterQuery = im.getFilterQuery();
			
			String additionalURLArgs = im.getAdditionalURLArgs();
			
			Map<String, Slot> slots = intent.getSlots();
			if(slots == null) {
				slots = new HashMap<String, Slot>();
			}
			
			if (dynamicAction != null && dynamicAction.length() > 0) {
				
				logger.info("This intent is a dynamic action");
				DynamicAction dam = configuration.getDynamicActions().get(dynamicAction);
				if (dam == null) {
					logger.error("No dynamic action mapping exists for " + dynamicAction);
					response = ResourceStringsUtil.getResource(configuration,input,ResourceStringsUtil.ERROR);
				}
				else {
					try {
						
						logger.info("Executing dynamic action");
						
						if(!dam.getClassName().isEmpty()) {
							
							logger.info("Dynamic Action is a Java Class");
							
							AbstractDynamicAction instance = (AbstractDynamicAction) (Class.forName(dam.getClassName()).newInstance());
							instance.setArgs(getParamMap(dynamicActionArgs));
							instance.setSlots(slots);
							instance.setResponseTemplate(response);
							response = instance.executeAction();
						
						}
						else if(!dam.getJavascript().isEmpty()) {
							
							logger.info("Dynamic Action is Javascript");
							
						    ScriptEngine engine = this.configuration.getScriptEngine();
    					    Bindings bindings = engine.createBindings();
    					    bindings.put("response", response);
    					    bindings.put("slots", slots);
    					    bindings.put("args", getParamMap(dynamicActionArgs));
    					  
    					    response  = (String) engine.eval(dam.getJavascript(), bindings);
	    					  
						}
						else {
							
						    logger.error("No Dynamic Action implementation was provided");
	    				    response = ResourceStringsUtil.getResource(configuration,input,ResourceStringsUtil.ERROR);
	    						
						}
					} catch (Exception e) {
						logger.error("Error executing dynamic action " + dynamicAction + " : " + e.getMessage());
						response = ResourceStringsUtil.getResource(configuration,input,ResourceStringsUtil.ERROR);
					}
				}
			}
			else if(solrQuery != null && solrQuery.length() > 0)  {
				
				logger.info("This intent is a Fusion REST call");
				
				Set<String> slotKeys = slots.keySet();
        		
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
        			
        			
        			response = response.replaceAll("\\$slot_" + key + "\\$", value);
        			solrQuery = solrQuery.replaceAll("\\$slot_" + key + "\\$", value);
        			filterQuery = filterQuery.replaceAll("\\$slot_" + key + "\\$", value);
        			additionalURLArgs = additionalURLArgs.replaceAll("\\$slot_" + key + "\\$", value);
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
						
					
					
					String uriPath = im.getUriPath();
					uriPath = uriPath.replaceAll("\\$app\\$", im.getApp());
					uriPath = uriPath.replaceAll("\\$pipeline_id\\$", im.getPipelineId());
					uriPath = uriPath.replaceAll("\\$collection\\$", im.getCollection());
					uriPath = uriPath.replaceAll("\\$request_handler\\$", im.getRequestHandler());
					
					URIBuilder builder = new URIBuilder();
					builder.setScheme(fusionSettings.getFusionScheme())
					.setHost(fusionSettings.getFusionHost())
					.setPort(fusionSettings.getFusionPort())
					.setPath(uriPath)
					    .setParameter("q", solrQuery)
					    .setParameter("fq", filterQuery)
					    .setParameter("fl", im.getFieldList())
					    .setParameter("sort", im.getSortFieldDirection())
						.setParameter("rows", String.valueOf(im.getMaxResultsPerPage()))
						.setParameter("df", im.getDefaultField())
						.setParameter("wt", "json")
						.setParameter("start", "0");
					
					
					Map<String, String> additionalParams = getParamMap(additionalURLArgs);
					
					for (Map.Entry<String, String> entry : additionalParams.entrySet()) {
					   
					    builder.setParameter(entry.getKey(), entry.getValue());
					}
					
					
					URI uri = builder.build();
					HttpGet httpGet = new HttpGet(uri);
					
					logger.info("Performing GET request to : "+uri.toString());
					
		
					httpResponse = httpclient.execute(httpGet,context);
				  					
				    int status = httpResponse.getStatusLine().getStatusCode();
				    
				    logger.info("Received HTTP response with code : "+status);
				    
				    JSONObject json = null;
                    if (status >= 200 && status < 300) {
                    	
                        HttpEntity entity = httpResponse.getEntity();
                        String jsonStr =  entity != null ? EntityUtils.toString(entity) : "{}";
                        
                        logger.debug("Received response JSON : "+jsonStr);
                        
                        json = new JSONObject(jsonStr);                       		       
                                                
                    } else {
                    	
                    	logger.error("Unexpected HTTP response status: " + status);
                    	
                    }
                    
                    JSONResponseHandler jrh = configuration.getJsonResponseHandlers().get(responseHandler);
    				if (jrh == null) {
    					logger.warn("No JSON Response Handler mapping exists for " + responseHandler + " , using the default handler.");
    					jrh = new JSONResponseHandler ();
    					jrh.setClassName(AbstractJSONResponseHandler.DEFAULT);
    
    				}
    				try {
    					
    					logger.info("Instantiating response handler "+responseHandler);
    					
    					if(!jrh.getClassName().isEmpty()) {
    						
    					  logger.info("Response Handler is a Java Class");
    					  AbstractJSONResponseHandler responseHandlerInstance = (AbstractJSONResponseHandler) (Class.forName(jrh.getClassName()).newInstance());
    					  responseHandlerInstance.setArgs(getParamMap(responseHandlerArgs));
    					
    					  response = responseHandlerInstance.processResponse(response,json,configuration,input);
    					}
    					else if(!jrh.getJavascript().isEmpty()) {
    						
    					  logger.info("Response Handler is Javascript");
    					  
    					  ScriptEngine engine = this.configuration.getScriptEngine();
    					  Bindings bindings = engine.createBindings();
    					  bindings.put("response", response);
    					  bindings.put("json", json);
    					  bindings.put("configuration", configuration);
    					  bindings.put("input", input);
    					  bindings.put("args", getParamMap(responseHandlerArgs));
    					  
    					  response  = (String) engine.eval(jrh.getJavascript(), bindings);
    						
    					}
    					else {
    						
    					  logger.error("No Response Handler implementation was provided");
    					  response = ResourceStringsUtil.getResource(configuration,input,ResourceStringsUtil.ERROR);
    						
    					}
    				} catch (Exception e) {
    					logger.error("Error executing response handler " + responseHandler + " : " + e.getMessage());
    					response = ResourceStringsUtil.getResource(configuration,input,ResourceStringsUtil.ERROR);
    				}
    				
    				
				} catch (Exception e) {
					logger.error("Error performing GET request to Fusion",e);
					response = ResourceStringsUtil.getResource(configuration,input,ResourceStringsUtil.ERROR);
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
	
	private String replacei18NTokens(String response,HandlerInput input) {
		
        Pattern pattern = Pattern.compile("\\$resourcestring_(\\w+)\\$");
		
		//try to match the regex
		Matcher m = pattern.matcher(response);

		//loop through each match group and perform replacement
		while (m.find()) {
			
			String matched = m.group(1);
			String message = ResourceStringsUtil.getResource(configuration,input,matched);
			response = response.replaceAll("\\$resourcestring_" + matched + "\\$", message);			
			
		}
		
		return response;
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