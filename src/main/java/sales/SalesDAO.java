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
        final MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(uri))
        .serverApi(serverApi)
        .build();

        try {
            mongoClient = MongoClients.create(settings);
            final MongoDatabase database = mongoClient.getDatabase("homesale");
            collection = database.getCollection("sales");
            System.out.println("Connected to MongoDB");
        } catch (MongoException e) {
            System.err.println("Error connecting to MongoDB: " + e.getMessage());
        }
    }

    private Document homeSaleToDocument(HomeSale homeSale) {
        return new Document()
                .append("propertyID", homeSale.propertyID)
                .append("post_code", homeSale.post_code)
                .append("purchase_price", homeSale.purchase_price)
                .append("downloadDate", homeSale.downloadDate)
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
        Integer result = 0; // default value
        if (obj instanceof Integer) {
            result = (Integer) obj;
        } else if (obj instanceof String) {
            if (obj.equals("")) {
                result = 0;
            } else {
                result = Integer.parseInt((String) obj);
            }
        } else if (obj instanceof Double) {
            result = ((Double) obj).intValue();
        }
        return result;
    }

    private String parseToString(Object obj) {
        String result = ""; // default value
        if (obj instanceof Integer) {
            result = Integer.toString((Integer) obj);
        } else if (obj instanceof String) {
            result = (String) obj;
        } else if (obj instanceof Double) {
            result = Double.toString((Double) obj);
        }
        return result;
    }

    private HomeSale documentToHomeSale(Document doc) {
        return new HomeSale(
               parseToInt(doc.get("propertyID")),
               doc.getString("downloadDate"),
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
        boolean success = false;
        try {
            final Document doc = homeSaleToDocument(homeSale);
            collection.insertOne(doc);
            System.out.println("Successfully inserted sale with ID: " + homeSale.propertyID);
            success = true;
        } catch (MongoException e) {
            System.err.println("Error inserting sale: " + e.getMessage());
            success = false; // explicit for clarity, though already false
        }
        return success;
    }

    public Optional<HomeSale> handleSaleByID(String propertyID) {
        Optional<HomeSale> result = Optional.empty(); // default value

        try {
            final Document doc = collection.find(Filters.eq("propertyID", parseToInt(propertyID))).first();
            if (doc != null) {
                System.out.println("Sale found with ID: " + propertyID);
                result = Optional.of(documentToHomeSale(doc));
            }
            // if doc is null, result remains Optional.empty()
        } catch (MongoException e) {
            System.err.println("Error finding sale by ID: " + e.getMessage());
            // result remains Optional.empty()
        }

        return result;
    }

    // returns a List of homesales  in a given postCode
    public List<HomeSale> getSalesByPostCode(String postCode) {
        System.out.println("Searching for post_code: " + postCode);
        List<HomeSale> sales = new ArrayList<>();

        try {
            List<HomeSale> finalSales = sales;
            collection.find(Filters.eq("post_code", parseToInt(postCode)))
                .forEach(doc -> finalSales.add(documentToHomeSale(doc)));
            System.out.println("Found " + sales.size() + " sales for post_code: " + postCode);
        } catch (MongoException e) {
            System.err.println("Error finding sales by post_code: " + e.getMessage());
            sales = Collections.emptyList(); // Reset to empty list on error
        }
        return sales;
    }

    // returns the individual prices for all sales. Potentially large
    public List<String> handleAllSalePrices() {
        List<String> prices = new ArrayList<>();

        try {
            List<String> finalPrices = prices;
            collection.find()
                .forEach(doc -> finalPrices.add(doc.getString("purchase_price")));
        } catch (MongoException e) {
            System.err.println("Error getting all sale prices: " + e.getMessage());
            prices = Collections.emptyList(); // Reset to empty list on error
        }

        return prices;
    }

    // returns all home sales. Potentially large
    public List<HomeSale> handleAllSales() {
        List<HomeSale> sales = new ArrayList<>();

        try {
            List<HomeSale> finalSales = sales;
            collection.find()
                .forEach(doc -> finalSales.add(documentToHomeSale(doc)));
        } catch (MongoException e) {
            System.err.println("Error getting all sales: " + e.getMessage());
            sales = Collections.emptyList(); // Reset to empty list on error
        }

        return sales;
    }

    // gets the average price for a given date range
    public double handleAveragePriceByDateRange(String startDate, String endDate) {
        List<HomeSale> sales = new ArrayList<>();
        double result = 0.0; // default value

        try {
            collection.find(Filters.and(Filters.gte("contract_date", startDate), Filters.lte("contract_date", endDate)))
                .forEach(doc -> sales.add(documentToHomeSale(doc)));

            if (!sales.isEmpty()) {
                double averagePrice = 0.0;

                for (final HomeSale sale : sales) {
                    averagePrice += sale.purchase_price;
                }

                averagePrice /= sales.size();
                result = Math.round(averagePrice * 100.0) / 100.0;
            }
        } catch (MongoException e) {
            System.err.println("No prices found for date range. Try formatting dates as YYYY-MM-DD");
            // result remains 0.0
        }

        return result;
    }

    // returns a list of sales under a given price
    public List<HomeSale> handleSalesUnderPrice(int price) {
        List<HomeSale> sales = new ArrayList<>();

        try {
            List<HomeSale> finalSales = sales;
            collection.find(Filters.lt("purchase_price", price))
                .forEach(doc -> finalSales.add(documentToHomeSale(doc)));
        } catch (MongoException e) {
            System.err.println("Error counting sales under price: " + e.getMessage());
            sales = Collections.emptyList(); // Reset to empty list on error
        }

        return sales;
    }

}
