package org.computate.ngsildsmartvillagesync.api.entity;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.api.service.WebApiServiceGen;
import io.vertx.serviceproxy.ServiceBinder;

@WebApiServiceGen
@ProxyGen
public interface EntityApiService {

	static void registerService(EventBus eventBus, JsonObject config, Vertx vertx) {
		new ServiceBinder(vertx).setAddress("ngsild-smartvillage-sync-Entity").register(EntityApiService.class, new EntityApiServiceImpl(eventBus, config));
	}

	public void postRequest(JsonObject body, ServiceRequest serviceRequest, Handler<AsyncResult<ServiceResponse>> eventHandler);
}
