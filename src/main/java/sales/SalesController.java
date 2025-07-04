package sales;

import io.javalin.http.Context;

import java.util.List;
import java.util.Optional;

public class SalesController {

    private SalesDAO homeSales;

    public SalesController(SalesDAO homeSales) {
        this.homeSales = homeSales;
    }

    // implements POST /sales
    public void createSale(Context ctx) {

        // Extract Home Sale from request body
        // TO DO override Validator exception method to report better error message
        final HomeSale sale = ctx.bodyValidator(HomeSale.class)
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
    public void handleAllSales(Context ctx) {
        final List <HomeSale> allSales = homeSales.handleAllSales();
        if (allSales.isEmpty()) {
            ctx.result("No Sales Found");
            ctx.status(404);
        } else {
            ctx.json(allSales);
            ctx.status(200);
        }
    }

    // implements GET /sales/{saleID}
    public void handleSaleByID(Context ctx, String id) { //NOPMD - suppressed ShortVariable - TODO explain reason for suppression

        final Optional<HomeSale> sale = homeSales.handleSaleByID(id);
        sale.map(ctx::json)
                .orElseGet (() -> error (ctx, "Sale not found", 404));

    }

    // Implements GET /sales/postcode/{postcodeID}
    public void findSaleByPostCode(Context ctx, String postCode) {
        final List<HomeSale> sales = homeSales.getSalesByPostCode(postCode);
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
    public void handleAveragePriceByDateRange(Context ctx, String startDate, String endDate) {
        final double averagePrice = homeSales.handleAveragePriceByDateRange(startDate, endDate);
        if (averagePrice == 0.0) {
            ctx.result("No prices found for date range. Try formatting dates as YYYY-MM-DD");
            ctx.status(404);
        } else {
            ctx.json(averagePrice);
            ctx.status(200);
        }
    }

    // implements GET /sales/under/{price}
    public void handleSalesUnderPrice(Context ctx, String price) {
        final int priceInt = Integer.parseInt(price);

        if (priceInt <= 0) {
            ctx.result("Invalid price specified");
            ctx.status(400);
            return;
        }

        // Get list of sales under the specified price
        final List<HomeSale> totalSales = homeSales.handleSalesUnderPrice(priceInt);

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
