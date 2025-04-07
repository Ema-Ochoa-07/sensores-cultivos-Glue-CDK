package com.myorg.lambda;

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
