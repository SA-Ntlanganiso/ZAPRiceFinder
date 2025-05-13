package sa_ntlanganiso.SmartPrices_SA.model;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection = "customers")
public class Customer {
    @Id
    private ObjectId id;
    private String name;
    private String email;
    private String password;
    private Date createdAt;
    private Date lastUpdated;

    public Customer() {
        this.createdAt = new Date();
        this.lastUpdated = new Date();
    }
    // Getters
    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    // Helper methods (keep these)
    public String getIdString() {
        return id != null ? id.toString() : null;
    }
    
    public void setIdFromString(String idString) {
        this.id = idString != null ? new ObjectId(idString) : null;
    }
}