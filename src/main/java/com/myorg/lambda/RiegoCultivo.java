package com.myorg.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class RiegoCultivo implements RequestHandler<Map<String, Object>, Map<String, Object>> { 

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        // Ejemplo de lógica básica
        int humedad = (int) input.getOrDefault("humedad", 0);
        boolean debeRegar = humedad < 30;

        return Map.of(
            "debeRegar", debeRegar,
            "mensaje", debeRegar ? "Activando riego" : "No se requiere riego"
        );
    }
}