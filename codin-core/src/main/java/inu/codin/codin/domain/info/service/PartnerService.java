package inu.codin.codin.domain.info.service;

import inu.codin.codin.domain.info.dto.request.PartnerCreateRequestDto;
import inu.codin.codin.domain.info.dto.response.PartnerDetailsResponseDto;
import inu.codin.codin.domain.info.dto.response.PartnerListResponseDto;
import inu.codin.codin.domain.info.entity.Partner;
import inu.codin.codin.domain.info.exception.InfoErrorCode;
import inu.codin.codin.domain.info.exception.InfoException;
import inu.codin.codin.domain.info.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;

    public List<PartnerListResponseDto> getPartnerList() {
        List<Partner> partners = partnerRepository.findAll();
        return partners.stream()
                .map(PartnerListResponseDto::from)
                .toList();
    }

    public PartnerDetailsResponseDto getPartnerDetails(String partnerId) {
        Partner partner = partnerRepository.findById(new ObjectId(partnerId))
                .orElseThrow(() -> new InfoException(InfoErrorCode.PARTNER_NOT_FOUND));
        return PartnerDetailsResponseDto.from(partner);
    }

    public void createPartner(PartnerCreateRequestDto partnerCreateRequestDto) {
        // todo 이미지 업로드, url 받기
        Partner partner = Partner.of(partnerCreateRequestDto);
        partnerRepository.save(partner);
    }
}
