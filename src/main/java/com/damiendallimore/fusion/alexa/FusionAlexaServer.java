package com.damiendallimore.fusion.alexa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;

import org.eclipse.jetty.server.handler.HandlerCollection;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.damiendallimore.fusion.alexa.config.AlexaWebServiceSettings;
import com.damiendallimore.fusion.alexa.config.Configuration;
import com.damiendallimore.fusion.alexa.config.DynamicAction;
import com.damiendallimore.fusion.alexa.config.FusionServerAPISettings;
import com.damiendallimore.fusion.alexa.config.IntentMapping;
import com.damiendallimore.fusion.alexa.config.JSONResponseHandler;

public class FusionAlexaServer {

	protected static Logger logger = LoggerFactory.getLogger(FusionAlexaServer.class);

	/**
	 * CONSTANTS
	 */
	
	private static final String CONFIGURATION_JSON = "configuration.json";
	private static final String CONFIGURATION_DIR = "conf";
	private static final String CRYPTO_DIR = "crypto";
	
	private static final String[] SUPPORTED_CIPHER_SUITES = new String[] { "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
			"SSL_RSA_WITH_RC4_128_SHA",

			"TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
			"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256", "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
			"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384", "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",

			"TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
			"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256", "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
			"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384", "TLS_ECDH_RSA_WITH_RC4_128_SHA",

			"TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
			"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
			"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",

			"TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
			"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
			"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", "TLS_ECDHE_RSA_WITH_RC4_128_SHA",

			"TLS_EMPTY_RENEGOTIATION_INFO_SCSV",

			"TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA256", "TLS_RSA_WITH_AES_256_CBC_SHA",
			"TLS_RSA_WITH_AES_256_CBC_SHA256", };

	
	/**
	 * Object the holds the configuration.json 
	 */
	private Configuration configuration;

