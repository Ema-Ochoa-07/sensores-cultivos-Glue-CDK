package com.myorg.jobs;

import software.constructs.Construct;
import software.amazon.awscdk.services.glue.*;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.s3.*;
import software.amazon.awscdk.services.iam.*;

import java.util.Map;

public class JobGlueConectRDS extends Construct {

    public JobGlueConectRDS(Construct scope, String id, IRole glueRole, DatabaseInstance rdsSensores, Bucket bucketSalida, String accountId) {
        super(scope, id);

        CfnJob.Builder.create(this, "JobGlueConectRDS")
            .name("extraccion-rds-cultivo")
            .role(glueRole.getRoleArn())
            .command(CfnJob.JobCommandProperty.builder()
                .name("glueetl")
                ..scriptLocation("s3://aws-glue-scripts-" + accountId + "/scripts/extract-cultivo-rds-to-s3.scala")
                .language("scala")
                .build())
            .defaultArguments(Map.of(
                "--RDS_ENDPOINT", rdsSensores.getDbInstanceEndpointAddress(),
                "--RDS_DB_NAME", rdsSensores.getDbName(),
                "--SECRET_NAME", rdsSensores.getSecret().getSecretName(),
                "--S3_OUTPUT_PATH", "s3://" + bucketSalida.getBucketName() + "/raw-data/"
            ))
            .glueVersion("4.0")
            .build();
    }
}
