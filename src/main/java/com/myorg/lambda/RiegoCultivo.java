package com.myorg.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

public class RiegoCultivo implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String DB_URL = "jdbc:mysql://" + System.getenv("RDS_ENDPOINT") + ":3306/cultivo_bd";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        int humedad = 0;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            String query = "SELECT humedad FROM lecturas_sensor ORDER BY fecha DESC LIMIT 1";
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                humedad = rs.getInt("humedad");
            }

        } catch (Exception e) {
            context.getLogger().log("Error al conectar a la base de datos: " + e.getMessage());
            return Map.of(
                    "error", "No se pudo obtener la humedad",
                    "detalle", e.getMessage()
            );
        }

        boolean debeRegar = humedad < 30;

        return Map.of(
                "humedad", humedad,
                "debeRegar", debeRegar,
                "mensaje", debeRegar ? "Activando riego" : "No se requiere riego"
        );
    }
}
