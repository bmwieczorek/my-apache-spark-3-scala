package com.bawi

import org.apache.spark.sql.Row
import org.apache.spark.sql.functions.udf

object MyBroadcastVariableApp {

  def main(args: Array[String]): Unit = {
    val spark = SparkUtils.createSpark(getClass, args)
    import spark.implicits._
    val personDF = Seq(("John", "London"), ("Jan", "Krakow"), ("Jeroen", "Amsterdam")).toDF("name", "city")
    val refMap = Map("Krakow" -> "Poland", "London" -> "UK")
    println(refMap)
    val refDF = Seq(("Krakow", "Poland"), ("London", "UK")).toDF("city", "country")
    val refMapFromDF = refDF.collect.map(t => t(0) -> t(1)).toMap.asInstanceOf[Map[String,String]]
    println(refMapFromDF)

    val refBroadcast = spark.sparkContext.broadcast(refMap)

    val getCountry = (city: String) => {
      refBroadcast.value.getOrElse(city, "UNKNOWN")
    }
    val getCountryUDF = udf(getCountry)

    personDF
      .map((p: Row) => {
        val name = p.getAs[String]("name")
        val city = p.getAs[String]("city")
        val country = refBroadcast.value.getOrElse(city, "UNKNOWN")
        (name, city, country)
      }).toDF(personDF.columns :+ "country":_*)
//      .withColumn("country", getCountryUDF(col("city")))
      .show()
    spark.stop()
  }
}
