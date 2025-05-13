package sa_ntlanganiso.SmartPrices_SA.service;

import org.springframework.stereotype.Service;

import sa_ntlanganiso.SmartPrices_SA.model.CartItem;
import sa_ntlanganiso.SmartPrices_SA.model.Customer;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClients;

@Service
public class UserService {

    private final MongoCollection<Document> customerCollection;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(
            @Value("${spring.data.mongodb.uri}") String uri,
            @Value("${spring.data.mongodb.database}") String dbName,
            PasswordEncoder passwordEncoder) {
        MongoDatabase database = MongoClients.create(uri).getDatabase(dbName);
        this.customerCollection = database.getCollection("customers");
        this.passwordEncoder = passwordEncoder;
    }

    public Customer registerCustomer(Customer customer) throws Exception {
        if (customerExists(customer.getEmail())) {
            throw new Exception("Email already exists");
        }
        
        ObjectId id = new ObjectId();
        Document doc = new Document()
                .append("_id", id)
                .append("name", customer.getName())
                .append("email", customer.getEmail())
                .append("password", passwordEncoder.encode(customer.getPassword()))
                .append("lastUpdated", new Date())
                .append("cart", new ArrayList<>());
        
        customerCollection.insertOne(doc);
        
        // Set the generated ID on the customer object
        customer.setId(id);
        return customer;
    }
    public Customer getCustomerByEmail(String email) throws Exception {
        Document customerDoc = customerCollection.find(Filters.eq("email", email)).first();
        if (customerDoc == null) {
            throw new Exception("Customer not found");
        }
        
        Customer customer = new Customer();
        customer.setId(customerDoc.getObjectId("_id"));
        customer.setName(customerDoc.getString("name"));
        customer.setEmail(customerDoc.getString("email"));
        customer.setPassword(customerDoc.getString("password"));
        return customer;
    }
    public List<CartItem> getCartItems(String email) throws MongoException {
        Document userDoc = customerCollection.find(Filters.eq("email", email)).first();
        if (userDoc == null) throw new RuntimeException("User not found");
        
        List<Document> cartDocs = userDoc.getList("cart", Document.class, new ArrayList<>());
        return cartDocs.stream()
                .map(this::convertDocumentToCartItem)
                .collect(Collectors.toList());
    }
    
    public boolean loginCustomer(String email, String password) {
        Document userDoc = customerCollection.find(Filters.eq("email", email)).first();
        if (userDoc == null) return false;
        
        String storedPassword = userDoc.getString("password");
        return passwordEncoder.matches(password, storedPassword);
    }

    public boolean customerExists(String email) {
        return customerCollection.find(Filters.eq("email", email)).first() != null;
    }
    
    public void addToCart(String email, CartItem item) throws MongoException {
        Document itemDoc = convertCartItemToDocument(item);
        
        UpdateResult result = customerCollection.updateOne(
                Filters.eq("email", email),
                Updates.push("cart", itemDoc)
        );

        if (result.getModifiedCount() == 0) {
            throw new RuntimeException("Failed to add item to cart");
        }
    }

    public void updateCartItem(String email, int index, CartItem updatedItem) throws MongoException {
        Document userDoc = customerCollection.find(Filters.eq("email", email)).first();
        if (userDoc == null) {
            throw new RuntimeException("User not found");
        }

        List<Document> cart = userDoc.getList("cart", Document.class, new ArrayList<>());
        if (index < 0 || index >= cart.size()) {
            throw new IndexOutOfBoundsException("Invalid cart index: " + index);
        }

        cart.set(index, convertCartItemToDocument(updatedItem));
        
        UpdateResult result = customerCollection.updateOne(
                Filters.eq("email", email),
                Updates.set("cart", cart)
        );

        if (result.getModifiedCount() == 0) {
            throw new RuntimeException("Failed to update cart item");
        }
    }

