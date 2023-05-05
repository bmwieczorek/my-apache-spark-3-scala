package com.bawi.spark

import org.apache.spark.sql.SaveMode
import org.slf4j.LoggerFactory

object MyReadAvroGcsAndWriteBQApp {
  private val LOGGER = LoggerFactory.getLogger(MyReadAvroGcsAndWriteBQApp.getClass)

  def main(args: Array[String]): Unit = {
    LOGGER.info("GOOGLE_APPLICATION_CREDENTIALS={}", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))

    val spark = SparkUtils.createSpark(getClass, args)
    val dataDF = spark.read.format("avro")
      .load("gs://" + spark.conf.get("projectId") + "-bartek-dataproc/myRecord.snappy.avro")

    dataDF.write.format("bigquery")
      .option("writeMethod", "indirect")
      .option("temporaryGcsBucket", spark.conf.get("projectId") + "-bartek-spark")
      .mode(SaveMode.Append)
      .save("bartek_person.bartek_person_table")
    spark.stop()
  }
}
