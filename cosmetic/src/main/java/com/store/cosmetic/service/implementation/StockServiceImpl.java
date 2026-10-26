package com.store.cosmetic.service.implementation;

import com.store.cosmetic.domain.dto.response.StockResponse;
import com.store.cosmetic.domain.model.Entry;
import com.store.cosmetic.domain.model.Product;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.repository.EntryDetailRepository;
import com.store.cosmetic.repository.EntryRepository;
import com.store.cosmetic.repository.ProductRepository;
import com.store.cosmetic.repository.SaleDetailRepository;
import com.store.cosmetic.service.contract.StockService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    private final ProductRepository productRepository;
    private final EntryRepository entryRepository;
    private final EntryDetailRepository entryDetailRepository;
    private final SaleDetailRepository saleDetailRepository;

    public StockServiceImpl(ProductRepository productRepository,
                            EntryRepository entryRepository,
                            EntryDetailRepository entryDetailRepository,
                            SaleDetailRepository saleDetailRepository) {
        this.productRepository = productRepository;
        this.entryRepository = entryRepository;
        this.entryDetailRepository = entryDetailRepository;
        this.saleDetailRepository = saleDetailRepository;
    }

    /**
     * Optimized stock calculation algorithm:
     * 1. Find the latest is_initial = true entry for the product (inventory cut baseline).
     * 2. If found at time T_cut:
     *    Stock = InitialQuantity + sum(entry quantities after T_cut, status != DELETED)
     *            - sum(sale quantities after T_cut, status != DELETED)
     * 3. If no initial cut exists:
     *    Stock = sum(all active entry quantities) - sum(all active sale quantities)
     */
    @Override
    @Transactional(readOnly = true)
    public StockResponse calculateStock(Long productId) {
        Product product = productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Find the latest is_initial entry for this product
        List<Entry> latestInitialEntries = entryRepository.findLatestInitialEntryForProduct(
                productId, PageRequest.of(0, 1));

        int stock;
        if (!latestInitialEntries.isEmpty()) {
            Entry cutEntry = latestInitialEntries.get(0);
            // Get the quantity from the initial cut for this product
            int initialQty = entryDetailRepository.sumQuantityByEntryAndProduct(productId, cutEntry.getId());
            // Sum entries after the cutoff date
            int entryQtyAfterCut = entryDetailRepository.sumQuantityAfterCutoff(productId, cutEntry.getDate());
            // Sum sales after the cutoff date
            int saleQtyAfterCut = saleDetailRepository.sumQuantityAfterCutoff(productId, cutEntry.getDate());
            stock = initialQty + entryQtyAfterCut - saleQtyAfterCut;
        } else {
            // No initial cut: use all active quantities
            int totalEntries = entryDetailRepository.sumAllActiveQuantity(productId);
            int totalSales = saleDetailRepository.sumAllActiveQuantity(productId);
            stock = totalEntries - totalSales;
        }

        return StockResponse.builder()
                .productId(productId)
                .productName(product.getName())
                .stock(Math.max(stock, 0))  // Prevent negative stock display
                .build();
    }
}
