package app;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import sales.SalesDAO;
import sales.SalesController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class REServer {
        private static final Logger LOG = LoggerFactory.getLogger(REServer.class);

        public static void main(String[] args) {

            var sales = new SalesDAO();
            SalesController salesHandler = new SalesController(sales);

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

                config.router.apiBuilder(() -> {
                    ApiBuilder.path("sales", () -> {
                        ApiBuilder.get(salesHandler::getAllSales);
                        ApiBuilder.post(salesHandler::createSale);

                        ApiBuilder.path("{saleID}", () -> {
                            ApiBuilder.get(ctx -> salesHandler.getSaleByID(ctx, ctx.pathParam("saleID")));
                        });

                        ApiBuilder.path("postcode/{postcode}", () -> {
                            ApiBuilder.get(ctx -> salesHandler.findSaleByPostCode(ctx, ctx.pathParam("postcode")));
                        });

                        // Get average price for a specified date range
                        // format dates as YYYY-MM-DD
                        ApiBuilder.path("/average-price/dates/{startDate}/{endDate}", () -> {
                            ApiBuilder.get(ctx -> salesHandler.getAveragePriceByDateRange(ctx, ctx.pathParam("startDate"), ctx.pathParam("endDate")));
                        });

                        // Get list of sales under a specified price
                        ApiBuilder.path("/sales/under/{price}", () -> {
                            ApiBuilder.get(ctx -> salesHandler.getSalesUnderPrice(ctx, ctx.pathParam("price")));
                        });
                    });
                });
            }).start(7070);
        };
}


