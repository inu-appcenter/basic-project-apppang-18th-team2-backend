package com.apppang.apppang2.domain.address.service;

import com.apppang.apppang2.domain.address.dto.request.AddressRequest;
import com.apppang.apppang2.domain.address.dto.request.AddressUpdateRequest;
import com.apppang.apppang2.domain.address.dto.response.AddressResponse;
import com.apppang.apppang2.domain.address.dto.response.AddressUpdateDefaultResponse;
import com.apppang.apppang2.domain.address.entity.Address;
import com.apppang.apppang2.domain.address.repository.AddressRepository;
import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.domain.user.repository.UserRepository;
import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    //로그인한 유저의 주소 목록을 가져옴
    public List<AddressResponse> getMyAddress(Long userId){
        return addressRepository.findByUserId(userId).stream()
                .map(AddressResponse::from)
                .toList();
    }

    //배송지 추가
    @Transactional
    public Long addAddress(Long userId, AddressRequest request){
        //유저 조회
        //@AuthenticationPrincipal로 이미 검증된 값이라 불필요한 DB접근으로 판단됨 (롤백을 위해 주석처리만)
//        User user = userRepository.findById(userId)
//                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        User user = userRepository.getReferenceById(userId); //프록시로 처리


        //새로 추가할 주소가 기본배송지일 경우 기존에 등록된 기본배송지를 해제
        if(request.isDefaultAddress()){
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
                .isDefault(request.isDefaultAddress())
                .build();

        //DB에 저장하고 생성된 배송지 ID 반환
        Address savedAddress = addressRepository.save(newAddress);
        return savedAddress.getId();
    }

    //배송지 수정
    @Transactional
    public void updateAddress(Long userId, Long addressId, AddressUpdateRequest updateRequest){
        //addressId로 수정할 배송지를 DB에서 찾음
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "배송지를 찾을 수 없습니다."));

        log.info("토큰에서 뽑은 로그인 유저 Id : {}", userId);
        log.info("DB에 저장된 배송지 주인 Id : {}", address.getUser().getId());

        //배송지가 로그인한 유저의 것이 맞는지 권한 확인
        if(!address.getUser().getId().equals(userId)){
            throw new CustomException(HttpStatus.FORBIDDEN, "수정권한이 없습니다.");
        }

        //엔티티의 데이터 업데이트(메서드가 종료되는 시점에 @Transactional이 엔티티 값 변경 감지하고 자동으로 DB 업데이트)
        address.updateAddress(updateRequest);

    }

    //기본배송지 수정
    @Transactional
    public AddressUpdateDefaultResponse updateDefaultAddress(Long userId, Long addressId){
        //기본 배송지로 만들 타겟 배송지 조회
        Address targetAddress = addressRepository.findById(addressId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND,"배송지를 찾을 수 없습니다."));

        //배송지가 로그인한 유저의 것이 맞는지 권한 확인
        if(!targetAddress.getUser().getId().equals(userId)){
            log.warn("권한 없는 배송지 접근 userId : {}, addressId : {}",userId, addressId);
            throw new CustomException(HttpStatus.FORBIDDEN, "수정권한이 없습니다.");
        }

        //기존에 기본배송지가 있다면 일반배송지로 변경
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                //기존 기본배송지 객체가 존재한다면 oldDefault 이름을 붙여 객체값 변경
                .ifPresent(oldDefault->{
                    oldDefault.updateDefault(false);
                });

        //새로운 타겟 배송지를 기본 배송지로 설정
        targetAddress.updateDefault(true);
        log.info("기본 배송지 변경 완료 userId : {}, 새로운 기본 addressId : {}",userId, addressId);

        return new AddressUpdateDefaultResponse(targetAddress.getId(), targetAddress.isDefault());
    }

    //배송지 삭제
    @Transactional
    public void deleteAddress(Long userId, Long addressId){
        //삭제할 배송지 조회
        Address address = addressRepository.findById(addressId)
                //다른 응답과 상태코드가 달라 404로 통일
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "배송지를 찾을 수 없습니다."));

        //배송지가 로그인한 유저의 것이 맞는지 권한 확인
        if(!address.getUser().getId().equals(userId)){
            log.warn("권한 없는 배송지 접근 userId : {}, addressId : {}",userId, addressId);
            throw new CustomException(HttpStatus.FORBIDDEN,"삭제권한이 없습니다.");
        }

        //기본배송지일 경우 삭제할 수 없음
        if(address.isDefault()){
            throw new CustomException(HttpStatus.CONFLICT, "기본 배송지는 삭제할 수 없습니다.");
        }

        addressRepository.delete(address);
        log.info("배송지 삭제 완료");
    }
}
