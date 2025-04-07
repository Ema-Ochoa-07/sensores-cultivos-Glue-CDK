package com.myorg.jobs;

import software.constructs.Construct;
import software.amazon.awscdk.services.glue.*;
import software.amazon.awscdk.services.s3.*;
import software.amazon.awscdk.services.iam.*;
import java.util.Map;

public class JobGlueViewS3 extends Construct {

    public JobGlueViewS3(Construct scope, String id, IRole glueRole, Bucket bucketSalida, String accountId) {
        super(scope, id);

        CfnJob job = CfnJob.Builder.create(this, "JobGlueViewS3")
            .name("transformacion-datos-cultivo")
            .role(glueRole.getRoleArn())
            .command(CfnJob.JobCommandProperty.builder()
                .name("glueetl")
                .scriptLocation("s3://" + bucketSalida.getBucketName() + "/scripts/transform-cultivo.scala")
                .build())
            .defaultArguments(Map.of(
                "--S3_INPUT_PATH", "s3://" + bucketSalida.getBucketName() + "/raw-data/",
                "--S3_OUTPUT_PATH", "s3://" + bucketSalida.getBucketName() + "/processed-data/"
            ))
            .glueVersion("4.0")
            .build();
    }
}
