package app;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.config.RouterConfig;
import sales.SalesDAO;
import sales.SalesController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.UseUtilityClass", "PMD.LawOfDemeter"})


public class REServer {
        public static void main(String[] args) {

            // in memory test data store
            final var sales = new SalesDAO();

            // API implementation
            final SalesController salesHandler = new SalesController(sales);

            // start Javalin on port 7070
            final var app = Javalin.create()
                    .get("/", ctx -> ctx.result("Real Estate server is running"))
                    .start(7070);

            // configure endpoint handlers to process HTTP requests
            final JavalinConfig config = new JavalinConfig();
            RouterConfig router = config.router;
            router.apiBuilder(() -> {
                // Sales records are immutable hence no PUT and DELETE

                // return a sale by sale ID
                app.get("/sales/{saleID}", ctx -> {
                    salesHandler.handleSaleByID(ctx, ctx.pathParam("saleID"));
                });
                // get all sales records - could be big!
                app.get("/sales", ctx -> {
                    salesHandler.handleAllSales(ctx);
                });
                // create a new sales record
                app.post("/sales", ctx -> {
                    salesHandler.createSale(ctx);
                });
                // Get all sales for a specified postcode
                app.get("/sales/postcode/{postcode}", ctx -> {
                    salesHandler.findSaleByPostCode(ctx, ctx.pathParam("postcode"));
                });
                // Get average price for a specified date range
                // format dates as YYYY-MM-DD
                app.get("/average-price/dates/{startDate}/{endDate}", ctx -> {
                    salesHandler.handleAveragePriceByDateRange(ctx, ctx.pathParam("startDate"), ctx.pathParam("endDate"));
                });
                // Get list of sales under a specified price
                app.get("/sales/under/{price}", ctx -> {
                    salesHandler.handleSalesUnderPrice(ctx, ctx.pathParam("price"));
                }); 
            });


        }
}


