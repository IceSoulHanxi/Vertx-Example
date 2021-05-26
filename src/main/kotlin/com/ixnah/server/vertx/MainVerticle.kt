package com.ixnah.server.vertx

import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import org.slf4j.LoggerFactory

class MainVerticle : CoroutineVerticle() {

  private val logger by lazy { LoggerFactory.getLogger(this::class.java) }

  override suspend fun start() {
    // 由于使用了Vertx全局Router 所以说必须使用await或者compose按顺序部署Verticle
    vertx.deployVerticle("kt:com.ixnah.server.vertx.web.BaseVerticle").await()
    vertx.deployVerticle("kt:com.ixnah.server.vertx.web.impl.TransactionServiceImpl").await()
    vertx.deployVerticle("kt:com.ixnah.server.vertx.web.WebServerVerticle").await()
    logger.info("All Verticle deployment completed!")
  }
}
