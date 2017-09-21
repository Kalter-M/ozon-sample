package ru.at_consulting.bigdata.train.ozon_sample.system

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions.lower
import org.apache.spark.{SparkConf, SparkContext}
import ru.at_consulting.bigdata.train.ozon_sample.system.Parameters._


object Main {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("agg-ozon").setMaster("local")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)


    val inputSmsDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").load(SMS_FILE)

    val inputUserDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").load(USER_FILE)

    val numAndTextDF = inputSmsDF.select("a_number", "text")
    val userNumDF = inputUserDF.select("subs_key")

    val joinUserAndTextDF = numAndTextDF.join(userNumDF, numAndTextDF.col("a_number") === userNumDF.col("subs_key"))
    val userAndTextDF = joinUserAndTextDF.select("a_number", "text").withColumnRenamed("a_number", "user")

    //userAndTextDF.persist()

    val userAndLowerCaseTextDF = userAndTextDF.withColumn("text", lower(userAndTextDF.col("text")) )

    val foundUserByKeyDF = userAndLowerCaseTextDF.where("text LIKE '%" + SEARCH_KEY + "%'").select("user")

    foundUserByKeyDF.show()

    sc.stop()
  }

}

