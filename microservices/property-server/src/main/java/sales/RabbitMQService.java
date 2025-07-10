package sales;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitMQService {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQService.class);
    private static final String PROPERTY_QUEUE_NAME = "property_access_queue";
    private static final String POSTCODE_QUEUE_NAME = "postcode_access_queue";
    private static final String EXCHANGE_NAME = "property_access_exchange";
    private static final String PROPERTY_ROUTING_KEY = "property.access";
    private static final String POSTCODE_ROUTING_KEY = "postcode.access";
    
    private final ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    
    public RabbitMQService() {
        this.factory = new ConnectionFactory();
        this.factory.setHost("localhost");
        this.factory.setPort(5672);
        this.factory.setUsername("guest");
        this.factory.setPassword("guest");
    }
    
    public void initialize() {
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            
            // Declare exchange
            channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
            
            // Declare property access queue
            channel.queueDeclare(PROPERTY_QUEUE_NAME, true, false, false, null);
            
            // Declare postcode access queue
            channel.queueDeclare(POSTCODE_QUEUE_NAME, true, false, false, null);
            
            // Bind queues to exchange
            channel.queueBind(PROPERTY_QUEUE_NAME, EXCHANGE_NAME, PROPERTY_ROUTING_KEY);
            channel.queueBind(POSTCODE_QUEUE_NAME, EXCHANGE_NAME, POSTCODE_ROUTING_KEY);
            
            logger.info("RabbitMQ service initialized successfully");
        } catch (IOException | TimeoutException e) {
            logger.error("Failed to initialize RabbitMQ service", e);
        }
    }
    
    public void publishPropertyAccess(String propertyId) {
        try {
            if (channel != null && channel.isOpen()) {
                String message = propertyId;
                channel.basicPublish(EXCHANGE_NAME, PROPERTY_ROUTING_KEY, null, 
                    message.getBytes(StandardCharsets.UTF_8));
                logger.info("Published property access message for property ID: {}", propertyId);
            } else {
                logger.warn("RabbitMQ channel is not available, skipping message publish");
            }
        } catch (IOException e) {
            logger.error("Failed to publish property access message", e);
        }
    }
    
    public void publishPostCodeAccess(String postCode) {
        try {
            if (channel != null && channel.isOpen()) {
                String message = postCode;
                channel.basicPublish(EXCHANGE_NAME, POSTCODE_ROUTING_KEY, null, 
                    message.getBytes(StandardCharsets.UTF_8));
                logger.info("Published postcode access message for postcode: {}", postCode);
            } else {
                logger.warn("RabbitMQ channel is not available, skipping message publish");
            }
        } catch (IOException e) {
            logger.error("Failed to publish postcode access message", e);
        }
    }
    
    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
            logger.info("RabbitMQ service closed successfully");
        } catch (IOException | TimeoutException e) {
            logger.error("Failed to close RabbitMQ service", e);
        }
    }
} 