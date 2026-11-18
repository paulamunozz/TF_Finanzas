package com.upc.cuentix.services;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class CutoffScheduler {
    private final CutoffService service;
    @Scheduled(fixedDelay=60000)
    public void issueDue() {
        var now=LocalDateTime.now(CutoffService.LIMA);
        for(var id:service.accountIds()) service.issue(id,now);
    }
}
