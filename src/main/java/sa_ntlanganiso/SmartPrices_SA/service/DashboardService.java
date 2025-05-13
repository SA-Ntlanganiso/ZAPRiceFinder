package sa_ntlanganiso.SmartPrices_SA.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import sa_ntlanganiso.SmartPrices_SA.db.MongoDbInterface;
import sa_ntlanganiso.SmartPrices_SA.model.Product;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final MongoDbInterface mongoDbInterface;

    @Autowired
    public DashboardService(MongoDbInterface mongoDbInterface) {
        this.mongoDbInterface = mongoDbInterface;
    }

    public Page<Product> filterProductsByStore(String store, int page, int size) throws Exception {
        List<Product> filtered = mongoDbInterface.getAllProducts().stream()
                .filter(p -> p.getStoreName().equalsIgnoreCase(store))
                .collect(Collectors.toList());
        return paginateList(filtered, page, size);
    }

   

    public Map<String, Object> getProductStats() throws Exception {
        List<Product> products = mongoDbInterface.getAllProducts();
        
        double average = products.stream()
                .mapToDouble(p -> parsePrice(p.getPrice()))
                .average()
                .orElse(0.0);

        double min = products.stream()
                .mapToDouble(p -> parsePrice(p.getPrice()))
                .min()
                .orElse(0.0);

        double max = products.stream()
                .mapToDouble(p -> parsePrice(p.getPrice()))
                .max()
                .orElse(0.0);

        return Map.of(
            "totalProducts", products.size(),
            "averagePrice", average,
            "minPrice", min,
            "maxPrice", max,
            "totalStores", getAllUniqueStores().size()
        );
    }

    private double parsePrice(String price) {
        try {
            return Double.parseDouble(price.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    public Page<Product> paginateList(List<Product> list, int page, int size) {
        int start = Math.min(page * size, list.size());
        int end = Math.min((page + 1) * size, list.size());
        return new PageImpl<>(
            list.subList(start, end), 
            PageRequest.of(page, size), 
            list.size()
        );
    }
    public Page<Product> getPaginatedProducts(int page, int size) throws Exception {
        try {
            List<Product> allProducts = mongoDbInterface.getAllProductsPaginated(page, size);
            return paginateList(allProducts, page, size);
        } catch (Exception e) {
            throw new Exception("Database error: " + e.getMessage(), e);
        }
    }
    public List<String> getAllUniqueStores() throws Exception {
        try {
            return mongoDbInterface.getAllProducts().stream()
                    .map(p -> p.getStoreName())
                    .filter(store -> store != null && !store.isEmpty()) // Add null/empty filter
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception("Failed to get stores: " + e.getMessage());
        }
    }
    
}