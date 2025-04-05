package com.myorg.stepfunctions;

import software.constructs.Construct;
import software.amazon.awscdk.services.stepfunctions.*;
import software.amazon.awscdk.services.stepfunctions.tasks.*;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.Duration;

public class RiegoStateMachine {
    private final StateMachine stateMachine;

    public RiegoStateMachine(Construct scope, Function riegoCultivoLambda) {
        // 1. Define estados (tasks, choices) - Eliminamos el Wait
        LambdaInvoke consultarHumedad = LambdaInvoke.Builder.create(scope, "ConsultarHumedad")
            .lambdaFunction(riegoCultivoLambda)
            .outputPath("$.Payload")
            .build();

        // 2. Lógica de decisión simplificada
        Choice decidirRiego = new Choice(scope, "¿DebeRegar?")
            .when(Condition.booleanEquals("$.debeRegar", true), 
                new Succeed(scope, "RiegoActivado"))
            .otherwise(
                new Succeed(scope, "EsperarSiguienteEjecucion") // Termina limpiamente
            );

        // 3. Definición del flujo (sin bucle)
        this.stateMachine = StateMachine.Builder.create(scope, "RiegoStateMachine")
            .definition(
                Chain.start(consultarHumedad)
                    .next(decidirRiego) // Fin del flujo
            )
            .timeout(Duration.minutes(5)) // Timeout por seguridad
            .build();
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }
}