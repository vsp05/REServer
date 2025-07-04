package sales;

import io.javalin.http.Context;

import java.util.List;
import java.util.Optional;

public class SalesController {

    // errorprone: Avoid Literals In If Condition
    private static final double DEFAULT_AVERAGE_PRICE = 0.0;
    private static final String NO_PRICES_MSG = "No prices found for date range. Try formatting dates as YYYY-MM-DD";

    private SalesDAO homeSales;

    public SalesController(SalesDAO homeSales) {
        this.homeSales = homeSales;
    }

    // implements POST /sales
    public void createSale(Context ctx) {

        // Extract Home Sale from request body
        // TO DO override Validator exception method to report better error message
        HomeSale sale = ctx.bodyValidator(HomeSale.class)
                            .get();

        // store new sale in data set
        if (homeSales.newSale(sale)) {
            ctx.result("Sale Created");
            ctx.status(201);
        } else {
            ctx.result("Failed to add sale");
            ctx.status(400);
        }
    }

    // implements Get /sales
    public void getAllSales(Context ctx) {
        List <HomeSale> allSales = homeSales.getAllSales();
        if (allSales.isEmpty()) {
            ctx.result("No Sales Found");
            ctx.status(404);
        } else {
            ctx.json(allSales);
            ctx.status(200);
        }
    }

    // implements GET /sales/{saleID}
    public void getSaleByID(Context ctx, String id) {

        Optional<HomeSale> sale = homeSales.getSaleById(id);
        sale.map(ctx::json)
                .orElseGet (() -> error (ctx, "Sale not found", 404));

    }

    // Implements GET /sales/postcode/{postcodeID}
    public void findSaleByPostCode(Context ctx, String postCode) {
        List<HomeSale> sales = homeSales.getSalesByPostCode(postCode);
        if (sales.isEmpty()) {
            ctx.result("No sales for postcode found");
            ctx.status(404);
        } else {
            ctx.json(sales);
            ctx.status(200);
        }
    }

    // implements GET /average-price/dates/{startDate}/{endDate}
    // format dates as YYYY-MM-DD
    public void getAveragePriceByDateRange(Context ctx, String startDate, String endDate) {
        double averagePrice = homeSales.getAveragePriceByDateRange(startDate, endDate);
        if (averagePrice == DEFAULT_AVERAGE_PRICE) {
            ctx.result(NO_PRICES_MSG);
            ctx.status(404);
        } else {
            ctx.json(averagePrice);
            ctx.status(200);
        }
    }

    // implements GET /sales/under/{price}
    public void getSalesUnderPrice(Context ctx, String price) {
        int priceInt = Integer.parseInt(price);

        if (priceInt <= 0) {
            ctx.result("Invalid price specified");
            ctx.status(400);
            return;
        }

        // Get list of sales under the specified price
        List<HomeSale> totalSales = homeSales.getSalesUnderPrice(priceInt);

        if (totalSales.isEmpty()) {
            ctx.result("No sales under price found");
            ctx.status(404);
        } else {
            ctx.json(totalSales);
            ctx.status(200);
        }
    }


    private Context error(Context ctx, String msg, int code) {
        ctx.result(msg);
        ctx.status(code);
        return ctx;
    }



    


}
