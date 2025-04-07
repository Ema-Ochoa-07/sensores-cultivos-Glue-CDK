file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/lambda/RiegoCultivo.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 1444
uri: file:///C:/Users/ORTEL/Documents/Enmanuel/AWS/Fund.SEPAV/2%20StepFunct%20-%20GLUE%20Sparck-%20%20%5BBIG%20DATA%5D/etl-cultivo-sensores/src/main/java/com/myorg/lambda/RiegoCultivo.java
text:
```scala
package com.myorg.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class RiegoCultivo implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Carga del driver JDBC para MySQL 8
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se pudo cargar el driver de MySQL", e);
        }
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        Map<String, Object> response = new HashMap<>();

        // Leer desde variables de entorno
        String endpoint = System.getenv("RDS_ENDPOINT");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        // Construir la URL JDBC
        String dbUrl = String.format("jdbc:mysql://%s:3306/cultivo_bd?useSSL=false&allowPublicKeyRetrieval=true", endpoint);

        context.getLogger().log("Intentando conectar a RDS en: " + dbUrl);

        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT humedad FROM lecturas_sensor ORDER BY fecha D@@ESC LIMIT 1")) {

            if (rs.next()) {
                int humedad = rs.getInt("humedad");
                response.put("status", "success");
                response.put("humedad", humedad);
                response.put("debeRegar", humedad < 30);
            }

        } catch (SQLException e) {
            context.getLogger().log("Error SQL: " + e.getMessage());
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