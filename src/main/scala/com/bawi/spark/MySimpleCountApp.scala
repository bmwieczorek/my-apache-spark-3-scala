package com.bawi.spark

import org.slf4j.{Logger, LoggerFactory}

object MySimpleCountApp {
  private val LOGGER: Logger = LoggerFactory.getLogger(getClass.getName)
  def main(args: Array[String]): Unit = {
    val spark = SparkUtils.createSpark(getClass, args)
    val data = spark.sparkContext.parallelize(Array(1, 2, 4, 3, 5))
    val allData = data.map(n => { LOGGER.info(s"all element=$n"); n})
    val cnt = allData.count()
    LOGGER.info(s"Cnt: $cnt")
    spark.stop()
  }
}
