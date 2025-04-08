file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/stepfunctions/RiegoStateMachine.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 662
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/stepfunctions/RiegoStateMachine.java
text:
```scala
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
            .lambdaFunction(riegoCultivoLam@@bda)
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
```



#### Error stacktrace:

```
scala.collection.Iterator$$anon$19.next(Iterator.scala:973)
	scala.collection.Iterator$$anon$19.next(Iterator.scala:971)
	scala.collection.mutable.MutationTracker$CheckedIterator.next(MutationTracker.scala:76)
	scala.collection.IterableOps.head(Iterable.scala:222)
	scala.collection.IterableOps.head$(Iterable.scala:222)
	scala.collection.AbstractIterable.head(Iterable.scala:935)
	dotty.tools.dotc.interactive.InteractiveDriver.run(InteractiveDriver.scala:164)
	dotty.tools.pc.CachingDriver.run(CachingDriver.scala:45)
	dotty.tools.pc.HoverProvider$.hover(HoverProvider.scala:40)
	dotty.tools.pc.ScalaPresentationCompiler.hover$$anonfun$1(ScalaPresentationCompiler.scala:389)
```
#### Short summary: 

java.util.NoSuchElementException: next on empty iterator