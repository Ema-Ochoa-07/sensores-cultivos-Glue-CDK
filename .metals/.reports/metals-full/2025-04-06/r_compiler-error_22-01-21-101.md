file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/lambda/DatabaseCultivoCredentials.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 21
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/lambda/DatabaseCultivoCredentials.java
text:
```scala
package com.riego.sen@@sores.db;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DatabaseCultivoCredentials {

    public static DatabaseCredentialsData getCredentials(String secretName) throws Exception {
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.US_EAST_1) // Cambia si usas otra región
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
        String secretString = getSecretValueResponse.secretString();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(secretString);

        return new DatabaseCredentialsData(
                jsonNode.get("username").asText(),
                jsonNode.get("password").asText(),
                jsonNode.get("host").asText(),
                jsonNode.get("port").asText(),
                jsonNode.get("dbname").asText()
        );
    }

    public static class DatabaseCredentialsData {
        public final String username;
        public final String password;
        public final String host;
        public final String port;
        public final String dbname;

        public DatabaseCredentialsData(String username, String password, String host, String port, String dbname) {
            this.username = username;
            this.password = password;
            this.host = host;
            this.port = port;
            this.dbname = dbname;
        }
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