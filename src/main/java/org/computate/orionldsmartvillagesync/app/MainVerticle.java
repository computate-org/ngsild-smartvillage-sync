package org.computate.orionldsmartvillagesync.app;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.computate.orionldsmartvillagesync.api.entity.EntityApiService;
import org.computate.orionldsmartvillagesync.config.ConfigKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.sstore.LocalSessionStore;



/**
 * Description: A Java class to start the Vert.x application as a main method. 
 * Keyword: classSimpleNameVerticle
 **/
public class MainVerticle extends AbstractVerticle {
	private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

	private Router router;

	private WebClient webClient;

	/**	
	 *	The main method for the Vert.x application that runs the Vert.x Runner class
	 **/
	public static void  main(String[] args) {
		Vertx vertx = Vertx.vertx();
		String configPath = System.getenv(ConfigKeys.CONFIG_PATH);
		configureConfig(vertx).onSuccess(config -> {
			try {
				Future<Void> originalFuture = Future.future(a -> a.complete());
				Future<Void> future = originalFuture;
	
				future = future.compose(a -> run(config));
				future.compose(a -> vertx.close());
			} catch(Exception ex) {
				LOG.error("Error running vertx", ex);
				vertx.close();
			}
		}).onFailure(ex -> {
			LOG.error(String.format("Error loading config: %s", configPath), ex);
			vertx.close();
		});
	}

	public static Future<Void> run(JsonObject config) {
		Promise<Void> promise = Promise.promise();
		try {
			VertxOptions vertxOptions = new VertxOptions();
			EventBusOptions eventBusOptions = new EventBusOptions();
	
			vertxOptions.setEventBusOptions(eventBusOptions);
	
			Vertx vertx = Vertx.vertx(vertxOptions);
			DeploymentOptions deploymentOptions = new DeploymentOptions();
			deploymentOptions.setInstances(config.getInteger(ConfigKeys.SITE_INSTANCES));
			deploymentOptions.setConfig(config);

			vertx.deployVerticle(MainVerticle.class, deploymentOptions).onSuccess(a -> {
				LOG.info("Started main verticle. ");
				promise.complete();
			}).onFailure(ex -> {
				LOG.error("Failed to start main verticle. ", ex);
			});
		} catch (Throwable ex) {
			LOG.error("Creating clustered Vertx failed. ", ex);
			promise.fail(ex);
		}
		return promise.future();
	}

	/**	
	 * Val.Complete.enUS:The config was configured successfully. 
	 * Val.Fail.enUS:Could not configure the config(). 
	 **/
	public static Future<JsonObject> configureConfig(Vertx vertx) {
		Promise<JsonObject> promise = Promise.promise();

		try {
			ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions();

			retrieverOptions.addStore(new ConfigStoreOptions().setType("file").setFormat("yaml").setConfig(new JsonObject().put("path", "application.yml")));

			String configPath = System.getenv(ConfigKeys.CONFIG_PATH);
			if(Strings.isNotBlank(configPath)) {
				ConfigStoreOptions configIni = new ConfigStoreOptions().setType("file").setFormat("yaml").setConfig(new JsonObject().put("path", configPath));
				retrieverOptions.addStore(configIni);
			}

			ConfigStoreOptions storeEnv = new ConfigStoreOptions().setType("env");
			retrieverOptions.addStore(storeEnv);

			ConfigRetriever configRetriever = ConfigRetriever.create(vertx, retrieverOptions);
			configRetriever.getConfig().onSuccess(config -> {
				LOG.info("The config was configured successfully. ");
				promise.complete(config);
			}).onFailure(ex -> {
				LOG.error("Unable to configure site context. ", ex);
				promise.fail(ex);
			});
		} catch(Exception ex) {
			LOG.error("Unable to configure site context. ", ex);
			promise.fail(ex);
		}

		return promise.future();
	}

	/**	
	 **/
	public Future<Void> configureWebClient() {
		Promise<Void> promise = Promise.promise();

		try {
			Boolean sslVerify = config().getBoolean(ConfigKeys.SSL_VERIFY);
			webClient = WebClient.create(vertx, new WebClientOptions().setVerifyHost(sslVerify).setTrustAll(!sslVerify));
			promise.complete();
		} catch(Exception ex) {
			LOG.error("Unable to configure site context. ", ex);
			promise.fail(ex);
		}

		return promise.future();
	}

	/**
	 * This is called by Vert.x when the verticle instance is deployed. 
	 * Initialize a new site context object for storing information about the entire site in English. 
	 * Setup the startPromise to handle the configuration steps and starting the server. 
	 **/
	@Override()
	public void  start(Promise<Void> startPromise) throws Exception, Exception {
		try {
			configureWebClient().onComplete(a ->
				configureOpenApi().onComplete(d -> 
					configureApi().onComplete(k -> 
						startServer().onComplete(n -> startPromise.complete())
					).onFailure(ex -> startPromise.fail(ex))
				).onFailure(ex -> startPromise.fail(ex))
			).onFailure(ex -> startPromise.fail(ex));
		} catch (Exception ex) {
			LOG.error("Couldn't start verticle. ", ex);
		}
	}

