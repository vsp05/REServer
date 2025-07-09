package app;
import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.apibuilder.ApiBuilder;
import query.QueryController;

public class QueryServer {
    public static void main(String[] args) {
        // start Javalin on port 8080
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
                    ApiBuilder.get(ctx -> QueryController.getSales(ctx));

                    ApiBuilder.post(ctx -> QueryController.createSale(ctx));

                    ApiBuilder.path("{saleID}", () -> {
                        ApiBuilder.get(ctx -> QueryController.getSaleByID(ctx, ctx.pathParam("saleID")));
                    });

                    ApiBuilder.path("postcode/{postcode}", () -> {
                        ApiBuilder.get(ctx -> QueryController.getSalesByPostcode(ctx, ctx.pathParam("postcode")));
                    });

                    ApiBuilder.path("average-price/dates/{startDate}/{endDate}", () -> {
                        ApiBuilder.get(ctx -> QueryController.getAveragePriceByDateRange(ctx, ctx.pathParam("startDate"), ctx.pathParam("endDate")));
                    });

                    ApiBuilder.path("/under/{price}", () -> {
                        ApiBuilder.get(ctx -> QueryController.getSalesUnderPrice(ctx, ctx.pathParam("price")));
                    });

                    ApiBuilder.path("/accessed-count/postcode/{postcode}", () -> {
                        ApiBuilder.get(ctx -> QueryController.getPostcodeAccessedCount(ctx, ctx.pathParam("postcode")));
                    });

                    ApiBuilder.path("/accessed-count/{propertyID}", () -> {
                        ApiBuilder.get(ctx -> QueryController.getPropertyAccessedCount(ctx, ctx.pathParam("propertyID")));
                    });
                });
            });
        }).start(8080);
    }
}
