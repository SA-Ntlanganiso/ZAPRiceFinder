package sa_ntlanganiso.SmartPrices_SA.db;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import sa_ntlanganiso.SmartPrices_SA.model.Product;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class MongoDbDatabase implements MongoDbInterface {

    private final MongoCollection<Document> collection;

    public MongoDbDatabase(
            @Value("${spring.data.mongodb.uri}") String uri,
            @Value("${spring.data.mongodb.database}") String dbName) {
        MongoDatabase database = MongoClients.create(uri).getDatabase(dbName);
        this.collection = database.getCollection("products");
    }
    @Override
    public boolean addProduct(Product product) throws Exception {
        // Validate the product object
        if (product == null || product.getBrandName() == null || product.getPrice() == null) {
            throw new IllegalArgumentException("Product or required fields cannot be null.MAybe the scraping value did instantiate properly.");
        }
        try {
            Document doc = new Document()
                    .append("_id", new ObjectId(product.getId()))
                    .append("brandName", product.getBrandName()) 
                    .append("price", product.getPrice())
                    .append("productDescription", product.getProductDescription())
                    .append("productImageUrl", product.getProductImageUrl())
                    .append("storeName", product.getStoreName())
                    .append("creationDate", product.getCreationDate());
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            throw new Exception("Failed to insert product. Reason: " + e.getMessage(), e);
        }
    }
    @Override
    public boolean deleteProduct(String productId) throws Exception {
        try {
            Document query = new Document("_id", new ObjectId(productId));
            collection.deleteOne(query);
            return true;
        } catch (Exception e) {
            throw new Exception("Error deleting product: " + e.getMessage(), e);
        }
    }
    @Override
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                products.add(new Product(
                    doc.getObjectId("_id") != null ? doc.getObjectId("_id").toString() : "",
                    doc.getString("brandName") != null ? doc.getString("brandName") : "",
                    doc.getString("price") != null ? doc.getString("price") : "0.00",
                    doc.getString("productDescription") != null ? doc.getString("productDescription") : "",
                    doc.getString("productImageUrl") != null ? doc.getString("productImageUrl") : "",
                    doc.getString("storeName") != null ? doc.getString("storeName") : "Unknown Store",
                    doc.getDate("creationDate") != null ? doc.getDate("creationDate") : new Date()
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving products", e);
        }
        return products;
    }
    @Override
    public Product getProductById(String productId) throws Exception {
        try {
            Document query = new Document("_id", new ObjectId(productId));
            Document doc = collection.find(query).first();
            if (doc == null) {
                throw new Exception("Product not found with ID: " + productId);
            }
            return new Product(
                    doc.getObjectId("_id").toString(),
                    doc.getString("brandName"),
                    doc.getString("price"),
                    doc.getString("productDescription"),
                    doc.getString("productImageUrl"),
                    doc.getString("storeName"),
                    doc.getDate("creationDate")
            );
        }catch(Exception e){
            throw new Exception("Error retrieving product: " + e.getMessage(), e);
        }
    }
    @Override
    public boolean updateProduct(String productId, Product updatedProduct) throws Exception {
        try {
            Document query = new Document("_id", new ObjectId(productId));
            Document updateDoc = new Document("$set", new Document()
                .append("brandName", updatedProduct.getBrandName())
                .append("price", updatedProduct.getPrice())
                .append("productDescription", updatedProduct.getProductDescription())
                .append("productImageUrl", updatedProduct.getProductImageUrl())
                .append("storeName", updatedProduct.getStoreName())
                .append("creationDate",updatedProduct.getCreationDate()));// Add timestamp here
    
            collection.updateOne(query, updateDoc);
            return true;
        } catch (Exception e) {
            throw new Exception("Error updating product: " + e.getMessage(), e);
        }
    }
    @Override
    public List<Product> findByDescriptionContaining(String term) {
        List<Product> products = new ArrayList<>();

        Pattern regex = Pattern.compile(term, Pattern.CASE_INSENSITIVE);
        Document query = new Document("productDescription", new Document("$regex", regex));

        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                products.add(new Product(
                    doc.getObjectId("_id").toString(),
                    doc.getString("brandName"),
                    doc.getString("price"),
                    doc.getString("productDescription"),
                    doc.getString("productImageUrl"),
                    doc.getString("storeName"),
                    doc.getDate("creationDate")
                ));
            }
        }
        return products;
    }
    
    @Override
    public boolean addReviewToProduct(String productId, int review) throws Exception {
        try {
            // Create a query document to find the product by its ID
            Document query = new Document("_id", new ObjectId(productId));
            Document update = new Document("$set", new Document("review", review));
            UpdateResult result = collection.updateOne(query, update);
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            throw new Exception("Failed to add review to product: " + e.getMessage(), e);
        }
    }
    @Override
    public List<Product> getAllProductsPaginated(int page, int size) throws Exception {
        List<Product> products = new ArrayList<>();
        try {
            collection.find()
                     .skip(page * size)
                     .limit(size)
                     .forEach(doc -> products.add(convertDocument(doc)));
        } catch (Exception e) {
            throw new Exception("Pagination error", e);
        }
        return products;
    }
    private Product convertDocument(Document doc) {
        return new Product(
            doc.getObjectId("_id") != null ? doc.getObjectId("_id").toString() : "",
            doc.getString("brandName"),
            doc.getString("price"),
            doc.getString("productDescription"),
            doc.getString("productImageUrl"),
            doc.getString("storeName"),
            doc.getDate("creationDate") != null ? doc.getDate("creationDate") : new Date()
        );
    }
   
}
