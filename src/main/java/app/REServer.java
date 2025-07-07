package app;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
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
            Javalin.create(config -> {
                config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                    pluginConfig.withDefinitionConfiguration((version, definition) -> {
                        definition.withOpenApiInfo(info -> info.setTitle("Real Estate Server Docs"));
                    });
                }));
                config.registerPlugin(new SwaggerPlugin(uiConfig ->
                        uiConfig.setUiPath("/docs/swagger")));
                config.registerPlugin(new ReDocPlugin(uiConfig ->
                        uiConfig.setUiPath("/docs/redoc")));

                // configure endpoint handlers to process HTTP requests
                config.router.apiBuilder(() -> {
                    ApiBuilder.path("sales", () -> {
                        // get all sales records - could be big!
                        ApiBuilder.get(salesHandler::handleAllSales);
                        // create a new sales record
                        ApiBuilder.post(salesHandler::createSale);

                        // return a sale by sale ID
                        ApiBuilder.path("{saleID}", () -> {
                            ApiBuilder.get(ctx -> salesHandler.handleSaleByID(ctx, ctx.pathParam("saleID")));
                        });

                        // Get all sales for a specified postcode
                        ApiBuilder.path("postcode/{postcode}", () -> {
                            ApiBuilder.get(ctx -> salesHandler.findSaleByPostCode(ctx, ctx.pathParam("postcode")));
                        });

                        // Get average price for a specified date range
                        // format dates as YYYY-MM-DD
                        ApiBuilder.path("/average-price/dates/{startDate}/{endDate}", () -> {
                            ApiBuilder.get(ctx -> salesHandler.handleAveragePriceByDateRange(ctx, ctx.pathParam("startDate"), ctx.pathParam("endDate")));
                        });

                        // Get list of sales under a specified price
                        ApiBuilder.path("/under/{price}", () -> {
                            ApiBuilder.get(ctx -> salesHandler.handleSalesUnderPrice(ctx, ctx.pathParam("price")));
                        });

                        // Get the number of times a postcode has been accessed
                        ApiBuilder.path("/postcode/{postcode}/accessed-count", () -> {
                            ApiBuilder.get(ctx -> salesHandler.handlePostCodeAccessedCount(ctx, ctx.pathParam("postcode")));
                        });

                        // Get the number of times a property has been accessed
                        ApiBuilder.path("/property/{propertyID}/accessed-count", () -> {
                            ApiBuilder.get(ctx -> salesHandler.handlePropertyAccessedCount(ctx, ctx.pathParam("propertyID")));
                        });
                    });
                });
            }).start(7070);
        };
}


