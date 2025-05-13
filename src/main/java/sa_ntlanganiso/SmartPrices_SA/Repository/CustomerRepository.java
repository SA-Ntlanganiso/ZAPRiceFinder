package sa_ntlanganiso.SmartPrices_SA.Repository;

import sa_ntlanganiso.SmartPrices_SA.model.Customer;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.lang.String;
import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, ObjectId> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
}

