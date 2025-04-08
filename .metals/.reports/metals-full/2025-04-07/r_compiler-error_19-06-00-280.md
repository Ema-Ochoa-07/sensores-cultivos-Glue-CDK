file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/jobs/JobGlueConectRDS.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 757
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/jobs/JobGlueConectRDS.java
text:
```scala
package com.myorg.jobs;

import software.constructs.Construct;
import software.amazon.awscdk.services.glue.*;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.s3.*;
import software.amazon.awscdk.services.iam.*;
import java.util.Map;

public class JobGlueConectRDS extends Construct {

    public JobGlueConectRDS(Construct scope, String id, IRole glueRole, DatabaseInstance rdsSensores, Bucket bucketSalida, String accountId) {
        super(scope, id);

        CfnJob.Builder.create(this, "JobGlueConectRDS")
            .name("extraccion-rds-cultivo")
            .role(glueRole.getRoleArn())
            .command(CfnJob.JobCommandProperty.builder()
                .name("glueetl")
                @@.scriptLocation("s3://datos-cultivo-procesados/scripts/extract-cultivo-rds-to-s3.scala")
                .build())
            .defaultArguments(Map.of(
                "--RDS_ENDPOINT", rdsSensores.getDbInstanceEndpointAddress(),
                "--RDS_DB_NAME", "cultivo_db",
                "--SECRET_NAME", rdsSensores.getSecret().getSecretName(),
                "--S3_OUTPUT_PATH", "s3://" + bucketSalida.getBucketName() + "/raw-data/"
            ))
            .glueVersion("4.0")
            .build();
    }
}

package com.myorg.jobs;
import software.amazon.awscdk.services.glue.CfnJob;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class JobGlueConectRDS extends Construct {

    public JobGlueConectRDS(final Construct scope, final String id, IRole glueJobRole) {
        super(scope, id);

        // 1. Obtener el secret generado por RDS
        software.amazon.awscdk.services.secretsmanager.ISecret rdsSecret =
    Secret.fromSecretNameV2(this, "CultivoRDSSecret", "EtlCultivoSensoresStackRdsS-LxRUE0C2O2gm");

        // 2. Argumentos para el script de Glue
        Map<String, Object> defaultArgs = new HashMap<>();
        defaultArgs.put("--class", "GlueApp");
        defaultArgs.put("--host", rdsSecret.secretValueFromJson("host").toString());
        defaultArgs.put("--user", rdsSecret.secretValueFromJson("username").toString());
        defaultArgs.put("--password", rdsSecret.secretValueFromJson("password").toString());
        defaultArgs.put("--extra-jars", "s3://datos-cultivo-procesados/drivers/mysql-connector-j-9.2.0.jar");

        // Ruta donde se guardarán los datos extraídos
        defaultArgs.put("--output_path", "s3://datos-cultivo-procesados/myData/");

        // 3. Crear el Glue Job
        CfnJob glueJob = CfnJob.Builder.create(this, "GlueJobExtraccionRDS")
            .name("extraccion-rds-cultivo")
            .role(glueJobRole.getRoleArn())
            .command(CfnJob.JobCommandProperty.builder()
                .name("glueetl")
                .scriptLocation("s3://ruta-a-tu-script/glue-script.scala") // <--- cambia esto a tu ruta real
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