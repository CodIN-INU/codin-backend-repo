package inu.codin.codin.domain.info.service;

import inu.codin.codin.domain.info.dto.request.PartnerCreateRequestDto;
import inu.codin.codin.domain.info.dto.response.PartnerDetailsResponseDto;
import inu.codin.codin.domain.info.dto.response.PartnerListResponseDto;
import inu.codin.codin.domain.info.entity.Partner;
import inu.codin.codin.domain.info.entity.PartnerImg;
import inu.codin.codin.domain.info.exception.InfoErrorCode;
import inu.codin.codin.domain.info.exception.InfoException;
import inu.codin.codin.domain.info.repository.PartnerRepository;
import inu.codin.codin.infra.s3.S3Service;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartnerServiceTest {

    @InjectMocks
    PartnerService partnerService;

    @Mock
    PartnerRepository partnerRepository;

    @Mock
    S3Service s3Service;

    @Test
    @DisplayName("제휴업체 목록 조회")
    void 제휴업체_목록_반환() {
        //given
        Partner partner = mock(Partner.class);
        given(partner.get_id()).willReturn(new ObjectId());
        given(partner.getImg()).willReturn(new PartnerImg("img1", List.of("img2")));

        given(partnerRepository.findAll()).willReturn(List.of(partner));

        //when
        List<PartnerListResponseDto> responseDtos = partnerService.getPartnerList();

        //then
        assertThat(responseDtos).hasSize(1);
    }

    @Test
    @DisplayName("제휴업체 상세 조회 - 성공")
    void 제휴업체_상세_조회_성공() {
        //given
        String partnerId = new ObjectId().toString();
        Partner partner = mock(Partner.class);
        given(partnerRepository.findById(new ObjectId(partnerId))).willReturn(Optional.ofNullable(partner));

        //when
        PartnerDetailsResponseDto responseDto = partnerService.getPartnerDetails(partnerId);

        //then
        assertNotNull(responseDto);
    }

    @Test
    @DisplayName("제휴업체 상세 조회 - 성공")
    void 제휴업체_상세_조회_실패() {
        //given
        String partnerId = new ObjectId().toString();
        given(partnerRepository.findById(new ObjectId(partnerId))).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> partnerService.getPartnerDetails(partnerId))
                .isInstanceOf(InfoException.class)
                .hasMessageContaining(InfoErrorCode.PARTNER_NOT_FOUND.message());
    }

    @Test
    @DisplayName("제휴업체 생성")
    void 제휴업체_생성() {
        //given
        PartnerCreateRequestDto requestDto = mock(PartnerCreateRequestDto.class);
        MultipartFile mainImage = mock(MultipartFile.class);
        List<MultipartFile> subImage = List.of(mock(MultipartFile.class));

        given(s3Service.handleImageUpload(List.of(mainImage))).willReturn(List.of("mainImage"));
        given(s3Service.handleImageUpload(subImage)).willReturn(List.of("subImage1", "subImage2"));

        //when
        partnerService.createPartner(requestDto, mainImage, subImage);

        //then
        ArgumentCaptor<Partner> captor = ArgumentCaptor.forClass(Partner.class);
        verify(partnerRepository, times(1)).save(captor.capture());

        Partner savedPartner = captor.getValue();
        assertNotNull(savedPartner);
    }

    @Test
    @DisplayName("이미지 없이 제휴업체 생성 성공")
    void 이미지_없이_제휴업체_생성_성공(){
        //given
        PartnerCreateRequestDto requestDto = mock(PartnerCreateRequestDto.class);

        //when
        partnerService.createPartner(requestDto, null, null);

        //then
        ArgumentCaptor<Partner> captor = ArgumentCaptor.forClass(Partner.class);
        verify(partnerRepository, times(1)).save(captor.capture());

        Partner savedPartner = captor.getValue();
        assertNotNull(savedPartner);
    }

    @Test
    @DisplayName("제휴업체 삭제")
    void 제휴업체_삭제() {
        //given
        String partnerId = new ObjectId().toString();

        //when
        partnerService.deletePartner(partnerId);

        //then
        verify(partnerRepository, times(1)).deleteById(new ObjectId(partnerId));
    }
}