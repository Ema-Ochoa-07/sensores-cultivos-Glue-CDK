// Script: Transformación de datos en S3 (Spark Scala)
// Descripción: Filtra datos crudos de S3, aplica limpieza y guarda resultados procesados.

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object GlueApp {
  def main(sysArgs: Array[String]): Unit = {
    // 1. Inicializar Spark
    val spark = SparkSession.builder().getOrCreate()

    // 2. Leer datos crudos de S3 (Parquet)
    val dfRaw = spark.read.parquet(s"s3://datos-cultivo-procesados-${sysArgs(0)}/raw/")

    // 3. Transformaciones:
    //    - Filtrar registros válidos (ej: temperatura > 0)
    //    - Calcular promedios por sensor
    val dfTransformed = dfRaw
      .filter(col("temperatura") > 0)
      .groupBy("sensor_id")
      .agg(
        avg("temperatura").as("avg_temp"),
        max("humedad").as("max_hum")
      )

    // 4. Escribir resultados en S3 (nueva ruta)
    dfTransformed.write
      .mode("overwrite")
      .parquet(s"s3://datos-cultivo-procesados-${sysArgs(0)}/processed/")

    // 5. Log de confirmación
    println("¡Transformación completada! Datos guardados en S3.")
  }
}