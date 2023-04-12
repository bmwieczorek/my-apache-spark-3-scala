package com.bawi

import org.apache.spark.sql.DataFrame

object MyReadAvroGcsApp {

  def main(args: Array[String]): Unit = {
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
