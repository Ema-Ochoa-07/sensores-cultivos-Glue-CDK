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
import java.util.Map;

public class EtlCultivoSensoresStack extends Stack {
    public EtlCultivoSensoresStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // =============================================
        // 1. BUCKET S3 PARA DATOS PROCESADOS
        // =============================================
        Bucket bucketSalida = Bucket.Builder.create(this, "BucketSalidaCultivo")
            .bucketName("datos-cultivo-procesados-" + this.getAccount())
            .versioned(false)
            .build();

        // =============================================
        // 2. RDS MYSQL (CONFIGURACIÓN COMPLETA)
        // =============================================
                DatabaseInstance rdsSensores = DatabaseInstance.Builder.create(this, "RdsSensoresCultivo")
            .engine(DatabaseInstanceEngine.mysql(MysqlInstanceEngineProps.builder()
                .version(MysqlEngineVersion.VER_8_0)
                .build()))
            .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.SMALL))
            .credentials(Credentials.fromGeneratedSecret("admin"))
            .vpc(Vpc.fromLookup(this, "SensorCultivoVpc", VpcLookupOptions.builder()
                .vpcId("vpc-32a04b48")
                .build()))
            .port(3306)
            .databaseName("cultivo_bd")
            .storageEncrypted(true)
            .build();

        // =================================================
        // 3. MOSTRAR ENDPOINT DE RDS (PARA CONEXIÓN MANUAL)
        // =================================================
        CfnOutput.Builder.create(this, "RdsEndpointOutput")
            .value(rdsSensores.getDbInstanceEndpointAddress())
            .description("Endpoint de RDS MySQL para conexiones externas")
            .build();

        CfnOutput.Builder.create(this, "RdsSecretNameOutput")
            .value(rdsSensores.getSecret().getSecretName())
            .description("Nombre del Secret en AWS Secrets Manager")
            .build();

        // ==============================
        // 4. PERMISOS ROL IAM PARA GLUE 
        // ==============================
        Role glueRole = Role.Builder.create(this, "GlueRoleCultivo")
            .assumedBy(new ServicePrincipal("glue.amazonaws.com"))
            .managedPolicies(List.of(
                ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSGlueServiceRole"),
                ManagedPolicy.fromAwsManagedPolicyName("AmazonRDSFullAccess"),
                ManagedPolicy.fromAwsManagedPolicyName("AmazonS3FullAccess")
            ))
            .build();
            // Permiso para leer el Secret de RDS
            rdsSensores.getSecret().grantRead(glueRole);


        // =====================================
        // 5. REFERENCIA DE JOBS
        // =====================================
        new GlueJobConstruct(this, "GlueJobExtraccion", glueRole, rdsSensores, bucketSalida);
    }
}