package sales;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SalesDAO {

    // List to hold data loaded from CSV
    private List<HomeSale> sales = new ArrayList<>();
    private static final String CSV_FILE_PATH = "src/main/java/app/nsw_property_data.csv";

    public SalesDAO() {
        loadSalesFromCSV();
    }

    /**
     * Loads sales data from the CSV file
     */
    private void loadSalesFromCSV() {
        try {
            // Read the CSV file
            try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
                String line;
                boolean isFirstLine = true;
                
                while ((line = br.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    
                    String[] values = line.split(",");
                    if (values.length >= 3) {
                        String saleID = values[0].trim();
                        String postcode = values[6].trim();
                        String salePrice = values[3].trim();
                        
                        sales.add(new HomeSale(saleID, postcode, salePrice));
                    }
                }
                
                System.out.println("Successfully loaded " + sales.size() + " sales from CSV file.");
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            System.err.println("Using default test data instead.");
        }
    }

    public boolean newSale (HomeSale homeSale){
            sales.add(homeSale);
            return true;
    }

    // returns Optional wrapping a HomeSale if id is found, empty Optional otherwise
    public Optional<HomeSale> getSaleById(String saleID) {

        for (HomeSale u : sales) {
            if (u.saleID.equals(saleID)) {
                System.out.println("id found ");
                return Optional.of(u);
             }
        }
        return Optional.empty();
    }

    // returns a List of homesales  in a given postCode
    public List<HomeSale> getSalesByPostCode(String postCode) {
        System.out.println("postcode is: " + postCode);
        List<HomeSale> tmp = new ArrayList<>();
         for (HomeSale u : sales) {
            if (u.postcode.equals(postCode)) {
                tmp.add(u);
                System.out.println("postcode found ");
            }
        }
        return tmp == null ? Collections.emptyList() : tmp;
    }

    // returns the individual prices for all sales. Potentially large
    public List<String> getAllSalePrices() {
        return   sales.stream()
                .map(e -> e.salePrice)
                .collect(Collectors.toList());
    }

    // returns all home sales. Potentially large
    public List<HomeSale> getAllSales() {
        return   sales.stream().collect(Collectors.toList());
    }

}
