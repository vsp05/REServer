package sales;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// OLD MONGODB IMPORTS - COMMENTED OUT FOR JDBC MIGRATION
// import org.bson.Document;
// import com.mongodb.ConnectionString;
// import com.mongodb.MongoClientSettings;
// import com.mongodb.MongoException;
// import com.mongodb.ServerApi;
// import com.mongodb.ServerApiVersion;
// import com.mongodb.client.MongoClient;
// import com.mongodb.client.MongoClients;
// import com.mongodb.client.MongoCollection;
// import com.mongodb.client.MongoDatabase;
// import com.mongodb.client.model.Filters;

@SuppressWarnings("PMD.LooseCoupling")
public class SalesDAO {

    // errorprone: Avoid Duplicate Literals
    private static final String FIELD_PURCHASE_PRICE = "purchase_price";

    // errorprone: Avoid Literals In If Condition
    private static final String EMPTY_STRING = "";

// MongoDB collection - commented out for JDBC migration
// private final MongoCollection<Document> collection;

    // JDBC connection
    private Connection connection;

    private static final String JDBC_URL = "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require";
    private static final String JDBC_USER = "postgres.uhzaslhptqmffpyoskhb";
    private static final String JDBC_PASSWORD = "cs45301!HJ9"; // Replace this


    // Helper method to get JDBC connection
    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("Creating new database connection to: " + JDBC_URL);
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            System.out.println("Database connection established successfully");
        }
        return connection;
    }

    // Helper method to close JDBC connection
    private void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
