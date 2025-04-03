package com.myorg.jobs;

import software.constructs.Construct;
import software.amazon.awscdk.services.glue.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.s3.*;
import java.util.Map;

public class GlueJobConstruct extends Construct {
    public GlueJobConstruct(
        Construct scope, 
        String id, 
        IRole glueRole, 
        DatabaseInstance rdsInstance, 
        Bucket outputBucket
    ) {
        super(scope, id);

        CfnJob.Builder.create(this, "GlueJobExtraccionCultivo")
            .name("extraccion-datos-cultivo")
            .role(glueRole.getRoleArn())
            .command(CfnJob.JobCommandProperty.builder()
                .name("glueetl")
                .scriptLocation("s3://aws-glue-scripts-" + this.getAccount() + "/scripts/extract-cultivo.py")
                .build())
            .defaultArguments(Map.of(
                "--RDS_ENDPOINT", rdsInstance.getDbInstanceEndpointAddress(),
                "--RDS_PORT", String.valueOf(rdsInstance.getDbInstanceEndpointPort()),
                "--RDS_DB_NAME", rdsInstance.getDbName(),
                "--SECRET_NAME", rdsInstance.getSecret().getSecretName(),
                "--S3_OUTPUT_PATH", "s3://" + outputBucket.getBucketName() + "/raw-data/"
            ))
            .glueVersion("4.0")
            .build();
    }
}