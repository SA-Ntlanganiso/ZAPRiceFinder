package sa_ntlanganiso.SmartPrices_SA;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import sa_ntlanganiso.SmartPrices_SA.db.MongoDbInterface;
import sa_ntlanganiso.SmartPrices_SA.model.Product;
import sa_ntlanganiso.SmartPrices_SA.takealotscraping.TakealotScraping;
import sa_ntlanganiso.SmartPrices_SA.superbalistscraping.SuperbalistScraping;
import sa_ntlanganiso.SmartPrices_SA.bashscraping.BashScraping;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CrossOrigin(origins = "http://localhost:3000")
@SpringBootApplication(scanBasePackages = "sa_ntlanganiso.SmartPrices_SA")
public class SmartPricesSaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartPricesSaApplication.class, args);
    }

    @Bean
    public CommandLineRunner runApplication(MongoDbInterface database) {
        return args -> {
            try {
                String term = args.length > 0 ? args[0] : "defaultSearchTerm";

                System.out.println("Starting scraping operations...");

                // Create an ExecutorService to handle the scraping tasks concurrently
                ExecutorService executorService = Executors.newFixedThreadPool(3); // Using 3 threads for 3 scraping tasks
                
                // Submit the scraping tasks to the executor
                executorService.submit(new SuperbalistScrapingTask(term));
                executorService.submit(new TakealotScrapingTask(term));
                executorService.submit(new BashScrapingTask(term));

                // Shut down the executor service after all tasks are submitted
                executorService.shutdown();
                while (!executorService.isTerminated()) {
                    // Wait for all threads to finish
                }
                System.out.println("Scraping operations completed.");
                // Database operations
                System.out.println("Starting database operations...");
                runDatabaseOperations(database);
                System.out.println("Database operations completed.");
            } catch (Exception e) {
                System.err.println("Error during application execution: " + e.getMessage());
            }
        };
    }

    // Task for Superbalist scraping
    static class SuperbalistScrapingTask implements Runnable {
        private final String term;

        SuperbalistScrapingTask(String term) {
            this.term = term;
        }

        @Override
        public void run() {
            try {
                System.out.println("Superbalist scraping started...");
                SuperbalistScraping.scrapeSuperbalist(term);
                System.out.println("Superbalist scraping completed.");
            } catch (Exception e) {
                System.err.println("Error during Superbalist scraping: " + e.getMessage());
            }
        }
    }
    // Task for Takealot scraping
    static class TakealotScrapingTask implements Runnable {
        private final String term;

        TakealotScrapingTask(String term) {
            this.term = term;
        }

        @Override
        public void run() {
            try {
                System.out.println("Takealot scraping started...");
                TakealotScraping.scrapeTakealot(term);
                System.out.println("Takealot scraping completed.");
            } catch (Exception e) {
                
                System.err.println("Error during Takealot scraping: " + e.getMessage());
            }
        }
    }
    // Task for Bash scraping
    static class BashScrapingTask implements Runnable {
        private final String term;

        BashScrapingTask(String term) {
            this.term = term;
        }

        @Override
        public void run() {
            try {
                System.out.println("Bash scraping started...");
                BashScraping.scrapeBash(term);
                System.out.println("Bash scraping completed.");
            } catch (Exception e) {
                System.err.println("Error during Bash scraping: " + e.getMessage());
            }
        }
    }
    private void runDatabaseOperations(MongoDbInterface database) {
        System.out.println("==================================================================================================================");
        try {
            List<Product> products = database.getAllProducts();
            if (!products.isEmpty()) {
                System.out.println("Retrieved products:");
                //products.forEach(product -> System.out.println(product.toString()));
            }
        } catch (Exception e) {
            System.err.println("Database operation error: " + e.getMessage());
        }
    }
}
