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

    private final MongoCollection<Document> collection;

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
            MongoClient mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("homesale");
            collection = database.getCollection("sales");
        } catch (MongoException e) {
            throw new MongoException("Failed to connect to MongoDB", e);
        }
    }

    private Document homeSaleToDocument(final HomeSale homeSale) {
        return new Document()
                .append("property_id", homeSale.propertyId)
                .append("post_code", homeSale.postCode)
                .append("purchase_price", homeSale.purchasePrice)
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
                .append("legal_description", homeSale.legalDescription);
    }

    private Integer parseToInt(final Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof String) {
            if ("".equals(obj)) {
                return 0;
            }
            return Integer.parseInt((String) obj);
        } else if (obj instanceof Double) {
            return ((Double) obj).intValue();
        }
        return 0;
    }

    private String parseToString(final Object obj) {
        if (obj instanceof Integer) {
            return Integer.toString((Integer) obj);
        } else if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof Double) {
            return Double.toString((Double) obj);
        }
        return "";
    }

    private HomeSale documentToHomeSale(final Document doc) {
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

    public boolean newSale(final HomeSale homeSale) {
        try {
            Document doc = homeSaleToDocument(homeSale);
            collection.insertOne(doc);
            return true;
        } catch (MongoException e) {
            return false;
        }
    }

    public Optional<HomeSale> getSaleById(final String propertyId) {
        try {
            Document doc = collection.find(Filters.eq("property_id", parseToInt(propertyId))).first();
            if (doc != null) {
                return Optional.of(documentToHomeSale(doc));
            }
            return Optional.empty();
        } catch (MongoException e) {
            return Optional.empty();
        }
    }

    // returns a List of homesales  in a given postCode
    public List<HomeSale> getSalesByPostCode(final String postCode) {
        List<HomeSale> sales = new ArrayList<>();
        try {
            collection.find(Filters.eq("post_code", parseToInt(postCode)))
                    .forEach(doc -> sales.add(documentToHomeSale(doc)));
            return sales;
        } catch (MongoException e) {
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
            return Collections.emptyList();
        }
    }

    // gets the average price for a given date range
    public double getAveragePriceByDateRange(final String startDate, final String endDate) {
        List<HomeSale> sales = new ArrayList<>();
        try {
            collection.find(Filters.and(Filters.gte("contract_date", startDate), Filters.lte("contract_date", endDate)))
                    .forEach(doc -> sales.add(documentToHomeSale(doc)));

            double averagePrice = 0.0;

            for (HomeSale sale : sales) {
                averagePrice += sale.purchasePrice;
            }

            averagePrice /= sales.size();
            return Math.round(averagePrice * 100.0) / 100.0;
        } catch (MongoException e) {
            return 0.0;
        }
    }

    // returns a list of sales under a given price
    public List<HomeSale> getSalesUnderPrice(final int price) {
        List<HomeSale> sales = new ArrayList<>();
        try {
            collection.find(Filters.lt("purchase_price", price))
                    .forEach(doc -> sales.add(documentToHomeSale(doc)));
            return sales;
        } catch (MongoException e) {
            return Collections.emptyList();
        }
    }

}
