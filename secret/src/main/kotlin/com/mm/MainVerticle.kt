package com.mm

import com.mm.http.HttpVerticle
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx

class MainVerticle : AbstractVerticle() {
    override fun start() {
        val deploymentOptions = DeploymentOptions().setConfig(config())
        vertx.deployVerticle(HttpVerticle(), deploymentOptions) { ar ->
            if (ar.succeeded()) {
                println("Application started")
            } else {
                println("Could not start application")
                ar.cause().printStackTrace()
            }
        }
    }
    companion object {
        @JvmStatic
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
    }
}

