package com.agrimarket.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:catalog_db;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.rabbitmq.host=localhost"
})
class CatalogServiceApplicationTests {

    @MockBean
    private ConnectionFactory connectionFactory;

    @Test
    void contextLoads() {
        // Verifica caricamento del contesto Spring con connessioni simulate
    }

}
