package app;

import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import sales.PropertyDAO;
import sales.PropertyController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.UseUtilityClass", "PMD.LawOfDemeter"})

public class PropertyServer {
    private static final Logger logger = LoggerFactory.getLogger(PropertyServer.class);
    public static void main(String[] args) {
        // in memory test data store
        final var sales = new PropertyDAO();
      
        // API implementation
        final PropertyController salesHandler = new PropertyController(sales);

        // start Javalin on port 7071
        Javalin.create(config -> {
            config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                pluginConfig.withDefinitionConfiguration((version, definition) -> {
                    definition.withOpenApiInfo(info -> info.setTitle("Property Server Docs"));
                });
            }));
            config.registerPlugin(new SwaggerPlugin(uiConfig ->
                    uiConfig.setUiPath("/docs/swagger")));
            config.registerPlugin(new ReDocPlugin(uiConfig ->
                    uiConfig.setUiPath("/docs/redoc")));

            // configure endpoint handlers to process HTTP requests
            config.router.apiBuilder(() -> {
                ApiBuilder.path("properties", () -> {
                    // get all sales records - could be big!
                    ApiBuilder.get(salesHandler::handleAllSales);
                    // create a new sales record
                    ApiBuilder.post(salesHandler::createSale);

                    // return a sale by sale ID
                    ApiBuilder.path("{propertyID}", () -> {
                        ApiBuilder.get(ctx -> salesHandler.handleSaleByID(ctx, ctx.pathParam("propertyID")));
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
                });
            });
        }).start(7071);
        
        // Add shutdown hook to properly close RabbitMQ service
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down property server...");
            salesHandler.close();
        }));
    }
} 