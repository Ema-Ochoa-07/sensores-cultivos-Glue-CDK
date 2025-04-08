package com.myorg.jobs;

import software.amazon.awscdk.services.glue.CfnJob;
import software.amazon.awscdk.services.iam.IRole;
import software.constructs.Construct;
import software.amazon.awscdk.services.s3.Bucket;

import java.util.HashMap;
import java.util.Map;

public class JobGlueConectRDS extends Construct {

    public JobGlueConectRDS(final Construct scope, final String id, IRole glueJobRole, Bucket outputBucket, String scriptLocation) {
        super(scope, id);

        // Argumentos que el script de Glue necesita
        Map<String, Object> defaultArgs = new HashMap<>();
        defaultArgs.put("--class", "GlueApp"); // opcional si es Spark Scala
        defaultArgs.put("--extra-jars", "s3://datos-cultivo-procesados/drivers/mysql-connector-j-9.2.0.jar");

        // Agregamos el parámetro de salida
        defaultArgs.put("--out_path", "s3://" + outputBucket.getBucketName() + "/myData/");

        // Crear el Glue Job
        CfnJob glueJob = CfnJob.Builder.create(this, "GlueJobExtraccionRDS")
            .name("extraccion-rds-cultivo")
            .role(glueJobRole.getRoleArn())
            .command(CfnJob.JobCommandProperty.builder()
                .name("glueetl")
                .scriptLocation("s3://datos-cultivo-procesados/scripts/extraccion_rds_cultivo.scala")
                .pythonVersion("3") 
                .build())
            .defaultArguments(defaultArgs)
            .glueVersion("3.0")
            .numberOfWorkers(2)
            .workerType("G.1X")
            .build();
    }
}
