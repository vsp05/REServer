package sales;

import io.javalin.http.Context;

import java.util.List;
import java.util.Optional;

public class SalesController {

    private SalesDAO homeSales;

    public SalesController(SalesDAO homeSales) {
        this.homeSales = homeSales;
    }

    public void createSale(Context ctx) {
        // TO DO override Validator exception method to report better error message

        HomeSale sale = ctx.bodyValidator(HomeSale.class)
                            .get();
        if (homeSales.newSale(sale)) {
            ctx.result("Sale Created");
            ctx.status(201);
        } else {
            ctx.result("Failed to add sale");
            ctx.status(400);
        }
    }

    public void getAllSales(Context ctx) {
        List <HomeSale> allSales = homeSales.getAllSales();
        if (allSales.isEmpty()) {
            ctx.result("No Sales Found");
            ctx.status(404);
        } else {
            ctx.json(allSales);
            ctx.status(404);
        }
    }


    public void getSaleByID(Context ctx, String id) {

        Optional<HomeSale> sale = homeSales.getSaleById(id);

        if (sale.isPresent()) {
            ctx.json(sale.get());
            ctx.status(200);
        } else {
            ctx.result("Sale not found");
            ctx.status(404);
        }

    }

    public void findSaleByPostCode(Context ctx, String postCode) {
        Optional<List<HomeSale>> sales = homeSales.getSalesByPostCode(postCode);
        if (sales.isPresent()) {
            ctx.json(sales.get());
            ctx.status(200);
        } else {
            ctx.result("Postcode not found");
            ctx.status(404);
        }
    }

//    private void handleOptionalResponse(Context ctx, Optional<HomeSale> sale) {
//        sale.map(ctx::json)
//                .orElse(ctx.status(404));
//    }

    


}
