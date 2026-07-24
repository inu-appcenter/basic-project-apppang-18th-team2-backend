package com.apppang.apppang2.domain.cart.dto.response;

import com.apppang.apppang2.domain.cart.entity.Cart;
import com.apppang.apppang2.domain.product.entity.Product;
import lombok.Getter;

@Getter
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private String thumbnail;
    private int originalPrice;
    private int discountRate;
    private int salePrice;
    private int quantity;
    private int stock;

    public CartItemResponse(Cart cart){
        Product product = cart.getProduct();
        this.cartItemId = cart.getId();
        this.productId = product.getId();
        this.productName = product.getName();
        this.thumbnail = product.getImage1();
        this.originalPrice = product.getPrice();
        this.discountRate = product.getDiscountRate() == null ? 0 : product.getDiscountRate();
        this.salePrice = product.getSalePrice();
        this.quantity = cart.getQuantity();
        this.stock = product.getStock();
    }
}