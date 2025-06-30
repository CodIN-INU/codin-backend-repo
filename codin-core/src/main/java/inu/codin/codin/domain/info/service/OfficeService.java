package inu.codin.codin.domain.info.service;

import inu.codin.codin.domain.info.dto.request.OfficeMemberCreateUpdateRequestDto;
import inu.codin.codin.domain.info.dto.request.OfficeUpdateRequestDto;
import inu.codin.codin.domain.info.dto.response.OfficeDetailsResponseDto;
import inu.codin.codin.domain.info.entity.Office;
import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.info.entity.OfficeMember;
import inu.codin.codin.domain.info.repository.InfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfficeService {

    private final InfoRepository infoRepository;

    public OfficeDetailsResponseDto getOfficeByDepartment(Department department) {
        log.info("[getOfficeByDepartment] '{}' 사무실 정보 조회 시작", department.getDescription());
        Office office = infoRepository.findOfficeByDepartment(department);
        log.info("[getOfficeByDepartment] '{}' 사무실 정보 열람", office.getDepartment().getDescription());
        return OfficeDetailsResponseDto.of(office);
    }

    public void createOfficeMember(Department department, OfficeMemberCreateUpdateRequestDto officeMemberCreateUpdateRequestDto) {
        log.info("[createOfficeMember] '{}' 사무실에 멤버 추가 시도", department.getDescription());
        Office office = infoRepository.findOfficeByDepartment(department);
        OfficeMember officeMember = OfficeMember.of(officeMemberCreateUpdateRequestDto);
        office.addOfficeMember(officeMember);
        infoRepository.save(office);
        log.info("[createOfficeMember] '{}' 사무실의 멤버 추가", office.getDepartment().getDescription());
    }

    public void updateOffice(Department department, OfficeUpdateRequestDto officeUpdateRequestDto) {
        Office office = infoRepository.findOfficeByDepartment(department);
        office.update(officeUpdateRequestDto);
        infoRepository.save(office);
        log.info("[updateOffice] '{}' 사무실 정보 업데이트", office.getDepartment().getDescription());
    }

    public void updateOfficeMember(Department department, int num, OfficeMemberCreateUpdateRequestDto officeMemberCreateUpdateRequestDto) {
        Office office = infoRepository.findOfficeByDepartment(department);
        OfficeMember officeMember = office.getMember().get(num);
        officeMember.update(officeMemberCreateUpdateRequestDto);
        infoRepository.save(office);
        log.info("[updateOfficeMember] '{}' 사무실의 '{}' 멤버 정보 업데이트", office.getDepartment().getDescription(), officeMember.getName());
    }

    public void deleteOfficeMember(Department department, int num) {
        Office office = infoRepository.findOfficeByDepartment(department);
        office.getMember().get(num).delete();
        infoRepository.save(office);
        log.info("[deleteOfficeMember] '{} 사무실의 '{}' 멤버 정보 삭제", department.getDescription(), office.getMember().get(num).getName());
    }
}
