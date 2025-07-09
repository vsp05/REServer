package sales;

import org.bson.Document;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class AnalyticsDAO {

    private final MongoCollection<Document> collection;

    public AnalyticsDAO() {
        String uri = "mongodb+srv://vspillai02:cs4530exercise1@cs4530exercise1.a7aa12o.mongodb.net/?retryWrites=true&w=majority&appName=cs4530exercise1";
        ServerApi serverApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build();
        final MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(uri))
        .serverApi(serverApi)
        .build();

        try {
            MongoClient mongoClient = MongoClients.create(settings);
            final MongoDatabase database = mongoClient.getDatabase("homesale");
            collection = database.getCollection("sales");
        } catch (MongoException e) {
            throw new MongoException("Failed to connect to MongoDB", e);
        }
    }

    private Integer parseToInt(final Object obj) {
        Integer result = 0; 
        if (obj instanceof Integer) {
            result = (Integer) obj;
        } else if (obj instanceof String) {
            if ("".equals(obj)) {
                result = 0;
            } else {
                result = Integer.parseInt((String) obj);
            }
        } else if (obj instanceof Double) {
            result = ((Double) obj).intValue();
        }
        return result;
    }

    // Increment property access count
    public boolean incrementPropertyAccessCount(final String propertyID) {
        try {
            collection.updateOne(
                Filters.eq("property_id", parseToInt(propertyID)), 
                new Document("$inc", new Document("property_accessed_count", 1))
            );
            return true;
        } catch (MongoException e) {
            return false;
        }
    }

    // Increment postcode access count
    public boolean incrementPostCodeAccessCount(final String postCode) {
        try {
            collection.updateMany(
                Filters.eq("post_code", parseToInt(postCode)), 
                new Document("$inc", new Document("post_code_accessed_count", 1))
            );
            return true;
        } catch (MongoException e) {
            return false;
        }
    }

    // Get property access count
    public int getPropertyAccessedCount(final String propertyID) {
        int result = 0;
        try {
            Document doc = collection.find(Filters.eq("property_id", parseToInt(propertyID))).first();
            if (doc != null) {
                result = parseToInt(doc.get("property_accessed_count"));
            }
        } catch (Exception e) {
            result = 0;
        }
        return result;
    }

    // Get postcode access count
    public int getPostCodeAccessedCount(final String postCode) {
        int result = 0;
        try {
            Document doc = collection.find(Filters.eq("post_code", parseToInt(postCode))).first();
            if (doc != null) {
                result = parseToInt(doc.get("post_code_accessed_count"));
            }
        } catch (Exception e) {
            result = 0;
        }
        return result;
    }
} 