package sales;

import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;

public class AnalyticsController {

    private final AnalyticsDAO analytics;

    public AnalyticsController(final AnalyticsDAO analytics) {
        this.analytics = analytics;
    }

    @OpenApi(
            path = "/analytics/property/{propertyID}/increment",
            methods = HttpMethod.POST,
            summary = "Increment property access count",
            operationId = "incrementPropertyAccess",
            tags = {"Analytics"},
            pathParams = {
                    @OpenApiParam(name = "propertyID", description = "ID of the property to increment access count")
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Access count incremented"),
                    @OpenApiResponse(status = "400", description = "Failed to increment access count")
            }
    )
    public void incrementPropertyAccess(final Context ctx, final String propertyID) {
        if (analytics.incrementPropertyAccessCount(propertyID)) {
            ctx.result("Property access count incremented");
            ctx.status(200);
        } else {
            ctx.result("Failed to increment property access count");
            ctx.status(400);
        }
    }

    @OpenApi(
            path = "/analytics/postcode/{postcode}/increment",
            methods = HttpMethod.POST,
            summary = "Increment postcode access count",
            operationId = "incrementPostCodeAccess",
            tags = {"Analytics"},
            pathParams = {
                    @OpenApiParam(name = "postcode", description = "Postcode to increment access count")
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Access count incremented"),
                    @OpenApiResponse(status = "400", description = "Failed to increment access count")
            }
    )
    public void incrementPostCodeAccess(final Context ctx, final String postcode) {
        if (analytics.incrementPostCodeAccessCount(postcode)) {
            ctx.result("Postcode access count incremented");
            ctx.status(200);
        } else {
            ctx.result("Failed to increment postcode access count");
            ctx.status(400);
        }
    }

    @OpenApi(
            path = "/analytics/property/{propertyID}/count",
            methods = HttpMethod.GET,
            summary = "Get property access count",
            operationId = "getPropertyAccessCount",
            tags = {"Analytics"},
            pathParams = {
                    @OpenApiParam(name = "propertyID", description = "ID of the property to get access count")
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Access count returned")
            }
    )
    public void getPropertyAccessCount(final Context ctx, final String propertyID) {
        final int count = analytics.getPropertyAccessedCount(propertyID);
        ctx.json(count);
        ctx.status(200);
    }

    @OpenApi(
            path = "/analytics/postcode/{postcode}/count",
            methods = HttpMethod.GET,
            summary = "Get postcode access count",
            operationId = "getPostCodeAccessCount",
            tags = {"Analytics"},
            pathParams = {
                    @OpenApiParam(name = "postcode", description = "Postcode to get access count")
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Access count returned")
            }
    )
    public void getPostCodeAccessCount(final Context ctx, final String postcode) {
        final int count = analytics.getPostCodeAccessedCount(postcode);
        ctx.json(count);
        ctx.status(200);
    }
} 