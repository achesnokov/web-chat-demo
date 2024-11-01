package org.achesnokov.demo.chat.repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.achesnokov.demo.chat.model.ChatParticipant;
import org.achesnokov.demo.chat.model.User;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Repository class for managing ChatParticipant entities in DynamoDB.
 * This class provides methods to save, find, and delete chat participants, as well as get chat participants as users.
 */
@ApplicationScoped
public class ChatParticipantRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<ChatParticipant> participantTable;
    private final DynamoDbTable<User> userTable;

    /**
     * Constructs a ChatParticipantRepository with the given DynamoDB client.
     *
     * @param dynamoDbClient the DynamoDB client to be used for data access.
     */
    @Inject
    public ChatParticipantRepository(DynamoDbClient dynamoDbClient) {
        this.enhancedClient = createEnhancedClient(dynamoDbClient);
        this.participantTable = enhancedClient.table("ChatParticipant", TableSchema.fromBean(ChatParticipant.class));
        this.userTable = enhancedClient.table("User", TableSchema.fromBean(User.class));
    }

    protected DynamoDbEnhancedClient createEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    /**
     * Saves a chat participant to the DynamoDB table.
     *
     * @param participant the ChatParticipant entity to be saved.
     */
    public void save(ChatParticipant participant) {
        Objects.requireNonNull(participant, "Participant must not be null");
        participantTable.putItem(participant);
    }

    /**
     * Finds all chat participants by the given chat ID.
     *
     * @param chatId the chat ID to find participants for.
     * @return a list of ChatParticipant entities for the given chat ID.
     */
    public List<ChatParticipant> findByChatId(String chatId) {
        return participantTable.query(QueryConditional.keyEqualTo(k -> k.partitionValue(chatId))).items().stream().collect(Collectors.toList());
    }

    /**
     * Retrieves all users who are participants in the specified chat.
     *
     * @param chatId the chat ID for which participants are retrieved.
     * @return a list of User entities representing the participants in the chat.
     */
    public List<User> getChatParticipantsAsUsers(String chatId) {
        ReadBatch.Builder<User> userReadBatchBuilder = ReadBatch.builder(User.class)
                .mappedTableResource(userTable);

        findByChatId(chatId)
                .stream()
                .map(ChatParticipant::getUserId)
                .distinct()
                .forEach(userId -> userReadBatchBuilder
                        .addGetItem(r -> r.key(k -> k.partitionValue(userId))));

        BatchGetItemEnhancedRequest batchGetItemRequest = BatchGetItemEnhancedRequest.builder()
                .addReadBatch(userReadBatchBuilder.build())
                .build();

        return enhancedClient.batchGetItem(batchGetItemRequest)
                .resultsForTable(userTable)
                .stream()
                .collect(Collectors.toList());
    }

    /**
     * Finds all chat participants by the given user ID.
     *
     * @param userId the user ID to find associated chat participants for.
     * @return a list of ChatParticipant entities for the given user ID.
     */
    public List<ChatParticipant> findByUserId(String userId) {
        return participantTable.scan().items().stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Deletes a chat participant from the DynamoDB table based on chat ID and user ID.
     *
     * @param chatId the chat ID of the participant to be deleted.
     * @param userId the user ID of the participant to be deleted.
     */
    public void delete(String chatId, String userId) {
        participantTable.deleteItem(r -> r.key(k -> k.partitionValue(chatId).sortValue(userId)));
    }
}
