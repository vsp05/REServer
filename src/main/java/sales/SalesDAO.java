package sales;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

public class SalesDAO {

    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    public SalesDAO() {
        String uri = "mongodb+srv://vspillai02:8CjpUFgayTxa4Lk9@cs4530exercise1.a7aa12o.mongodb.net/?retryWrites=true&w=majority&appName=cs4530exercise1";
        ServerApi serverApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build();
        MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(uri))
        .serverApi(serverApi)
        .build();

        try {
            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("homesale");
            collection = database.getCollection("sales");
            System.out.println("Connected to MongoDB");
        } catch (MongoException e) {
            System.err.println("Error connecting to MongoDB: " + e.getMessage());
        }
    }

    private Document homeSaleToDocument(HomeSale homeSale) {
        return new Document()
                .append("property_id", homeSale.property_id)
                .append("post_code", homeSale.post_code)
                .append("purchase_price", homeSale.purchase_price)
                .append("download_date", homeSale.download_date)
                .append("council_name", homeSale.council_name)
                .append("address", homeSale.address)
                .append("property_type", homeSale.property_type)
                .append("strata_lot_number", homeSale.strata_lot_number)
                .append("property_name", homeSale.property_name)
                .append("area", homeSale.area)
                .append("area_type", homeSale.area_type)
                .append("contract_data", homeSale.contract_data)
                .append("settlement_date", homeSale.settlement_date)
                .append("zoning", homeSale.zoning)
                .append("nature_of_property", homeSale.nature_of_property)
                .append("primary_purpose", homeSale.primary_purpose)
                .append("legal_description", homeSale.legal_description);
    }

    private Integer parseToInt(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof String) {
            if (obj.equals("")) {
                return 0;
            }
            return Integer.parseInt((String) obj);
        } else if (obj instanceof Double) {
            return ((Double) obj).intValue();
        }
        return 0;
    }

    private String parseToString(Object obj) {
        if (obj instanceof Integer) {
            return Integer.toString((Integer) obj);
        } else if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof Double) {
            return Double.toString((Double) obj);
        }
        return "";
    }

    private HomeSale documentToHomeSale(Document doc) {
        return new HomeSale(
               parseToInt(doc.get("property_id")),
               doc.getString("download_date"),
               doc.getString("council_name"),
               parseToInt(doc.get("purchase_price")),
               doc.getString("address"),
               parseToInt(doc.get("post_code")),
               doc.getString("property_type"),
               parseToString(doc.get("strata_lot_number")),
               doc.getString("property_name"),
               parseToInt(doc.get("area")),
               doc.getString("area_type"),
               doc.getString("contract_data"),
               doc.getString("settlement_date"),
               doc.getString("zoning"),
               parseToString(doc.get("nature_of_property")),   
               doc.getString("primary_purpose"),
               parseToString(doc.get("legal_description"))
       );
    }

    public boolean newSale(HomeSale homeSale) {
        try {
            Document doc = homeSaleToDocument(homeSale);
            collection.insertOne(doc);
            System.out.println("Successfully inserted sale with ID: " + homeSale.property_id);
            return true;
        } catch (MongoException e) {
            System.err.println("Error inserting sale: " + e.getMessage());
            return false;
        }
    }

    public Optional<HomeSale> getSaleById(String property_id) {
        try {
            Document doc = collection.find(Filters.eq("property_id", parseToInt(property_id))).first();
            if (doc != null) {
                System.out.println("Sale found with ID: " + property_id);
                return Optional.of(documentToHomeSale(doc));
            }
            return Optional.empty();
        } catch (MongoException e) {
            System.err.println("Error finding sale by ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    // returns a List of homesales  in a given postCode
    public List<HomeSale> getSalesByPostCode(String postCode) {
        System.out.println("Searching for post_code: " + postCode);
        List<HomeSale> sales = new ArrayList<>();
        try {
            collection.find(Filters.eq("post_code", parseToInt(postCode)))
                    .forEach(doc -> sales.add(documentToHomeSale(doc)));
            System.out.println("Found " + sales.size() + " sales for post_code: " + postCode);
            return sales;
        } catch (MongoException e) {
            System.err.println("Error finding sales by post_code: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // returns the individual prices for all sales. Potentially large
    public List<String> getAllSalePrices() {
        List<String> prices = new ArrayList<>();
        try {
            collection.find()
                    .forEach(doc -> prices.add(doc.getString("purchase_price")));
            return prices;
        } catch (MongoException e) {
            System.err.println("Error getting all sale prices: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // returns all home sales. Potentially large
    public List<HomeSale> getAllSales() {
        List<HomeSale> sales = new ArrayList<>();
        try {
            collection.find()
                    .forEach(doc -> sales.add(documentToHomeSale(doc)));
            return sales;
        } catch (MongoException e) {
            System.err.println("Error getting all sales: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // gets the average price for a given date range
    public double getAveragePriceByDateRange(String startDate, String endDate) {
        List<HomeSale> sales = new ArrayList<>();
        try {
            collection.find(Filters.and(Filters.gte("contract_date", startDate), Filters.lte("contract_date", endDate)))
                    .forEach(doc -> sales.add(documentToHomeSale(doc)));

            double averagePrice = 0.0;

            for (HomeSale sale : sales) {
                averagePrice += sale.purchase_price;
            }

            averagePrice /= sales.size();
            return Math.round(averagePrice * 100.0) / 100.0;
        } catch (MongoException e) {
            System.err.println("No prices found for date range. Try formatting dates as YYYY-MM-DD");
            return 0.0;
        }
    }

    // returns a list of sales under a given price
    public List<HomeSale> getSalesUnderPrice(int price) {
        List<HomeSale> sales = new ArrayList<>();
        try {
            collection.find(Filters.lt("purchase_price", price))
                    .forEach(doc -> sales.add(documentToHomeSale(doc)));
            return sales;
        } catch (MongoException e) {
            System.err.println("Error counting sales under price: " + e.getMessage());
            return Collections.emptyList();
        }
    }

}
