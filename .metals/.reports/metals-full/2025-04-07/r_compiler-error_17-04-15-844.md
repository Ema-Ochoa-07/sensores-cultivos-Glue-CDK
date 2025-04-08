file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/lambda/RiegoCultivo.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 2380
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/lambda/RiegoCultivo.java
text:
```scala
package com.myorg.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class RiegoCultivo implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se pudo cargar el driver de MySQL", e);
        }
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        Map<String, Object> response = new HashMap<>();
        String secretName = "EtlCultivoSensoresStackRdsS-LxRUEOC2O2gm";
        Region region = Region.US_EAST_1;

        try {
            // Obtener el secreto desde AWS Secrets Manager
            SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                    .region(region)
                    .build();

            GetSecretValueRequest secretRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse secretResponse = secretsClient.getSecretValue(secretRequest);
            String secretString = secretResponse.secretString();

            JSONObject secretJson = new JSONObject(secretString);
            String host = secretJson.getString("host");
            String username = secretJson.getString("username");
            String password = secretJson.getString("password");
            String dbname = secretJson.getString("dbname");

            String dbUrl = String.format("jdbc:mysql://%s:3306/%s?useSSL=false&allowPublicKeyRetrieval=true", host, dbname);

            context.getLogger().log("Conectando a: " + dbUrl);

            try (Connection conn = DriverManager.getConnection@@(dbUrl, username, password);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT humedad FROM datos_sensores ORDER BY fecha DESC LIMIT 1")) {

                if (rs.next()) {
                    int humedad = rs.getInt("humedad");
                    response.put("status", "success");
                    response.put("humedad", humedad);
                    response.put("debeRegar", humedad < 30);
                } else {
                    response.put("status", "no-data");
                }

            } catch (Exception e) {
                context.getLogger().log("Error al conectar o consultar la base de datos: " + e.getMessage());
                response.put("status", "db-error");
                response.put("message", e.getMessage());
            }

        } catch (Exception e) {
            context.getLogger().log("Error al leer el secreto: " + e.getMessage());
            response.put("status", "secret-error");
            response.put("message", e.getMessage());
        }

        return response;
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