	/**
	 * This Application starts a secure web server that will listen for intent requests from Alexa
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			logger.info("Initialising the Fusion Alexa Web Service");
			new FusionAlexaServer().init();
		} catch (Exception e) {
			logger.error("Error initialising the Fusion Alexa Web Service :" + e.getMessage());
		}

	}

	/**
	 * Initialisation methods
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {

		logger.info("Loading the configuration JSON");
		loadConfigurationJSON();
		
		logger.info("Starting the JSON file change monitor");
		startJSONFileChangeMonitor();
		
		logger.info("Starting the Jetty Web Server");
		startWebServer();

	}


	/**
	 * Start the Jetty Web Server
	 */
	private void startWebServer() {

		ServerConnector serverConnector = null;
		
		try {
			AlexaWebServiceSettings settings = this.configuration.getAlexaWebServiceSettings();
			
			// Configure server and its associated servlets
			Server server = new Server();
			SslConnectionFactory sslConnectionFactory = new SslConnectionFactory();
			SslContextFactory sslContextFactory = sslConnectionFactory.getSslContextFactory();
			
			File keyStore = new File(settings.getKeystorePath());
			// if not a canonical path , perhaps it is just a filename in the crypto directory
			if (!keyStore.exists()) {

				keyStore = new File(getFilePath(CRYPTO_DIR, settings.getKeystorePath()));
				
				if (!keyStore.exists()) {
					
					throw new Exception("Java Keystore "+settings.getKeystorePath() + " does not exist");
				}
			}
			
			sslContextFactory.setKeyStorePath(keyStore.getCanonicalPath());
			sslContextFactory.setKeyStorePassword(settings.getKeystorePassword());
			sslContextFactory.setIncludeCipherSuites(SUPPORTED_CIPHER_SUITES);
			
			HttpConfiguration httpConf = new HttpConfiguration();
			httpConf.setSecurePort(settings.getHttpsListenPort());
			httpConf.setSecureScheme(settings.getHttpsListenScheme());
			httpConf.addCustomizer(new SecureRequestCustomizer());
			
			HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConf);
			serverConnector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
			serverConnector.setPort(settings.getHttpsListenPort());
			
			Connector[] connectors = new Connector[1];
			connectors[0] = serverConnector;
			server.setConnectors(connectors);
			
			// alexa web service
			ServletContextHandler alexaContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
			alexaContext.setContextPath("/");
			
			
			alexaContext.addServlet(new ServletHolder(new FusionAlexaServlet(this.configuration)),
					settings.getHttpsListenEndpoint().substring(settings.getHttpsListenEndpoint().indexOf("/")));
			
			HandlerCollection handlerCollection = new HandlerCollection();
			handlerCollection.setHandlers(new Handler[] { alexaContext });
			server.setHandler(handlerCollection);
			
			server.start();
			logger.info("Jetty Web Server started and listening on " + settings.getHttpsListenScheme() + " port "+settings.getHttpsListenPort());
			server.join();
			
		} catch (Exception e) {
			logger.error("Error starting the web server :" + e.getMessage());
		} finally {
			if (serverConnector != null)
				serverConnector.close();
		}
	}

	/**
	 * Load the configuration.json file into an Object to work with
	 * 
	 * @throws Exception
	 */
	private void loadConfigurationJSON() throws Exception {

		this.configuration = new Configuration();
		
		JSONObject configJSON = loadJSON(CONFIGURATION_DIR, CONFIGURATION_JSON);
		
		logger.debug("Configuration JSON : "+configJSON.toString());
		
		JSONObject resourceStrings = configJSON.getJSONObject("resource_strings");
		
		String [] locales = JSONObject.getNames(resourceStrings);
		
		Map<String,Map<String, String>> resourceStringsMap = new HashMap<String,Map<String, String>>();
		
		for(String locale:locales) {
			
			Map<String, String> localeMap = new HashMap<String, String>();
			
			JSONArray resourcePropertys = resourceStrings.getJSONArray(locale);

			for (int i = 0; i < resourcePropertys.length(); i++) {
				JSONObject item = resourcePropertys.getJSONObject(i);

				
				try {
					localeMap.put(item.getString("key"),item.getString("value"));
				} catch (Exception e) {
				}
	
			}
			
			resourceStringsMap.put(locale,localeMap);
			
		}
		
		this.configuration.setResourceStrings(resourceStringsMap);
		
		String globalJSONResponseHandler = "";
		String globalJSONResponseHandlerArgs = "";
		String globalUriPath = "";
		String globalApp = "";
		String globalPipelineId = "";
		String globalCollection = "";
		String globalRequestHandler = "";
		int globalMaxResultsPerPage = 10;
		

		JSONObject globals = configJSON.getJSONObject("global_fusion_search_settings");
		
		if (globals.has("global_json_response_handler_args"))
			globalJSONResponseHandlerArgs = globals.getString("global_json_response_handler_args");
		if (globals.has("global_json_response_handler"))
			globalJSONResponseHandler = globals.getString("global_json_response_handler");
		if (globals.has("global_uri_path"))
			globalUriPath = globals.getString("global_uri_path");
		if (globals.has("global_app"))
			globalApp = globals.getString("global_app");
		if (globals.has("global_pipeline_id"))
			globalPipelineId = globals.getString("global_pipeline_id");
		if (globals.has("global_collection"))
			globalCollection = globals.getString("global_collection");
		if (globals.has("global_request_handler"))
			globalRequestHandler = globals.getString("global_request_handler");
		if (globals.has("global_max_results_per_page"))
			globalMaxResultsPerPage = globals.getInt("global_max_results_per_page");
		
		

		AlexaWebServiceSettings alexaWebServiceSettings = new AlexaWebServiceSettings();

		JSONObject webService = configJSON.getJSONObject("alexa_web_service_settings");

		if (webService.has("https_listen_port"))
			alexaWebServiceSettings.setHttpsListenPort(webService.getInt("https_listen_port"));
		if (webService.has("https_listen_scheme"))
			alexaWebServiceSettings.setHttpsListenScheme(webService.getString("https_listen_scheme"));
		if (webService.has("https_listen_endpoint"))
			alexaWebServiceSettings.setHttpsListenEndpoint(webService.getString("https_listen_endpoint"));
		if (webService.has("keystore_path"))
			alexaWebServiceSettings.setKeystorePath(webService.getString("keystore_path"));
		if (webService.has("keystore_password"))
			alexaWebServiceSettings.setKeystorePassword(webService.getString("keystore_password"));
		if (webService.has("skill_id"))
			alexaWebServiceSettings.setSkillID(webService.getString("skill_id"));

		this.configuration.setAlexaWebServiceSettings(alexaWebServiceSettings);

		FusionServerAPISettings fusionServerAPISettings = new FusionServerAPISettings();

		JSONObject fusionServer = configJSON.getJSONObject("fusion_server_api_settings");

		if (fusionServer.has("fusion_scheme"))
			fusionServerAPISettings.setFusionScheme(fusionServer.getString("fusion_scheme"));
		if (fusionServer.has("fusion_host"))
			fusionServerAPISettings.setFusionHost(fusionServer.getString("fusion_host"));
		if (fusionServer.has("fusion_port"))
			fusionServerAPISettings.setFusionPort(fusionServer.getInt("fusion_port"));
		if (fusionServer.has("fusion_user"))
			fusionServerAPISettings.setFusionUser(fusionServer.getString("fusion_user"));
		if (fusionServer.has("fusion_password"))
			fusionServerAPISettings.setFusionPassword(fusionServer.getString("fusion_password"));

		this.configuration.setFusionServerAPISettings(fusionServerAPISettings);

		Map<String, DynamicAction> dynamicActionMappings = new HashMap<String, DynamicAction>();

		JSONArray actionMappings = configJSON.getJSONArray("dynamic_actions");

		for (int i = 0; i < actionMappings.length(); i++) {
			JSONObject item = actionMappings.getJSONObject(i);

			DynamicAction da = new DynamicAction();
			try {
				da.setName(item.getString("name"));
			} catch (Exception e) {
			}
			try {
				da.setClassName(item.getString("class"));
			} catch (Exception e) {
			}

			dynamicActionMappings.put(da.getName(), da);
		}

		this.configuration.setDynamicActions(dynamicActionMappings);
		
		Map<String, JSONResponseHandler> responseHandlerMappings = new HashMap<String, JSONResponseHandler>();

		JSONArray responseMappings = configJSON.getJSONArray("json_response_handlers");

		for (int i = 0; i < responseMappings.length(); i++) {
			JSONObject item = responseMappings.getJSONObject(i);

			JSONResponseHandler rh = new JSONResponseHandler();
			try {
				rh.setName(item.getString("name"));
			} catch (Exception e) {
			}
			try {
				rh.setClassName(item.getString("class"));
			} catch (Exception e) {
			}

			responseHandlerMappings.put(rh.getName(), rh);
		}

		this.configuration.setJsonResponseHandlers(responseHandlerMappings);
		

		Map<String, IntentMapping> intentMappings = new HashMap<String, IntentMapping>();

		JSONArray intents = configJSON.getJSONArray("intent_mappings");

		for (int i = 0; i < intents.length(); i++) {
			JSONObject item = intents.getJSONObject(i);

			IntentMapping im = new IntentMapping();

			// globals

			try {
				if (item.has("json_response_handler"))
					im.setJsonResponseHandler(item.getString("json_response_handler"));
				else
					im.setJsonResponseHandler(globalJSONResponseHandler);
			} catch (Exception e) {
			}
			try {
				if (item.has("json_response_handler_args"))
					im.setJsonResponseHandlerArgs(item.getString("json_response_handler_args"));
				else
					im.setJsonResponseHandlerArgs(globalJSONResponseHandlerArgs);
			} catch (Exception e) {
			}
			try {
				if (item.has("uri_path"))
					im.setUriPath(item.getString("uri_path"));
				else
					im.setUriPath(globalUriPath);
			} catch (Exception e) {
			}
			try {
				if (item.has("app"))
					im.setApp(item.getString("app"));
				else
					im.setApp(globalApp);
			} catch (Exception e) {
			}
			try {
				if (item.has("pipeline_id"))
					im.setPipelineId(item.getString("pipeline_id"));
				else
					im.setPipelineId(globalPipelineId);
			} catch (Exception e) {
			}
			try {
				if (item.has("collection"))
					im.setCollection(item.getString("collection"));
				else
					im.setCollection(globalCollection);
			} catch (Exception e) {
			}
			try {
				if (item.has("request_handler"))
					im.setRequestHandler(item.getString("request_handler"));
				else
					im.setRequestHandler(globalRequestHandler);
			} catch (Exception e) {
			}
			try {
				if (item.has("max_results_per_page"))
					im.setMaxResultsPerPage(item.getInt("max_results_per_page"));
				else
					im.setMaxResultsPerPage(globalMaxResultsPerPage);
			} catch (Exception e) {
			}
			

			try {
				im.setDynamicAction(item.getString("dynamic_action"));
			} catch (Exception e) {
			}
			try {
				im.setDynamicActionArgs(item.getString("dynamic_action_args"));
			} catch (Exception e) {
			}

			try {
				im.setIntent(item.getString("intent"));
			} catch (Exception e) {
			}
			try {
				im.setResponse(item.getString("response"));
			} catch (Exception e) {
			}

			try {
				im.setAdditionalURLArgs(item.getString("additional_url_args"));
			} catch (Exception e) {
			}
			
			try {
				im.setSolrQuery(item.getString("solr_query"));
			} catch (Exception e) {
			}
			try {
				im.setFieldList(item.getString("field_list"));
			} catch (Exception e) {
			}
			try {
				im.setFilterQuery(item.getString("filter_query"));
			} catch (Exception e) {
			}
			try {
				im.setSortFieldDirection(item.getString("sort_field_direction"));
			} catch (Exception e) {
			}
			try {
				im.setDefaultField(item.getString("default_field"));
			} catch (Exception e) {
			}

			intentMappings.put(im.getIntent(), im);
		}

		this.configuration.setIntentMappings(intentMappings);

	}

	/**
	 * Load a JSON file into a JSON Object
	 * 
	 * @param dir
	 * @param file
	 * @return
	 * @throws Exception
	 */
	private JSONObject loadJSON(String dir, String file) throws Exception {
		String jsonMapping = getFilePath(dir, file);
		return new JSONObject(readFile(jsonMapping));
	}

	/**
	 * Read a file into a String
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private String readFile(String fileName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	/**
	 * Start a thread to monitor if configuration.json has changed , and reload it if necessary
	 */
	private void startJSONFileChangeMonitor() {
		new FileChangeMonitor().start();

	}

	/**
	 * Get the application root directory
	 * @return
	 */
	private String getRootDir() {

		return new File(System.getProperty("user.dir")).getParent();
	}

	
	/**
	 * Get the canonical path of a file in the application installation
	 * @param dir
	 * @param file
	 * @return
	 */
	private String getFilePath(String dir, String file) {

		return getRootDir() + File.separator + dir + File.separator + file;

	}

	/**
	 * Thread to monitor if configuration.json has changed , and reload it if necessary
	 */
	private class FileChangeMonitor extends Thread {

		long lastModifiedConfiguration;
		File configuration;

		FileChangeMonitor() {

			getFileHandles();
			this.lastModifiedConfiguration = configuration.lastModified();

		}

		private void getFileHandles() {

			this.configuration = new File(getFilePath(CONFIGURATION_DIR, CONFIGURATION_JSON));

		}

		@Override
		public void run() {

			while (true) {

				getFileHandles();
				long thisModifiedConfiguration = configuration.lastModified();

				if (thisModifiedConfiguration > this.lastModifiedConfiguration) {
					this.lastModifiedConfiguration = thisModifiedConfiguration;
					try {
						logger.info("Reloading the configuration JSON");
						loadConfigurationJSON();
					} catch (Exception e) {
						logger.error("Error reloading configuration JSON :" + e.getMessage());
					}
				}

				try {
					// poll every 10 seconds
					Thread.sleep(10000);
				} catch (InterruptedException e) {

				}
			}
		}

	}

}
