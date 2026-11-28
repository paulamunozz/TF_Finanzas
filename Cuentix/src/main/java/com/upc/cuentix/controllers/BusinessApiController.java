package com.upc.cuentix.controllers;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

import com.upc.cuentix.dto.UnidadMedidaDTO;
import com.upc.cuentix.services.BusinessApplicationService;
import com.upc.cuentix.services.BusinessApplicationService.*;
import com.upc.cuentix.services.GroupedPaymentService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BusinessApiController {
  private final BusinessApplicationService service;
  private final GroupedPaymentService groupedPayments;

  public BusinessApiController(BusinessApplicationService service, GroupedPaymentService groupedPayments) {
    this.service = service;
    this.groupedPayments = groupedPayments;
  }

  @GetMapping("/business/context")
  public Context context(HttpSession s) {
    return service.context(s);
  }

  @GetMapping("/clients")
  public List<ClientView> clients(@RequestParam(defaultValue="false") boolean includeInactive) {
    return service.clients(includeInactive);
  }

  @GetMapping("/clients/{id}")
  public ClientView client(@PathVariable Integer id) {
    return service.client(id);
  }

  @PostMapping("/clients")
  public ClientView createClient(@RequestBody ClientView d, HttpSession s) {
    return service.createClient(d, s);
  }

  @PutMapping("/clients/{id}")
  public ClientView updateClient(@PathVariable Integer id, @RequestBody ClientView d) {
    return service.updateClient(id, d);
  }

  @DeleteMapping("/clients/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteClient(@PathVariable Integer id) {
    service.deleteClient(id);
  }

  @GetMapping("/catalog")
  public List<ProductView> catalog(HttpSession s, @RequestParam(defaultValue="false") boolean includeInactive) {
    return includeInactive ? service.allProducts(s) : service.catalog(s);
  }

  @PostMapping("/catalog")
  public ProductView createProduct(@RequestBody ProductView d, HttpSession s) {
    return service.createProduct(d, s);
  }

  @GetMapping("/catalog/currencies")
  public List<CurrencyOption> currencies() {
    return service.currencyOptions();
  }

  @GetMapping("/catalog/units")
  public List<UnidadMedidaDTO> units() {
    return service.unitOptions();
  }

  @PutMapping("/catalog/{id}")
  public ProductView updateProduct(@PathVariable Integer id, @RequestBody ProductView d) {
    return service.updateProduct(id, d);
  }

  @DeleteMapping("/catalog/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProduct(@PathVariable Integer id) {
    service.deleteProduct(id);
  }

  @PutMapping("/clients/{id}/activate")
  public ClientView activateAccount(@PathVariable Integer id) { return service.activateAccount(id); }

  @PutMapping("/catalog/{id}/activate")
  public ProductView activateProduct(@PathVariable Integer id) { return service.activateProduct(id); }

  @GetMapping("/purchases")
  public List<PurchaseView> purchases() {
    return service.purchases();
  }

  @GetMapping("/purchases/{id}")
  public PurchaseView purchase(@PathVariable Integer id) {
    return service.purchase(id);
  }

  @PostMapping("/business/purchases/quote")
  public PurchaseQuote quotePurchase(@RequestBody PurchaseRequest d) { return service.quotePurchase(d); }

  @GetMapping("/payments/{id}/quote")
  public PaymentView quotePayment(@PathVariable Integer id, @RequestParam @DateTimeFormat(iso = DATE) LocalDate paidAt) {
    return service.paymentQuote(id, paidAt);
  }

  @GetMapping("/clients/{accountId}/payments/{dueDate}/quote")
  public GroupedPaymentService.Quote quoteGroupedPayment(
      @PathVariable Integer accountId, @PathVariable @DateTimeFormat(iso = DATE) LocalDate dueDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate paidAt) {
    return groupedPayments.quote(accountId, dueDate, paidAt);
  }

  @PostMapping("/clients/{accountId}/payments/{dueDate}/confirm")
  public GroupedPaymentService.Receipt confirmGroupedPayment(
      @PathVariable Integer accountId, @PathVariable @DateTimeFormat(iso = DATE) LocalDate dueDate,
      @RequestBody GroupedPaymentService.Confirmation request) {
    return groupedPayments.confirm(accountId, dueDate, request);
  }

  @PostMapping("/business/purchases")
  public PurchaseView createPurchase(@RequestBody PurchaseRequest d) {
    return service.createPurchase(d);
  }

  @GetMapping("/payments")
  public List<PaymentView> payments(
      @RequestParam(required = false) Integer clientId,
      @RequestParam(required = false) Integer purchaseId) {
    return service.payments(clientId, purchaseId);
  }

  @GetMapping("/payments/{id}")
  public PaymentView payment(@PathVariable Integer id) {
    return service.payment(id);
  }

  @PostMapping("/payments/{id}/cancel")
  public PaymentView cancel(@PathVariable Integer id, @RequestBody PaymentRequest d) {
    return service.cancel(id, d);
  }
}
