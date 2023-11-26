package org.computate.ngsildsmartvillagesync.api.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;

public class EntityApiServiceImpl implements EntityApiService {
	protected static final Logger LOG = LoggerFactory.getLogger(EntityApiServiceImpl.class);

	protected EventBus eventBus;

	protected JsonObject config;

	public EntityApiServiceImpl(EventBus eventBus, JsonObject config) {
		this.eventBus = eventBus;
		this.config = config;
	}

	@Override
	public void postRequest(JsonObject body, ServiceRequest serviceRequest, Handler<AsyncResult<ServiceResponse>> eventHandler) {
		LOG.info(body.encodePrettily());
		eventHandler.handle(Future.succeededFuture(ServiceResponse.completedWithJson(Buffer.buffer(new JsonObject().put("success", true).encodePrettily()))));
	}
}
