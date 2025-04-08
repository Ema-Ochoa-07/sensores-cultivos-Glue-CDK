file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/EtlCultivoSensoresStack.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 7116
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/EtlCultivoSensoresStack.java
text:
```scala
package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.s3.LifecycleRule;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.MySqlInstanceEngineProps;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.targets.SfnStateMachine;
import software.amazon.awscdk.services.glue.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.stepfunctions.*;
import software.amazon.awscdk.services.stepfunctions.tasks.*;
import com.myorg.stepfunctions.RiegoStateMachine;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.events.Schedule;
import java.util.Map;
import software.amazon.awscdk.services.athena.CfnNamedQuery;
import software.amazon.awscdk.services.secretsmanager.*;
import com.myorg.utils.GlueScriptsUploader;
import java.util.*;
import com.myorg.jobs.JobGlueConectRDS;
import com.myorg.jobs.JobGlueViewS3;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.rds.MysqlEngineVersion;

public class EtlCultivoSensoresStack extends Stack {

    public EtlCultivoSensoresStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        String accountId = this.getAccount();
        String region = this.getRegion();


        // CONSTANTES PARA NOMBRES DE BUCKETS (NOMBRES FIJOS)
        final String PROCESSED_DATA_BUCKET = "datos-cultivo-procesados";
        final String GLUE_SCRIPTS_BUCKET = "mis-glue-scripts";

        // Definir la VPC que se va a utlizar en las diferemtes funciones
        IVpc vpc = Vpc.fromLookup(this, "SensorCultivoVpc", VpcLookupOptions.builder()
            .vpcId("vpc-32a04b48") 
            .build());

        // 4. ROL IAM PARA GLUE 
        Role glueRole = Role.Builder.create(this, "GlueRoleCultivo")
            .assumedBy(new ServicePrincipal("glue.amazonaws.com"))
            .managedPolicies(List.of(
                ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSGlueServiceRole"),
                ManagedPolicy.fromAwsManagedPolicyName("AmazonRDSFullAccess"),
                ManagedPolicy.fromAwsManagedPolicyName("AmazonS3FullAccess")
            ))
            .build();


        // 1. BUCKET S3 PARA DATOS PROCESADOS
        Bucket bucketSalida = Bucket.Builder.create(this, "BucketSalidaCultivo")
            .bucketName(PROCESSED_DATA_BUCKET)
            .versioned(false)
            .autoDeleteObjects(true)
            .removalPolicy(RemovalPolicy.DESTROY)
            .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
            .encryption(BucketEncryption.S3_MANAGED)
            .enforceSsl(true)
            .lifecycleRules(List.of(
                LifecycleRule.builder()
                    .expiration(Duration.days(365))
                    .build()))
            .build();

        // 6. ROL PARA DESPLIEGUE DE SCRIPTS
        Role deploymentRole = Role.Builder.create(this, "DeploymentRole")
            .assumedBy(new CompositePrincipal(
                new ServicePrincipal("cloudformation.amazonaws.com"),
                new ServicePrincipal("lambda.amazonaws.com") 
            ))
            .managedPolicies(List.of(
                ManagedPolicy.fromAwsManagedPolicyName("AmazonS3FullAccess")
            ))
            .build();


        //  BUCKET PARA SCRIPTS DE GLUE
        Bucket scriptsBucket = Bucket.Builder.create(this, "GlueScriptsBucket")
            .bucketName(GLUE_SCRIPTS_BUCKET)
            .versioned(false)
            .autoDeleteObjects(true)
            .removalPolicy(RemovalPolicy.DESTROY)
            .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
            .encryption(BucketEncryption.S3_MANAGED)
            .enforceSsl(true)
            .build();

        // Conceder permisos explícitos al rol de Glue
        scriptsBucket.grantReadWrite(glueRole);
        bucketSalida.grantReadWrite(glueRole);

        //  RDS MYSQL (CONFIGURACIÓN COMPLETA)          
        DatabaseInstance rdsSensores = DatabaseInstance.Builder.create(this, "RdsSensoresCultivo")
            .engine(DatabaseInstanceEngine.mysql(MySqlInstanceEngineProps.builder()
                .version(MysqlEngineVersion.VER_8_0)
                .build()))
            .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.SMALL))
            .credentials(Credentials.fromGeneratedSecret("admin"))
            .vpc(vpc)
            .vpcSubnets(SubnetSelection.builder()
                .subnetType(SubnetType.PUBLIC)
                .build())
            .port(3306)
            .databaseName("cultivo_db")
            .storageEncrypted(true)
            .build();

        // Grant acceso al secret generado automáticamente
        rdsSensores.getSecret().grantRead(glueRole);

        // =============================================
        //  PERMITIR ACCESO AL PUERTO 3306 (MYSQL)
        // =============================================
        rdsSensores.getConnections().allowFromAnyIpv4(Port.tcp(3306), 
            "Allow inbound MySQL access from any IP (testing only)");

        //  GLUE DATABASE PARA METADATOS
        CfnDatabase glueDatabase = CfnDatabase.Builder.create(this, "CultivoGlueDB")
            .catalogId(accountId)
            .databaseInput(CfnDatabase.DatabaseInputProperty.builder()
                .name("cultivo_db")
                .build())
            .build();


        //  SUBIR LOS SCRIPTS DE GLUE - PREVIO A LOS JOBS
        GlueScriptsUploader uploader = new GlueScriptsUploader(this, "UploadGlueScripts", bucketSalida);


        // 8. JOBS DE GLUE (DESPUÉS DE SUBIR LOS SCRIPTS)
        // Ruta al script en el bucket de salida
String scriptLocation = "s3://" + bucketSalida.getBucketName() + "/scripts/extraccion_rds_cultivo.scala";

        // Job de extracción con nueva firma (ya no usa rds ni accountId)
        JobGlueConectRDS jobExtraccion = new JobGlueConectRDS(this, "GlueJobExtraccion", glueRole, bucketSalida, scriptLocation);
        jobExtraccion.getNode().addDependency(uploader);
        jobExtraccion.getNode().addDependency(uploader);

        JobGlueViewS3 jobTransformacion = new JobGlueViewS3(this, "GlueJobTransformacion", glueRole, bucketSalida, accountId);
        jobTransformacion.getNode().addDependency(uploader);

   @@     // ==============================
        // 9. AGREGAR LA LAMBDA DE RIEGO
        // ==============================
        Map<String, String> lambdaEnvVars = new HashMap<>();
        lambdaEnvVars.put("RDS_ENDPOINT", rdsSensores.getDbInstanceEndpointAddress());
        lambdaEnvVars.put("DB_USER", "admin");
        lambdaEnvVars.put("DB_PASSWORD", rdsSensores.getSecret().getSecretValue().unsafeUnwrap());

        SecurityGroup lambdaSG = SecurityGroup.Builder.create(this, "LambdaSG")
            .vpc(vpc)
            .allowAllOutbound(true)
            .build();

        rdsSensores.getConnections().allowFrom(lambdaSG, Port.tcp(3306), "Permitir acceso Lambda a RDS");

        // =============================================
        // Conexión Glue a RDS para usar desde Workbench (AJUSTADA A SUBNET PÚBLICA)
        // =============================================
        CfnConnection glueConnection = CfnConnection.Builder.create(this, "RdsGlueConnection")
            .catalogId(accountId)
            .connectionInput(CfnConnection.ConnectionInputProperty.builder()
                .connectionType("JDBC")
                .connectionProperties(Map.of(
                    "JDBC_CONNECTION_URL", "jdbc:mysql://" + rdsSensores.getDbInstanceEndpointAddress() + ":3306/cultivo_db",
                    "USERNAME", "admin",
                    "PASSWORD", rdsSensores.getSecret().getSecretValue().unsafeUnwrap()
                ))
                .physicalConnectionRequirements(CfnConnection.PhysicalConnectionRequirementsProperty.builder()
                    .subnetId(vpc.getPublicSubnets().get(0).getSubnetId())
                    .securityGroupIdList(List.of(lambdaSG.getSecurityGroupId()))
                    .availabilityZone(vpc.getAvailabilityZones().get(0))
                    .build())
                .name("cultivo-rds-connection")
                .description("Conexión JDBC a RDS para datos de cultivo")
                .build())
            .build();

        Function riegoCultivoLambda = Function.Builder.create(this, "RiegoCultivoLambda")
            .runtime(Runtime.JAVA_11)
            .handler("com.myorg.lambda.RiegoCultivo::handleRequest")
            .code(Code.fromAsset("target/etl-cultivo-sensores-0.1.jar"))
            .vpc(vpc)
            .vpcSubnets(SubnetSelection.builder()
                .subnetType(SubnetType.PUBLIC)
                .build())
            .allowPublicSubnet(true)
            .securityGroups(Collections.singletonList(lambdaSG))
            .environment(lambdaEnvVars)
            .timeout(Duration.seconds(30))
            .memorySize(512)
            .build();

        riegoCultivoLambda.getRole().addManagedPolicy(
            ManagedPolicy.fromAwsManagedPolicyName("AmazonRDSFullAccess"));
        riegoCultivoLambda.getRole().addManagedPolicy(
            ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite"));

        // ========================================
        // 10. STEPFUNCTION - ASOCIAR STATE MACHINE
        // ========================================
        RiegoStateMachine riegoFlow = new RiegoStateMachine(this, riegoCultivoLambda);

        Rule.Builder.create(this, "TriggerProgramado")
            .schedule(Schedule.rate(Duration.hours(1)))
            .targets(List.of(new SfnStateMachine(riegoFlow.getStateMachine())))
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