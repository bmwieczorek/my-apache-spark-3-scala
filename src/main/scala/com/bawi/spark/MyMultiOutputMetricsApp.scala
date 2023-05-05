package com.bawi.spark

import com.codahale.metrics.Counter
import org.apache.spark.api.plugin.{DriverPlugin, ExecutorPlugin, PluginContext, SparkPlugin}
import org.slf4j.{Logger, LoggerFactory}

import java.util

object MyMultiOutputMetricsApp {
  private val LOGGER: Logger = LoggerFactory.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {
    val spark = SparkUtils.createSparkWithMetrics(getClass, CustomMetricSparkPlugin.getClass, args)
    val evenNumberAccum = spark.sparkContext.longAccumulator("evenNumberAccum")
    val oddNumberAccum = spark.sparkContext.longAccumulator("oddNumberAccum")

    val data = spark.sparkContext.parallelize(1 to 9)
    val allData = data.map(n => { LOGGER.info(s"all element=$n"); n})
    allData.cache()

    allData.filter(n =>n % 2 == 0).map(n => {
      CustomMetricSparkPlugin.evenNumberCounter.inc()
      evenNumberAccum.add(1)
      LOGGER.info(s"even element=$n")
      n
    }).count()

    allData.filter(n => n % 2 == 1).map(n => {
      CustomMetricSparkPlugin.oddNumberCounter.inc()
      oddNumberAccum.add(1)
      LOGGER.info(s"odd element=$n")
      n
    }).count()

    LOGGER.info(s"Even: ${evenNumberAccum.value}, odd: ${oddNumberAccum.value}")
    spark.stop()
  }

  object CustomMetricSparkPlugin {
    val oddNumberCounter = new Counter
    val evenNumberCounter = new Counter
  }
  class CustomMetricSparkPlugin extends SparkPlugin {
    override def driverPlugin(): DriverPlugin = null
    override def executorPlugin(): ExecutorPlugin = new ExecutorPlugin {
      override def init(ctx: PluginContext, extraConf: util.Map[String, String]): Unit = {
        val metricRegistry = ctx.metricRegistry()
        metricRegistry.register("even_counter", CustomMetricSparkPlugin.evenNumberCounter)
        metricRegistry.register("odd_counter", CustomMetricSparkPlugin.oddNumberCounter)
      }
    }
  }
}
