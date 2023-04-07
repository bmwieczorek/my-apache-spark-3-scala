package com.bawi

import org.apache.spark.sql.SaveMode

object MyReadAvroGcsAndWriteBQApp {

  def main(args: Array[String]): Unit = {
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
