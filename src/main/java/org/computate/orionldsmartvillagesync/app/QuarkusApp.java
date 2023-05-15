package org.computate.orionldsmartvillagesync.app;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;


@QuarkusMain
@ApplicationScoped
public class QuarkusApp extends MainVerticle {
	private static final Logger LOG = LoggerFactory.getLogger(QuarkusApp.class);

	@Inject
	Vertx vertx;

	public static void main(String...args) {
		Quarkus.run(args);
	}

	public void init(@Observes StartupEvent ev) {
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
}
