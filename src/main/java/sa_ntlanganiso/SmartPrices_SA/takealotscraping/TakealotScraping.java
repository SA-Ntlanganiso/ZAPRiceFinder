package sa_ntlanganiso.SmartPrices_SA.takealotscraping;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;
import sa_ntlanganiso.SmartPrices_SA.db.MongoDbDatabase;
import sa_ntlanganiso.SmartPrices_SA.model.Product;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Date;

import org.bson.types.ObjectId;

public class TakealotScraping {

    private static MongoDbDatabase mongoDatabase;

    /**
     * Scrapes products from Takealot based on the given search term.
     *
     * @param term The term to search for on Takealot.
     */
    public static void scrapeTakealot(String term) {
        System.out.println("Scraping data from Takealot...");

        // MongoDB connection
        mongoDatabase = new MongoDbDatabase(
            "mongodb+srv://singiswaaliciaJC:oyTjOZNUYUkI0Yjv@cluster0.0frnp.mongodb.net/?retryWrites=true&w=majority",
            "product-api-db"
        );

        // Set up WebDriver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--start-maximized", "--disable-popup-blocking",
                "--disable-gpu", "--no-sandbox", "--disable-extensions");

                System.setProperty("webdriver.chrome.driver", 
                "src/main/resources/drivers/chromedriver-win64/chromedriver.exe");
                WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));

        JSONArray productData = new JSONArray();
        String storeName = "Takealot";
                //src\main\resources\drivers\chromedriver-win64\chromedriver-win64\chromedriver.exe"
        try {
            driver.get("https://www.takealot.com");

            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("search")));
            searchBox.sendKeys(term, Keys.RETURN);

            // Wait for products to load
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("article.product-card")));

            boolean hasNextPage = true;
            int pageNumber = 1;

            while (hasNextPage) {
                System.out.println("Scraping Page: " + pageNumber);
                Elements products = Jsoup.parse(driver.getPageSource()).select("article.product-card");

                for (Element product : products) {
                    try {
                        // Extract product details
                        String productImageUrl = product.select("img.product-card-image-module_product-image_3mJsJ").attr("src");
                        String productDescription = product.select("h4.product-card-module_product-title_16xh8").text();
                        String productBrand = product.selectFirst("div[data-ref='brand-wrapper'] a") != null ?
                                product.selectFirst("div[data-ref='brand-wrapper'] a").text() : "N/A";
                        String productPrice = product.selectFirst("li.price span.currency") != null ?
                                product.selectFirst("li.price span.currency").text() : "N/A";

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
                                  // Create valid ObjectId
                        // Save product to JSON array
                        JSONObject productJson = new JSONObject();
                        Date creationDate = new Date();
                        productJson.put("_id", objectId.toString());
                        productJson.put("brandName", productBrand);
                        productJson.put("Price", productPrice);
                        productJson.put("productDescription", productDescription);
                        productJson.put("productImageUrl", productImageUrl);
                        productJson.put("storeName", storeName);
                        productJson.put("creationDate", creationDate);

                        
                        productData.put(productJson);
                        Product prdct = new Product(objectId.toString(), productBrand, productPrice, productDescription, productImageUrl, storeName, creationDate);
                        mongoDatabase.addProduct(prdct);

                        // Print details
                        System.out.println("Product Image URL: " + productImageUrl);
                        System.out.println("Product Description: " + productDescription);
                        System.out.println("Brand: " + productBrand);
                        System.out.println("Price: " + productPrice);
                        System.out.println("Store: " + storeName);
                        System.out.println("--------------------------------------------------");

                    } catch (Exception e) {
                        System.out.println("Error processing product: " + e.getMessage());
                    }
                }

                // Check for next page
                try {
                    WebElement nextPageButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@aria-label='Next']")));
                    nextPageButton.click();
                    pageNumber++;
                } catch (Exception e) {
                    hasNextPage = false;
                }
            }

        } catch (Exception e) {
            System.out.println("Error during scraping: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }
}
