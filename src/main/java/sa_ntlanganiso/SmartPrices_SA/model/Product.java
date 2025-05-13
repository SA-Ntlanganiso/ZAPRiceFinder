package sa_ntlanganiso.SmartPrices_SA.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String brandName;
    private String price;
    private String productDescription;
    private String productImageUrl;
    private String storeName;
    private Date creationDate;
   

    // Default constructor
    public Product() {}

    // Parameterized constructor
    public Product(String id, String brandName, String price, String productDescription, String productImageUrl, String storeName ,Date creationDate) {
        this.id = id;
        this.brandName = brandName;
        this.price = price;
        this.productDescription = productDescription;
        this.productImageUrl = productImageUrl;
        this.storeName = storeName;
        this.creationDate = creationDate;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public String getStoreName() {
        return storeName;
    }
    
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    @Override
    public String toString() {
        return "Product [id=" + id + ", brandName=" + brandName + ", price=" + price + 
               ", productDescription=" + productDescription + ", productImageUrl=" + productImageUrl + 
               ", storeName=" + storeName + ",creationDate = " + creationDate + "]";
    }
}
