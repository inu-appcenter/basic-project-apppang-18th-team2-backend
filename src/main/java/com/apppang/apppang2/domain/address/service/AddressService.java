package com.apppang.apppang2.domain.address;

import com.apppang.apppang2.domain.address.dto.AddressRequest;
import com.apppang.apppang2.domain.address.dto.AddressResponse;
import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    //유저 정보를 DB에서 먼저 조회
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;        //유저 존재 여부

    //로그인한 유저의 주소 목록을 가져옴
    public List<AddressResponse> getMyAddress(Long userId){
        return addressRepository.findByUserId(userId).stream()
                .map(AddressResponse::from)
                .toList();
    }

    //배송지 추가
    public Long addAddress(Long userId, AddressRequest request){

        //유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("유저를 찾을 수 없습니다."));


        //새로 추가할 주소가 기본배송지일 경우 기존에 등록된 기본배송지를 해제
        if(request.isDefault()){
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(existingAddress -> existingAddress.updateDefault(false));
        }

        //요청 정보를 바탕으로 새로운 배송지 엔티티 생성
        Address newAddress = Address.builder()
                .user(user)
                .receiver(request.getReceiver())
                .receiverPhone(request.getReceiverPhone())
                .roadAddress(request.getRoadAddress())
                .detailAddress(request.getDetailAddress())
                .isDefault(request.isDefault())
                .build();

        //DB에 저장하고 생성된 배송지 ID 반환
        Address savedAddress = addressRepository.save(newAddress);
        return savedAddress.getId();
    }

}
