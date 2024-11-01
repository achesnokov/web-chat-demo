package org.achesnokov.demo.chat.repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.achesnokov.demo.chat.model.User;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

/**
 * Repository class for managing User entities in DynamoDB.
 * This class provides methods to save, find, and delete users, as well as find users by username or user IDs.
 */
@ApplicationScoped
public class UserRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<User> userTable;

    /**
     * Constructs a UserRepository with the given DynamoDB client.
     *
     * @param dynamoDbClient the DynamoDB client to be used for data access.
     */
    @Inject
    public UserRepository(DynamoDbClient dynamoDbClient) {
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.userTable = enhancedClient.table("User", TableSchema.fromBean(User.class));
    }

    /**
     * Saves a user entity to the DynamoDB table.
     *
     * @param user the User entity to be saved.
     * @throws IllegalArgumentException if the username already exists.
     */
    public void save(User user) {
        PutItemEnhancedRequest<User> putRequest = PutItemEnhancedRequest.builder(User.class)
                .item(user)
                .conditionExpression(Expression.builder()
                        .expression("attribute_not_exists(userName)")
                        .build())
                .build();

        try {
            userTable.putItem(putRequest);
        } catch (ConditionalCheckFailedException e) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
    }

    /**
     * Finds a user by their unique identifier.
     *
     * @param userId the user ID to find.
     * @return the User entity with the specified ID, or null if not found.
     */
    public User findById(String userId) {
        return userTable.getItem(r -> r.key(k -> k.partitionValue(userId)));
    }

    /**
     * Finds multiple users by their unique identifiers.
     *
     * @param userIds the set of user IDs to find.
     * @return a set of User entities with the specified IDs.
     */
    public Set<User> findByIds(Set<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptySet();
        }

        ReadBatch.Builder<User> userReadBatchBuilder = ReadBatch.builder(User.class)
                .mappedTableResource(userTable);

        userIds.stream()
                .distinct()
                .forEach(userId -> userReadBatchBuilder
                        .addGetItem(r -> r.key(k -> k.partitionValue(userId))));

        BatchGetItemEnhancedRequest batchGetItemRequest = BatchGetItemEnhancedRequest.builder()
                .addReadBatch(userReadBatchBuilder.build())
                .build();

        return enhancedClient.batchGetItem(batchGetItemRequest)
                .resultsForTable(userTable)
                .stream()
                .collect(Collectors.toSet());
    }

    /**
     * Finds all users in the DynamoDB table.
     *
     * @return a list of all User entities.
     */
    public List<User> findAll() {
        return userTable.scan().items().stream().collect(Collectors.toList());
    }

    /**
     * Finds a user by their username.
     *
     * @param username the username to find.
     * @return an Optional containing the User entity, or empty if not found.
     */
    public Optional<User> findByUsername(String username) {
        // Using scan to find user by username
        return userTable.scan().items().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    /**
     * Deletes a user from the DynamoDB table by their user ID.
     *
     * @param userId the user ID of the entity to be deleted.
     */
    public void delete(String userId) {
        userTable.deleteItem(r -> r.key(k -> k.partitionValue(userId)));
    }
}