    public void removeFromCart(String email, int index) throws MongoException {
        Document userDoc = customerCollection.find(Filters.eq("email", email)).first();
        if (userDoc == null) {
            throw new RuntimeException("User not found");
        }

        List<Document> cart = userDoc.getList("cart", Document.class, new ArrayList<>());
        if (index < 0 || index >= cart.size()) {
            throw new IndexOutOfBoundsException("Invalid cart index: " + index);
        }

        cart.remove(index);
        
        UpdateResult result = customerCollection.updateOne(
                Filters.eq("email", email),
                Updates.set("cart", cart)
        );

        if (result.getModifiedCount() == 0) {
            throw new RuntimeException("Failed to remove item from cart");
        }
    }

    public void clearCart(String email) throws MongoException {
        UpdateResult result = customerCollection.updateOne(
                Filters.eq("email", email),
                Updates.set("cart", new ArrayList<>())
        );
        
        if (result.getModifiedCount() == 0) {
            throw new RuntimeException("Failed to clear cart");
        }
    }

    public void syncCart(String email, List<CartItem> items) throws MongoException {
        List<Document> cartDocs = items.stream()
                .map(this::convertCartItemToDocument)
                .collect(Collectors.toList());
                
        UpdateResult result = customerCollection.updateOne(
                Filters.eq("email", email),
                Updates.set("cart", cartDocs)
        );
        
        if (result.getModifiedCount() == 0) {
            throw new RuntimeException("Failed to sync cart");
        }
    }

    // Helper methods
    private CartItem convertDocumentToCartItem(Document doc) {
        CartItem item = new CartItem();
        item.setProductId(doc.getString("productId"));
        item.setProductDescription(doc.getString("productDescription"));
        item.setBrandName(doc.getString("brandName"));
        item.setPrice(doc.getDouble("price"));
        item.setStoreName(doc.getString("storeName"));
        item.setProductImageUrl(doc.getString("productImageUrl"));
        item.setAddedAt(doc.getDate("addedAt"));
        return item;
    }

    private Document convertCartItemToDocument(CartItem item) {
        return new Document()
                .append("productId", item.getProductId())
                .append("productDescription", item.getProductDescription())
                .append("brandName", item.getBrandName())
                .append("price", item.getPrice() != null ? item.getPrice() : 0.0)
                .append("storeName", item.getStoreName())
                .append("productImageUrl", item.getProductImageUrl())
                .append("addedAt", item.getAddedAt() != null ? item.getAddedAt() : new Date());
    }
    public Optional<Customer> getCustomerById(ObjectId id) {
        try {
            Document customerDoc = customerCollection.find(Filters.eq("_id", id)).first();
            if (customerDoc == null) {
                return Optional.empty();
            }
            
            Customer customer = new Customer();
            customer.setId(customerDoc.getObjectId("_id"));
            customer.setName(customerDoc.getString("name"));
            customer.setEmail(customerDoc.getString("email"));
            customer.setPassword(customerDoc.getString("password"));
            customer.setLastUpdated(customerDoc.getDate("lastUpdated"));
            
            return Optional.of(customer);
        } catch (MongoException e) {
            throw new RuntimeException("Error fetching customer by ID", e);
        }
    }
    public Customer updateCustomer(Customer customer) {
        try {
            customer.setLastUpdated(new Date());
            
            Document updatedDoc = new Document()
                .append("name", customer.getName())
                .append("email", customer.getEmail())
                .append("password", customer.getPassword())
                .append("lastUpdated", customer.getLastUpdated());
            
            UpdateResult result = customerCollection.updateOne(
                Filters.eq("_id", customer.getId()),
                new Document("$set", updatedDoc)
            );
            
            if (result.getModifiedCount() == 0) {
                throw new RuntimeException("Failed to update customer");
            }
            
            return customer;
        } catch (MongoException e) {
            throw new RuntimeException("Error updating customer", e);
        }
    }
    
}