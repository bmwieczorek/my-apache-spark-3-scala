package com.bawi.spark.metrics

import com.codahale.metrics.Counter
import org.apache.spark.api.plugin.{DriverPlugin, ExecutorPlugin, PluginContext, SparkPlugin}

import java.util

object CustomMetricSparkPlugin {
  val counter = new Counter
}

// needs to be registered in spark conf
// .set("spark.plugins", "com.bawi.spark.metrics.CustomMetricSparkPlugin")
class CustomMetricSparkPlugin extends SparkPlugin {
  override def driverPlugin(): DriverPlugin = null

  override def executorPlugin(): ExecutorPlugin = new ExecutorPlugin {
    override def init(ctx: PluginContext, extraConf: util.Map[String, String]): Unit = {
      val metricRegistry = ctx.metricRegistry()
      metricRegistry.register("customMetricCounter", CustomMetricSparkPlugin.counter)
    }
  }
}
