package inu.codin.codin.domain.report.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/suspends")
public class SuspendMvcController {

    @GetMapping
    public String getSuspendedView(@RequestParam("endDate") String endDate, Model model){
        LocalDateTime dateTime = LocalDateTime.parse(endDate);
        LocalDateTime adjustedDateTime = dateTime.plusDays(1);
        String formattedDate = adjustedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        model.addAttribute("endDate", formattedDate);
        return "suspend";
    }
}
