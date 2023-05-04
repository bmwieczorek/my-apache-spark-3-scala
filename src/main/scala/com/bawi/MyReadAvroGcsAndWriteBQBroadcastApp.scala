package com.bawi

import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.{DataFrame, Row, SaveMode}
import org.slf4j.LoggerFactory

object MyReadAvroGcsAndWriteBQBroadcastApp {

  private val LOGGER = LoggerFactory.getLogger(MyReadAvroGcsAndWriteBQBroadcastApp.getClass)

  def main(args: Array[String]): Unit = {
    LOGGER.info("GOOGLE_APPLICATION_CREDENTIALS={}", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))

    val spark = SparkUtils.createSpark(getClass, args)
    import spark.implicits._

    val processedRecordsCounter = spark.sparkContext.longAccumulator("metricsCounter")

    var dataDF: DataFrame = spark.read.format("avro").load("gs://" + spark.conf.get("projectId") + "-bartek-dataproc/myRecord.snappy.avro")

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
      processedRecordsCounter.add(1)
      val name = p.getAs[String]("name")
      val body = p.getAs[Array[Byte]]("body")
      val uname = p.getAs[String]("uname")
      LOGGER.info("processing {}", (name, new String(body), uname))
      (name, body, uname)
    }).toDF(dataDF.columns: _*)

    dataDF.write.format("bigquery")
      .option("writeMethod", "indirect")
      .option("temporaryGcsBucket", spark.conf.get("projectId") + "-bartek-spark")
      .mode(SaveMode.Append)
      .save("bartek_person.bartek_person_spark")

    LOGGER.info(s"processedRecordsCounter=$processedRecordsCounter.value")

    spark.stop()
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
}
