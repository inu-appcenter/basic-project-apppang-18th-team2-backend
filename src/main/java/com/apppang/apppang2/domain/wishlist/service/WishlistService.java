package com.apppang.apppang2.domain.wishlist.service;

import com.apppang.apppang2.domain.product.dto.response.ProductResponse;
import com.apppang.apppang2.domain.wishlist.dto.WishlistResponse;
import com.apppang.apppang2.domain.wishlist.entity.Wishlist;
import com.apppang.apppang2.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;


    public WishlistResponse getWishlist(Long userId){

        //데이터 조회
        List<Wishlist> wishList = wishlistRepository.findAllByUserIdWithProduct(userId);

        //조회된 엔티티 리스트를 ProductReponse로 반환
        List<ProductResponse> productResponses = wishList.stream()
                .map(wishlist->new ProductResponse(wishlist.getProduct(),true))
                .collect(Collectors.toList());

        return new WishlistResponse(productResponses);
    }
}
