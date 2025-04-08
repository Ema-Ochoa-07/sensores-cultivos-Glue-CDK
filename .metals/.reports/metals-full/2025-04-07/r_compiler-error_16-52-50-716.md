file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/lambda/RiegoCultivo.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 232
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/lambda/RiegoCultivo.java
text:
```scala
package com.myorg.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.secretsmanager.*;
import com.amazonaws.services.secrets@@manager.model.*;
import org.json.JSONObject;

import java.sql.*;
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
        context.getLogger().log("Iniciando Lambda RiegoCultivo...\n");

        // Nombre del secreto tal como aparece en Secrets Manager
        String secretName = "EtlCultivoSensoresStackRdsS-LxRUEOC2O2gm";
        String region = "us-east-1";

        String username, password, host;

        try {
            AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                    .withRegion(region)
                    .build();

            GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                    .withSecretId(secretName);
            GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);

            String secretString = getSecretValueResult.getSecretString();
            JSONObject secretJson = new JSONObject(secretString);

            username = secretJson.getString("username");
            password = secretJson.getString("password");
            host = secretJson.getString("host");

            context.getLogger().log("Secreto obtenido correctamente\n");

        } catch (Exception e) {
            context.getLogger().log("Error obteniendo secreto: " + e.getMessage() + "\n");
            response.put("status", "error");
            response.put("message", "Error al obtener secreto de Secrets Manager");
            return response;
        }

        String dbUrl = String.format("jdbc:mysql://%s:3306/cultivo_db?useSSL=false&allowPublicKeyRetrieval=true", host);
        context.getLogger().log("Conectando a RDS en: " + dbUrl + "\n");

        try (Connection conn = DriverManager.getConnection(dbUrl, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT humedad FROM datos_sensores ORDER BY fecha DESC LIMIT 1")) {

            if (rs.next()) {
                int humedad = rs.getInt("humedad");
                response.put("status", "success");
                response.put("humedad", humedad);
                response.put("debeRegar", humedad < 30);
                context.getLogger().log("Lectura exitosa. Humedad: " + humedad + "\n");
            } else {
                response.put("status", "error");
                response.put("message", "No se encontraron datos de humedad.");
                context.getLogger().log("No se encontraron datos en la tabla\n");
            }

        } catch (SQLException e) {
            context.getLogger().log("Error SQL: " + e.getMessage() + "\n");
            response.put("status", "error");
            response.put("message", "Código SQL: " + e.getErrorCode());
            response.put("sqlState", e.getSQLState());
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