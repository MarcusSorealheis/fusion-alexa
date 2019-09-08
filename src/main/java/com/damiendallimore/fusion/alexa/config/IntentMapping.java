package com.damiendallimore.fusion.alexa.config;

public class IntentMapping {

	private String app = "";
	private String intent = "";
	private String solrQuery = "";
	private String fieldList = "";
	private String pipelineId = "";
	private String collection = "";
	private String requestHandler = "select";
	private String filterQuery = "";
	private String sortFieldDirection = "";
	private int maxResultsPerPage = 10;
	private String defaultField = "";
	private String response = "";
	private String dynamicAction = "";
	private String dynamicActionArgs = "";

	public IntentMapping() {
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public String getSolrQuery() {
		return solrQuery;
	}

	public void setSolrQuery(String solrQuery) {
		this.solrQuery = solrQuery;
	}

	public String getFieldList() {
		return fieldList;
	}

	public void setFieldList(String fieldList) {
		this.fieldList = fieldList;
	}

	public String getPipelineId() {
		return pipelineId;
	}

	public void setPipelineId(String pipelineId) {
		this.pipelineId = pipelineId;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getRequestHandler() {
		return requestHandler;
	}

	public void setRequestHandler(String requestHandler) {
		this.requestHandler = requestHandler;
	}

	public String getFilterQuery() {
		return filterQuery;
	}

	public void setFilterQuery(String filterQuery) {
		this.filterQuery = filterQuery;
	}

	public String getSortFieldDirection() {
		return sortFieldDirection;
	}

	public void setSortFieldDirection(String sortFieldDirection) {
		this.sortFieldDirection = sortFieldDirection;
	}

	public int getMaxResultsPerPage() {
		return maxResultsPerPage;
	}

	public void setMaxResultsPerPage(int maxResultsPerPage) {
		this.maxResultsPerPage = maxResultsPerPage;
	}


	public String getDefaultField() {
		return defaultField;
	}

	public void setDefaultField(String defaultField) {
		this.defaultField = defaultField;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getDynamicAction() {
		return dynamicAction;
	}

	public void setDynamicAction(String dynamicAction) {
		this.dynamicAction = dynamicAction;
	}

	public String getDynamicActionArgs() {
		return dynamicActionArgs;
	}

	public void setDynamicActionArgs(String dynamicActionArgs) {
		this.dynamicActionArgs = dynamicActionArgs;
	}

}
