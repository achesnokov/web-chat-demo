package org.achesnokov.demo.chat.repository;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.achesnokov.demo.chat.model.Chat;
import org.achesnokov.demo.chat.model.ChatParticipant;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Repository class for managing Chat entities in DynamoDB.
 * This class provides methods to save, find, and delete chats, as well as find chats by participant.
 */
@ApplicationScoped
public class ChatRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Chat> chatTable;
    private final DynamoDbTable<ChatParticipant> chatParticipantTable;
    private final DynamoDbIndex<ChatParticipant> userIndex;

    /**
     * Constructs a ChatRepository with the given DynamoDB client.
     *
     * @param dynamoDbClient the DynamoDB client to be used for data access.
     */
    @Inject
    public ChatRepository(DynamoDbClient dynamoDbClient) {
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        this.chatTable = enhancedClient.table("Chat", TableSchema.fromBean(Chat.class));
        this.chatParticipantTable = enhancedClient.table("ChatParticipant", TableSchema.fromBean(ChatParticipant.class));
        this.userIndex = chatParticipantTable.index("userId-index");
    }

    /**
     * Saves a chat entity to the DynamoDB table.
     *
     * @param chat the Chat entity to be saved.
     */
    public void save(Chat chat) {
        chatTable.putItem(chat);
    }

    /**
     * Finds a chat by its unique identifier.
     *
     * @param chatId the chat ID to find.
     * @return the Chat entity with the specified ID, or null if not found.
     */
    public Chat findById(String chatId) {
        return chatTable.getItem(r -> r.key(k -> k.partitionValue(chatId)));
    }

    /**
     * Finds all chats.
     *
     * @return a list of all Chat entities.
     */
    public List<Chat> findAll() {
        return chatTable.scan().items().stream().collect(Collectors.toList());
    }

    /**
     * Finds all chats that a specific participant is currently part of.
     *
     * @param participantId the ID of the participant to search for.
     * @return a list of Chat entities that the specified participant is part of.
     */
    public List<Chat> findByParticipant(String participantId) {
        return userIndex.query(QueryConditional.keyEqualTo(k -> k.partitionValue(participantId)))
                .stream()
                .flatMap(page -> page.items().stream())
                .filter(chatParticipant -> chatParticipant.getLeftAt() == null)
                .map(ChatParticipant::getChatId)
                .distinct()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a chat entity from the DynamoDB table by its chat ID.
     *
     * @param chatId the chat ID of the entity to be deleted.
     */
    public void delete(String chatId) {
        chatTable.deleteItem(r -> r.key(k -> k.partitionValue(chatId)));
    }
}
