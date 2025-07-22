//package inu.codin.codinticketingapi.domain.ticketing.controller;
//
//
//import inu.codin.codinticketingapi.common.response.SingleResponse;
//import inu.codin.codinticketingapi.domain.ticketing.dto.request.TicketingUserProfileRequest;
//import inu.codin.codinticketingapi.domain.ticketing.service.ProfileService;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/ticketing")
//@RequiredArgsConstructor
//@Tag(name = "Ticketing Profile API", description = "티켓팅 수령자 정보 API")
//public class ProfileController {
//
//    private final ProfileService profileService;
//
//    /** 접속 유저 티켓팅 수령자 정보 조회 */
//    @GetMapping("/user-profile")
//    public ResponseEntity<SingleResponse<?>> getUserTicketingProfile() {
//        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 수령자 정보 반환",
//                profileService.getUserTicketingProfile()));
//    }
//
//    /** 티켓팅 수령자 정보 입력 */
//    @PostMapping("/user-profile")
//    public ResponseEntity<SingleResponse<?>> registerUserTicketingInfo(
//            @RequestBody TicketingUserProfileRequest ticketingUserProfileRequest
//    ) {
//        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 수령자 정보 입력 성공",
//                profileService.createUserTicketingProfile(ticketingUserProfileRequest)));
//    }
//}
