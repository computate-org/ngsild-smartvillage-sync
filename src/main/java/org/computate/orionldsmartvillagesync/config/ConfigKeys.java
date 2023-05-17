package org.computate.orionldsmartvillagesync.config;

public class ConfigKeys {

	/**
	 * The path to the config file of the site. 
	 **/
	public static final String CONFIG_PATH = "CONFIG_PATH";

	/**
	 * The number of instances of the Vertx verticle to deploy. 
	 **/
	public static final String SITE_INSTANCES = "SITE_INSTANCES";

	/**
	 * The port of the site. 
	 **/
	public static final String SITE_PORT = "SITE_PORT";

	/**
	 * The base URL for the URLs of the site. 
	 **/
	public static final String SITE_BASE_URL = "SITE_BASE_URL";

	/**
	 * Whether to enable host verification otherwise trust all SSL/TLS certificates. 
	 **/
	public static final String SSL_VERIFY = "SSL_VERIFY";

	/**
	 * 
	 **/
	public static final String AUTH_REALM = "AUTH_REALM";

	/**
	 * The Auth client ID of the site. 
	 **/
	public static final String AUTH_CLIENT = "AUTH_CLIENT";

	/**
	 * The Auth client secret of the site. 
	 **/
	public static final String AUTH_SECRET = "AUTH_SECRET";

	/**
	 * The port to the Auth server. 
	 **/
	public static final String AUTH_PORT = "AUTH_PORT";

	/**
	 * Whether the Auth server uses SSL. 
	 **/
	public static final String AUTH_SSL = "AUTH_SSL";

	/**
	 * The token URI to the Auth server. 
	 **/
	public static final String AUTH_TOKEN_URI = "AUTH_TOKEN_URI";

	/**
	 * 
	 **/
	public static final String AUTH_HOST_NAME = "AUTH_HOST_NAME";

	/**
	 * The port to the Smarta Byar Smart Village application. 
	 **/
	public static final String SMARTVILLAGE_PORT = "SMARTVILLAGE_PORT";

	/**
	 * Whether the Smarta Byar Smart Village application uses SSL. 
	 **/
	public static final String SMARTVILLAGE_SSL = "SMARTVILLAGE_SSL";

	/**
	 * Smarta Byar Smart Village application host name. 
	 **/
	public static final String SMARTVILLAGE_HOST_NAME = "SMARTVILLAGE_HOST_NAME";
}
