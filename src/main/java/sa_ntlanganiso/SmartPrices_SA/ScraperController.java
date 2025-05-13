package sa_ntlanganiso.SmartPrices_SA;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import sa_ntlanganiso.SmartPrices_SA.bashscraping.BashScraping;
import sa_ntlanganiso.SmartPrices_SA.db.MongoDbDatabase;
import sa_ntlanganiso.SmartPrices_SA.model.Product;
import sa_ntlanganiso.SmartPrices_SA.superbalistscraping.SuperbalistScraping;
import sa_ntlanganiso.SmartPrices_SA.takealotscraping.TakealotScraping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/scrape")
public class ScraperController {

    private static final Logger logger = LoggerFactory.getLogger(ScraperController.class);

    @Autowired
    private MongoDbDatabase database;

    /**
     * Search for products, and scrape data if no products are found.
     * 
     * @param term The search term.
     * @return A response with the products.
     */
    @GetMapping(value = "/search")
    public ResponseEntity<List<Product>> searchOrScrapeProducts(@RequestParam String term) {
        try {
            List<Product> products = database.findByDescriptionContaining(term);

            if (!products.isEmpty()) {
                return ResponseEntity.ok(products);
            }

            // Trigger scraping if no products are found
            scrapeAll(term);

            // Re-fetch the products after scraping
            products = database.findByDescriptionContaining(term);
            if (!products.isEmpty()) {
                return ResponseEntity.ok(products);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }
        } catch (Exception e) {
            logger.error("Error during search or scraping: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Trigger scraping for all sources.
     * 
     * @param term The search term.
     */
    private void scrapeAll(String term) {
        try {
            SuperbalistScraping.scrapeSuperbalist(term);
            TakealotScraping.scrapeTakealot(term);
            BashScraping.scrapeBash(term);

        }catch (Exception e) {
            logger.error("Error during scraping: {}", e.getMessage(), e);
            throw new RuntimeException("Error during scraping: " + e.getMessage());
        } 
    }

    /**
     * Get the scraper status.
     * 
     * @return A response with the scraper status.
     */
    @GetMapping(value = "/scraperStatus", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getScraperStatus() {
        try {
            // This is a placeholder; you could implement real-time task monitoring
            String status = "All scrapers are operational.";
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Error fetching scraper status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching scraper status");
        }
    }
}
