package dev.CaoNguyen_1883.ecommerce.common.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String productName) {
        super(String.format("Product '%s' is out of stock", productName));
    }
    public OutOfStockException(String productName, int available, int requested) {
        super(String.format("Product '%s' has only %d items available, but %d requested",
                productName, available, requested));
    }
}