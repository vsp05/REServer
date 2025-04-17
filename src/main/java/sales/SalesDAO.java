package sales;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SalesDAO {

    private  List<HomeSale> sales = new ArrayList<>();


    public SalesDAO() {

        sales.add(new HomeSale("0", "2257", "2000000"));
        sales.add(new HomeSale("1", "2262", "1300000"));
        sales.add(new HomeSale("2", "2000", "4000000"));
        sales.add(new HomeSale("3", "2000", "1000000"));
      }

    public boolean newSale (HomeSale homeSale){
            sales.add(homeSale);
            return true;
    }

    public Optional<HomeSale> getSaleById(String saleID) {
        System.out.println("id is " + saleID);
        for (HomeSale u : sales) {
            if (u.saleID.equals(saleID)) {
                System.out.println("id found ");
                return Optional.of(u);
             }
        }
        return Optional.empty();
    }

    public Optional<List<HomeSale>> getSalesByPostCode(String postCode) {
        System.out.println("postcode is: " + postCode);
        List<HomeSale> tmp = new ArrayList<>();
        for (HomeSale u : sales) {
            if (u.postcode.equals(postCode)) {
                tmp.add(u);
                System.out.println("postcode found ");
            }
        }
        if (tmp.isEmpty()) {
            return Optional.empty();
        } else
         return Optional.of(tmp);
    }

    public List<String> getAllSalePrices() {
        return   sales.stream()
                .map(e -> e.salePrice)
                .collect(Collectors.toList());
    }

    public List<HomeSale> getAllSales() {
        return   sales.stream().collect(Collectors.toList());
    }

}
