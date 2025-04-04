package com.myorg.jobs;

import software.constructs.Construct;
import software.amazon.awscdk.services.glue.*;
import software.amazon.awscdk.services.s3.*;
import software.amazon.awscdk.services.iam.*;
import java.util.Map;

/**
 * Job de Glue que transforma datos en S3 y configura un Crawler para el Data Catalog.
 */
public class JobGlueViewS3 extends Construct {

    /**
     * Constructor del Job.
     * @param scope - Contexto del constructo CDK.
     * @param id    - ID único del constructo.
     * @param glueRole - Rol IAM con permisos para Glue y S3.
     * @param bucketSalida - Bucket S3 donde están los datos crudos y procesados.
     */
    public JobGlueViewS3(Construct scope, String id, IRole glueRole, Bucket bucketSalida, String accountId) {
        super(scope, id);

        // Configuración del Job de transformación
        CfnJob job = CfnJob.Builder.create(this, "JobGlueViewS3")
            .name("transformacion-datos-cultivo")  // Nombre del Job en AWS Glue
            .role(glueRole.getRoleArn())  // Rol IAM
            .command(CfnJob.JobCommandProperty.builder()
                .name("glueetl")  // Tipo de comando
                .scriptLocation("s3://aws-glue-scripts-" + accountId + "/scripts/transform-cultivo.scala")
                .language("scala")
                .build())
            .defaultArguments(Map.of(  // Parámetros del script
                "--S3_INPUT_PATH", "s3://" + bucketSalida.getBucketName() + "/raw-data/",  // Ruta de entrada (datos crudos)
                "--S3_OUTPUT_PATH", "s3://" + bucketSalida.getBucketName() + "/processed-data/"  // Ruta de salida (datos procesados)
            ))
            .glueVersion("4.0")  // Versión de Glue
            .build();

        // Configuración del Crawler para el Data Catalog
        CfnCrawler.Builder.create(this, "CultivoCrawler")
            .name("crawler-datos-cultivo")  // Nombre del Crawler
            .role(glueRole.getRoleArn())  // Rol IAM con permisos para Glue y S3
            .databaseName("cultivo_db")  // Base de datos en el Data Catalog
            .targets(CfnCrawler.TargetsProperty.builder()
                .s3Targets(List.of(
                    CfnCrawler.S3TargetProperty.builder()
                        .path("s3://" + bucketSalida.getBucketName() + "/processed-data/")  // Ruta que escaneará
                        .build()
                ))
                .build())
            .build();
    }
}