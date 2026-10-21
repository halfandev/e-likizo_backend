
package com.abe.e_likizo.leave.entity;

import com.abe.e_likizo.leave.service.LeaveTypeService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LeaveDataInitializer implements CommandLineRunner {

    private final LeaveTypeService leaveTypeService;

    public LeaveDataInitializer(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @Override
    public void run(String... args) {
        leaveTypeService.initializeDefaultLeaveTypes();
    }
}
