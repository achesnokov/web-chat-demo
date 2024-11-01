package org.achesnokov.dynamodb.runner;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class DynamoDbContainerInitializer {

    private static final DockerImageName DYNAMODB_IMAGE = DockerImageName.parse("amazon/dynamodb-local");
    private static GenericContainer<?> dynamoDbContainer;

    public static void main(String[] args) {
        dynamoDbContainer = new GenericContainer<>(DYNAMODB_IMAGE)
                .withExposedPorts(8000);
        dynamoDbContainer.start();

        String endpoint = String.format("http://%s:%d", dynamoDbContainer.getHost(), dynamoDbContainer.getMappedPort(8000));
        System.setProperty("QUARKUS_DYNAMODB_ENDPOINT_OVERRIDE", endpoint);
        System.out.println("DynamoDB Local started at: " + endpoint);
        System.out.println("You can now connect your backend to this endpoint.");



        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
