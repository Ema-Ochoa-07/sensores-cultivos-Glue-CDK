import sys.process._
import com.amazonaws.services.glue.GlueContext
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

val spark: SparkSession = SparkSession.builder().getOrCreate()
val sc: SparkContext = spark.sparkContext
val glueContext: GlueContext = new GlueContext(sc)

// Leer argumentos del script
val args = sc.getConf
val host = args.get("spark.hadoop.mapreduce.glue.args.host")
val user = args.get("spark.hadoop.mapreduce.glue.args.user")
val password = args.get("spark.hadoop.mapreduce.glue.args.password")
val outPath = args.get("spark.hadoop.mapreduce.glue.args.out_path") 

println(s"Conectando a la base de datos en $host con usuario $user")
println(s"Guardando resultados en: $outPath")

val jdbcUrl = s"jdbc:mysql://$host:3306/cultivo_bd"
val connectionProperties = new java.util.Properties()
connectionProperties.setProperty("user", user)
connectionProperties.setProperty("password", password)
connectionProperties.setProperty("driver", "com.mysql.cj.jdbc.Driver")

// Leer los datos de la tabla 'datos_sensores'
val lecturasDF = spark.read.jdbc(jdbcUrl, "datos_sensores", connectionProperties)

lecturasDF.show()

// Escribir los datos en S3 en formato Parquet
lecturasDF.write
  .mode("overwrite")
  .parquet(outPath)
