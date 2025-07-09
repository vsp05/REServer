package app;

import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import sales.AnalyticsDAO;
import sales.AnalyticsController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.UseUtilityClass", "PMD.LawOfDemeter"})

public class AnalyticsServer {
    public static void main(String[] args) {
        // analytics data store
        final var analytics = new AnalyticsDAO();
      
        // API implementation
        final AnalyticsController analyticsHandler = new AnalyticsController(analytics);

        // start Javalin on port 7072
        Javalin.create(config -> {
            config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                pluginConfig.withDefinitionConfiguration((version, definition) -> {
                    definition.withOpenApiInfo(info -> info.setTitle("Analytics Server Docs"));
                });
            }));
            config.registerPlugin(new SwaggerPlugin(uiConfig ->
                    uiConfig.setUiPath("/docs/swagger")));
            config.registerPlugin(new ReDocPlugin(uiConfig ->
                    uiConfig.setUiPath("/docs/redoc")));

            // configure endpoint handlers to process HTTP requests
            config.router.apiBuilder(() -> {
                ApiBuilder.path("analytics", () -> {
                    // Increment property access count
                    ApiBuilder.path("property/{propertyID}", () -> {
                        ApiBuilder.path("increment", () -> {
                            ApiBuilder.post(ctx -> analyticsHandler.incrementPropertyAccess(ctx, ctx.pathParam("propertyID")));
                        });
                        ApiBuilder.path("count", () -> {
                            ApiBuilder.get(ctx -> analyticsHandler.getPropertyAccessCount(ctx, ctx.pathParam("propertyID")));
                        });
                    });

                    // Increment postcode access count
                    ApiBuilder.path("postcode/{postcode}", () -> {
                        ApiBuilder.path("increment", () -> {
                            ApiBuilder.post(ctx -> analyticsHandler.incrementPostCodeAccess(ctx, ctx.pathParam("postcode")));
                        });
                        ApiBuilder.path("count", () -> {
                            ApiBuilder.get(ctx -> analyticsHandler.getPostCodeAccessCount(ctx, ctx.pathParam("postcode")));
                        });
                    });
                });
            });
        }).start(7072);
    }
} 