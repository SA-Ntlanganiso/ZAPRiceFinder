package sa_ntlanganiso.SmartPrices_SA;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sa_ntlanganiso.SmartPrices_SA.model.Product;
import sa_ntlanganiso.SmartPrices_SA.service.ProductService;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "https://accounts.google.com"})
@RestController
@RequestMapping("/api/dbproducts")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ScraperController scraperController;

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam String term) {
        try {
            List<Product> products = productService.searchProductsInDatabase(term);

            if (!products.isEmpty()) {
                return ResponseEntity.ok(products);
            } else {
                List<Product> products2 = productService.searchAndScrapeProducts(term);
                if (!products2.isEmpty()) {
                    return ResponseEntity.ok(products2);
                }
            }

            ResponseEntity<List<Product>> scrapeResponse = scraperController.searchOrScrapeProducts(term);

            if (scrapeResponse.getStatusCode() == HttpStatus.OK) {
                return scrapeResponse;
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No products found after scraping for: " + term);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Search error: " + e.getMessage());
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving products: " + e.getMessage());
        }
    }
}