file:///C:/Users/ORTEL/Documents/Enmanuel/AWS-FundacionSEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/jobs/JobGlueConectRDS.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 1192
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS-FundacionSEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/jobs/JobGlueConectRDS.java
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
                .scriptLocation("s3://aws-glue-scripts-" + accountId + "/scripts/extract-cultivo-rds-to-s3.scala")
                .build())
            .defaultArguments(Map.of(
                "--RDS_ENDPOINT", rdsSensores.getDbInstanceEndpointAddress(),
                "--RDS_DB_NAME", "cultivo_bd",
                "--SECRET_NAME", rdsSensores.getSecret().getSecretName(),
                "--S3_OUTPUT_PATH", "s3://" + bucketSalida.getBucke@@tName() + "/raw-data/"
            ))
            .glueVersion("4.0")
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