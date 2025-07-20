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
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final S3Service s3Service;

    public List<PartnerListResponseDto> getPartnerList() {
        return partnerRepository.findAll()
                        .stream()
                        .map(PartnerListResponseDto::from)
                        .toList();
    }

    public PartnerDetailsResponseDto getPartnerDetails(String partnerId) {
        Partner partner = partnerRepository.findById(new ObjectId(partnerId))
                .orElseThrow(() -> new InfoException(InfoErrorCode.PARTNER_NOT_FOUND));
        return PartnerDetailsResponseDto.from(partner);
    }

    public void createPartner(PartnerCreateRequestDto partnerCreateRequestDto, MultipartFile mainImage, List<MultipartFile> subImages) {
        PartnerImg partnerImg = getPartnerImg(mainImage, subImages);
        Partner partner = Partner.of(partnerCreateRequestDto, partnerImg);
        partnerRepository.save(partner);
    }

    private PartnerImg getPartnerImg(MultipartFile mainImage, List<MultipartFile> subImages) {
        String main = Optional.ofNullable(mainImage)
                .map(img -> s3Service.handleImageUpload(List.of(img)).get(0))
                .orElse(null);
        List<String> subs = s3Service.handleImageUpload(subImages);
        return new PartnerImg(main, subs);
    }

    public void deletePartner(String partnerId) {
        partnerRepository.deleteById(new ObjectId(partnerId));
    }
}
