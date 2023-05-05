package com.bawi.spark

import org.apache.spark.sql.functions.broadcast

object MyBroadcastJoinApp {

  def main(args: Array[String]): Unit = {
    val spark = SparkUtils.createSpark(getClass, args)
    import spark.implicits._
    val personDF = Seq(("John", "London"), ("Jan", "Krakow"), ("Jeroen", "Amsterdam")).toDF("name", "city")
    val refDF = Seq(("Krakow", "Poland"), ("London", "UK")).toDF("city", "country")
    personDF.join(broadcast(refDF), personDF("city") <=> refDF("city"), "leftouter").drop(refDF("city"))
//    personDF.join(broadcast(refDF), "city")
      .show()
    spark.stop()
  }
}
