package sa_ntlanganiso.SmartPrices_SA.Repository;

import sa_ntlanganiso.SmartPrices_SA.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    // Existing methods
    List<Product> findByProductDescriptionContainingIgnoreCase(String term);
    List<Product> findByStoreName(String storeName);
    
    // New pagination methods
    Page<Product> findByProductDescriptionContainingIgnoreCase(String term, Pageable pageable);
    Page<Product> findByStoreName(String storeName, Pageable pageable);
    
    // Store validation
    boolean existsByStoreName(String storeName);
    
    // Distinct stores aggregation
    @Aggregation(pipeline = {
        "{ $group: { _id: '$storeName' } }",
        "{ $project: { storeName: '$_id' } }"
    })
    List<String> findDistinctStoreNames();
    
}