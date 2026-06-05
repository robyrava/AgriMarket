package com.agrimarket.catalog.controller;

import com.agrimarket.catalog.dto.ProductDTO;
import com.agrimarket.catalog.exception.ProductNotFoundException;
import com.agrimarket.catalog.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    public void testGetProductById() throws Exception {
        ProductDTO dto = new ProductDTO(1L, "Mela", "Mela Fuji", new BigDecimal("1.50"));
        Mockito.when(productService.getProductById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/catalog/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Mela"));
    }

    @Test
    public void testGetProductById_NotFound() throws Exception {
        Mockito.when(productService.getProductById(1L)).thenThrow(new ProductNotFoundException(1L));

        mockMvc.perform(get("/api/catalog/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateProduct() throws Exception {
        ProductDTO inputDto = new ProductDTO(null, "Pera", "Pera Abate", new BigDecimal("2.00"));
        ProductDTO outputDto = new ProductDTO(1L, "Pera", "Pera Abate", new BigDecimal("2.00"));

        Mockito.when(productService.createProduct(any(ProductDTO.class))).thenReturn(outputDto);

        mockMvc.perform(post("/api/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Pera\",\"descrizione\":\"Pera Abate\",\"prezzo\":2.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Pera"));
    }
}
