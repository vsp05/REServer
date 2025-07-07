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

@SuppressWarnings("PMD.LooseCoupling")
public class SalesDAO {

    // errorprone: Avoid Duplicate Literals
    private static final String FIELD_PURCHASE_PRICE = "purchase_price";

    // errorprone: Avoid Literals In If Condition
    private static final String EMPTY_STRING = "";


    private final MongoCollection<Document> collection;

    public SalesDAO() {
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

    private Document homeSaleToDocument(final HomeSale homeSale) {
        return new Document()
                .append("property_id", homeSale.propertyId)
                .append("post_code", homeSale.postCode)
                .append(FIELD_PURCHASE_PRICE, homeSale.purchasePrice)
                .append("download_date", homeSale.downloadDate)
                .append("council_name", homeSale.councilName)
                .append("address", homeSale.address)
                .append("property_type", homeSale.propertyType)
                .append("strata_lot_number", homeSale.strataLotNumber)
                .append("property_name", homeSale.propertyName)
                .append("area", homeSale.area)
                .append("area_type", homeSale.areaType)
                .append("contract_data", homeSale.contractData)
                .append("settlement_date", homeSale.settlementDate)
                .append("zoning", homeSale.zoning)
                .append("nature_of_property", homeSale.natureOfProperty)
                .append("primary_purpose", homeSale.primaryPurpose)
                .append("legal_description", homeSale.legalDescription)
                .append("property_accessed_count", homeSale.propertyAccessedCount)
                .append("post_code_accessed_count", homeSale.postCodeAccessedCount);
    }

    private Integer parseToInt(final Object obj) {
        Integer result = 0; 
        if (obj instanceof Integer) {
            result = (Integer) obj;
        } else if (obj instanceof String) {
            if (EMPTY_STRING.equals(obj)) {
                result = 0;
            } else {
                result = Integer.parseInt((String) obj);
            }
        } else if (obj instanceof Double) {
            result = ((Double) obj).intValue();
        }
        return result;
    }


    private String parseToString(final Object obj) {
        String result = "";
        if (obj instanceof Integer) {
            result = Integer.toString((Integer) obj);
        } else if (obj instanceof String) {
            result = (String) obj;
        } else if (obj instanceof Double) {
            result = Double.toString((Double) obj);
        }
        return result;
    }

    private HomeSale documentToHomeSale(final Document doc) {
        return new HomeSale(
               parseToInt(doc.get("property_id")),
               doc.getString("download_date"),
               doc.getString("council_name"),
               parseToInt(doc.get(FIELD_PURCHASE_PRICE)),
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
               parseToString(doc.get("legal_description")),
               parseToInt(doc.get("property_accessed_count")),
               parseToInt(doc.get("post_code_accessed_count"))
       );
    }

    public boolean newSale(HomeSale homeSale) {
        boolean success = false;
        int postCodeAccessedCount = 0;

        try {
            postCodeAccessedCount = getSalesByPostCode(String.valueOf(homeSale.postCode), false).get(0).postCodeAccessedCount;
        } catch (Exception e) {
            postCodeAccessedCount = 0;
        }

        homeSale.postCodeAccessedCount = postCodeAccessedCount;

        try {
            final Document doc = homeSaleToDocument(homeSale);
            collection.insertOne(doc);
            success = true;
        } catch (MongoException e) {
            success = false;
        }
        return success;
    }

    public Optional<HomeSale> handleSaleByID(final String propertyID) {
        Optional<HomeSale> result = Optional.empty();

        try {
            final Document doc = collection.find(Filters.eq("property_id", parseToInt(propertyID))).first();
            if (doc != null) {
                result = Optional.of(documentToHomeSale(doc));
                collection.updateOne(Filters.eq("property_id", parseToInt(propertyID)), new Document("$inc", new Document("property_accessed_count", 1)));
            }
        } catch (MongoException e) {
            result = Optional.empty();
        }

        return result;
    }

    // returns a List of homesales  in a given postCode
    public List<HomeSale> getSalesByPostCode(final String postCode, final boolean incrementAccessCount) {
        List<HomeSale> sales = new ArrayList<>();

        try {
            List<HomeSale> finalSales = sales;
            collection.find(Filters.eq("post_code", parseToInt(postCode)))
                .forEach(doc -> finalSales.add(documentToHomeSale(doc)));
        } catch (MongoException e) {
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
                .forEach(doc -> finalPrices.add(doc.getString(FIELD_PURCHASE_PRICE)));
        } catch (MongoException e) {
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
            sales = Collections.emptyList();
        }

        return sales;
    }

    // gets the average price for a given date range
    public double handleAveragePriceByDateRange(final String startDate, final String endDate) {
        List<HomeSale> sales = new ArrayList<>();
        double result = 0.0; // default value

        try {
            collection.find(Filters.and(Filters.gte("contract_date", startDate), Filters.lte("contract_date", endDate)))
                .forEach(doc -> sales.add(documentToHomeSale(doc)));

            if (!sales.isEmpty()) {
                double averagePrice = 0.0;
                for (final HomeSale sale : sales) {
                    averagePrice += sale.purchasePrice;
                }

                averagePrice /= sales.size();
                result = Math.round(averagePrice * 100.0) / 100.0;
            }
        } catch (MongoException e) {
            result = 0.0;
        }

        return result;
    }

    // returns a list of sales under a given price
    public List<HomeSale> handleSalesUnderPrice(final int price) {

        List<HomeSale> sales = new ArrayList<>();

        try {
            List<HomeSale> finalSales = sales;
            collection.find(Filters.lt(FIELD_PURCHASE_PRICE, price))
                .forEach(doc -> finalSales.add(documentToHomeSale(doc)));
        } catch (MongoException e) {
            sales = Collections.emptyList(); 
        }

        return sales;
    }

}
