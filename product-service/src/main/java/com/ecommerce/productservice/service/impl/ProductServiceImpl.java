package com.ecommerce.productservice.service.impl;

import com.ecommerce.commonlib.enums.ErrorCode;
import com.ecommerce.commonlib.exception.BusinessException;
import com.ecommerce.commonlib.util.PageResponse;
import com.ecommerce.productservice.config.CacheNames;
import com.ecommerce.productservice.dto.request.CreateProductRequest;
import com.ecommerce.productservice.dto.request.UpdateProductRequest;
import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ProductNotFoundException;
import com.ecommerce.productservice.mapper.ProductMapper;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    // On create — evict the all-products cache so paginated
    // lists don't serve stale data
    @CacheEvict(value = CacheNames.PRODUCTS_ALL, allEntries = true)
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with SKU: {}", request.sku());

        if (productRepository.existsBySku(request.sku())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Product already exists with SKU: " + request.sku());
        }

        Product saved = productRepository.save(productMapper.toEntity(request));
        log.info("Product created: id={}", saved.getId());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    // Cache hit: returns from Redis with key "product:id::1"
    // Cache miss: hits DB, stores result in Redis, returns
    @Cacheable(value = CacheNames.PRODUCT_BY_ID, key = "#id")
    public ProductResponse getProductById(Long id) {
        log.info("Cache MISS — fetching product id={} from DB", id);
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.PRODUCT_BY_SKU, key = "#sku")
    public ProductResponse getProductBySku(String sku) {
        log.info("Cache MISS — fetching product sku={} from DB", sku);
        return productRepository.findBySku(sku)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    @Override
    @Transactional(readOnly = true)
    // Paginated results cached with page+size as key
    @Cacheable(value = CacheNames.PRODUCTS_ALL,
            key = "#pageable.pageNumber + ':' + #pageable.pageSize")
    public PageResponse<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Cache MISS — fetching all products page={} size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.from(
                productRepository.findAll(pageable)
                        .map(productMapper::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByCategory(
            String category, Pageable pageable) {
        Product.Category cat = Product.Category.valueOf(category.toUpperCase());
        return PageResponse.from(
                productRepository.findByCategory(cat, pageable)
                        .map(productMapper::toResponse)
        );
    }

    @Override
    // @Caching lets us apply multiple cache operations at once:
    // 1. Update the by-id cache with new data
    // 2. Evict the by-sku cache (SKU might have changed)
    // 3. Evict the all-products cache
    @Caching(
            put = {
                    @CachePut(value = CacheNames.PRODUCT_BY_ID, key = "#id")
            },
            evict = {
                    @CacheEvict(value = CacheNames.PRODUCT_BY_SKU,
                            allEntries = true),
                    @CacheEvict(value = CacheNames.PRODUCTS_ALL,
                            allEntries = true)
            }
    )
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product id={} — cache will be refreshed", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (request.name() != null) product.setName(request.name());
        if (request.description() != null)
            product.setDescription(request.description());
        if (request.price() != null) product.setPrice(request.price());
        if (request.category() != null)
            product.setCategory(Product.Category
                    .valueOf(request.category().toUpperCase()));
        if (request.status() != null)
            product.setStatus(Product.ProductStatus
                    .valueOf(request.status().toUpperCase()));

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    // On delete — evict ALL related caches for this product
    @Caching(evict = {
            @CacheEvict(value = CacheNames.PRODUCT_BY_ID, key = "#id"),
            @CacheEvict(value = CacheNames.PRODUCT_BY_SKU, allEntries = true),
            @CacheEvict(value = CacheNames.PRODUCTS_ALL,  allEntries = true)
    })
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
        log.info("Deleted product id={} — caches evicted", id);
    }
}