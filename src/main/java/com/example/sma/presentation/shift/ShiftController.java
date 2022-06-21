package com.example.sma.presentation.shift;

import com.example.sma.application.employee.EmployeeApplicationService;
import com.example.sma.application.shift.ShiftApplicationService;
import com.example.sma.domain.models.shift.Shift;
import com.example.sma.domain.models.shift.ShiftPattern;
import com.example.sma.domain.models.shift.VacationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin("http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shift")
public class ShiftController {

    private final ShiftApplicationService shiftApplicationService;
    private final EmployeeApplicationService employeeApplicationService;

    @GetMapping("/{year}/{month}")
    public List<ShiftForm> getShift(@PathVariable("year") int year, @PathVariable("month") int month) {

        List<String> employeeNameList = employeeApplicationService.getEmployeeNameList();

        List<List<Shift>> allEmployeeShiftList
                = shiftApplicationService.findShift(year, month, employeeApplicationService.getEmployeeIdList());
        List<ShiftPattern> shiftPatterns = shiftApplicationService.findAllShiftPattern();

        return allEmployeeShiftList
                .stream()
                .map(individualShiftList -> new ShiftForm(individualShiftList, shiftPatterns, employeeNameList))
                .toList();
    }

    @GetMapping("/draft")
    public DraftForm getDraft() {

        LocalDateTime nextMonth = LocalDateTime.now().plusMonths(1);

        if (shiftApplicationService.shiftDontExist(nextMonth.getYear(),nextMonth.getMonthValue())) {
            //todo: 例外処理
            try {
                shiftApplicationService.createDraft(employeeApplicationService.findAllEmployee());
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        List<List<Shift>> shiftList = new ArrayList<>();
        shiftList = shiftApplicationService.findShift(
                nextMonth.getYear(),
                nextMonth.getMonthValue(),
                employeeApplicationService.getEmployeeIdList());

        List<String> employeeNameList = employeeApplicationService.getEmployeeNameList();
        List<ShiftPattern> shiftPatternList = shiftApplicationService.findAllShiftPattern();

        return new DraftForm(shiftList, employeeNameList, shiftPatternList);
    }

    @GetMapping("/vacation-requests")
    public List<VacationRequestListForm> getVacationRequestList() {
        List<Integer> employeeIdList = employeeApplicationService.getEmployeeIdList();
        List<String> employeeNameList = employeeApplicationService.getEmployeeNameList();
        List<List<VacationRequest>> vacationRequestList = shiftApplicationService.findAllVacationRequest(employeeIdList);

        List<VacationRequestListForm> hoge =
                vacationRequestList.stream().filter(individualRequestList -> !CollectionUtils.isEmpty(individualRequestList))
                        .map(individualRequestList -> new VacationRequestListForm(individualRequestList, employeeNameList)).toList();

        return hoge;
    }

    @PostMapping("/vacation-requests/{employeeId}")
    public String postVacationRequestList(@RequestBody VacationRequestListForm vacationRequestListForm) {
        if (!shiftApplicationService.registerVacationRequest(vacationRequestListForm)) return "{\"result\":\"false\"}";
        return "{\"result\":\"ok\"}";
    }

    @PutMapping("/vacation-requests/{employeeId}")
    public String patchVacationRequestList(@RequestBody VacationRequestListForm vacationRequestListForm) {
        shiftApplicationService.updateVacationRequest(vacationRequestListForm);
        return "{\"result\":\"ok\"}";
    }

    @GetMapping("/vacation-requests/{employeeId}")
    public VacationRequestListForm getVacationRequest(@PathVariable("employeeId") int employeeId) {
        return new VacationRequestListForm(shiftApplicationService.findVacationRequest(employeeId), employeeId);
    }

    @GetMapping("/patterns")
    public List<ShiftPattern> getShiftPatterns() {
        return shiftApplicationService.findAllShiftPattern();
    }

}
