package com.ixnah.server.vertx.util

import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.VertxException
import io.vertx.ext.web.Router
import java.util.concurrent.ConcurrentHashMap

/**
 * 从当前线程上下文获取Vertx
 * 如果不是Vertx线程则抛出异常
 */
fun getOrCreateVertx(): Vertx {
  return if (Context.isOnVertxThread())
    Vertx.currentContext().owner()
  else
    throw VertxException("Current thread is not a Vertx thread")
}

/**
 * Vertx全局Router
 * 以Vertx对象的物理内存地址储存
 * 避免跨上下文调用Router
 */
class GlobalRouter {
  companion object {
    private val routerMap: ConcurrentHashMap<Int, Router> = ConcurrentHashMap(4)

    /**
     * 从当前Vertx获取全局Router
     * 如果不存在则新建
     */
    fun getRouter(): Router {
      val vertx = getOrCreateVertx()
      val memoryAddress = System.identityHashCode(vertx) // 获取Vertx对象的物理内存地址
      return routerMap.getOrPut(memoryAddress) { Router.router(vertx) }
    }
  }
}
