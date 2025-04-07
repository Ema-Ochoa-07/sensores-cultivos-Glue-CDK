package com.myorg.lambda;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class GrantAdminPrivilegesHandler implements RequestHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        String jdbcUrl = System.getenv("JDBC_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             Statement stmt = connection.createStatement()) {

            stmt.execute("GRANT ALL PRIVILEGES ON cultivo_bd.* TO 'admin'@'%'");
            stmt.execute("FLUSH PRIVILEGES");
            return "Permisos otorgados correctamente al usuario 'admin'.";
        } catch (Exception e) {
            return "Error al otorgar permisos: " + e.getMessage();
        }
    }
}
