package org.achesnokov.demo.chat.repository;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.achesnokov.demo.chat.model.Message;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Repository class for managing Message entities in DynamoDB.
 * This class provides methods to save, find, and delete messages, as well as find messages by chat ID and user IDs.
 */
@ApplicationScoped
public class MessageRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Message> messageTable;

    /**
     * Constructs a MessageRepository with the given DynamoDB client.
     *
     * @param dynamoDbClient the DynamoDB client to be used for data access.
     */
    @Inject
    public MessageRepository(DynamoDbClient dynamoDbClient) {
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.messageTable = enhancedClient.table("Message", TableSchema.fromBean(Message.class));
    }

    /**
     * Saves a message entity to the DynamoDB table.
     *
     * @param message the Message entity to be saved.
     */
    public void save(Message message) {
        messageTable.putItem(message);
    }

    /**
     * Finds a message by its chat ID and message ID.
     *
     * @param chatId    the chat ID the message belongs to.
     * @param messageId the unique message ID.
     * @return the Message entity with the specified IDs, or null if not found.
     */
    public Message findById(String chatId, String messageId) {
        return messageTable.getItem(r -> r.key(k -> k.partitionValue(chatId).sortValue(messageId)));
    }

    /**
     * Finds all messages for a given chat ID.
     *
     * @param chatId the chat ID to find messages for.
     * @return a list of Message entities for the given chat ID.
     */
    public List<Message> findAllByChatId(String chatId) {
        return messageTable.query(QueryConditional.keyEqualTo(k -> k.partitionValue(chatId))).items().stream().collect(Collectors.toList());
    }

    /**
     * Finds all messages for a given chat ID, sorted by timestamp in ascending order.
     *
     * @param chatId the chat ID to find messages for.
     * @return a list of Message entities for the given chat ID, sorted by timestamp.
     */
    public List<Message> findByChatIdSortedByTimestamp(String chatId) {
        return messageTable.query(r -> r.queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(chatId).build()))
                        .scanIndexForward(true))
                .items()
                .stream()
                .collect(Collectors.toList());
    }

    /**
     * Finds messages for a given chat ID that were sent by specific users.
     *
     * @param chatId  the chat ID to find messages for.
     * @param userIds the list of user IDs to filter messages by.
     * @return a list of Message entities for the given chat ID and user IDs.
     */
    public List<Message> findByChatIdAndUserIds(String chatId, List<String> userIds) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(chatId).build());

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .scanIndexForward(true)  // Sorting in ascending order by default
                .build();

        return messageTable.query(request)
                .items()
                .stream()
                .filter(message -> userIds.contains(message.getUserId()))
                .collect(Collectors.toList());
    }

    /**
     * Deletes a message from the DynamoDB table by its chat ID and message ID.
     *
     * @param chatId    the chat ID of the message to be deleted.
     * @param messageId the message ID of the message to be deleted.
     */
    public void delete(String chatId, String messageId) {
        messageTable.deleteItem(r -> r.key(k -> k.partitionValue(chatId).sortValue(messageId)));
    }
}
