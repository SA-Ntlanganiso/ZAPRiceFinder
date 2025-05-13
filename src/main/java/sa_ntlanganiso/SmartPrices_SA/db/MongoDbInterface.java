package sa_ntlanganiso.SmartPrices_SA.db;


import sa_ntlanganiso.SmartPrices_SA.model.Product;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface MongoDbInterface {
    public boolean addProduct(Product product) throws Exception;
    public Product getProductById(String productId) throws Exception;
    public List<Product> getAllProducts() throws Exception;
    
    public boolean updateProduct(String productId, Product updatedProduct) throws Exception;
    public boolean deleteProduct(String productId) throws Exception;
    public List<Product> findByDescriptionContaining(String term);
    public boolean addReviewToProduct(String productId, int review) throws Exception;
    List<Product> getAllProductsPaginated(int page, int size) throws Exception;
}
