package inu.codin.codin.domain.info.domain.office.service;

import inu.codin.codin.domain.info.domain.office.dto.OfficeListResponseDto;
import inu.codin.codin.domain.info.domain.office.dto.OfficeMemberResponseDto;
import inu.codin.codin.domain.info.domain.office.entity.Office;
import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.repository.InfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfficeService {

    private final InfoRepository infoRepository;
    public List<OfficeListResponseDto> getAllOffice() {
        List<Office> offices = infoRepository.findAllOffices();
        return offices.stream().map(OfficeListResponseDto::of).toList();
    }

    public List<OfficeMemberResponseDto> getOfficeByDepartment(Department department) {
        List<Office> offices = infoRepository.findOfficeByDepartment(department);
        return offices.stream().map(OfficeMemberResponseDto::of).toList();
    }
}
