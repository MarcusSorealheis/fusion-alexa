# Alexa Integration for Lucidworks Fusion

## License

Licensed under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.txt)

## Overview

### If they can't ask for it...they can't buy it.


Check out the [YOU TUBE VIDEO DEMO HERE](https://www.youtube.com/watch?v=ZHYHyXjiCtI)


This is a standalone Java Application that provisions a Natural Language Voice interface to Lucidworks Fusion.

You can then use any Alexa device to ask Fusion anything you want.

This Application should be run as a daemon process and will spawn an HTTPs web server to start listening for incoming requests from the Amazon Alexa Cloud Service.

The ultimate purpose of this example is to inspire Lucidworks Developers to build and publish their own custom Alexa skills that talk to Lucidworks Fusion on their specific application platforms / e-commerce stores etc..


## Dependencies

* Internet accessible server with a resolvable domain name.
* Ability to open your firewall to incoming HTTPs requests , default port 443. For opening a port < 1024 , you'll need to be running the Java Application as a privileged user.
* Java Runtime version 8+ installed
* Accessible Fusion Server or Cloud instance
* An Alexa device(Echo/Dot etc..) and a [free Amazon Developer account](http://developer.amazon.com)

## Internet accessible server

For development and testing we simply spun up an Ubuntu server using Amazon Lightsail. 

This gives you an internet accessible server with a static IP(to DNS map your domain name too) and port 443 opened.

## Fusion setup

[Follow the Fusion tutorial](https://doc.lucidworks.com/fusion-server/4.2/getting-started/tutorials/get-started/index.html) to setup some test data using the MovieLens Dataset.

I developed and tested this Application against Fusion Server v4.2.3

## Building

`./gradlew distZip`

This creates a zip file in the `build/distributions` directory

## Running The Built Zip

Unzip the built zip and execute the `./fusion-alexa/bin/fusion-alexa` start script

Running in the background as a daemon `sudo nohup ./fusion-alexa/bin/fusion-alexa &`


## Generate Your Crypto Assets

Place your crypto assets and Java Keystore file (`java-keystore.jks`) in the `crypto` directory.

[Follow the docs here for creating a certificate and private key](https://developer.amazon.com/docs/custom-skills/configure-web-service-self-signed-certificate.html#create-a-private-key-and-self-signed-certificate-for-testing)

Use the following `openssl` command to create a PKCS #12 archive file from your private key and certificate. Replace the `private-key.pem` and `certificate.pem` values shown here with the filenames for your key and certificate. Specify a password for the archive when prompted.

```
openssl pkcs12 -keypbe PBE-SHA1-3DES \
               -certpbe PBE-SHA1-3DES \
               -inkey private-key.pem \
               -in certificate.pem \
               -export \
               -out keystore.pkcs12
```

Use the following keytool command to import the PKCS #12 file into a Java KeyStore, specifying a password for both the destination KeyStore and source PKCS #12 archive:

```
$JAVA_HOME/bin/keytool -importkeystore \
           -destkeystore java-keystore.jks \
           -srckeystore keystore.pkcs12 \
           -srcstoretype PKCS12
```

Note , make sure the keystore and the key have the **SAME** password. 

## Firewall

You will need to open your firewall to your internet accessible Splunk instance to accept incoming requests for the HTTPs port 443.Currently you can only accept requests on this port, but could employ a reverse proxy approach if you want to forward to other ports inside your network.

## Setting up your Fusion Alexa Skill

This Java Application is a custom web service based implementation for your Alexa Skill (it does not use AWS Lambdas).

The means by which you interface your Alexa device to Fusion is by registering a custom Alexa Skill with the AWS Alexa Cloud Service and configuring this applications configuration files.


As we want this custom skill to be private and secure to your own developer usage , you are going to be 
registering the skill under your own free Developer account rather than a publicly published Alexa skill.

### Setting up the Application configuration files

#### alexa\_interaction\_model.json

This is an example interaction model to get you started.

As you add your own intents/utterances/slots , you'll update this file, copy it into your Alexa skill builder and rebuild your Alexa skill model.

[Refer to the Alexa docs for the syntax for this file.](https://developer.amazon.com/docs/custom-skills/create-the-interaction-model-for-your-skill.html)

#### configuration.json

This is the main configuration file for setting up your Application to integrate Alexa and Fusion.


#### alexa\_web\_service\_settings

*  **https_listen_port** : This must be 443
*  **https_listen_scheme** : This must be https
*  **https_listen_endpoint** : Your host URI ie: foo.myhost.com/alexa
*  **keystore_path** : A canonical path to your Java Keystore , or just the filename if it is in the `crypto` directory
*  **keystore_password** : Your Java Keystore password
*  **skill_id** : The Alexa skill ID for this custom skill

#### fusion\_server\_api\_settings

*  **fusion_scheme** : http or https , defaults to http
*  **fusion_host** : Hostname of your Fusion server
*  **fusion_port** : API port of your Fusion server , defaults to 8764
*  **fusion_user** : Username for your Fusion server
*  **fusion_password** : Password for your Fusion server

#### global\_fusion\_search\_settings

These are global settings for `intent_mappings`. 

If you need, they can be overidden in each `intent_mapping` that you declare.

*  **global_uri_path** : the URI Path for the REST call , can have tokens in the path for dynamic replacement.See the example in `configuration.json`
*  **global_app** : the Fusion App Context
*  **global_pipeline_id** : the Fusion Pipeline ID
*  **global_collection** : the Fusion Collection
*  **global_request_handler** : the Fusion search request handler
*  **global_max_results_per_page** : Maximum results to return in each request.
*  **global_json_response_handler** : Name of a handler defined in `json_response_handlers`. 
*  **global_json_response_handler_args** :  a key=value,key2=value2 ..... list of arguments that can be passed into the handler and accessed by your code 

#### dynamic\_actions

*  **name** : A name to refer to this action when wiring up in `intent_mappings`
*  **class** : Fully qualified classname  

#### json\_response\_handlers

*  **name** : A name to refer to this handler when wiring up in `intent_mappings`
*  **class** : Fully qualified classname  

#### resource\_strings

An array of supported Alexa i8n locales containing a collection of resource strings 

*  **key** : resource string key
*  **value** : the i18n resource string value 

#### intent\_mappings

The fields below that you include will determine how the intent request is handled.

If **solr_query** is present , then a REST call will be made to Fusion.Searches can return 1 or many documents , this is automagically handled for you.

If **dynamic_action** is present , then a dynamic action will be executed.

If neither of the above fields is present , then it is assumed that the **response** field is just some static text to echo back to Alexa.

*  **intent** : the name of the incoming request intent to map this action to 
*  **solr_query** : [query parameters](https://doc.lucidworks.com/fusion-server/4.2/search-development/getting-data-out/query-language-cheat-sheet.html#common-query-parameters) 
*  **filter_query** : [filter query](https://doc.lucidworks.com/fusion-server/4.2/search-development/getting-data-out/query-language-cheat-sheet.html#common-query-parameters)
*  **field_list** : [field list](https://doc.lucidworks.com/fusion-server/4.2/search-development/getting-data-out/query-language-cheat-sheet.html#common-query-parameters)
*  **uri_path** : the URI Path for the REST call , can have tokens in the path for dynamic replacement.See the example in `configuration.json`
*  **app** : the Fusion App Context 
*  **pipeline_id** : the Fusion Pipeline ID 
*  **collection** : the Fusion Collection 
*  **request_handler** : the Fusion search request handler 
*  **sort_field_direction** : [sort field/direction](https://doc.lucidworks.com/fusion-server/4.2/search-development/getting-data-out/query-language-cheat-sheet.html#common-query-parameters)
*  **max_results_per_page** : Maximum results to return in each request.
*  **default_field** : [default field](https://doc.lucidworks.com/fusion-server/4.2/search-development/getting-data-out/query-language-cheat-sheet.html#common-query-parameters )
*  **response** : the response to return back to Alexa. See more below on response formatting 
*  **dynamic_action** : the action name specified in `dynamic_actions` 
*  **dynamic_action_args** : a key=value,key2=value2 ..... list of arguments that can be passed into the action and accessed by your code 
*  **json_response_handler** : Name of a handler defined in `json_response_handlers`. 
*  **json_response_handler_args** :  a key=value,key2=value2 ..... list of arguments that can be passed into the handler and accessed by your code
*  **additional_url_args** : a key=value,key2=value2 ..... list of any other url arguments that you want to add to the REST search request

`solr_query` , `filter_query` and `additional_url_args` may also include slot values from the request. See the example in `configuration.json`

### Custom JSON response handlers

You can implement you own custom JSON Response handler.

These are the steps for creating a new handler :

1. Create a class that extends the AbstractJSONResponseHandler base class.
2. Implement the `processResponse()` method .You can access custom arguments from within your code also.
3. Update build.gradle with any dependencies.
4. Update the `configuration.json` file to map the class name to some handler name that you can refer to
5. Apply this handler either globally using the `global_json_response_handler` field or for a specific `intent_mapping` using the `json_response_handler` field
6. Rebuild everything , `./gradlew distZip`

Refer to the examples in `configuration.json`.

The default handler `com.damiendallimore.fusion.alexa.responsehandler.DefaultResponseHandler` will always be fallen back to if no handlers can be found.

### Create your own dynamic actions

You can easily extend the available set of built in actions by creating your own custom dynamic actions and plugging them in , all you need is some simple Java coding skills.

This App ships with 2 example dynamic actions , `com.damiendallimore.fusion.alexa.dynamicaction.FooAction` and `com.damiendallimore.fusion.alexa.dynamicaction.GooAction`. They are just trivial skeleton examples.

These are the steps for creating a new Dynamic Action :

1. Create a class that extends the AbstractDynamicAction base class.
2. Implement the `executeAction()` method .You can access slot values and custom arguments from within your code also.
3. Update build.gradle with any dependencies.
4. Update the `configuration.json` file to map the class name to some action name that you can refer to
5. Add a mapping from an incoming intent request to this dynamic action in `configuration.json`
6. Rebuild everything , `./gradlew distZip`

Refer to the examples in `configuration.json`.
 

### Response formatting

The JSON `response` field in `configuration.json` can be in plain text or [SSML](https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/speech-synthesis-markup-language-ssml-reference).

The response text or SSML can contain tokens to replace from the values of any slots that were passed in the request  ie: `$slot_movie_genre$` or `$slot_movie_year$` 

The response text or SSML can contain tokens to replace from the values of any i18N resource strings ie: `$resourcestring_whatisfusion$`

For Solr Querys the response text or SSML can contain tokens to replace from the results of the searches. These are declared in the format `$resultfield_xxx$` , where `xxx` is the name of the field in the search result.

Dynamic action responses also have a special token `$dynamic_response` which is some dynamic text that the action returns .This token can be used standalone or mixed in with plain text, SSML and slot tokens.

Response Examples :

*  Static response : `Hello this is a lovely day`
*  Response with an i18n string : `$resourcestring_hello$ this is a lovely day`
*  Response with slot values : `Hello , I see you asked about movies in $slot_movie_year$`
*  Response with slot values and search result fields : `The movie $resultfield_title_txt$ was released in $slot_movie_year$`  (where `title_txt` would be the name of the search result field)
*  SSML response : `<speak> Hello there </speak>`
*  Dynamic action response : `My dynamic response is $dynamic_response$`

### Setting up the Alexa Skill

1. [Sign up for your free Developer Account](http://developer.amazon.com)

2. [Create the new Fusion skill](https://developer.amazon.com/alexa/console/ask?)

As you move through the setup wizard , use/select these values :

* **Skill Name** : My Fusion Skill
* **Model** : Custom
* **Method** : Provision Your Own
* **Template** : Start From Scratch

### Interaction Model Tab

#### Invocation

* **Skill Invocation Name** : "fusion"   , this is then used when you talk to your Alexa device (" Alexa .... ask fusion .....") .You can use any name you want.

#### Intents / Slot / Utterances

You can copy/paste the JSON from `conf/alexa_interaction_model.json` into the JSON editor

### Interfaces Tab

Defaults are fine

### Endpoint Tab

Select HTTPs

* **Default Region** : https://YOURHOST/alexa. The value of YOURHOST should match what you have in the certificate you created and be a resolvable domain name, not an IP Address.

* **Select SSL Certificate Type** : "I will upload a self-signed certificate in X 509 format"

When you save this endpoint you'll be prompted to upload your certificate.



## Logging

All logs will get written to the `logs` directory.

## Troubleshooting

* Correct Java Runtime version 8+ ?
* HTTPs port was successfully opened ? `netstat -plnt` is a useful command to check with.
* Running as a privileged user for using a HTTPs port < 1024 ?
* Firewall is open for incoming traffic for the HTTPs port ?
* Correct path to Java keystore ?
* Correct name of Java keystore
* Correct Java keystore password ?
* Keystore and Key passwords are the same ?
* Have you looked in the logs for errors ? 
* Can you successfully test the skill from the Amazon developer console ?
* Did you generate your certificate with the correct hostname/domain for your running Splunk instance ?
