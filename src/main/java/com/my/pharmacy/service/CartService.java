package com.my.pharmacy.service;

import com.my.pharmacy.model.Batch;
import com.my.pharmacy.model.SaleItem;
import com.my.pharmacy.util.CalculationEngine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * CartService — Manages the shopping cart state for the POS screen.
 *
 * Extracted from POSController to keep the controller focused on UI only.
 * All cart mutation logic (add, merge, remove, clear) lives here.
 */
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    private final ObservableList<SaleItem> cartData = FXCollections.observableArrayList();

    public ObservableList<SaleItem> getCartData() {
        return cartData;
    }

    /**
     * Adds an item to the cart. If the same batch already exists,
     * quantities are merged instead of creating a duplicate line.
     *
     * @return null on success, or an error message string if stock is insufficient
     */
    public String addOrMerge(SaleItem newItem, Batch sourceBatch) {
        log.debug("addOrMerge: batch={} qty={} bonus={}", newItem.getBatchId(), newItem.getQuantity(), newItem.getBonusQty());

        Optional<SaleItem> existing = cartData.stream()
                .filter(i -> i.getBatchId() == newItem.getBatchId())
                .findFirst();

        if (existing.isPresent()) {
            SaleItem existingItem = existing.get();
            int mergedQty   = existingItem.getQuantity() + newItem.getQuantity();
            int mergedBonus = existingItem.getBonusQty() + newItem.getBonusQty();
            int totalNeeded = mergedQty + mergedBonus;

            if (totalNeeded > sourceBatch.getQtyOnHand()) {
                String error = "Cannot merge: total requested (" + totalNeeded +
                        " boxes) exceeds available stock (" + sourceBatch.getQtyOnHand() + ").";
                log.warn("Stock check failed during merge: {}", error);
                return error;
            }

            existingItem.setBonusQty(mergedBonus);
            existingItem.setQuantity(mergedQty);
            log.info("Merged batch {} into existing cart item — new qty={}", newItem.getBatchId(), mergedQty);
        } else {
            cartData.add(newItem);
            log.info("Added new item to cart: {} qty={}", newItem.getProductName(), newItem.getQuantity());
        }
        return null; // success
    }

    public void removeItem(SaleItem item) {
        log.info("Removed from cart: {}", item.getProductName());
        cartData.remove(item);
    }

    public void clear() {
        log.info("Cart cleared ({} items removed)", cartData.size());
        cartData.clear();
    }

    public boolean isEmpty() {
        return cartData.isEmpty();
    }

    public double getGrandTotal() {
        return CalculationEngine.calculateGrandTotal(
                cartData.stream().map(SaleItem::getSubTotal).toList()
        );
    }
}
