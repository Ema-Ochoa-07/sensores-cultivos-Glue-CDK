import com.amazonaws.services.glue.GlueContext
import com.amazonaws.services.glue.util.GlueArgParser
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object GlueApp {
  def main(sysArgs: Array[String]): Unit = {
    val args = GlueArgParser.getResolvedOptions(sysArgs, Array("host", "user", "password", "accountid"))

    val spark: SparkSession = SparkSession.builder().getOrCreate()
    val glueContext: GlueContext = new GlueContext(spark.sparkContext)

    val jdbcUrl = s"jdbc:mysql://${args("host")}:3306/cultivo_db"
    val connectionProperties = new java.util.Properties()
    connectionProperties.put("user", args("user"))
    connectionProperties.put("password", args("password"))
    connectionProperties.put("driver", "com.mysql.cj.jdbc.Driver")

    val dfRDS = spark.read.jdbc(jdbcUrl, "mediciones", connectionProperties)

    dfRDS.write
      .mode("overwrite")
      .parquet(s"s3://datos-cultivo-procesados-${args("accountid")}/raw/")

    println("¡Extracción completada! Datos guardados en S3.")
  }
}
