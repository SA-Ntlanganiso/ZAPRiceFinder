package sa_ntlanganiso.SmartPrices_SA.model;


import java.util.Date;

public class CartItem {
    private String productId;
    private String productDescription;
    private String brandName;
    private Double price;
    private String storeName;
    private String productImageUrl;
    private Date addedAt;

    public CartItem() {
        this.addedAt = new Date(); // Default to current time
    }

    // Getters
    public String getProductId() {
        return productId;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public String getBrandName() {
        return brandName;
    }

    public Double getPrice() {
        return price;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public Date getAddedAt() {
        return addedAt;
    }

    // Setters
    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public void setAddedAt(Date addedAt) {
        this.addedAt = addedAt;
    }
}