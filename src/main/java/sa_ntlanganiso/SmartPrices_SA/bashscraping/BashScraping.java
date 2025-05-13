package sa_ntlanganiso.SmartPrices_SA.bashscraping;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import sa_ntlanganiso.SmartPrices_SA.db.MongoDbDatabase;
import sa_ntlanganiso.SmartPrices_SA.model.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BashScraping {

    private static MongoDbDatabase mngDB;

    /**
     * Scrapes products from Bash based on the given search term.
     * 
     * @param term The term to search for on Bash.
     */
    public static void scrapeBash(String term) {
        System.out.println("Scraping data from Bash...");

        mngDB = new MongoDbDatabase(
            "mongodb+srv://singiswaaliciaJC:oyTjOZNUYUkI0Yjv@cluster0.0frnp.mongodb.net/?retryWrites=true&w=majority", 
            "product-api-db"
        );

        // Set up ChromeDriver with options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("start-maximized");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--blink-settings=imagesEnabled=false");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-extensions");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36");

        // Initialize WebDriver
        // Define the path to the chromedriver relative to the project structure
        System.setProperty("webdriver.chrome.driver", 
        "src/main/resources/drivers/chromedriver-win64/chromedriver.exe");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));



        //src\main\resources\drivers\chromedriver-win64\chromedriver-win64\chromedriver.exe"
        // Create a JSONArray to store all product data
        JSONArray productData = new JSONArray();

        try {
            String baseUrl = "https://bash.com/s?search=" + term;
            int pageNumber = 1;
            boolean hasNextPage = true;

            while (hasNextPage) {
                System.out.println("Scraping Page: " + pageNumber);

                driver.get(baseUrl + "&page=" + pageNumber);

                // Wait for product cards to load
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("[data-testid='product-card']")));

                Document doc = Jsoup.parse(driver.getPageSource());
                Elements products = doc.select("[data-testid='product-card']");

                // Process products on this page
                List<Product> productList = new ArrayList<>();
                for (Element product : products) {
                    try {
                        // JSoup null check and element extraction
                        Element imageElement = product.selectFirst("img[data-testid='image']");
                        String productImageUrl = (imageElement != null) ? imageElement.attr("src") : "default_image_url";
                        String productDescription = product.select("h3 a[data-testid='link']").text();
                        String productBrand = product.select("h4 a[data-testid='link']").text();
                        String productPrice = product.select("[data-testid='price']").text();

                        // Unique ID creation with SHA-256 hash
                        String uniqueId = productDescription + productBrand + productPrice + System.currentTimeMillis();  // Add timestamp
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest(uniqueId.getBytes());
                        StringBuilder hexString = new StringBuilder();
                        for (byte b : hash) {
                            hexString.append(String.format("%02x", b));  // Convert to hex
                        }

                        // Generate ObjectId using hash
                        String objectIdHex = hexString.toString().substring(0, 24);
                        ObjectId objectId = new ObjectId(objectIdHex);  // Create valid ObjectId

                        String storeName = "BASH";
                        storeName = storeName.toLowerCase();
                        // Create the product JSON object in the required order
                        JSONObject productJson = new JSONObject();
                        productJson.put("_id", objectId.toString());  // ObjectId will be converted to a 24-character string
                        productJson.put("brandName", productBrand);
                        productJson.put("price", productPrice + " ");
                        productJson.put("productDescription", productDescription);
                        productJson.put("ProductImageUrl", productImageUrl);
                        productJson.put("StoreName", storeName);
                        Date creationDate = new Date();
                        productJson.put("creationDate", creationDate);
                        productData.put(productJson);
                        
                        
                        // Add the product to the batch list for MongoDB insertion
                        Product prdct = new Product(objectId.toString(), productBrand, productPrice,productDescription, productImageUrl, storeName, creationDate);  // Added creationDate
                        productList.add(prdct);
                        // Save batch to MongoDB in bulk
                        mngDB.addProduct(prdct);
                        // Output the scraped data for reference
                        System.out.println("Product ID: " + uniqueId);
                        System.out.println("Product Image URL: " + productImageUrl);
                        System.out.println("Product Description: " + productDescription);
                        System.out.println("Brand Name: " + productBrand);
                        System.out.println("Price: " + productPrice);
                        System.out.println("Creation timeStamp: " + creationDate);
                        System.out.println("---------------");

                    } catch (Exception e) {
                        System.out.println("Error parsing product: " + e.getMessage());
                    }
                }
                // Check if there's a next page
                if (doc.select("a[aria-label='Next']").isEmpty()) {
                    System.out.println("No more pages to scrape.");
                    hasNextPage = false;
                } else {
                    pageNumber++;
                }
            }
            // Save data to a JSON file
            saveToFile(productData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
    private static void saveToFile(JSONArray productData) {
        String outputPath = "output/BashScraping_products.json";
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(productData.toString(4)); // Pretty print with indentation
            System.out.println("Data saved to '" + outputPath + "'");
        } catch (IOException e) {
            System.out.println("Error writing to JSON file: " + e.getMessage());
        }
    }
}
