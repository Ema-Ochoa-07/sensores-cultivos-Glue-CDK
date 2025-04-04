file:///C:/Users/ORTEL/Documents/Enmanuel/AWS-FundacionSEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/EtlCultivoSensoresStack.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 1794
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS-FundacionSEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/EtlCultivoSensoresStack.java
text:
```scala
package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.glue.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.RemovalPolicy;  // ✅ Import agregado
import java.util.List;

// Importar los Jobs de Glue
import com.myorg.jobs.JobGlueConectRDS;  // ✅ Import agregado
import com.myorg.jobs.JobGlueViewS3;  // ✅ Import agregado

public class EtlCultivoSensoresStack extends Stack {

    public EtlCultivoSensoresStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        String accountId = this.getAccount();

        // =============================================
        // 1. BUCKET S3 PARA DATOS PROCESADOS
        // =============================================
        Bucket bucketSalida = Bucket.Builder.create(this, "BucketSalidaCultivo")
            .bucketName("datos-cultivo-procesados-" + accountId)
            .versioned(false)
            .removalPolicy(RemovalPolicy.DESTROY)  // ✅ Ahora es reconocido
            .build();

        // =============================================
        // 2. RDS MYSQL (CONFIGURACIÓN COMPLETA)
        // =============================================
        DatabaseInstance rdsSensores = DatabaseInstance.Builder.create(this, "RdsSensoresCultivo")
           @@ .engine(DatabaseInstanceEngine.mysql(MysqlEngineVersion.VER_8_0))  // ✅ Corrección aquí
            .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.SMALL))
            .credentials(Credentials.fromGeneratedSecret("admin"))
            .vpc(Vpc.fromLookup(this, "SensorCultivoVpc", VpcLookupOptions.builder()
                .isDefault(true)
                .build()))
            .port(3306)
            .databaseName("cultivo_bd")
            .storageEncrypted(true)
            .build();

        // ==============================
        // 3. JOBS DE GLUE
        // ==============================
        new JobGlueConectRDS(this, "GlueJobExtraccion", glueRole, rdsSensores, bucketSalida, accountId);
        new JobGlueViewS3(this, "GlueJobTransformacion", glueRole, bucketSalida, accountId);
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