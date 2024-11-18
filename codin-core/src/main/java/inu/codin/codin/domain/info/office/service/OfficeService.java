package inu.codin.codin.domain.info.office.service;

import inu.codin.codin.domain.info.office.dto.OfficeListResDTO;
import inu.codin.codin.domain.info.office.dto.OfficeMemberResDTO;
import inu.codin.codin.domain.info.office.entity.Office;
import inu.codin.codin.domain.info.office.repository.OfficeRepository;
import inu.codin.codin.domain.user.entity.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfficeService {

    private final OfficeRepository officeRepository;
    public List<OfficeListResDTO> getAllOffice() {
        List<Office> offices = officeRepository.findAll();
        return offices.stream().map(OfficeListResDTO::of).toList();
    }

    public List<OfficeMemberResDTO> getOfficeByDepartment(Department department) {
        List<Office> offices = officeRepository.findAllByDepartment(department);
        return offices.stream().map(OfficeMemberResDTO::of).toList();
    }
}
