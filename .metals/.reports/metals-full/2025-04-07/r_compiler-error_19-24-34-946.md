file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/jobs/JobGlueConectRDS.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 457
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/jobs/JobGlueConectRDS.java
text:
```scala
package com.myorg.jobs;

import software.amazon.awscdk.services.glue.CfnJob;
import software.amazon.awscdk.services.iam.IRole;
import software.constructs.Construct;
import software.amazon.awscdk.services.s3.Bucket;

import java.util.HashMap;
import java.util.Map;

public class JobGlueConectRDS extends Construct {

    public JobGlueConectRDS(final Construct scope, final String id, IRole glueJobRole, Bucket outputBucket, String scriptLocation@@) {
        super(scope, id);

        // Argumentos que el script de Glue necesita
        Map<String, Object> defaultArgs = new HashMap<>();
        defaultArgs.put("--class", "GlueApp"); // opcional si es Spark Scala
        defaultArgs.put("--extra-jars", "s3://datos-cultivo-procesados/drivers/mysql-connector-j-9.2.0.jar");

        // Agregamos el parámetro de salida
        defaultArgs.put("--out_path", "s3://" + outputBucket.getBucketName() + "/myData/");

        // Crear el Glue Job
        CfnJob glueJob = CfnJob.Builder.create(this, "GlueJobExtraccionRDS")
            .name("extraccion-rds-cultivo")
            .role(glueJobRole.getRoleArn())
            .command(CfnJob.JobCommandProperty.builder()
                .name("glueetl")
                .scriptLocation("s3://datos-cultivo-procesados/scripts/extraccion_rds_cultivo.scala")
                .pythonVersion("3") 
                .build())
            .defaultArguments(defaultArgs)
            .glueVersion("3.0")
            .numberOfWorkers(2)
            .workerType("G.1X")
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