	/**	
	 * 
	 **/
	public Future<Void> configureOpenApi() {
		Promise<Void> promise = Promise.promise();
		try {

	
			LocalSessionStore sessionStore = LocalSessionStore.create(vertx, "ActiveLearningStudio-API-sessions");
			SessionHandler sessionHandler = SessionHandler.create(sessionStore);
	
			RouterBuilder.create(vertx, "webroot/openapi3.yml").onSuccess(routerBuilder -> {
					routerBuilder.mountServicesFromExtensions();
	
					routerBuilder.serviceExtraPayloadMapper(routingContext -> new JsonObject()
							.put("uri", routingContext.request().uri())
							.put("method", routingContext.request().method().name())
							);
					routerBuilder.rootHandler(sessionHandler);
	
					router = routerBuilder.createRouter();
					router.post("/").handler(eventHandler -> {
						requestAuth(eventHandler).onSuccess(accessToken -> {
							listPUTImportSmartDataModel(eventHandler, accessToken).onSuccess(a -> {
								eventHandler.end(Buffer.buffer(new JsonObject().encodePrettily()));
							}).onFailure(ex -> {
								eventHandler.fail(401, ex);
							});
						}).onFailure(ex -> {
							eventHandler.fail(401, ex);
						});
					});
	
					LOG.info("Configure OpenAPI succeeded");
					promise.complete();
			}).onFailure(ex -> {
				Exception ex2 = new RuntimeException("Configure OpenAPI failed", ex);
				LOG.error("Configure OpenAPI failed", ex2);
				promise.fail(ex2);
			});
		} catch (Exception ex) {
			LOG.error("Configure OpenAPI failed", ex);
			promise.fail(ex);
		}
		return promise.future();
	}

//	public Future<String> importEntity(RoutingContext eventHandler) {
//		Promise<String> promise = Promise.promise();
//		requestAuth(eventHandler).onSuccess(accessToken -> {
//			
//		}).onFailure(ex -> {
//			String msg = String.format("401 UNAUTHORIZED user %s to %s %s", siteRequest.getUser().attributes().getJsonObject("accessToken").getString("preferred_username"), serviceRequest.getExtra().getString("method"), serviceRequest.getExtra().getString("uri"));
//			eventHandler.fail(0).handle(Future.succeededFuture(
//				new ServiceResponse(401, "UNAUTHORIZED",
//					Buffer.buffer().appendString(
//						new JsonObject()
//							.put("errorCode", "401")
//							.put("errorMessage", msg)
//							.encodePrettily()
//						), MultiMap.caseInsensitiveMultiMap()
//				)
//			));
//		})
//
//		return promise.future();
//	}

	public Future<String> requestAuth(RoutingContext eventHandler) {
		Promise<String> promise = Promise.promise();
		try {
			String authHostName = config().getString(ConfigKeys.AUTH_HOST_NAME);
			Integer authPort = config().getInteger(ConfigKeys.AUTH_PORT);
			String authTokenUri = config().getString(ConfigKeys.AUTH_TOKEN_URI);
			Boolean authSsl = config().getBoolean(ConfigKeys.AUTH_SSL);
			String authClient = config().getString(ConfigKeys.AUTH_CLIENT);
			String authSecret = config().getString(ConfigKeys.AUTH_SECRET);
			MultiMap form = MultiMap.caseInsensitiveMultiMap();
			form.add("grant_type", "client_credentials");
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(authClient, authSecret);
			webClient.post(authPort, authHostName, authTokenUri).ssl(authSsl).authentication(credentials).putHeader("Content-Type", "application/json").expect(ResponsePredicate.SC_OK).sendForm(form).onSuccess(requestAuthResponse -> {
				promise.complete(requestAuthResponse.bodyAsJsonObject().getString("access_token"));
			}).onFailure(ex -> {
				LOG.error(String.format("requestAuth failed. "), new RuntimeException(ex));
				promise.fail(ex);
			});
		} catch(Exception ex) {
			LOG.error(String.format("requestAuth failed. "), ex);
			promise.fail(ex);
		}
		return promise.future();
	}

