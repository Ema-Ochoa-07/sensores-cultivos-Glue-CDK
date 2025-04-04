file:///C:/Users/ORTEL/Documents/Enmanuel/AWS-FundacionSEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/jobs/JobGlueViewS3.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 1484
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS-FundacionSEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/jobs/JobGlueViewS3.java
text:
```scala
package com.myorg.jobs;

import software.constructs.Construct;
import software.amazon.awscdk.services.glue.*;
import software.amazon.awscdk.services.s3.*;
import software.amazon.awscdk.services.iam.*;
import java.util.Map;

/**
 * Job de Glue que transforma datos en S3 y configura un Crawler para el Data Catalog.
 */
public class JobGlueViewS3 extends Construct {

    /**
     * Constructor del Job.
     * @param scope - Contexto del constructo CDK.
     * @param id    - ID único del constructo.
     * @param glueRole - Rol IAM con permisos para Glue y S3.
     * @param bucketSalida - Bucket S3 donde están los datos crudos y procesados.
     */
    public JobGlueViewS3(Construct scope, String id, IRole glueRole, Bucket bucketSalida, String accountId) {
        super(scope, id);

        // Configuración del Job de transformación
        CfnJob job = CfnJob.Builder.create(this, "JobGlueViewS3")
            .name("transformacion-datos-cultivo")  // Nombre del Job en AWS Glue
            .role(glueRole.getRoleArn())  // Rol IAM
            .command(CfnJob.JobCommandProperty.builder()
                .name("glueetl")  // Tipo de comando
                .scriptLocation("s3://aws-glue-scripts-" + accountId + "/scripts/transform-cultivo.scala")
                .language("scala")
                .build())
            .defaultArguments(Map.of(  // Parámetros del script
                "--S3_INPUT_PATH", "s3://" + bucketSalida.getBucketN@@ame() + "/raw-data/",  // Ruta de entrada (datos crudos)
                "--S3_OUTPUT_PATH", "s3://" + bucketSalida.getBucketName() + "/processed-data/"  // Ruta de salida (datos procesados)
            ))
            .glueVersion("4.0")  // Versión de Glue
            .build();

        // Configuración del Crawler para el Data Catalog
        CfnCrawler.Builder.create(this, "CultivoCrawler")
            .name("crawler-datos-cultivo")  // Nombre del Crawler
            .role(glueRole.getRoleArn())  // Rol IAM con permisos para Glue y S3
            .databaseName("cultivo_db")  // Base de datos en el Data Catalog
            .targets(CfnCrawler.TargetsProperty.builder()
                .s3Targets(List.of(
                    CfnCrawler.S3TargetProperty.builder()
                        .path("s3://" + bucketSalida.getBucketName() + "/processed-data/")  // Ruta que escaneará
                        .build()
                ))
                .build())
            .build();
    }
}
```



#### Error stacktrace:

```
scala.collection.Iterator$$anon$19.next(Iterator.scala:973)
	scala.collection.Iterator$$anon$19.next(Iterator.scala:971)
	scala.collection.mutable.MutationTracker$CheckedIterator.next(MutationTracker.scala:76)
	scala.collection.IterableOps.head(Iterable.scala:222)
	scala.collection.IterableOps.head$(Iterable.scala:222)
	scala.collection.AbstractIterable.head(Iterable.scala:935)
	dotty.tools.dotc.interactive.InteractiveDriver.run(InteractiveDriver.scala:164)
	dotty.tools.pc.CachingDriver.run(CachingDriver.scala:45)
	dotty.tools.pc.HoverProvider$.hover(HoverProvider.scala:40)
	dotty.tools.pc.ScalaPresentationCompiler.hover$$anonfun$1(ScalaPresentationCompiler.scala:389)
```
#### Short summary: 

java.util.NoSuchElementException: next on empty iterator