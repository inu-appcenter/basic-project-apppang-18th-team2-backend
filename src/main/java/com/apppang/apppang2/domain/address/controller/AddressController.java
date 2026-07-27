package com.apppang.apppang2.domain.address.controller;

import com.apppang.apppang2.domain.address.dto.request.AddressRequest;
import com.apppang.apppang2.domain.address.dto.request.AddressUpdateRequest;
import com.apppang.apppang2.domain.address.dto.response.AddressResponse;
import com.apppang.apppang2.domain.address.dto.response.AddressUpdateDefaultResponse;
import com.apppang.apppang2.domain.address.service.AddressService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "ADDRESS")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
public class AddressController {
    private final AddressService addressService;

    //유저 배송지 목록 조회
    @Operation(summary = "배송지 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddress(@AuthenticationPrincipal Long userId){

        //서비스에 넘겨서 해당 ID로 배송지 목록 조회
        List<AddressResponse> addressList = addressService.getMyAddress(userId);

        return ResponseEntity.ok(ApiResponse.success("배송지 목록 조회에 성공했습니다.",addressList));
    }

    //배송지 추가
    @Operation(summary = "배송지 추가")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> addAddress(@AuthenticationPrincipal Long userId, @Valid @RequestBody AddressRequest request){

        //요청받은 정보로 배송지를 생성하고 생성된 주소의 ID 반환받음
        Long addressId = addressService.addAddress(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("배송지가 등록되었습니다.",addressId));
    }

    //배송지 수정
    @Operation(summary = "배송지 수정", description = "기존 배송지 정보를 수정합니다.")
    @PatchMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> updateAddress(@AuthenticationPrincipal Long userId, @PathVariable Long addressId, @Valid @RequestBody  AddressUpdateRequest updateRequest){

        addressService.updateAddress(userId, addressId, updateRequest);

        return ResponseEntity.ok(ApiResponse.success("배송지 정보가 수정되었습니다."));
    }

    //기본배송지 변경
    @Operation(summary = "기본 배송지 변경", description = "기본 배송지를 변경합니다.")
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<AddressUpdateDefaultResponse>> updateDefault(@AuthenticationPrincipal Long userId, @PathVariable Long addressId){

        AddressUpdateDefaultResponse data = addressService.updateDefaultAddress(userId, addressId);

        return ResponseEntity.ok(ApiResponse.success("기본 배송지가 변경되었습니다.", data));
    }

    //배송지 삭제
    @Operation(summary = "배송지 삭제")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@AuthenticationPrincipal Long userId, @PathVariable Long addressId){

        addressService.deleteAddress(userId, addressId);

        return ResponseEntity.ok(ApiResponse.success("배송지가 삭제되었습니다."));
    }
}
