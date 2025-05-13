package sa_ntlanganiso.SmartPrices_SA.superbalistscraping;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.json.JSONArray;
import org.json.JSONObject;
import org.bson.types.ObjectId;
import sa_ntlanganiso.SmartPrices_SA.db.MongoDbDatabase;
import sa_ntlanganiso.SmartPrices_SA.model.Product;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Date;

import org.json.JSONException;

public class SuperbalistScraping {

    
    /**
     * Scrapes products from Superbalist based on the given search term.
     *
     * @param term The term to search for on Superbalist.
     */
    private  static MongoDbDatabase mongoDatabase;

    public static void scrapeSuperbalist(String term) {

        System.out.println("==========================================================================================================================");
        System.out.println("Scraping data from Superbalist...");

        // MongoDB connection
        mongoDatabase = new MongoDbDatabase(
            "mongodb+srv://singiswaaliciaJC:oyTjOZNUYUkI0Yjv@cluster0.0frnp.mongodb.net/?retryWrites=true&w=majority",
            "product-api-db"
        );
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--start-maximized", "--disable-popup-blocking",
                "--disable-gpu", "--no-sandbox", "--disable-extensions");
                System.setProperty("webdriver.chrome.driver", 
                "src/main/resources/drivers/chromedriver-win64/chromedriver.exe");
             WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));

        JSONArray productData = new JSONArray();
        String storeName = "Superbalist";

        try {
            driver.get("https://www.superbalist.com");

            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div/nav/div/div[1]/div/div[1]/input")));
            searchBox.sendKeys(term, Keys.RETURN);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div/div[3]/div/div/div[4]/div[2]/div[2]/div[2]")));

            boolean hasNextPage = true;
            int pageNumber = 1;

            while (hasNextPage) {
                System.out.println("Scraping Page: " + pageNumber);
                
                for (int i = 1; i <= 10; i++) { // Assuming 10 products per page
                    try {
                        String productXpath = "/html/body/div/div[3]/div/div/div[4]/div[2]/div[2]/div[2]/div[" + i + "]";
                        WebElement productElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(productXpath)));
                        
                        String productDescription = productElement.findElement(By.xpath(".//h4[@class='product-title']")).getText();
                        String productBrand = productElement.findElement(By.xpath(".//span[@class='product-maker']")).getText();
                        String productPrice = productElement.findElement(By.xpath(".//span[@class='price']")).getText();
                        String productImageUrl = productElement.findElement(By.xpath(".//img[@class='lazy lazy-loaded']")).getAttribute("src");
                        
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



                        JSONObject productJson = new JSONObject();
                        productJson.put("_id", objectId.toString());
                        productJson.put("brandName", productBrand);
                        productJson.put("Price", productPrice);
                        productJson.put("productDescription", productDescription);
                        productJson.put("productImageUrl", productImageUrl);
                        productJson.put("storeName", storeName);
                        
                        Date creationDate = new Date();
                        productJson.put("creationDate", creationDate);
                        
                        productData.put(productJson);

                        Product prdct = new Product(objectId.toString(), productBrand, productPrice, 
    productDescription, productImageUrl, storeName, creationDate); 
                        mongoDatabase.addProduct(prdct);



                        System.out.println("Product Image URL: " + productImageUrl);
                        System.out.println("Product Description: " + productDescription);
                        System.out.println("Brand: " + productBrand);
                        System.out.println("Price: " + productPrice);
                        System.out.println("Store: " + storeName);
                        System.out.println("Creation timeStamp: " + creationDate);
                        System.out.println("--------------------------------------------------");
                    } catch (Exception e) {
                        System.out.println("Error processing product: " + e.getMessage());
                    }
                }

                try {
                    WebElement nextPageButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@aria-label='Next']")));
                    nextPageButton.click();
                    pageNumber++;
                } catch (Exception e) {
                    hasNextPage = false;
                }
            }
            saveToFile(productData);
        } catch (Exception e) {
            System.out.println("Error during scraping: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private static void saveToFile(JSONArray productData) throws JSONException {
        String outputPath = "output/SuperbalistScraping_products.json";
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(productData.toString(4));
            System.out.println("Data saved to '" + outputPath + "'");
        } catch (IOException e) {
            System.out.println("Error writing to JSON file: " + e.getMessage());
        }
    }
}