// Log error if needed
        }
    }

    public SalesDAO() {
// OLD MONGODB CODE - COMMENTED OUT
// String uri = "mongodb+srv://vspillai02:cs4530exercise1@cs4530exercise1.a7aa12o.mongodb.net/?retryWrites=true&w=majority&appName=cs4530exercise1";
// ServerApi serverApi = ServerApi.builder()
// .version(ServerApiVersion.V1)
// .build();
// final MongoClientSettings settings = MongoClientSettings.builder()
// .applyConnectionString(new ConnectionString(uri))
// .serverApi(serverApi)
// .build();

// try {
//MongoClient mongoClient = MongoClients.create(settings);
//final MongoDatabase database = mongoClient.getDatabase("homesale");
        //     collection = database.getCollection("sales");
        // } catch (MongoException e) {
        //     throw new MongoException("Failed to connect to MongoDB", e);
        // }

        // NEW JDBC CODE
        try {
            // Test the connection
            getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to PostgreSQL database", e);
        }
    }

    // Helper method to convert HomeSale to SQL insert statement
    private String createInsertSQL(HomeSale homeSale) {
        return "INSERT INTO sales (property_id, post_code, purchase_price, download_date, council_name, " +
                "address, property_type, strata_lot_number, property_name, area, area_type, contract_date, " +
                "settlement_date, zoning, nature_of_property, primary_purpose, legal_description, " +
                "property_accessed_count, post_code_accessed_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    // Helper method to set PreparedStatement parameters for HomeSale
    private void setHomeSaleParameters(PreparedStatement pstmt, HomeSale homeSale) throws SQLException {
        pstmt.setInt(1, homeSale.propertyId);
        pstmt.setInt(2, homeSale.postCode);
        pstmt.setInt(3, homeSale.purchasePrice);
        pstmt.setString(4, homeSale.downloadDate);
        pstmt.setString(5, homeSale.councilName);
        pstmt.setString(6, homeSale.address);
        pstmt.setString(7, homeSale.propertyType);
        pstmt.setString(8, homeSale.strataLotNumber);
        pstmt.setString(9, homeSale.propertyName);
        pstmt.setInt(10, homeSale.area);
        pstmt.setString(11, homeSale.areaType);
        pstmt.setString(12, homeSale.contractDate);
        pstmt.setString(13, homeSale.settlementDate);
        pstmt.setString(14, homeSale.zoning);
        pstmt.setString(15, homeSale.natureOfProperty);
        pstmt.setString(16, homeSale.primaryPurpose);
        pstmt.setString(17, homeSale.legalDescription);
        pstmt.setInt(18, homeSale.propertyAccessedCount);
        pstmt.setInt(19, homeSale.postCodeAccessedCount);
    }

    // Helper method to convert ResultSet to HomeSale
    private HomeSale resultSetToHomeSale(ResultSet rs) throws SQLException {
        return new HomeSale(
                rs.getInt("property_id"),
                rs.getString("download_date"),
                rs.getString("council_name"),
                rs.getInt("purchase_price"),
                rs.getString("address"),
                rs.getInt("post_code"),
                rs.getString("property_type"),
                rs.getString("strata_lot_number"),
                rs.getString("property_name"),
                rs.getInt("area"),
                rs.getString("area_type"),
                rs.getString("contract_date"),
                rs.getString("settlement_date"),
                rs.getString("zoning"),
                rs.getString("nature_of_property"),
                rs.getString("primary_purpose"),
                rs.getString("legal_description"),
                rs.getInt("property_accessed_count"),
                rs.getInt("post_code_accessed_count")
        );
    }

    // OLD MONGODB HELPER METHODS - COMMENTED OUT
    // private Document homeSaleToDocument(final HomeSale homeSale) {
    //     return new Document()
    //             .append("property_id", homeSale.propertyId)
    //             .append("post_code", homeSale.postCode)
    //             .append(FIELD_PURCHASE_PRICE, homeSale.purchasePrice)
    //             .append("download_date", homeSale.downloadDate)
    //             .append("council_name", homeSale.councilName)
    //             .append("address", homeSale.address)
    //             .append("property_type", homeSale.propertyType)
    //             .append("strata_lot_number", homeSale.strataLotNumber)
    //             .append("property_name", homeSale.propertyName)
    //             .append("area", homeSale.area)
    //             .append("area_type", homeSale.areaType)
    //             .append("contract_data", homeSale.contractData)
    //             .append("settlement_date", homeSale.settlementDate)
    //             .append("zoning", homeSale.zoning)
    //             .append("nature_of_property", homeSale.natureOfProperty)
    //             .append("primary_purpose", homeSale.primaryPurpose)
    //             .append("legal_description", homeSale.legalDescription)
    //             .append("property_accessed_count", homeSale.propertyAccessedCount)
    //             .append("post_code_accessed_count", homeSale.postCodeAccessedCount);
    // }

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

    // OLD MONGODB HELPER METHOD - COMMENTED OUT
    // private HomeSale documentToHomeSale(final Document doc) {
    //     return new HomeSale(
    //            parseToInt(doc.get("property_id")),
    //            doc.getString("download_date"),
    //            doc.getString("council_name"),
    //            parseToInt(doc.get(FIELD_PURCHASE_PRICE)),
    //            doc.getString("address"),
    //            parseToInt(doc.get("post_code")),
    //            doc.getString("property_type"),
    //            parseToString(doc.get("strata_lot_number")),
    //            doc.getString("property_name"),
    //            parseToInt(doc.get("area")),
    //            doc.getString("area_type"),
    //            doc.getString("contract_data"),
    //            doc.getString("settlement_date"),
    //            doc.getString("zoning"),
    //            parseToString(doc.get("nature_of_property")),
    //            doc.getString("primary_purpose"),
    //            parseToString(doc.get("legal_description")),
    //            parseToInt(doc.get("property_accessed_count")),
    //            parseToInt(doc.get("post_code_accessed_count"))
    //    );
    // }

    public boolean newSale(HomeSale homeSale) {
        boolean success = false;
        int postCodeAccessedCount = 0;

        try {
            postCodeAccessedCount = getSalesByPostCode(String.valueOf(homeSale.postCode), false).get(0).postCodeAccessedCount;
        } catch (Exception e) {
            postCodeAccessedCount = 0;
        }

        homeSale.postCodeAccessedCount = postCodeAccessedCount;

        // OLD MONGODB CODE - COMMENTED OUT
        // try {
        //     final Document doc = homeSaleToDocument(homeSale);
        //     collection.insertOne(doc);
        //     success = true;
        // } catch (MongoException e) {
        //     success = false;
        // }

        // NEW JDBC CODE
        try {
            String sql = createInsertSQL(homeSale);
            PreparedStatement pstmt = getConnection().prepareStatement(sql);
            setHomeSaleParameters(pstmt, homeSale);
            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;
            pstmt.close();
        } catch (SQLException e) {
            success = false;
        }
        return success;
    }

    public Optional<HomeSale> handleSaleByID(final String propertyID) {
        Optional<HomeSale> result = Optional.empty();

        // OLD MONGODB CODE - COMMENTED OUT
        // try {
        //     final Document doc = collection.find(Filters.eq("property_id", parseToInt(propertyID))).first();
        //     if (doc != null) {
        //         result = Optional.of(documentToHomeSale(doc));
        //         collection.updateOne(Filters.eq("property_id", parseToInt(propertyID)), new Document("$inc", new Document("property_accessed_count", 1)));
        //     }
        // } catch (MongoException e) {
        //     result = Optional.empty();
        // }

        // NEW JDBC CODE
        try {
            int id = parseToInt(propertyID);
            System.out.println("Looking up property_id = " + id); // debug add

            String sql = "SELECT * FROM sales WHERE property_id = ?";
            PreparedStatement pstmt = getConnection().prepareStatement(sql);
            pstmt.setInt(1, parseToInt(propertyID));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Found sale for ID: " + id); //
                result = Optional.of(resultSetToHomeSale(rs));

                // Update access count
                String updateSql = "UPDATE sales SET property_accessed_count = property_accessed_count + 1 WHERE property_id = ?";
                PreparedStatement updateStmt = getConnection().prepareStatement(updateSql);
                updateStmt.setInt(1, parseToInt(propertyID));
                updateStmt.executeUpdate();
                updateStmt.close();
            } else {
                System.out.println("No sale found for ID: " + id); // <-- ADD THIS
            }


            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("SQL Error in handleSaleByID: " + e.getMessage());
            result = Optional.empty();
        }

        return result;
    }

    // returns a List of homesales  in a given postCode
    public List<HomeSale> getSalesByPostCode(final String postCode, final boolean incrementAccessCount) {
        List<HomeSale> sales = new ArrayList<>();

        // OLD MONGODB CODE - COMMENTED OUT
        // try {
        //     int previousPostCodeAccessedCount = collection.find(Filters.eq("post_code", parseToInt(postCode))).first().getInteger("post_code_accessed_count");
        //     List<HomeSale> finalSales = sales;
        //     collection.find(Filters.eq("post_code", parseToInt(postCode)))
        //         .forEach(doc -> finalSales.add(documentToHomeSale(doc)));
        //     if (incrementAccessCount) {
        //         collection.updateMany(Filters.eq("post_code", parseToInt(postCode)), new Document("$set", new Document("post_code_accessed_count", previousPostCodeAccessedCount + 1)));
        //     }
        // } catch (MongoException e) {
        //     sales = Collections.emptyList(); // Reset to empty list on error
        // }

        // NEW JDBC CODE
        try {
            // Get previous access count
            int previousPostCodeAccessedCount = 0;
            String countSql = "SELECT post_code_accessed_count FROM sales WHERE post_code = ? LIMIT 1";
            PreparedStatement countStmt = getConnection().prepareStatement(countSql);
            countStmt.setInt(1, parseToInt(postCode));
            ResultSet countRs = countStmt.executeQuery();
            if (countRs.next()) {
                previousPostCodeAccessedCount = countRs.getInt("post_code_accessed_count");
            }
            countRs.close();
            countStmt.close();

            // Get all sales for post code
            String sql = "SELECT * FROM sales WHERE post_code = ?";
            PreparedStatement pstmt = getConnection().prepareStatement(sql);
            pstmt.setInt(1, parseToInt(postCode));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sales.add(resultSetToHomeSale(rs));
            }

            // Update access count if needed
            if (incrementAccessCount) {
                String updateSql = "UPDATE sales SET post_code_accessed_count = ? WHERE post_code = ?";
                PreparedStatement updateStmt = getConnection().prepareStatement(updateSql);
                updateStmt.setInt(1, previousPostCodeAccessedCount + 1);
                updateStmt.setInt(2, parseToInt(postCode));
                updateStmt.executeUpdate();
                updateStmt.close();
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            sales = Collections.emptyList(); // Reset to empty list on error
        }
        return sales;
    }

    // returns the individual prices for all sales. Potentially large
    public List<String> handleAllSalePrices() {
        List<String> prices = new ArrayList<>();

        // OLD MONGODB CODE - COMMENTED OUT
        // try {
        //     List<String> finalPrices = prices;
        //     collection.find()
        //         .forEach(doc -> finalPrices.add(doc.getString(FIELD_PURCHASE_PRICE)));
        // } catch (MongoException e) {
        //     prices = Collections.emptyList(); // Reset to empty list on error
        // }

        // NEW JDBC CODE
        try {
            String sql = "SELECT purchase_price FROM sales";
            PreparedStatement pstmt = getConnection().prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                prices.add(String.valueOf(rs.getInt("purchase_price")));
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            prices = Collections.emptyList(); // Reset to empty list on error
        }

        return prices;
    }

    // returns all home sales. Potentially large
    public List<HomeSale> handleAllSales() {
        List<HomeSale> sales = new ArrayList<>();

        // OLD MONGODB CODE - COMMENTED OUT
        // try {
        //     List<HomeSale> finalSales = sales;
        //     collection.find()
        //         .forEach(doc -> finalSales.add(documentToHomeSale(doc)));
        // } catch (MongoException e) {
        //     sales = Collections.emptyList();
        // }

        // NEW JDBC CODE
        try {
            String sql = "SELECT * FROM sales";
            System.out.println("Executing SQL: " + sql);
            PreparedStatement pstmt = getConnection().prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                sales.add(resultSetToHomeSale(rs));
                count++;
            }
            System.out.println("Found " + count + " sales in database");

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("SQL Exception in handleAllSales: " + e.getMessage());
            e.printStackTrace();
            sales = Collections.emptyList();
        }

        return sales;
    }

    // gets the average price for a given date range
    public double handleAveragePriceByDateRange(final String startDate, final String endDate) {
        List<HomeSale> sales = new ArrayList<>();
        double result = 0.0; // default value

        // OLD MONGODB CODE - COMMENTED OUT
        // try {
        //     collection.find(Filters.and(Filters.gte("contract_date", startDate), Filters.lte("contract_date", endDate)))
        //         .forEach(doc -> sales.add(documentToHomeSale(doc)));

        //     if (!sales.isEmpty()) {
        //         double averagePrice = 0.0;
        //         for (final HomeSale sale : sales) {
        //             averagePrice += sale.purchasePrice;
        //         }

        //         averagePrice /= sales.size();
        //         result = Math.round(averagePrice * 100.0) / 100.0;
        //     }
        // } catch (MongoException e) {
        //     result = 0.0;
        // }

        // NEW JDBC CODE
        try {
            String sql = "SELECT * FROM sales WHERE contract_date >= ? AND contract_date <= ?";
            PreparedStatement pstmt = getConnection().prepareStatement(sql);
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sales.add(resultSetToHomeSale(rs));
            }

            if (!sales.isEmpty()) {
                double averagePrice = 0.0;
                for (final HomeSale sale : sales) {
                    averagePrice += sale.purchasePrice;
                }

                averagePrice /= sales.size();
                result = Math.round(averagePrice * 100.0) / 100.0;
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            result = 0.0;
        }

        return result;
    }

    // returns a list of sales under a given price
    public List<HomeSale> handleSalesUnderPrice(final int price) {

        List<HomeSale> sales = new ArrayList<>();

        // OLD MONGODB CODE - COMMENTED OUT
        // try {
        //     List<HomeSale> finalSales = sales;
        //     collection.find(Filters.lt(FIELD_PURCHASE_PRICE, price))
        //         .forEach(doc -> finalSales.add(documentToHomeSale(doc)));
        // } catch (MongoException e) {
        //     sales = Collections.emptyList();
        // }

        // NEW JDBC CODE
        try {
            String sql = "SELECT * FROM sales WHERE purchase_price < ?";
            PreparedStatement pstmt = getConnection().prepareStatement(sql);
            pstmt.setInt(1, price);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sales.add(resultSetToHomeSale(rs));
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            sales = Collections.emptyList();
        }

        return sales;
    }

    // returns the number of times a postcode has been accessed
    public int getPostCodeAccessedCount(final String postCode) {
        int result = 0;

        // OLD MONGODB CODE - COMMENTED OUT
        // try {
        //     result = collection.find(Filters.eq("post_code", parseToInt(postCode))).first().getInteger("post_code_accessed_count");
        // } catch (Exception e) {
        //     result = 0;
        // }

        // NEW JDBC CODE
        try {
            String sql = "SELECT post_code_accessed_count FROM sales WHERE post_code = ? LIMIT 1";
            PreparedStatement pstmt = getConnection().prepareStatement(sql);
            pstmt.setInt(1, parseToInt(postCode));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                result = rs.getInt("post_code_accessed_count");
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            result = 0;
        }
        return result;
    }

    // returns the number of times a property has been accessed
    public int getPropertyAccessedCount(final String propertyID) {
        int result = 0;

        // OLD MONGODB CODE - COMMENTED OUT
        // try {
        //     result = collection.find(Filters.eq("property_id", parseToInt(propertyID))).first().getInteger("property_accessed_count");
        // } catch (Exception e) {
        //     result = 0;
        // }

        // NEW JDBC CODE
        try {
            String sql = "SELECT property_accessed_count FROM sales WHERE property_id = ? LIMIT 1";
            PreparedStatement pstmt = getConnection().prepareStatement(sql);
            pstmt.setInt(1, parseToInt(propertyID));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                result = rs.getInt("property_accessed_count");
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            result = 0;
        }
        return result;
    }
}
