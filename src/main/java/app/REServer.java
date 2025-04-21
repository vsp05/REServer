package app;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import sales.SalesDAO;
import sales.SalesController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import io.javalin.openapi.plugin.OpenApiConfiguration;
//import io.javalin.openapi.plugin.OpenApiPlugin;
//import io.javalin.openapi.plugin.redoc.ReDocConfiguration;
//import io.javalin.openapi.plugin.redoc.ReDocPlugin;
//import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
//import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

//import static io.javalin.apibuilder.ApiBuilder.*;


public class REServer {
        private static final Logger LOG = LoggerFactory.getLogger(REServer.class);

        public static void main(String[] args) {

            LOG.info("hello {}!","world");


            // in memory test data store
            var sales = new SalesDAO();

            // API implementation
            SalesController salesHandler = new SalesController(sales);

            // start Javalin on port 7070
            var app = Javalin.create()
                    .get("/", ctx -> ctx.result("Real Estate server is running"))
                    .start(7070);

            // configure endpoint handlers to process HTTP requests
            JavalinConfig config = new JavalinConfig();
            config.router.apiBuilder(() -> {
                // Sales records are immutable hence no PUT and DELETE

                // return a sale by sale ID
                app.get("/sales/{saleID}", ctx -> {
                    salesHandler.getSaleByID(ctx, ctx.pathParam("saleID"));
                });
                // get all sales records - could be big!
                app.get("/sales", ctx -> {
                    salesHandler.getAllSales(ctx);
                });
                // create a new sales record
                app.post("/sales", ctx -> {
                    salesHandler.createSale(ctx);
                });
                // Get all sales for a specified postcode
                app.get("/sales/postcode/{postcode}", ctx -> {
                    salesHandler.findSaleByPostCode(ctx, ctx.pathParam("postcode"));
                });
            });


        }
}


