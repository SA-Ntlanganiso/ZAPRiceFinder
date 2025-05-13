package sa_ntlanganiso.SmartPrices_SA.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sa_ntlanganiso.SmartPrices_SA.bashscraping.BashScraping;
import sa_ntlanganiso.SmartPrices_SA.db.MongoDbInterface;
import sa_ntlanganiso.SmartPrices_SA.model.Product;
import sa_ntlanganiso.SmartPrices_SA.superbalistscraping.SuperbalistScraping;
import sa_ntlanganiso.SmartPrices_SA.takealotscraping.TakealotScraping;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private MongoDbInterface mongoDbInterface;

    /**
     * Searches the database for products by description and triggers scraping if not found.
     *
     * @param term The search term entered by the user.
     * @return A list of products matching the search term.
     */
    public List<Product> searchProductsInDatabase(String term) {
        // Logic to search for products in the database
        return mongoDbInterface.findByDescriptionContaining(term); // Assuming MongoDB or another database
    }
    public List<Product> searchAndScrapeProducts(String term) {
        try {
            // Query MongoDB for products matching the description
            List<Product> products = mongoDbInterface.findByDescriptionContaining(term);

            if (products.isEmpty()) {
                // Trigger scraping since no results are found in the database
                scrapeProducts(term);
            }

            return products;
        } catch (Exception e) {
            throw new RuntimeException("Error while searching and scraping products", e);
        }
    }

    /**
     * Scrapes products from multiple sources asynchronously and saves them to the database.
     *
     * @param term The search term used for scraping.
     * @return A list of scraped products.
     */

     public boolean scrapeProducts(String term) {
        try {
            TakealotScraping.scrapeTakealot(term);
            BashScraping.scrapeBash(term);
            SuperbalistScraping.scrapeSuperbalist(term);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error while scraping products", e);
        }
    }    

    /**
     * Retrieves all products from the database.
     *
     * @return A list of all products.
     */
    public List<Product> getAllProducts() {
        try {
            return mongoDbInterface.getAllProducts(); // Ensure this method works properly
        } catch (Exception e) {
            throw new RuntimeException("Error while retrieving all products", e);
        }
    }
    

}
