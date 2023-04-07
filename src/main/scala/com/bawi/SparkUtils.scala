package com.bawi

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object SparkUtils {
  private val LOGGER = LoggerFactory.getLogger(SparkUtils.getClass)

  def createSpark(clazz: Class[_], args: Array[String], additionalArgs: String*): SparkSession = {
    val sparkConf = new SparkConf().setAppName(clazz.getName)
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
    sparkConf.set("fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
    sparkConf.set("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
    if (sparkConf.contains("projectId")) {
        sparkConf.set("fs.gs.project.id", sparkConf.get("projectId"))
    }
    LOGGER.info("Setting Spark conf: {}", sparkConf.getAll.map(e => e._1 + "=" + e._2))
    SparkSession.builder().config(sparkConf).getOrCreate()
  }

  private def isLocal: Boolean = {
    val osName = System.getProperty("os.name").toLowerCase
    osName.contains("mac") || osName.contains("windows")
  }

  def assertNotNullAndGet(arg: Any): Any = {
    if (arg == null) throw new IllegalArgumentException("Argument " + arg + " should not be null!")
  }
}
