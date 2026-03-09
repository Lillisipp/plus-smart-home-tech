package ru.yandex.practicum.error;

public class ProductInShoppingCartNotInWarehouse extends RuntimeException{
    public ProductInShoppingCartNotInWarehouse(String message) { super(message); }
}