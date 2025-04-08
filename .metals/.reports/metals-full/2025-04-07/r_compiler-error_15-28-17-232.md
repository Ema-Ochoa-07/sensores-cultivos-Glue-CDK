file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/utils/GlueScriptsUploader.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 617
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/utils/GlueScriptsUploader.java
text:
```scala
// Clase para subir scripts de Glue al bucket S3 desde CDK
package com.myorg.utils;

import software.constructs.Construct;
import software.amazon.awscdk.services.s3.assets.Asset;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.Duration;

public class GlueScriptsUploader extends Construct {

    public GlueScriptsUploader(Construct scope, String id, Bucket bucketDestino) {
        super(scope, id);

        BucketDeployment deployment @@= BucketDeployment.Builder.create(this, "GlueScriptsDeployment")
            .destinationBucket(bucketDestino)
            .destinationKeyPrefix("scripts") // Sube todo al prefijo scripts/
            .sources(java.util.List.of(
                Source.asset("src/main/java/com/myorg/scripts") 
            ))
            .retainOnDelete(false)
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