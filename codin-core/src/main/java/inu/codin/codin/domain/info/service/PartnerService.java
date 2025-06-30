package inu.codin.codin.domain.info.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.info.dto.response.PartnerDetailsResponseDto;
import inu.codin.codin.domain.info.dto.response.PartnerListResponseDto;
import inu.codin.codin.domain.info.entity.Partner;
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
                .orElseThrow(() -> new NotFoundException("제휴업체를 찾을 수 없습니다."));
        return PartnerDetailsResponseDto.from(partner);
    }
}
