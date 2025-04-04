// Script: Extracción de datos desde RDS MySQL a S3 (Spark Scala)
// Descripción: Lee datos de la tabla 'mediciones' en RDS y los escribe en S3 en formato Parquet.

import com.amazonaws.services.glue.GlueContext
import com.amazonaws.services.glue.util.GlueArgParser
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object GlueApp {
  def main(sysArgs: Array[String]): Unit = {
    // 1. Inicializar contexto de Glue y Spark
    val spark: SparkSession = SparkSession.builder().getOrCreate()
    val glueContext: GlueContext = new GlueContext(spark.sparkContext) 

    // 2. Leer datos desde RDS MySQL (usando conexión JDBC)
    val jdbcUrl = s"jdbc:mysql://${sysArgs(0)}:3306/cultivo_bd"
    val connectionProperties = new java.util.Properties()
    connectionProperties.put("user", sysArgs(1))
    connectionProperties.put("password", sysArgs(2))
    connectionProperties.put("driver", "com.mysql.cj.jdbc.Driver")

    val dfRDS = spark.read.jdbc(jdbcUrl, "mediciones", connectionProperties)

    // 3. Escribir en S3 (formato Parquet)
    dfRDS.write
      .mode("overwrite")
      .parquet(s"s3://datos-cultivo-procesados-${sysArgs(3)}/raw/")

    // 4. Log de confirmación
    println("¡Extracción completada! Datos guardados en S3.")
  }
}