package org.achesnokov.demo.chat.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.achesnokov.demo.chat.model.Chat;
import org.achesnokov.demo.chat.model.ChatParticipant;
import org.achesnokov.demo.chat.model.Message;
import org.achesnokov.demo.chat.model.User;
import org.jboss.logging.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.BeanTableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

/**
 * Class responsible for initializing the DynamoDB tables used in the chat application.
 * This class ensures that the required tables are created if they do not exist.
 */
@ApplicationScoped
public class DynamoDbInitializer {
    private static final Logger LOGGER = Logger.getLogger(DynamoDbInitializer.class);

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbClient dynamoDbClient;

    @Inject
    public DynamoDbInitializer(DynamoDbClient dynamoDbClient) {
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.dynamoDbClient = dynamoDbClient;
        LOGGER.debug(">>>> DynamoDbInitializer created");
    }

    /**
     * Initializes the database by creating the required tables if they do not exist.
     */
    public void initializeDatabase() {
        LOGGER.debug(">>>> initializeDatabase started");
        createTableIfNotExists("User", User.class);
        createTableIfNotExists("Chat", Chat.class);
        createTableIfNotExists("Message", Message.class);
        createTableIfNotExists("ChatParticipant", ChatParticipant.class);
    }

    /**
     * Observes the startup event to initialize the database when the application starts.
     *
     * @param ev the startup event.
     */
    void onStartup(@Observes StartupEvent ev) {
        initializeDatabase();
    }

    /**
     * Creates a table if it does not already exist.
     *
     * @param tableName the name of the table to create.
     * @param clazz the class type for the table schema.
     * @param <T> the type of the class.
     */
    private <T> void createTableIfNotExists(String tableName, Class<T> clazz) {
        LOGGER.debug(">>>> createTableIfNotExists started: " + tableName);

        try {
            BeanTableSchema<T> tableSchema = TableSchema.fromBean(clazz);
            DynamoDbTable<T> table = enhancedClient.table(tableName, tableSchema);
            table.createTable();
            LOGGER.debug(">>>> table created: " + tableName);
        }
        catch (ResourceInUseException e) {
            LOGGER.error("Table already exists: " + clazz.getSimpleName());
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to create table: " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Creates the ChatParticipant table with a global secondary index (GSI) if it does not already exist.
     */
    private void createChatParticipantTableWithGSI() {
        String tableName = "ChatParticipant";
        LOGGER.debug(">>>> createChatParticipantTableWithGSI started");

        try {
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(tableName)
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("chatId").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("userId").attributeType(ScalarAttributeType.S).build()
                    )
                    .keySchema(
                            KeySchemaElement.builder().attributeName("chatId").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("userId").keyType(KeyType.RANGE).build()
                    )
                    .globalSecondaryIndexes(GlobalSecondaryIndex.builder()
                            .indexName("userId-index")
                            .keySchema(KeySchemaElement.builder()
                                    .attributeName("userId")
                                    .keyType(KeyType.HASH)
                                    .build())
                            .projection(Projection.builder()
                                    .projectionType(ProjectionType.ALL)
                                    .build())
                            .provisionedThroughput(ProvisionedThroughput.builder()
                                    .readCapacityUnits(5L)
                                    .writeCapacityUnits(5L)
                                    .build())
                            .build())
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(5L)
                            .writeCapacityUnits(5L)
                            .build())
                    .build();


            dynamoDbClient.createTable(request);
            LOGGER.debug(">>>> table created with GSI: " + tableName);
        } catch (ResourceInUseException e) {
            LOGGER.warn("Table already exists: " + tableName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create table with GSI: " + tableName, e);
        }
    }
}
