package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class EtlCultivoSensoresApp {
    public static void main(final String[] args) {
        App app = new App();

        // Define env con cuenta y región
        Environment env = Environment.builder()
                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                .region(System.getenv("CDK_DEFAULT_REGION"))
                .build();

        // Pasa env al stack
        new EtlCultivoSensoresStack(app, "EtlCultivoSensoresStack", StackProps.builder()
                .env(env)
                .build());

        app.synth();
    }
}