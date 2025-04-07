// Script: Transformación de datos en S3 (Spark Scala)
// Descripción: Filtra datos crudos de S3, aplica limpieza y guarda resultados procesados.

import com.amazonaws.services.glue.util.GlueArgParser
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object GlueApp {
  def main(sysArgs: Array[String]): Unit = {
    // 1. Leer argumentos usando GlueArgParser
    val args = GlueArgParser.getResolvedOptions(sysArgs, Array("accountid"))

    // 2. Inicializar Spark
    val spark = SparkSession.builder().getOrCreate()

    // 3. Leer datos crudos de S3 (Parquet)
    val dfRaw = spark.read.parquet(s"s3://datos-cultivo-procesados-${args("accountid")}/raw/")

    // 4. Transformaciones:
    //    - Filtrar registros válidos (ej: temperatura > 0)
    //    - Calcular promedios por sensor
    val dfTransformed = dfRaw
      .filter(col("temperatura") > 0)
      .groupBy("sensor_id")
      .agg(
        avg("temperatura").as("avg_temp"),
        max("humedad").as("max_hum")
      )

    // 5. Escribir resultados en S3 (nueva ruta)
    dfTransformed.write
      .mode("overwrite")
      .parquet(s"s3://datos-cultivo-procesados-${args("accountid")}/processed/")

    // 6. Log de confirmación
    println("¡Transformación completada! Datos guardados en S3.")
  }
}
