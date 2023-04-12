package com.bawi

import org.apache.spark.sql.DataFrame
import org.slf4j.LoggerFactory

object MyReadAvroGcsApp {
  private val LOGGER = LoggerFactory.getLogger(MyReadAvroGcsApp.getClass)

  def main(args: Array[String]): Unit = {
    LOGGER.info("GOOGLE_APPLICATION_CREDENTIALS={}", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))

    val spark = SparkUtils.createSpark(getClass, args)
//    val spark = SparkSession.builder().config(new SparkConf().setAppName("MyReadAvroGcsApp").setMaster("local[*]")).getOrCreate()
    val data: DataFrame = spark.read.format("avro")
//      .load("src/test/resources/myRecord.snappy.avro")
      .load("gs://" + spark.conf.get("projectId") + "-bartek-dataproc/myRecord.snappy.avro")
//      .load("gs://my-bucket/myRecord.snappy.avro")
    data.show()
    spark.stop()
  }
}
