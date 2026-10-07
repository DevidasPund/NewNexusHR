package com.nexushr.leave;

import com.nexushr.common.exception.BadRequestException;
import com.nexushr.common.exception.ResourceNotFoundException;
import com.nexushr.common.enums.LeaveStatus;
import com.nexushr.common.enums.LeaveType;
import com.nexushr.employee.Employee;
import com.nexushr.employee.EmployeeService;
import com.nexushr.leave.dto.CreateLeaveRequest;
import com.nexushr.leave.dto.LeaveBalanceDto;
import com.nexushr.leave.dto.LeaveRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class LeaveService {

    /** Default annual allocation per leave type (days). */
    public static final Map<LeaveType, Double> DEFAULT_ALLOCATION = new EnumMap<>(LeaveType.class);

    static {
        DEFAULT_ALLOCATION.put(LeaveType.CASUAL, 12.0);
        DEFAULT_ALLOCATION.put(LeaveType.SICK, 10.0);
        DEFAULT_ALLOCATION.put(LeaveType.EARNED, 15.0);
        DEFAULT_ALLOCATION.put(LeaveType.UNPAID, 0.0);
    }

    private final LeaveRequestRepository requestRepo;
    private final LeaveBalanceRepository balanceRepo;
    private final EmployeeService employeeService;

    public LeaveService(LeaveRequestRepository requestRepo,
                        LeaveBalanceRepository balanceRepo,
                        EmployeeService employeeService) {
        this.requestRepo = requestRepo;
        this.balanceRepo = balanceRepo;
        this.employeeService = employeeService;
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> listAll() {
        return requestRepo.findAllByOrderByStartDateDesc().stream()
                .map(LeaveRequestDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> listPending() {
        return requestRepo.findByStatusOrderByStartDateDesc(LeaveStatus.PENDING).stream()
                .map(LeaveRequestDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> listForEmployee(Long employeeId) {
        return requestRepo.findByEmployee_IdOrderByStartDateDesc(employeeId).stream()
                .map(LeaveRequestDto::from)
                .toList();
    }

    @Transactional
    public LeaveRequestDto create(Long employeeId, CreateLeaveRequest req) {
        if (req.endDate().isBefore(req.startDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }
        Employee employee = employeeService.getEntity(employeeId);
        int days = (int) (ChronoUnit.DAYS.between(req.startDate(), req.endDate()) + 1);

        LeaveRequest r = new LeaveRequest();
        r.setEmployee(employee);
        r.setType(req.type());
        r.setStartDate(req.startDate());
        r.setEndDate(req.endDate());
        r.setDays(days);
        r.setReason(req.reason());
        r.setStatus(LeaveStatus.PENDING);
        return LeaveRequestDto.from(requestRepo.save(r));
    }

    @Transactional
    public LeaveRequestDto approve(Long requestId, Long approverId) {
        LeaveRequest r = load(requestId);
        requirePending(r);
        Employee approver = employeeService.getEntity(approverId);
        r.setStatus(LeaveStatus.APPROVED);
        r.setApproverId(approverId);
        r.setApproverName(approver.getFullName());
        r.setDecidedAt(Instant.now());
        applyToBalance(r.getEmployee().getId(), r.getType(), r.getDays());
        return LeaveRequestDto.from(requestRepo.save(r));
    }

    @Transactional
    public LeaveRequestDto reject(Long requestId, Long approverId) {
        LeaveRequest r = load(requestId);
        requirePending(r);
        Employee approver = employeeService.getEntity(approverId);
        r.setStatus(LeaveStatus.REJECTED);
        r.setApproverId(approverId);
        r.setApproverName(approver.getFullName());
        r.setDecidedAt(Instant.now());
        return LeaveRequestDto.from(requestRepo.save(r));
    }

    @Transactional
    public LeaveRequestDto cancel(Long requestId, Long employeeId) {
        LeaveRequest r = load(requestId);
        if (!r.getEmployee().getId().equals(employeeId)) {
            throw new BadRequestException("You can only cancel your own leave requests");
        }
        if (r.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending requests can be cancelled");
        }
        r.setStatus(LeaveStatus.CANCELLED);
        r.setDecidedAt(Instant.now());
        return LeaveRequestDto.from(requestRepo.save(r));
    }

    @Transactional
    public List<LeaveBalanceDto> balancesForEmployee(Long employeeId) {
        ensureBalances(employeeId);
        return balanceRepo.findByEmployee_Id(employeeId).stream()
                .sorted(Comparator.comparingInt(b -> b.getType().ordinal()))
                .map(LeaveBalanceDto::from)
                .toList();
    }

    /** Creates any missing default balance rows for the employee. */
    @Transactional
    public void ensureBalances(Long employeeId) {
        List<LeaveBalance> existing = balanceRepo.findByEmployee_Id(employeeId);
        List<LeaveType> present = existing.stream().map(LeaveBalance::getType).toList();
        Employee employee = employeeService.getEntity(employeeId);
        List<LeaveBalance> toCreate = new ArrayList<>();
        for (LeaveType type : LeaveType.values()) {
            if (!present.contains(type)) {
                toCreate.add(new LeaveBalance(employee, type, DEFAULT_ALLOCATION.getOrDefault(type, 0.0)));
            }
        }
        if (!toCreate.isEmpty()) {
            balanceRepo.saveAll(toCreate);
        }
    }

    private void applyToBalance(Long employeeId, LeaveType type, int days) {
        LeaveBalance balance = balanceRepo.findByEmployee_IdAndType(employeeId, type)
                .orElseGet(() -> {
                    Employee employee = employeeService.getEntity(employeeId);
                    return new LeaveBalance(employee, type, DEFAULT_ALLOCATION.getOrDefault(type, 0.0));
                });
        balance.setUsed(balance.getUsed() + days);
        balanceRepo.save(balance);
    }

    private LeaveRequest load(Long id) {
        return requestRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));
    }

    private void requirePending(LeaveRequest r) {
        if (r.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request has already been " + r.getStatus().name().toLowerCase());
        }
    }
}
