file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/EtlCultivoSensoresStack.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 2768
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/EtlCultivoSensoresStack.java
text:
```scala
package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.MysqlEngineVersion;
import software.amazon.awscdk.services.rds.StorageType;
import software.amazon.awscdk.services.rds.InstanceType;
import software.amazon.awscdk.services.rds.InstanceClass;
import software.amazon.awscdk.services.rds.InstanceSize;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.secretsmanager.Secret;

import com.myorg.lambda.SecretsCreator;
import com.myorg.jobs.JobGlueConectRDS;
import com.myorg.jobs.JobGlueViewS3;
import com.myorg.utils.GlueScriptsUploader;

public class EtlCultivoSensoresStack extends Stack {
    public EtlCultivoSensoresStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // VPC fija (ya existente)
        IVpc vpc = Vpc.fromLookup(this, "VPC", Vpc.LookupOptions.builder()
            .vpcId("vpc-32a04b48")
            .build());

        // Bucket existente
        IBucket bucketSalida = Bucket.fromBucketName(this, "BucketSalida", "datos-cultivo-procesados-164797387787-us-east-1");

        // Rol para Glue
        Role glueRole = Role.Builder.create(this, "GlueServiceRole")
            .assumedBy(new ServicePrincipal("glue.amazonaws.com"))
            .managedPolicies(java.util.List.of(
                ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSGlueServiceRole"),
                ManagedPolicy.fromAwsManagedPolicyName("AmazonS3FullAccess"),
                ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite"),
                ManagedPolicy.fromAwsManagedPolicyName("AmazonRDSFullAccess")
            ))
            .build();

        // Crear el Secret para credenciales RDS
        Secret secret = new SecretsCreator(this, "RdsCredentialsSecret", "admin", "clave1234").getSecret();

        // Crear la instancia de RDS
        DatabaseInstance rdsInstance = DatabaseInstance.Builder.create(this, "RDSSensoresCultivo")
            .engine(DatabaseInstanceEngine.mysql(MysqlEngineVersion.VER_8_0))
            .instanceType(InstanceType.of(Instan@@ceClass.BURSTABLE3, InstanceSize.MICRO))
            .vpc(vpc)
            .credentials(Credentials.fromSecret(secret))
            .allocatedStorage(20)
            .storageType(StorageType.GP2)
            .multiAz(false)
            .publiclyAccessible(false)
            .vpcSubnets(subnetSelection -> subnetSelection.subnetType(SubnetType.PRIVATE_WITH_EGRESS))
            .databaseName("cultivo_db")
            .build();

        // Subir scripts al bucket
        new GlueScriptsUploader(this, "ScriptsUploader", bucketSalida);

        // Crear Glue Job 1 - Extracción RDS
        new JobGlueConectRDS(this, "JobExtraccionRDS", glueRole, rdsInstance, bucketSalida, this.getAccount());

        // Crear Glue Job 2 - Transformación en S3
        new JobGlueViewS3(this, "JobTransformacion", glueRole, bucketSalida, this.getAccount());

        // TODO: Comentado por ahora: Step Functions y visualización con Athena
        // new StepFunctionWorkflow(this, "PasoTrabajoETL", ...);
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