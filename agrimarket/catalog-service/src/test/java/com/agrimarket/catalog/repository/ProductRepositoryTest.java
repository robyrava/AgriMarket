package com.agrimarket.catalog.repository;

import com.agrimarket.catalog.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testSaveAndFindProduct() {
        Product product = new Product(null, "Mela", "Mela Fuji", new BigDecimal("1.50"));
        Product saved = productRepository.save(product);
        
        assertThat(saved.getId()).isNotNull();
        
        Product found = productRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getNome()).isEqualTo("Mela");
    }
}
