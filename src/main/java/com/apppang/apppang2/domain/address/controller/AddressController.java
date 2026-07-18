package com.apppang.apppang2.domain.address.controller;

import com.apppang.apppang2.domain.address.dto.request.AddressRequest;
import com.apppang.apppang2.domain.address.dto.request.AddressUpdateRequest;
import com.apppang.apppang2.domain.address.dto.response.AddressResponse;
import com.apppang.apppang2.domain.address.service.AddressService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    //유저 배송지 목록 조회
    @Operation(summary = "배송지 목록 조회")
    @GetMapping
    //스프링 시큐리티가 SecurityContext에 인증정보를 UserDetails 객체로 저장했기 때문에 타입 불일치를 막기 위해 UserDetails로 받음
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddress(@AuthenticationPrincipal Long userId){

        //서비스에 넘겨서 해당 ID로 배송지 목록 조회
        List<AddressResponse> addressList = addressService.getMyAddress(userId);

        return ResponseEntity.ok(ApiResponse.success("배송지 목록 조회에 성공했습니다.",addressList));
    }

    //배송지 추가
    @Operation(summary = "배송지 추가")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> addAddress(@AuthenticationPrincipal UserDetails userDetails, @RequestBody AddressRequest request){


        Long userId = Long.valueOf(userDetails.getUsername());

        //요청받은 정보로 배송지를 생성하고 생성된 주소의 ID 반환받음
        Long addressId = addressService.addAddress(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("배송지가 등록되었습니다.",addressId));
    }

    //배송지 수정
    @Operation(summary = "배송지 수정", description = "기존 배송지 정보를 수정합니다.")
    @PatchMapping("/{addressId}")
    //SecurityContext에 저장된 유저 정보를 꺼내오고, URL 경로에 매핑된 addressId 값을 변수로, Http Body로 넘어온 JSON을 DTO로 변환하고 유효성 검사 수행
    public ResponseEntity<ApiResponse<String>> updateAddress(@AuthenticationPrincipal Long userId, @PathVariable Long addressId, @Valid @RequestBody  AddressUpdateRequest updateRequest){

        addressService.updateAddress(userId, addressId, updateRequest);

        return ResponseEntity.ok(ApiResponse.success("배송지 정보가 수정되었습니다."));
    }
}
