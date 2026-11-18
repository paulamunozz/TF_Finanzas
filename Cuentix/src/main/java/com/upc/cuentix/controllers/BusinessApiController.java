package com.upc.cuentix.controllers;

import com.upc.cuentix.services.BusinessApplicationService;
import com.upc.cuentix.services.BusinessApplicationService.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BusinessApiController {
  private final BusinessApplicationService service;

  public BusinessApiController(BusinessApplicationService service) {
    this.service = service;
  }

  @GetMapping("/business/context")
  public Context context(HttpSession s) {
    return service.context(s);
  }

  @GetMapping("/clients")
  public List<ClientView> clients() {
    return service.clients();
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
  public List<ProductView> catalog(HttpSession s) {
    return service.catalog(s);
  }

  @PostMapping("/catalog")
  public ProductView createProduct(@RequestBody ProductView d, HttpSession s) {
    return service.createProduct(d, s);
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

  @GetMapping("/purchases")
  public List<PurchaseView> purchases() {
    return service.purchases();
  }

  @GetMapping("/purchases/{id}")
  public PurchaseView purchase(@PathVariable Integer id) {
    return service.purchase(id);
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
