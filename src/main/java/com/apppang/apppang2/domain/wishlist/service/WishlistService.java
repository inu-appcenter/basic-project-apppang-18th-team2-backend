package com.apppang.apppang2.domain.wishlist.service;

import com.apppang.apppang2.domain.address.entity.Address;
import com.apppang.apppang2.domain.product.dto.response.ProductResponse;
import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.domain.user.repository.UserRepository;
import com.apppang.apppang2.domain.wishlist.dto.WishlistResponse;
import com.apppang.apppang2.domain.wishlist.entity.Wishlist;
import com.apppang.apppang2.domain.wishlist.repository.WishlistRepository;
import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    //찜 목록 조회
    public WishlistResponse getWishlist(Long userId){

        //데이터 조회
        List<Wishlist> wishList = wishlistRepository.findAllByUserIdWithProduct(userId);

        //조회된 엔티티 리스트를 ProductReponse로 반환
        List<ProductResponse> productResponses = wishList.stream()
                .map(wishlist->new ProductResponse(wishlist.getProduct(),true))
                .collect(Collectors.toList());

        return new WishlistResponse(productResponses);
    }


    public void addWishlist(Long userId, Long productId){
        //해당 사용자가 이미 찜한 상품인지 중복 체크
        if(wishlistRepository.existsByUserIdAndProductId(userId, productId)){
            throw new CustomException(HttpStatus.CONFLICT, "이미 찜한 상품입니다.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다"));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        wishlistRepository.save(wishlist);
    }

}
