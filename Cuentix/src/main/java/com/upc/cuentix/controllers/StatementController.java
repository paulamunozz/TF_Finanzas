package com.upc.cuentix.controllers;

import com.upc.cuentix.services.*;
import jakarta.servlet.http.HttpSession;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatementController {
    private final CutoffService cutoffs;
    private final BusinessApplicationService business;
    @GetMapping("/business/cutoff")
    public List<CutoffService.Report> reports(@RequestParam String month,HttpSession session) {
        return cutoffs.reports(Integer.valueOf(business.context(session).id()),YearMonth.parse(month));
    }
    public record Statement(BusinessApplicationService.ClientView client,List<BusinessApplicationService.PurchaseView> purchases,
        List<BusinessApplicationService.PaymentView> items) {}
    @GetMapping("/clients/{id}/statement")
    public Statement statement(@PathVariable Integer id) {
        var client=business.client(id);
        return new Statement(client,business.purchases().stream().filter(p -> p.clientId().equals(id.toString())).toList(),business.payments(id,null));
    }
}
