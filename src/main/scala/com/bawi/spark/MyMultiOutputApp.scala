package com.bawi.spark

import org.slf4j.{Logger, LoggerFactory}

object MyMultiOutputApp {
  private val LOGGER: Logger = LoggerFactory.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {
    val spark = SparkUtils.createSpark(getClass, args)
    val data = spark.sparkContext.parallelize(Array(1, 2, 4, 3, 5))
    val allData = data.map(n => { LOGGER.info(s"all element=$n"); n})
    allData.cache()
    val even = allData.filter(n =>n % 2 == 0).map(n => { LOGGER.info(s"even element=$n"); n}).count()
    val odd = allData.filter(n => n % 2 == 1).map(n => { LOGGER.info(s"odd element=$n"); n}).count()
    LOGGER.info(s"Even: $even, odd: $odd")
    spark.stop()
  }
}
