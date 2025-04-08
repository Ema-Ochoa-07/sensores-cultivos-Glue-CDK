error id: `<none>`.
file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/scripts/extract-cultivo-rds-to-s3.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1647
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/scripts/extract-cultivo-rds-to-s3.scala
text:
```scala
import com.amazonaws.services.glue.GlueContext
import com.amazonaws.services.glue.util.GlueArgParser
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions._

object GlueApp {
  def main(sysArgs: Array[String]): Unit = {
    val args = GlueArgParser.getResolvedOptions(sysArgs, Array("host", "user", "password", "accountid"))

    val spark: SparkSession = SparkSession.builder().getOrCreate()
    val glueContext: GlueContext = new GlueContext(spark.sparkContext)

    // Configurar la conexión JDBC a la base de datos MySQL
    val jdbcUrl = s"jdbc:mysql://${args("host")}:3306/cultivo_db"
    val connectionProperties = new java.util.Properties()
    connectionProperties.put("user", args("user"))
    connectionProperties.put("password", args("password"))
    connectionProperties.put("driver", "com.mysql.cj.jdbc.Driver")

    println("🔌 Conectando a la base de datos MySQL...")

    // Leer la tabla 'cultivo' desde la base de datos
    val dfRDS = spark.read.jdbc(jdbcUrl, "cultivo", connectionProperties)
    val rowCount = dfRDS.count()

    println("Registros extraídos de la tabla 'cultivo_db': $rowCount")

    if (rowCount > 0) {
      val outputPath = s"s3://datos-cultivo-procesados-${args("accountid")}/raw/"

      println(s"📤 Guardando datos en: $outputPath")

      dfRDS.repartition(1)
        .write
        .mode(SaveMode.Overwrite)
        .option("compression", "snappy")
        .option("maxRecordsPerFile", "1000000")
        .parquet(outputPath)

      println("✅ ¡Datos guardados exitosamente en S3!")
    } else {
      println("La tabla '@@cultivo' no tiene registros para exportar.")
    }
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.