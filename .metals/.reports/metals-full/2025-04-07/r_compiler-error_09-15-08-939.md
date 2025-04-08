file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/SecretsCreator.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 368
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/SecretsCreator.java
text:
```scala
package com.myorg.lambda;

import software.amazon.awscdk.services.secretsmanager.Secret;
import software.constructs.Construct;

public class SecretsCreator {
    private final Secret secret;

    public SecretsCreator(Construct scope, String id, String username, String password) {
        this.secret = Secret.Builder.create(scope, id)
            .secretName("cultiv@@o-db-secret")
            .generateSecretString(software.amazon.awscdk.services.secretsmanager.SecretStringGenerator.builder()
                .secretStringTemplate(String.format("{\"username\":\"%s\"}", username))
                .generateStringKey("password")
                .passwordLength(12)
                .excludePunctuation(true)
                .build())
            .build();
    }

    public Secret getSecret() {
        return secret;
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