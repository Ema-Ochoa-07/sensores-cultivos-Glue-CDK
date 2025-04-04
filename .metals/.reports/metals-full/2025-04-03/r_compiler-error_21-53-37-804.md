file:///C:/Users/ORTEL/Documents/Enmanuel/AWS-FundacionSEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/jobs/JobGlueViewS3.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 602
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS-FundacionSEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/jobs/JobGlueViewS3.java
text:
```scala
package com.myorg.jobs;

import software.constructs.Construct;
import software.amazon.awscdk.services.glue.*;
import software.amazon.awscdk.services.s3.*;
import software.amazon.awscdk.services.iam.*;
import java.util.Map;

public class JobGlueViewS3 extends Construct {

    public JobGlueViewS3(Construct scope, String id, IRole glueRole, Bucket bucketSalida, String accountId) {
        super(scope, id);

        CfnJob job = CfnJob.Builder.create(this, "JobGlueViewS3")
            .name("transformacion-datos-cultivo")
            .role(glueRole.getRoleArn())
            .command@@(CfnJob.JobCommandProperty.builder()
                .name("glueetl")
                .scriptLocation("s3://aws-glue-scripts-" + accountId + "/scripts/transform-cultivo.scala")
                .language("scala")
                .build())
            .defaultArguments(Map.of(
                "--S3_INPUT_PATH", "s3://" + bucketSalida.getBucketName() + "/raw-data/",
                "--S3_OUTPUT_PATH", "s3://" + bucketSalida.getBucketName() + "/processed-data/"
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