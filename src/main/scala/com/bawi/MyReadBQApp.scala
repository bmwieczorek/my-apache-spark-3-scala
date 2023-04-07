package com.bawi

object MyReadBQApp {

  def main(args: Array[String]): Unit = {
    val spark = SparkUtils.createSpark(getClass, args)
    val sql = "SELECT name, UPPER(name) as uname FROM bartek_person.bartek_person_table"
    val refDF = spark.read.format("bigquery")
      .option("viewsEnabled", "true")
      .option("materializationDataset", "bartek_person")
      .load(sql)
    val refMap = refDF.collect.map(t => t(0) -> t(1)).toMap.asInstanceOf[Map[String, String]]
    println(refMap)
    spark.stop()
  }
}
