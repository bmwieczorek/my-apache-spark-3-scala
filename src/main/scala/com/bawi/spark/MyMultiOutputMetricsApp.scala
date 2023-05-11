package com.bawi.spark

import com.codahale.metrics.Counter
import org.apache.spark.SparkContext
import org.apache.spark.api.plugin.{DriverPlugin, ExecutorPlugin, PluginContext, SparkPlugin}
import org.slf4j.{Logger, LoggerFactory}

import java.util
import java.util.Collections

object MyMultiOutputMetricsApp {
  private val LOGGER: Logger = LoggerFactory.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {
    val spark = SparkUtils.createSparkWithCustomMetrics(getClass, CustomMetricSparkPlugin.getClass, args
      // optional custom metrics sink requires maven -Pcustom-metrics-sink when running locally
//    , "--spark.metrics.conf.*.sink.mygcpmetric.class=org.apache.spark.metrics.sink.MyGcpMetricSink"
//    , "--spark.metrics.conf.*.sink.myconsole.class=org.apache.spark.metrics.sink.MyConsoleSink"
    )
    val evenNumberAccum = spark.sparkContext.longAccumulator("evenNumberAccum")
    val oddNumberAccum = spark.sparkContext.longAccumulator("oddNumberAccum")
    val allNumberAccum = spark.sparkContext.longAccumulator("allNumberAccum")

    val data = spark.sparkContext.parallelize(1 to 9)
    val allData = data.map(n => { LOGGER.info(s"all element=$n"); n})
    allData.cache()

    allData.filter(n =>n % 2 == 0).map(n => {
      CustomMetricSparkPlugin.evenNumberCounter.inc()
      CustomMetricSparkPlugin.allNumberCounter.inc()
      evenNumberAccum.add(1)
      allNumberAccum.add(1)
      LOGGER.info(s"even element=$n")
      n
    }).count()

    allData.filter(n => n % 2 == 1).map(n => {
      CustomMetricSparkPlugin.oddNumberCounter.inc()
      CustomMetricSparkPlugin.allNumberCounter.inc()
      oddNumberAccum.add(1)
      allNumberAccum.add(1)
      LOGGER.info(s"odd element=$n")
      n
    }).count()

    CustomMetricSparkPlugin.allDriverNumberCounter.inc(allNumberAccum.value)
    LOGGER.info(s"Even: ${evenNumberAccum.value}, odd: ${oddNumberAccum.value}, all ${allNumberAccum.value}")

    spark.stop()
  }

  object CustomMetricSparkPlugin {
    val oddNumberCounter = new Counter
    val evenNumberCounter = new Counter
    val allNumberCounter = new Counter
    val allDriverNumberCounter = new Counter
  }
  class CustomMetricSparkPlugin extends SparkPlugin {
    override def driverPlugin(): DriverPlugin = new DriverPlugin {
      override def init(sparkContext: SparkContext, pluginContext: PluginContext): util.Map[String, String] = {
        val metricRegistry = pluginContext.metricRegistry()
        metricRegistry.register("driver_all_counter", CustomMetricSparkPlugin.allDriverNumberCounter)
        Collections.emptyMap[String, String]
      }
    }
    override def executorPlugin(): ExecutorPlugin = new ExecutorPlugin {
      override def init(ctx: PluginContext, extraConf: util.Map[String, String]): Unit = {
        val metricRegistry = ctx.metricRegistry()
        metricRegistry.register("even_counter", CustomMetricSparkPlugin.evenNumberCounter)
        metricRegistry.register("odd_counter", CustomMetricSparkPlugin.oddNumberCounter)
        metricRegistry.register("all_counter", CustomMetricSparkPlugin.allNumberCounter)
      }
    }
  }
}

// MyMultiOutputMetricsApp.1.plugin.com.bawi.spark.MyMultiOutputMetricsApp$CustomMetricSparkPlugin.all_counter=9 on bartek-spark-313s-on-dataproc-w-1
// MyMultiOutputMetricsApp.driver.plugin.com.bawi.spark.MyMultiOutputMetricsApp$CustomMetricSparkPlugin.all_driver_worker_counter=0 on bartek-spark-313s-on-dataproc-m
