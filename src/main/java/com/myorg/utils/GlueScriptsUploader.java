// Clase para subir scripts de Glue al bucket S3 desde CDK
package com.myorg.utils;

import software.constructs.Construct;
import software.amazon.awscdk.services.s3.assets.Asset;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.Duration;

public class GlueScriptsUploader extends Construct {

    public GlueScriptsUploader(Construct scope, String id, Bucket bucketDestino) {
        super(scope, id);

        BucketDeployment deployment = BucketDeployment.Builder.create(this, "GlueScriptsDeployment")
            .destinationBucket(bucketDestino)
            .destinationKeyPrefix("scripts") // Sube todo al prefijo scripts/
            .sources(java.util.List.of(
                Source.asset("src/main/java/com/myorg/scripts") 
            ))
            .retainOnDelete(false)
            .build();
    }
}
