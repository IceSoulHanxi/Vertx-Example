package com.ixnah.server.vertx.web.impl

import com.ixnah.server.vertx.util.GlobalRouter
import com.ixnah.server.vertx.web.api.TransactionService
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.api.service.RouteToEBServiceHandler
import io.vertx.ext.web.api.service.ServiceRequest
import io.vertx.ext.web.api.service.ServiceResponse
import io.vertx.ext.web.validation.builder.Bodies.json
import io.vertx.ext.web.validation.builder.Parameters.optionalParam
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRouter
import io.vertx.json.schema.SchemaRouterOptions
import io.vertx.json.schema.common.dsl.Schemas.objectSchema
import io.vertx.json.schema.common.dsl.Schemas.stringSchema
import io.vertx.json.schema.draft7.Draft7SchemaParser
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.serviceproxy.ServiceBinder
import org.slf4j.LoggerFactory


class TransactionServiceImpl() : TransactionService, CoroutineVerticle() {

  private val logger by lazy { LoggerFactory.getLogger(this::class.java) }
  private lateinit var messageConsumer: MessageConsumer<JsonObject>

  override suspend fun start() {
    val parser = Draft7SchemaParser.create(SchemaRouter.create(vertx, SchemaRouterOptions()))
    val eventBus = vertx.eventBus()

    val router = GlobalRouter.getRouter()
    // 注意!! router的handler会在WebServerVerticle的EventLoop线程执行
    // 不要在handler里使用非线程安全内容 RouteToEBServiceHandler是线程安全的
    // 如果直接在handler里调用当前实现类的代码 可能会出现并发问题!!!
    // 如果直接在handler里调用当前实现类的代码 可能会出现并发问题!!!
    // 如果直接在handler里调用当前实现类的代码 可能会出现并发问题!!!
    router.get("/api/transactions")
      .handler(
        ValidationHandlerBuilder.create(parser)
          .queryParameter(optionalParam("from", stringSchema()))
          .queryParameter(optionalParam("to", stringSchema()))
          .build()
      ).handler(
        RouteToEBServiceHandler.build(eventBus, TransactionService::class.java.name, "getTransactionsList")
      )
    router.post("/api/transactions")
      .handler(
        ValidationHandlerBuilder.create(parser)
          .body(json(objectSchema()))
          .build()
      ).handler(
        RouteToEBServiceHandler.build(eventBus, TransactionService::class.java.name, "putTransaction")
      )

    // 注册Service Service会在当前线程中运行
    messageConsumer = ServiceBinder(vertx)
      .setAddress(TransactionService::class.java.name)
      .register(TransactionService::class.java, this)

    logger.info("TransactionService deployment completed!")
  }

  override suspend fun stop() {
    messageConsumer.unregister().await()
  }

  override fun getTransactionsList(
    from: String?,
    to: String?,
    context: ServiceRequest?,
    resultHandler: Handler<AsyncResult<ServiceResponse?>?>?
  ) {
    logger.info("Thread: ${Thread.currentThread().name}, from: $from, to: $to")
    resultHandler!!.handle(
      Future.succeededFuture(
        ServiceResponse.completedWithJson(JsonArray())
      )
    )
  }

  override fun putTransaction(
    body: JsonObject?,
    context: ServiceRequest?,
    resultHandler: Handler<AsyncResult<ServiceResponse?>?>?
  ) {
    logger.info("Thread: ${Thread.currentThread().name}, body: $body")
    resultHandler!!.handle(
      Future.succeededFuture(
        ServiceResponse.completedWithJson(JsonArray())
      )
    )
  }
}