	/**
	 * Description: A helper method for generating a URL friendly unique ID for this object
	 */
	public String toId(String s) {
		if(s != null) {
			s = Normalizer.normalize(s, Normalizer.Form.NFD);
			s = StringUtils.uncapitalize(s);
			s = RegExUtils.replacePattern(s, "([A-Z])", "-$1");
			s = StringUtils.lowerCase(s);
			s = StringUtils.trim(s);
			s = RegExUtils.replacePattern(s, "\\s{1,}", "-");
			s = RegExUtils.replacePattern(s, "[^\\w-]", "");
			s = RegExUtils.replacePattern(s, "-{2,}", "-");
		}

		return s;
	}

	public Future<Void> listPUTImportSmartDataModel(RoutingContext eventHandler, String accessToken) {
		Promise<Void> promise = Promise.promise();
		List<Future> futures = new ArrayList<>();
		JsonArray jsonArray = Optional.ofNullable(eventHandler.getBodyAsJson()).map(o -> o.getJsonArray("data")).orElse(new JsonArray());
		try {
			String smartvillageHostName = config().getString(ConfigKeys.SMARTVILLAGE_HOST_NAME);
			Integer smartvillagePort = config().getInteger(ConfigKeys.SMARTVILLAGE_PORT);
			Boolean smartvillageSsl = config().getBoolean(ConfigKeys.SMARTVILLAGE_SSL);
			jsonArray.forEach(obj -> {
				JsonObject entity = (JsonObject)obj;
				futures.add(Future.future(promise1 -> {
					JsonObject importData = new JsonObject();
					JsonArray importList = new JsonArray();
					String entityId = entity.getString("id");
					String entityType = entity.getString("type");
					String entityTypeId = toId(entityType);
					JsonObject importEntity = new JsonObject().put("id", entityId).put("inheritPk", entityId).put("type", entityType);
					JsonArray importSaves = new JsonArray().add("id").add("inheritPk").add("type");
					String smartvillageUri = String.format("/api/%s-import", entityTypeId);
					for(String key : entity.fieldNames()) {
						Object val = entity.getValue(key);
						if(val instanceof JsonObject) {
							Object value = ((JsonObject) val).getValue("value");
							if(value != null) {
								importEntity.put(key, value.toString());
								importSaves.add(key);
							}
						}
					}
					importEntity.put("saves", importSaves);
					importList.add(importEntity);
					importData.put("list", importList);
					LOG.info(String.format("%s %s %s %s %s", smartvillagePort, smartvillageHostName, smartvillageUri, smartvillageSsl, importData.encodePrettily()));
					webClient.put(smartvillagePort, smartvillageHostName, smartvillageUri).ssl(smartvillageSsl).putHeader("Authorization", String.format("Bearer %s", accessToken)).putHeader("Content-Type", "application/json").expect(ResponsePredicate.SC_OK).sendJsonObject(importData).onSuccess(requestAuthResponse -> {
						LOG.info(String.format("%s smart data model imported: %s", entityType, entityId));
						promise1.complete(requestAuthResponse.bodyAsJsonObject());
					}).onFailure(ex -> {
						LOG.error(String.format("listPUTImportSmartDataModel failed. "), new RuntimeException(ex));
						promise1.fail(ex);
					});
				}));
			});
			CompositeFuture.all(futures).onSuccess(a -> {
				promise.complete();
			}).onFailure(ex -> {
				LOG.error(String.format("listPUTImportSmartDataModel failed. "), ex);
				promise.fail(ex);
			});
		} catch(Exception ex) {
			LOG.error(String.format("listPUTImportSmartDataModel failed. "), ex);
			promise.fail(ex);
		}
		return promise.future();
	}

	/**
	 */
	public Future<Void> configureApi() {
		Promise<Void> promise = Promise.promise();
		try {
			EntityApiService.registerService(vertx.eventBus(), config(), vertx);

			LOG.info("Configure API completed");
			promise.complete();
		} catch(Exception ex) {
			LOG.error("Configure API failed", ex);
			promise.fail(ex);
		}
		return promise.future();
	}

	/**	
	 *	Start the Vert.x server. 
	 **/
	public Future<Void> startServer() {
		Promise<Void> promise = Promise.promise();

		try {
			Integer sitePort = config().getInteger(ConfigKeys.SITE_PORT);
			String siteBaseUrl = config().getString(ConfigKeys.SITE_BASE_URL);
			HttpServerOptions options = new HttpServerOptions();
			options.setPort(sitePort);
	
			LOG.info(String.format("Starting server: %s", siteBaseUrl));
			vertx.createHttpServer(options).requestHandler(router).listen(ar -> {
				if (ar.succeeded()) {
					LOG.info(String.format("Start server succeeded: %s", siteBaseUrl));
					promise.complete();
				} else {
					LOG.error("Start server failed", ar.cause());
					promise.fail(ar.cause());
				}
			});
		} catch (Exception ex) {
			LOG.error("Start server failed", ex);
			promise.fail(ex);
		}

		return promise.future();
	}

}
