package sales;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitMQConsumer {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
    private static final String PROPERTY_QUEUE_NAME = "property_access_queue";
    private static final String POSTCODE_QUEUE_NAME = "postcode_access_queue";
    private static final String EXCHANGE_NAME = "property_access_exchange";
    private static final String PROPERTY_ROUTING_KEY = "property.access";
    private static final String POSTCODE_ROUTING_KEY = "postcode.access";
    
    private final ConnectionFactory factory;
    private final AnalyticsDAO analyticsDAO;
    private Connection connection;
    private Channel channel;
    private boolean isRunning = false;
    
    public RabbitMQConsumer(AnalyticsDAO analyticsDAO) {
        this.analyticsDAO = analyticsDAO;
        this.factory = new ConnectionFactory();
        this.factory.setHost("localhost");
        this.factory.setPort(5672);
        this.factory.setUsername("guest");
        this.factory.setPassword("guest");
    }
    
    public void start() {
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
            
            // Set up property access consumer
            Consumer propertyConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                        AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String propertyId = new String(body, StandardCharsets.UTF_8);
                    logger.info("Received property access message for property ID: {}", propertyId);
                    
                    // Increment property access count
                    if (analyticsDAO.incrementPropertyAccessCount(propertyId)) {
                        logger.info("Successfully incremented access count for property ID: {}", propertyId);
                    } else {
                        logger.error("Failed to increment access count for property ID: {}", propertyId);
                    }
                    
                    // Acknowledge the message
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            };
            
            // Set up postcode access consumer
            Consumer postcodeConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                        AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String postCode = new String(body, StandardCharsets.UTF_8);
                    logger.info("Received postcode access message for postcode: {}", postCode);
                    
                    // Increment postcode access count
                    if (analyticsDAO.incrementPostCodeAccessCount(postCode)) {
                        logger.info("Successfully incremented access count for postcode: {}", postCode);
                    } else {
                        logger.error("Failed to increment access count for postcode: {}", postCode);
                    }
                    
                    // Acknowledge the message
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            };
            
            // Start consuming messages from both queues
            channel.basicConsume(PROPERTY_QUEUE_NAME, false, propertyConsumer);
            channel.basicConsume(POSTCODE_QUEUE_NAME, false, postcodeConsumer);
            isRunning = true;
            logger.info("RabbitMQ consumer started successfully");
            
        } catch (IOException | TimeoutException e) {
            logger.error("Failed to start RabbitMQ consumer", e);
        }
    }
    
    public void stop() {
        isRunning = false;
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
            logger.info("RabbitMQ consumer stopped successfully");
        } catch (IOException | TimeoutException e) {
            logger.error("Failed to stop RabbitMQ consumer", e);
        }
    }
    
    public boolean isRunning() {
        return isRunning;
    }
} 