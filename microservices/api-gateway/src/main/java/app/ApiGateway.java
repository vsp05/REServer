package app;

import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import shared.HomeSale;

import java.io.IOException;
import java.util.List;

@SuppressWarnings({"PMD.UseUtilityClass", "PMD.LawOfDemeter"})

public class ApiGateway {
    
    private static final String PROPERTY_SERVER_URL = "http://localhost:7071";
    private static final String ANALYTICS_SERVER_URL = "http://localhost:7072";
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        // start Javalin on port 7070
        Javalin.create(config -> {
            config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                pluginConfig.withDefinitionConfiguration((version, definition) -> {
                    definition.withOpenApiInfo(info -> info.setTitle("API Gateway Docs"));
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
                    ApiBuilder.get(ApiGateway::handleAllSales);
                    // create a new sales record
                    ApiBuilder.post(ApiGateway::createSale);

                    // return a sale by sale ID
                    ApiBuilder.path("{saleID}", () -> {
                        ApiBuilder.get(ctx -> ApiGateway.handleSaleByID(ctx, ctx.pathParam("saleID")));
                    });

                    // Get all sales for a specified postcode
                    ApiBuilder.path("postcode/{postcode}", () -> {
                        ApiBuilder.get(ctx -> ApiGateway.findSaleByPostCode(ctx, ctx.pathParam("postcode")));
                    });

                    // Get average price for a specified date range
                    // format dates as YYYY-MM-DD
                    ApiBuilder.path("/average-price/dates/{startDate}/{endDate}", () -> {
                        ApiBuilder.get(ctx -> ApiGateway.handleAveragePriceByDateRange(ctx, ctx.pathParam("startDate"), ctx.pathParam("endDate")));
                    });

                    // Get list of sales under a specified price
                    ApiBuilder.path("/under/{price}", () -> {
                        ApiBuilder.get(ctx -> ApiGateway.handleSalesUnderPrice(ctx, ctx.pathParam("price")));
                    });

                    // Get the number of times a postcode has been accessed
                    ApiBuilder.path("/postcode/{postcode}/accessed-count", () -> {
                        ApiBuilder.get(ctx -> ApiGateway.handlePostCodeAccessedCount(ctx, ctx.pathParam("postcode")));
                    });

                    // Get the number of times a property has been accessed
                    ApiBuilder.path("/{propertyID}/accessed-count", () -> {
                        ApiBuilder.get(ctx -> ApiGateway.handlePropertyAccessedCount(ctx, ctx.pathParam("propertyID")));
                    });
                });
            });
        }).start(7070);
    }

    @OpenApi(
            path = "/sales",
            methods = HttpMethod.POST,
            summary = "Create a new home sale",
            operationId = "createSale",
            tags = {"Sales"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = HomeSale.class)),
            responses = {
                    @OpenApiResponse(status = "201", description = "Sale created"),
                    @OpenApiResponse(status = "400", description = "Failed to add sale")
            }
    )
    public static void createSale(final Context ctx) {
        try {
            // Forward request to property server
            Request request = new Request.Builder()
                    .url(PROPERTY_SERVER_URL + "/properties")
                    .post(RequestBody.create(ctx.body(), MediaType.get("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                ctx.result(response.body().string());
                ctx.status(response.code());
            }
        } catch (IOException e) {
            ctx.result("Failed to create sale");
            ctx.status(500);
        }
    }

    @OpenApi(
            path = "/sales",
            methods = HttpMethod.GET,
            summary = "Get all home sales",
            operationId = "getAllSales",
            tags = {"Sales"},
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = HomeSale[].class)),
                    @OpenApiResponse(status = "404", description = "No sales found")
            }
    )
    public static void handleAllSales(final Context ctx) {
        try {
            Request request = new Request.Builder()
                    .url(PROPERTY_SERVER_URL + "/properties")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                ctx.result(response.body().string());
                ctx.status(response.code());
            }
        } catch (IOException e) {
            ctx.result("Failed to get sales");
            ctx.status(500);
        }
    }

    @OpenApi(
            path = "/sales/{saleID}",
            methods = HttpMethod.GET,
            summary = "Get a sale by its ID",
            operationId = "getSaleByID",
            tags = {"Sales"},
            pathParams = {
                    @OpenApiParam(name = "saleID", description = "ID of the sale to retrieve")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = HomeSale.class)),
                    @OpenApiResponse(status = "404", description = "Sale not found")
            }
    )
    public static void handleSaleByID(final Context ctx, final String id) {
        try {
            // Get property data
            Request propertyRequest = new Request.Builder()
                    .url(PROPERTY_SERVER_URL + "/properties/" + id)
                    .get()
                    .build();

            try (Response propertyResponse = httpClient.newCall(propertyRequest).execute()) {
                if (propertyResponse.isSuccessful()) {
                    // Increment property access count
                    Request analyticsRequest = new Request.Builder()
                            .url(ANALYTICS_SERVER_URL + "/analytics/property/" + id + "/increment")
                            .post(RequestBody.create("", MediaType.get("application/json")))
                            .build();

                    try (Response analyticsResponse = httpClient.newCall(analyticsRequest).execute()) {
                        // Analytics response doesn't affect the main response
                    }

                    ctx.result(propertyResponse.body().string());
                    ctx.status(propertyResponse.code());
                } else {
                    ctx.result(propertyResponse.body().string());
                    ctx.status(propertyResponse.code());
                }
            }
        } catch (IOException e) {
            ctx.result("Failed to get sale");
            ctx.status(500);
        }
    }

    @OpenApi(
            path = "/sales/postcode/{postcode}",
            methods = HttpMethod.GET,
            summary = "Find sales by postcode",
            operationId = "findSaleByPostCode",
            tags = {"Sales"},
            pathParams = {
                    @OpenApiParam(name = "postcode", description = "Postcode to filter sales")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = HomeSale[].class)),
                    @OpenApiResponse(status = "404", description = "No sales found for postcode")
            }
    )
    public static void findSaleByPostCode(final Context ctx, final String postCode) {
        try {
            // Get property data
            Request propertyRequest = new Request.Builder()
                    .url(PROPERTY_SERVER_URL + "/properties/postcode/" + postCode)
                    .get()
                    .build();

            try (Response propertyResponse = httpClient.newCall(propertyRequest).execute()) {
                if (propertyResponse.isSuccessful()) {
                    // Increment postcode access count
                    Request analyticsRequest = new Request.Builder()
                            .url(ANALYTICS_SERVER_URL + "/analytics/postcode/" + postCode + "/increment")
                            .post(RequestBody.create("", MediaType.get("application/json")))
                            .build();

                    try (Response analyticsResponse = httpClient.newCall(analyticsRequest).execute()) {
                        // Analytics response doesn't affect the main response
                    }

                    ctx.result(propertyResponse.body().string());
                    ctx.status(propertyResponse.code());
                } else {
                    ctx.result(propertyResponse.body().string());
                    ctx.status(propertyResponse.code());
                }
            }
        } catch (IOException e) {
            ctx.result("Failed to get sales by postcode");
            ctx.status(500);
        }
    }

    @OpenApi(
            path = "/sales/average-price/dates/{startDate}/{endDate}",
            methods = HttpMethod.GET,
            summary = "Get average sale price within a date range",
            operationId = "getAveragePriceByDateRange",
            tags = {"Sales"},
            pathParams = {
                    @OpenApiParam(name = "startDate", description = "Start date (YYYY-MM-DD)"),
                    @OpenApiParam(name = "endDate", description = "End date (YYYY-MM-DD)")
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Average price returned"),
                    @OpenApiResponse(status = "404", description = "No prices found for date range")
            }
    )
    public static void handleAveragePriceByDateRange(final Context ctx, final String startDate, final String endDate) {
        try {
            Request request = new Request.Builder()
                    .url(PROPERTY_SERVER_URL + "/properties/average-price/dates/" + startDate + "/" + endDate)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                ctx.result(response.body().string());
                ctx.status(response.code());
            }
        } catch (IOException e) {
            ctx.result("Failed to get average price");
            ctx.status(500);
        }
    }

    @OpenApi(
            path = "/sales/under/{price}",
            methods = HttpMethod.GET,
            summary = "Get sales under a specified price",
            operationId = "getSalesUnderPrice",
            tags = {"Sales"},
            pathParams = {
                    @OpenApiParam(name = "price", description = "Maximum sale price (e.g. 500000)")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = HomeSale[].class)),
                    @OpenApiResponse(status = "400", description = "Invalid price specified"),
                    @OpenApiResponse(status = "404", description = "No sales under price found")
            }
    )
    public static void handleSalesUnderPrice(final Context ctx, final String price) {
        try {
            Request request = new Request.Builder()
                    .url(PROPERTY_SERVER_URL + "/properties/under/" + price)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                ctx.result(response.body().string());
                ctx.status(response.code());
            }
        } catch (IOException e) {
            ctx.result("Failed to get sales under price");
            ctx.status(500);
        }
    }

    @OpenApi(
            path = "/sales/postcode/{postcode}/accessed-count",
            methods = HttpMethod.GET,
            summary = "Get postcode access count",
            operationId = "getPostCodeAccessedCount",
            tags = {"Sales"},
            pathParams = {
                    @OpenApiParam(name = "postcode", description = "Postcode to get access count")
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Access count returned")
            }
    )
    public static void handlePostCodeAccessedCount(final Context ctx, final String postCode) {
        try {
            Request request = new Request.Builder()
                    .url(ANALYTICS_SERVER_URL + "/analytics/postcode/" + postCode + "/count")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                ctx.result(response.body().string());
                ctx.status(response.code());
            }
        } catch (IOException e) {
            ctx.result("Failed to get postcode access count");
            ctx.status(500);
        }
    }

    @OpenApi(
            path = "/sales/{propertyID}/accessed-count",
            methods = HttpMethod.GET,
            summary = "Get property access count",
            operationId = "getPropertyAccessedCount",
            tags = {"Sales"},
            pathParams = {
                    @OpenApiParam(name = "propertyID", description = "Property ID to get access count")
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Access count returned")
            }
    )
    public static void handlePropertyAccessedCount(final Context ctx, final String propertyID) {
        try {
            Request request = new Request.Builder()
                    .url(ANALYTICS_SERVER_URL + "/analytics/property/" + propertyID + "/count")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                ctx.result(response.body().string());
                ctx.status(response.code());
            }
        } catch (IOException e) {
            ctx.result("Failed to get property access count");
            ctx.status(500);
        }
    }
} 