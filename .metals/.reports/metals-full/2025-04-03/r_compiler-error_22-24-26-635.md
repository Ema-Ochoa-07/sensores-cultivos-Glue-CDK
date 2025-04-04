file:///C:/Users/ORTEL/Documents/Enmanuel/AWS-FundacionSEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/EtlCultivoSensoresStack.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 884
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
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;


import java.util.List;

import com.myorg.jobs.JobGlueConectRDS;
import com.myorg.jobs.JobGlueViewS3;

import software.amazon.awscdk.RemovalPolicy;


public cla@@ss EtlCultivoSensoresStack extends Stack {

    public EtlCultivoSensoresStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        String accountId = this.getAccount();

        // =============================================
        // 1. BUCKET S3 PARA DATOS PROCESADOS
        // =============================================
        Bucket bucketSalida = Bucket.Builder.create(this, "BucketSalidaCultivo")
            .bucketName("datos-cultivo-procesados-" + accountId)
            .versioned(false)
            .removalPolicy(RemovalPolicy.DESTROY)
            .build();

        // =============================================
        // 2. RDS MYSQL (CONFIGURACIÓN COMPLETA)
        // =============================================
        DatabaseInstance rdsSensores = DatabaseInstance.Builder.create(this, "RdsSensoresCultivo")
        .engine(DatabaseInstanceEngine.mysql(MySqlInstanceEngineProps.builder()
            .version(MysqlEngineVersion.VER_8_0)
            .build()))
        .instanceType(new InstanceType("t3.small")) // Alternativa corregida
        .credentials(Credentials.fromGeneratedSecret("admin"))
        .vpc(Vpc.fromLookup(this, "SensorCultivoVpc", VpcLookupOptions.builder()
            .isDefault(true)
            .build()))
        .port(3306)
        .databaseName("cultivo_bd")
        .storageEncrypted(true)
        .build(
    


        // ===================================================================
        // 3. GLUE DATABASE PARA METADATOS
        // ====================================================================
        CfnDatabase glueDatabase = CfnDatabase.Builder.create(this, "CultivoGlueDB")
            .catalogId(accountId)
            .databaseInput(CfnDatabase.DatabaseInputProperty.builder()
                .name("cultivo_db")
                .build())
            .build();

        // ==============================
        // 4. ROL IAM PARA GLUE 
        // ==============================
        Role glueRole = Role.Builder.create(this, "GlueRoleCultivo")
            .assumedBy(new ServicePrincipal("glue.amazonaws.com"))
            .managedPolicies(List.of(
                ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSGlueServiceRole"),
                ManagedPolicy.fromAwsManagedPolicyName("AmazonRDSFullAccess"),
                ManagedPolicy.fromAwsManagedPolicyName("AmazonS3FullAccess")
            ))
            .build();
        
        rdsSensores.getSecret().grantRead(glueRole);

        // =================================================
        // 5. OUTPUTS (ENDPOINT RDS Y SECRET)
        // =================================================
        CfnOutput.Builder.create(this, "RdsEndpointOutput")
            .value(rdsSensores.getDbInstanceEndpointAddress())
            .description("Endpoint de RDS MySQL para conexiones externas")
            .build();

        CfnOutput.Builder.create(this, "RdsSecretNameOutput")
            .value(rdsSensores.getSecret().getSecretName())
            .description("Nombre del Secret en AWS Secrets Manager")
            .build();

        // ==================================
        // 6. BUCKET PARA SCRIPTS DE GLUE (CREACIÓN AUTOMÁTICA)
        // ==================================
        Bucket scriptsBucket = Bucket.Builder.create(this, "GlueScriptsBucket")
            .bucketName("aws-glue-scripts-" + accountId)
            .removalPolicy(RemovalPolicy.DESTROY)
            .build();

        // ==================================
        // 7. ROL PARA DESPLIEGUE DE SCRIPTS
        // ==================================
        Role deploymentRole = Role.Builder.create(this, "DeploymentRole")
            .assumedBy(new ServicePrincipal("cloudformation.amazonaws.com"))
            .managedPolicies(List.of(
                ManagedPolicy.fromAwsManagedPolicyName("AmazonS3FullAccess")
            ))
            .build();

        // ==================================
        // 8. SUBIR SCRIPTS AL BUCKET (DESDE CARPETA LOCAL './scripts')
        // ==================================
        BucketDeployment.Builder.create(this, "DeployGlueScripts")
        .sources(List.of(Source.asset("src/main/java/com/myorg/scripts")))
        .destinationBucket(scriptsBucket)
        .destinationKeyPrefix("scripts/")
        .role(deploymentRole)
        .build();

        // =====================================
        // 9. JOBS DE GLUE (USANDO TUS CLASES EXISTENTES)
        // =====================================
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