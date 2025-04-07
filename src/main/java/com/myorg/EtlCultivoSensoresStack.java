package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.targets.SfnStateMachine;
import software.amazon.awscdk.services.glue.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.services.ec2.Connections;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
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
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.secretsmanager.*;
import java.util.*;

import com.myorg.jobs.JobGlueConectRDS;
import com.myorg.jobs.JobGlueViewS3;

import software.amazon.awscdk.RemovalPolicy;


// ... tus imports se mantienen exactamente igual ...

public class EtlCultivoSensoresStack extends Stack {

    public EtlCultivoSensoresStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        String accountId = this.getAccount();

        // Definir la VPC que se va a utlizar en las diferemtes funciones
        IVpc vpc = Vpc.fromLookup(this, "SensorCultivoVpc", VpcLookupOptions.builder()
            .vpcId("vpc-32a04b48") 
            .build());

        // =============================================
        // 1. BUCKET S3 PARA DATOS PROCESADOS
        // =============================================
        Bucket bucketSalida = Bucket.Builder.create(this, "BucketSalidaCultivo")
            .bucketName("datos-cultivo-procesados-" + accountId)
            .versioned(false)
            .autoDeleteObjects(true)
            .removalPolicy(RemovalPolicy.DESTROY)
            .build();

        // =============================================
        // 2. RDS MYSQL (CONFIGURACIÓN COMPLETA)
        // =============================================            
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
            .databaseName("cultivo_bd")
            .storageEncrypted(true)
            .build();

        // =============================================
        // 2.1 PERMITIR ACCESO AL PUERTO 3306 (MYSQL)
        // =============================================
        rdsSensores.getConnections().allowFromAnyIpv4(Port.tcp(3306), 
            "Allow inbound MySQL access from any IP (testing only)");


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

        // Grant acceso al secret generado automáticamente
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
            .assumedBy(new CompositePrincipal(
                new ServicePrincipal("cloudformation.amazonaws.com"),
                new ServicePrincipal("lambda.amazonaws.com") 
            ))
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

        // ==============================
        // 10. AGREGAR LA LAMBDA DE RIEGO
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
        // 11. STEPFUNCTION - ASOCIAR STATE MACHINE
        // ========================================
        RiegoStateMachine riegoFlow = new RiegoStateMachine(this, riegoCultivoLambda);

        Rule.Builder.create(this, "TriggerProgramado")
            .schedule(Schedule.rate(Duration.hours(1)))
            .targets(List.of(new SfnStateMachine(riegoFlow.getStateMachine())))
            .build();

        // ==============================
        // 12. LAMBDA PARA OTORGAR PRIVILEGIOS AL USUARIO ADMIN
        // ==============================
        Map<String, String> grantEnvVars = new HashMap<>();
        grantEnvVars.put("JDBC_URL", "jdbc:mysql://" + rdsSensores.getDbInstanceEndpointAddress() + ":3306/cultivo_bd?useSSL=false&allowPublicKeyRetrieval=true");
        grantEnvVars.put("DB_USER", "admin");
        grantEnvVars.put("DB_PASSWORD", rdsSensores.getSecret().getSecretValue().unsafeUnwrap());

        Function grantPrivilegesLambda = Function.Builder.create(this, "GrantPrivilegesLambda")
            .runtime(Runtime.JAVA_11)
            .handler("com.myorg.lambda.GrantAdminPrivilegesHandler::handleRequest")
            .code(Code.fromAsset("target/etl-cultivo-sensores-0.1.jar"))
            .vpc(vpc)
            .vpcSubnets(SubnetSelection.builder()
                .subnetType(SubnetType.PUBLIC)
                .build())
            .allowPublicSubnet(true)
            .securityGroups(Collections.singletonList(lambdaSG)) // Puedes reutilizar lambdaSG
            .environment(grantEnvVars)
            .timeout(Duration.seconds(30))
            .memorySize(512)
            .build();

        // Permisos necesarios
        grantPrivilegesLambda.getRole().addManagedPolicy(
            ManagedPolicy.fromAwsManagedPolicyName("AmazonRDSFullAccess"));
        grantPrivilegesLambda.getRole().addManagedPolicy(
            ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite"));


        // =========================================
        // 13 CONFIGURACIÓN DE ATHENA PARA CONSULTAS
        // =========================================
        Bucket athenaResultsBucket = Bucket.Builder.create(this, "AthenaResultsBucket")
        .bucketName("athena-query-results-" + accountId)
        .removalPolicy(RemovalPolicy.DESTROY)
        .autoDeleteObjects(true)
        .build();

        CfnNamedQuery namedQuery = CfnNamedQuery.Builder.create(this, "AthenaNamedQuery")
        .database("cultivo_db") // Debe coincidir con tu Glue DB
        .queryString("SELECT * FROM datos_sensores LIMIT 10;") // Reemplaza con tu tabla Glue real
        .name("ConsultaPruebaSensores")
        .description("Consulta de prueba sobre tabla sensores")
        .workGroup("primary")
        .build();

        // Dar permisos a la Lambda para ejecutar consultas Athena
        ((Role) riegoCultivoLambda.getRole()).addToPolicy(PolicyStatement.Builder.create()
        .actions(List.of(
            "athena:StartQueryExecution",
            "athena:GetQueryResults",
            "athena:GetQueryExecution",
            "glue:GetTable",
            "glue:GetDatabase",
            "glue:GetPartition",
            "s3:GetObject",
            "s3:PutObject"
        ))
        .resources(List.of("*"))
        .build());

        // Permiso al bucket de resultados de Athena
        athenaResultsBucket.grantReadWrite(riegoCultivoLambda);

    }
}
