package sales;

import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import java.util.List;
import java.util.Optional;

public class SalesController {

    private SalesDAO homeSales;

    public SalesController(SalesDAO homeSales) {
        this.homeSales = homeSales;
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
    // implements POST /sales
    public void createSale(Context ctx) {

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
    // implements Get /sales
    public void getAllSales(Context ctx) {
        List <HomeSale> allSales = homeSales.getAllSales();
        if (allSales.isEmpty()) {
            ctx.result("No Sales Found");
            ctx.status(404);
        } else {
            ctx.json(allSales);
            ctx.status(200);
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
    // implements GET /sales/{saleID}
    public void getSaleByID(Context ctx, String id) {

        Optional<HomeSale> sale = homeSales.getSaleById(id);
        sale.map(ctx::json)
                .orElseGet (() -> error (ctx, "Sale not found", 404));

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
    // Implements GET /sales/postcode/{postcodeID}
    public void findSaleByPostCode(Context ctx, String postCode) {
        List<HomeSale> sales = homeSales.getSalesByPostCode(postCode);
        if (sales.isEmpty()) {
            ctx.result("No sales for postcode found");
            ctx.status(404);
        } else {
            ctx.json(sales);
            ctx.status(200);
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
    // implements GET /average-price/dates/{startDate}/{endDate}
    // format dates as YYYY-MM-DD
    public void getAveragePriceByDateRange(Context ctx, String startDate, String endDate) {
        double averagePrice = homeSales.getAveragePriceByDateRange(startDate, endDate);
        if (averagePrice == 0.0) {
            ctx.result("No prices found for date range. Try formatting dates as YYYY-MM-DD");
            ctx.status(404);
        } else {
            ctx.json(averagePrice);
            ctx.status(200);
        }
    }

    @OpenApi(
            path = "/sales/sales/under/{price}",
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
    // implements GET /sales/under/{price}
    public void getSalesUnderPrice(Context ctx, String price) {
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


    private Context error(Context ctx, String msg, int code) {
        ctx.result(msg);
        ctx.status(code);
        return ctx;
    }



    


}
