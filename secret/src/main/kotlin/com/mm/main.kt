package com.mm

import com.mm.http.HttpVerticle
import io.vertx.core.Vertx
import scala.App

fun main(args : Array<String>) {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(HttpVerticle()) { ar ->
        if (ar.succeeded()) {
            println("Application started")
        } else {
            println("Could not start application")
            ar.cause().printStackTrace()
        }
    }
}