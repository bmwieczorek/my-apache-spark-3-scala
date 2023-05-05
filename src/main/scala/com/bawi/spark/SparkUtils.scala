package com.bawi.spark

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object SparkUtils {
  private val LOGGER = LoggerFactory.getLogger(SparkUtils.getClass)

  def createSpark(appClazz: Class[_], args: Array[String], additionalArgs: String*): SparkSession = {
    val sparkConf = new SparkConf().setAppName(getClassSimpleName(appClazz))
//      .set("spark.plugins", getClassName(CustomMetricSparkPlugin.getClass))
      .set("spark.metrics.namespace", getClassSimpleName(appClazz))

      .set("spark.metrics.conf.*.sink.slf4j.class", "org.apache.spark.metrics.sink.Slf4jSink")
      .set("spark.metrics.conf.*.sink.slf4j.period", "10")
      .set("spark.metrics.conf.*.sink.slf4j.unit", "seconds")

//      .set("spark.metrics.conf.*.sink.csv.class", "org.apache.spark.metrics.sink.CsvSink")
//      .set("spark.metrics.conf.*.sink.csv.period", "5")
//      .set("spark.metrics.conf.*.sink.csv.unit", "seconds")
//      .set("spark.metrics.conf.*.sink.csv.directory", "/Users/sg0212148/dev/my-apache-spark-3-scala/target/csv")
//      .set("spark.metrics.conf.worker.sink.csv.period", "5")
//      .set("spark.metrics.conf.worker.sink.csv.unit", "seconds")

      .set("spark.metrics.conf.driver.source.jvm.class", "org.apache.spark.metrics.source.JvmSource")

//      .set("spark.metrics.conf", "metrics-slf4j.properties")

    val allArgs: Array[String] = Array.concat(additionalArgs.toArray, args)
    allArgs
      .filter(s => s.startsWith("--"))
      .map(s => {
        val key = s.substring(2, s.indexOf("="))
        val value = s.substring(s.indexOf("=") + 1)
        (key, value)
      })
      .map(t => (t._1, t._2)).toMap
      .foreach(e => sparkConf.set(e._1, e._2))
    if (isLocal) {
      sparkConf.setMaster("local[*]")
      if (!sparkConf.contains("projectId"))
        sparkConf.set("projectId", System.getenv("GCP_PROJECT"))
    }
    //    sparkConf.set("fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
    //    sparkConf.set("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
    //    if (sparkConf.contains("projectId")) {
    //        sparkConf.set("fs.gs.project.id", sparkConf.get("projectId"))
    //    }
    LOGGER.info("Setting Spark conf: {}", sparkConf.getAll.map(e => e._1 + "=" + e._2))
    SparkSession.builder().config(sparkConf).getOrCreate()
  }

  private def isLocal: Boolean = {
    val osName = System.getProperty("os.name").toLowerCase
    osName.contains("mac") || osName.contains("windows")
  }

  def getClassName(clazz: Class[_]): String = {
    clazz.getName.split('$')(0)
  }

  private def getClassSimpleName(clazz: Class[_]): String = {
    clazz.getSimpleName.split('$')(0)
  }
}
