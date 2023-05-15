package org.computate.orionldsmartvillagesync.app;

import java.util.Optional;

import org.computate.orionldsmartvillagesync.api.entity.EntityApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
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

	/**	
	 *	The main method for the Vert.x application that runs the Vert.x Runner class
	 **/
	public static void  main(String[] args) {
		Vertx vertx = Vertx.vertx();
		try {
			Future<Void> originalFuture = Future.future(a -> a.complete());
			Future<Void> future = originalFuture;

			future = future.compose(a -> run(new JsonObject()));
			future.compose(a -> vertx.close());
		} catch(Exception ex) {
			LOG.error("Error running vertx", ex);
			vertx.close();
		}
	}

	public static Future<Void> run(JsonObject config) {
		Promise<Void> promise = Promise.promise();
		try {
			VertxOptions vertxOptions = new VertxOptions();
			EventBusOptions eventBusOptions = new EventBusOptions();
	
			Integer siteInstances = 2;
			vertxOptions.setEventBusOptions(eventBusOptions);
	
			Vertx vertx = Vertx.vertx(vertxOptions);
			DeploymentOptions deploymentOptions = new DeploymentOptions();
			deploymentOptions.setInstances(siteInstances);
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
	 * This is called by Vert.x when the verticle instance is deployed. 
	 * Initialize a new site context object for storing information about the entire site in English. 
	 * Setup the startPromise to handle the configuration steps and starting the server. 
	 **/
	@Override()
	public void  start(Promise<Void> startPromise) throws Exception, Exception {
		try {
			configureOpenApi().onComplete(d -> 
				configureApi().onComplete(k -> 
					startServer().onComplete(n -> startPromise.complete())
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
			Integer sitePort = Integer.parseInt(Optional.ofNullable(System.getenv("SITE_PORT")).orElse("12080"));
			String siteBaseUrl = String.format("http://localhost:%s", sitePort);
			HttpServerOptions options = new HttpServerOptions();
			options.setPort(sitePort);
	
			LOG.info(String.format("Start server", siteBaseUrl));
			vertx.createHttpServer(options).requestHandler(router).listen(ar -> {
				if (ar.succeeded()) {
					LOG.info(String.format("Start server succeeded", siteBaseUrl));
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
