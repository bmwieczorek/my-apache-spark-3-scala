package com.bawi

import org.apache.spark.sql.DataFrame

object MyReadAvroGcsApp {

  def main(args: Array[String]): Unit = {
    System.getenv("GOOGLE_APPLICATION_CREDENTIALS") // print(GoogleCredentials.getApplicationDefault)
    val spark = SparkUtils.createSpark(getClass, args)
//    val data: DataFrame = spark.read.format("avro").load("src/test/resources/myRecord.snappy.avro")
    val data: DataFrame = spark.read.format("avro").load("gs://" + spark.conf.get("projectId") + "-bartek-dataproc/myRecord.snappy.avro")
    data.show()
    spark.stop()
  }
}
