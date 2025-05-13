package sa_ntlanganiso;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class MongoDBTest {

    @Autowired
    private MongoDBContainer mongoDbContainer;

    @Test
    void testMongoDbConnection() {
        // Test logic to verify MongoDB container connection
        assert mongoDbContainer.isRunning();
        // Additional logic to interact with the MongoDB container...
    }
}
