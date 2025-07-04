package sales;

import io.javalin.http.Context;

import java.util.List;
import java.util.Optional;

public class SalesController {

    private final SalesDAO homeSales;

    public SalesController(final SalesDAO homeSales) {
        this.homeSales = homeSales;
    }

    // implements POST /sales
    public void createSale(final Context ctx) {

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
    public void getAllSales(final Context ctx) {
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
    public void getSaleByID(final Context ctx, final String id) {

        Optional<HomeSale> sale = homeSales.getSaleById(id);
        sale.map(ctx::json)
                .orElseGet (() -> error (ctx, "Sale not found", 404));

    }

    // Implements GET /sales/postcode/{postcodeID}
    public void findSaleByPostCode(final Context ctx, final String postCode) {
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
    public void getAveragePriceByDateRange(final Context ctx, final String startDate, final String endDate) {
        double averagePrice = homeSales.getAveragePriceByDateRange(startDate, endDate);
        if (averagePrice == 0.0) {
            ctx.result("No prices found for date range. Try formatting dates as YYYY-MM-DD");
            ctx.status(404);
        } else {
            ctx.json(averagePrice);
            ctx.status(200);
        }
    }

    // implements GET /sales/under/{price}
    public void getSalesUnderPrice(final Context ctx, final String price) {
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


    private Context error(final Context ctx, final String msg, final int code) {
        ctx.result(msg);
        ctx.status(code);
        return ctx;
    }



    


}
