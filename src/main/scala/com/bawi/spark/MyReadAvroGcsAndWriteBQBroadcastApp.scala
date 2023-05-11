package com.bawi.spark

import com.codahale.metrics.Counter
import org.apache.spark.SparkContext
import org.apache.spark.api.plugin.{DriverPlugin, ExecutorPlugin, PluginContext, SparkPlugin}
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.{DataFrame, Row, SaveMode}
import org.slf4j.LoggerFactory

import java.util
import java.util.Collections

object MyReadAvroGcsAndWriteBQBroadcastApp {

  private val LOGGER = LoggerFactory.getLogger(MyReadAvroGcsAndWriteBQBroadcastApp.getClass)

  def main(args: Array[String]): Unit = {
    LOGGER.info("GOOGLE_APPLICATION_CREDENTIALS={}", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))

    val spark = SparkUtils.createSparkWithCustomMetrics(getClass, CustomMetricSparkPlugin.getClass, args
//      ,s"--spark.plugins=${classOf[CustomMetricSparkPlugin].getName}"
      // optional custom metrics sink requires maven -Pcustom-metrics-sink when running locally
//          , "--spark.metrics.conf.*.sink.mygcpmetric.class=org.apache.spark.metrics.sink.MyGcpMetricSink"
//          , "--spark.metrics.conf.*.sink.myconsole.class=org.apache.spark.metrics.sink.MyConsoleSink"
    )

    import spark.implicits._

    val processedRecordsAccumulator = spark.sparkContext.longAccumulator("metricsCounter")

    var dataDF: DataFrame = spark.read.format("avro")
      .load("gs://" + spark.conf.get("projectId") + "-bartek-dataproc/myRecord*1k.snappy.avro")
//      .load("gs://" + spark.conf.get("projectId") + "-bartek-dataproc/myRecord.snappy.avro")

    val refDF = spark.read.format("bigquery")
      .option("viewsEnabled", "true")
      .option("materializationDataset", "bartek_person")
      .load("SELECT name, UPPER(name) as uname FROM bartek_person.bartek_person_table")

    val refMap = refDF.collect.map(t => t(0) -> t(1)).toMap.asInstanceOf[Map[String, String]]
    val refBroadcast = spark.sparkContext.broadcast(refMap)

    val getCountry = (name: String) => {
      refBroadcast.value.getOrElse(name, "UNKNOWN")
    }
    val getCountryUDF = udf(getCountry)

    dataDF = dataDF.withColumn("uname", getCountryUDF(col("name")))
    dataDF = dataDF.map((p: Row) => {
      processedRecordsAccumulator.add(1)
      CustomMetricSparkPlugin.executor_counter.inc()
      val name = p.getAs[String]("name")
      val body = p.getAs[Array[Byte]]("body")
      val uname = p.getAs[String]("uname")
//      LOGGER.info("processing {}", (name, new String(body), uname))
      Thread.sleep(100)
      (name, body, uname)
    }).toDF(dataDF.columns: _*)

    dataDF.write.format("bigquery")
      .option("writeMethod", "indirect")
      .option("temporaryGcsBucket", spark.conf.get("projectId") + "-bartek-spark")
      .mode(SaveMode.Append)
      .save("bartek_person.bartek_person_spark")

    CustomMetricSparkPlugin.driver_counter.inc(processedRecordsAccumulator.value)

    LOGGER.info(s"processedRecordsAccumulator=${processedRecordsAccumulator.value}")

    spark.stop()
  }

  object CustomMetricSparkPlugin {
    val executor_counter = new Counter
    val driver_counter = new Counter
  }
  // needs to be registered in spark conf .set("spark.plugins", "com.bawi.spark.metrics.CustomMetricSparkPlugin")
  class CustomMetricSparkPlugin extends SparkPlugin {
    override def driverPlugin(): DriverPlugin = new DriverPlugin {
      override def init(sparkContext: SparkContext, pluginContext: PluginContext): util.Map[String, String] = {
        val metricRegistry = pluginContext.metricRegistry()
        metricRegistry.register("driver_records_processed", CustomMetricSparkPlugin.driver_counter)
        Collections.emptyMap[String, String]
      }
    }
    override def executorPlugin(): ExecutorPlugin = new ExecutorPlugin {
      override def init(ctx: PluginContext, extraConf: util.Map[String, String]): Unit = {
        val metricRegistry = ctx.metricRegistry()
        metricRegistry.register("executor_records_processed", CustomMetricSparkPlugin.executor_counter)
      }
    }
  }
}

// Spark execution 2 stages on dataproc
//  Stage 1:
//    Scan avro
//      FileScanRDD[3] save at BigQueryWriteHelper.java: 105
//      MapPartitionsRDD[4] save at BigQueryWriteHelper.java: 105
//  WholeStageCodegen (1)
//      MapPartitionsRDD[5] save at BigQueryWriteHelper.java: 105
//
//  Stage 0:
//      PreScala213BigQueryRDD[0] RDD at PreScala213BigQueryRDD.java: 71
//  WholeStageCodegen(1)
//      MapPartitionsRDD[1] collect at MyReadAvroGcsAndWriteBQBroadcastApp.scala: 24
//  mapPartitionsInternal
//      MapPartitionsRDD[2] collect at MyReadAvroGcsAndWriteBQBroadcastApp.scala: 24