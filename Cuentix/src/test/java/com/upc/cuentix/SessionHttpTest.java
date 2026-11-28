package com.upc.cuentix;

import static org.junit.jupiter.api.Assertions.*;

import com.upc.cuentix.entities.*;
import com.upc.cuentix.repositories.*;
import com.upc.cuentix.services.CutoffService;
import java.math.BigDecimal;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.json.JsonMapper;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SessionHttpTest {
  @LocalServerPort int port;
  @Autowired RolRepository roles;
  @Autowired UsuarioRepository users;
  @Autowired ClienteRepository clients;
  @Autowired NegocioRepository businesses;
  @Autowired CuentaCreditoRepository accounts;
  @Autowired CompraRepository purchases;
  @Autowired CutoffService cutoffs;
  @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
  private final JsonMapper json = new JsonMapper();
  private final HttpClient http =
      HttpClient.newBuilder()
          .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
          .build();

  private HttpResponse<String> send(String method, String url, String body, String token)
      throws Exception {
    return http.send(request(method, url, body, token), HttpResponse.BodyHandlers.ofString());
  }

  private HttpRequest request(String method, String url, String body, String token) {
    var b =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + url))
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/json");
    if (token != null) b.header("X-Cuentix-CSRF", token);
    return b.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : body)).build();
  }

  private Rol role(int id, String name) {
    return roles
        .findById(id)
        .orElseGet(
            () -> {
              jdbc.update("insert into rol(id_rol,nombre) values (?,?)", id, name);
              return roles.findById(id).orElseThrow();
            });
  }

  private Usuario user(String code, Rol role) {
    Usuario u = new Usuario();
    u.setCodigo(code);
    u.setEstado(true);
    u.setRol(role);
    u.setContrasena(new BCryptPasswordEncoder().encode("Password123"));
    return users.save(u);
  }

  private Cliente client(Usuario u) {
    Cliente c = new Cliente();
    c.setUsuario(u);
    c.setEstado(true);
    c.setNombre("Ana");
    c.setApellido("Perez");
    c.setTelefono("999123456");
    return clients.save(c);
  }

  private String field(String json, String name) {
    var m = Pattern.compile("\"" + name + "\"\\s*:\\s*\"?([^\",}]+)").matcher(json);
    assertTrue(m.find(), json);
    return m.group(1);
  }

  private void seedSol() {
    jdbc.update("""
        insert into moneda(nombre, simbolo)
        select 'Sol', 'S/' where not exists (select 1 from moneda where nombre = 'Sol')
        """);
  }

  @Test
  void referenceCatalogUsesStoredIdsForCreditAccountsProductsAndBusinesses() throws Exception {
    client(user("22334455", role(2, "CLIENTE")));
    var shop = new Negocio();
    shop.setUsuario(user("20199200123", role(3, "NEGOCIO")));
    shop.setRazonSocial("Tienda de catálogos");
    shop.setDireccion("Lima");
    shop.setTelefono("999111222");
    shop.setEstado(true);
    businesses.save(shop);
    jdbc.update("insert into moneda(id_moneda, nombre, simbolo) values (71, 'Dólar de prueba', '$')");
    jdbc.update("insert into unidad_medida(id_um, nombre) values (81, 'gramo'), (82, 'libra')");

    assertEquals(401, send("GET", "/api/catalog/currencies", null, null).statusCode());
    var login = send("POST", "/api/auth/login",
        """
        {"codigo":"20199200123","contrasena":"Password123"}
        """, null);
    assertEquals(200, login.statusCode(), login.body());
    String token = field(login.body(), "csrfToken");
    var currencies = send("GET", "/api/catalog/currencies", null, null);
    assertEquals(200, currencies.statusCode(), currencies.body());
    assertTrue(json.readTree(currencies.body()).valueStream().anyMatch(option ->
        option.get("id").asInt() == 71 && option.get("nombre").asText().equals("Dólar de prueba")
            && option.get("simbolo").asText().equals("$") && option.get("code").asText().equals("USD")));
    var units = send("GET", "/api/catalog/units", null, null);
    assertEquals(200, units.statusCode(), units.body());
    assertTrue(json.readTree(units.body()).valueStream().anyMatch(option ->
        option.get("id").asInt() == 81 && option.get("nombre").asText().equals("gramo")));
    assertEquals(403, send("GET", "/api/admin/businesses/giros", null, null).statusCode());

    String account = """
        {"dni":"22334455","currencyId":%d,"currency":"USD","compensatoryRate":0,
         "rateType":"TE","ratePeriod":"Mensual","lateRate":0,"lateRateType":"TN",
         "maxMonths":12,"creditLine":500,"cutOffDay":20,"paymentDay":25}
        """;
    assertEquals(400, send("POST", "/api/clients", account.formatted(9999), token).statusCode());
    var agreement = send("POST", "/api/clients", account.formatted(71), token);
    assertEquals(200, agreement.statusCode(), agreement.body());
    assertEquals("71", field(agreement.body(), "currencyId"));
    assertEquals("USD", field(agreement.body(), "currency"));
    String agreementUrl = "/api/clients/" + field(agreement.body(), "id");
    assertEquals(400, send("PUT", agreementUrl, account.formatted(9999), token).statusCode());
    var savedAgreement = send("GET", agreementUrl, null, null);
    assertEquals("71", field(savedAgreement.body(), "currencyId"));

    String product = """
        {"name":"Producto de prueba de unidad","unitId":%d,"cashPrice":10,"creditPrice":15}
        """;
    var created = send("POST", "/api/catalog", product.formatted(81), token);
    assertEquals(200, created.statusCode(), created.body());
    assertEquals("gramo", field(created.body(), "unit"));
    String productUrl = "/api/catalog/" + field(created.body(), "id");
    assertEquals(400, send("PUT", productUrl, product.formatted(9999), token).statusCode());
    var edited = send("PUT", productUrl, product.formatted(82), token);
    assertEquals(200, edited.statusCode(), edited.body());
    assertEquals("82", field(edited.body(), "unitId"));
    assertEquals("libra", field(edited.body(), "unit"));

    user("catalog-admin", role(1, "ADMIN"));
    jdbc.update("insert into giro(id_giro, nombre) values (91, 'Servicios de prueba')");
    var adminLogin = send("POST", "/api/auth/login",
        """
        {"codigo":"catalog-admin","contrasena":"Password123"}
        """, null);
    assertEquals(200, adminLogin.statusCode(), adminLogin.body());
    String adminToken = field(adminLogin.body(), "csrfToken");
    var sectors = send("GET", "/api/admin/businesses/giros", null, null);
    assertEquals(200, sectors.statusCode(), sectors.body());
    assertTrue(json.readTree(sectors.body()).valueStream().anyMatch(option ->
        option.get("id").asInt() == 91 && option.get("nombre").asText().equals("Servicios de prueba")));
    String business = """
        {"ruc":"20991234567","contrasena":"Password123","razonSocial":"Tienda de prueba",
         "direccion":"Lima","telefono":"999111222","giroId":%d}
        """;
    assertEquals(400, send("POST", "/api/admin/businesses", business.formatted(9999), adminToken).statusCode());
    var registered = send("POST", "/api/admin/businesses", business.formatted(91), adminToken);
    assertEquals(200, registered.statusCode(), registered.body());
    assertEquals("91", field(registered.body(), "giroId"));
  }

  @Test
  void groupedPaymentIsAtomicAndReconcilesWithTheIssuedStatement() throws Exception {
    var buyer = client(user("11337799", role(2, "CLIENTE")));
    var shop = new Negocio();
    shop.setUsuario(user("20998877665", role(3, "NEGOCIO")));
    shop.setRazonSocial("Tienda con cobro conjunto");
    shop.setDireccion("Lima");
    shop.setTelefono("999111222");
    shop.setEstado(true);
    shop = businesses.save(shop);
    seedSol();
    jdbc.update("insert into unidad_medida(nombre) values ('pieza')");

    var login = send("POST", "/api/auth/login",
        """
        {"codigo":"20998877665","contrasena":"Password123"}
        """, null);
    assertEquals(200, login.statusCode(), login.body());
    String token = field(login.body(), "csrfToken");
    var agreement = send("POST", "/api/clients",
        """
        {"dni":"11337799","name":"Ana Perez","currency":"PEN","compensatoryRate":0,
         "rateType":"TE","ratePeriod":"Mensual","lateRate":36,"lateRateType":"TN",
         "maxMonths":120,"creditLine":1000,"cutOffDay":20,"cutoffTime":"18:00","paymentDay":25}
        """, token);
    assertEquals(200, agreement.statusCode(), agreement.body());
    var accountId = Integer.valueOf(field(agreement.body(), "id"));
    var product = send("POST", "/api/catalog",
        """
        {"name":"Producto para el corte","unit":"pieza","cashPrice":100,"creditPrice":100}
        """, token);
    assertEquals(200, product.statusCode(), product.body());
    int productId = Integer.parseInt(field(product.body(), "id"));

    YearMonth month = YearMonth.from(LocalDate.now(java.time.ZoneId.of("America/Lima"))).minusMonths(2);
    LocalDate due = month.atDay(25);
    LocalDate paidAt = month.plusMonths(1).atDay(5);
    String purchaseRequest = """
        {"accountId":%d,"mode":"single","purchaseDate":"%s","purchaseTime":"%s",
         "items":[{"productId":%d,"quantity":%d}]}
        """;
    var first = send("POST", "/api/business/purchases",
        purchaseRequest.formatted(accountId, month.atDay(15), "10:00", productId, 2), token);
    var second = send("POST", "/api/business/purchases",
        purchaseRequest.formatted(accountId, month.atDay(20), "17:59:59", productId, 3), token);
    var nextCycle = send("POST", "/api/business/purchases",
        purchaseRequest.formatted(accountId, month.atDay(20), "18:00", productId, 1), token);
    assertEquals(200, first.statusCode(), first.body());
    assertEquals(200, second.statusCode(), second.body());
    assertEquals(200, nextCycle.statusCode(), nextCycle.body());
    int firstPurchaseId = Integer.parseInt(field(first.body(), "id"));
    int nextPurchaseId = Integer.parseInt(field(nextCycle.body(), "id"));
    assertEquals(month.plusMonths(1).atDay(20), purchases.findById(nextPurchaseId).orElseThrow().getFechaCorte());

    var laterTerms = send("PUT", "/api/clients/" + accountId,
        agreement.body().replace("\"cutOffDay\":20", "\"cutOffDay\":22")
            .replace("\"paymentDay\":25", "\"paymentDay\":28"), token);
    assertEquals(200, laterTerms.statusCode(), laterTerms.body());
    assertEquals(22, accounts.findById(accountId).orElseThrow().getDiaCorte());
    cutoffs.issue(accountId, month.atDay(20).atTime(18, 0));
    String statementUrl = "/api/business/cutoff?month=" + month;
    var issued = send("GET", statementUrl, null, token);
    assertEquals(200, issued.statusCode(), issued.body());
    var issuedEntries = json.readTree(issued.body()).get(0).get("entries");
    assertEquals(2, issuedEntries.size());
    assertEquals(firstPurchaseId, issuedEntries.get(0).get("purchaseId").intValue());
    assertEquals(due.toString(), issuedEntries.get(0).get("dueDate").asString());
    assertTrue(issuedEntries.get(0).get("paidLate").isNull());

    String paymentUrl = "/api/clients/" + accountId + "/payments/" + due;
    assertEquals(400, send("GET", paymentUrl + "/quote?paidAt=incorrecta", null, token).statusCode());
    var quoted = send("GET", paymentUrl + "/quote?paidAt=" + paidAt, null, token);
    assertEquals(200, quoted.statusCode(), quoted.body());
    var quote = json.readTree(quoted.body());
    assertEquals(505.0, quote.get("total").doubleValue());
    assertEquals(5.0, quote.get("late").doubleValue());
    assertEquals(500.0, quote.get("capital").doubleValue());
    int firstId = quote.get("lines").get(0).get("installmentId").intValue();
    int secondId = quote.get("lines").get(1).get("installmentId").intValue();
    String confirmation = """
        {"paidAt":"%s","amount":505.00,"installmentIds":[%d,%d],"requestKey":"%s"}
        """.formatted(paidAt, firstId, secondId, UUID.randomUUID());
    assertEquals(403, send("POST", paymentUrl + "/confirm", confirmation, null).statusCode());
    assertEquals(409, send("PUT", "/pagarCuota", Integer.toString(firstId), token).statusCode());
    assertEquals(409, send("POST", "/api/payments/" + firstId + "/cancel",
        "{\"paidAt\":\"" + paidAt + "\"}", token).statusCode());
    assertEquals(409, send("POST", paymentUrl + "/confirm",
        confirmation.replace(firstId + "," + secondId, Integer.toString(firstId)), token).statusCode());
    assertEquals(400, send("POST", paymentUrl + "/confirm",
        confirmation.replace("505.00", "202.00"), token).statusCode());
    assertEquals(600.0, accounts.findById(accountId).orElseThrow().getDeuda());

    var confirmationRequest = request("POST", paymentUrl + "/confirm", confirmation, token);
    var firstAttempt = http.sendAsync(confirmationRequest, HttpResponse.BodyHandlers.ofString());
    var retry = http.sendAsync(confirmationRequest, HttpResponse.BodyHandlers.ofString());
    var saved = firstAttempt.get();
    var retried = retry.get();
    assertEquals(200, saved.statusCode(), saved.body());
    assertEquals(200, retried.statusCode(), retried.body());
    assertEquals(json.readTree(saved.body()), json.readTree(retried.body()));
    assertEquals(1, jdbc.queryForObject("select count(*) from recibo_pago where cuenta_id = ?", Integer.class, accountId));
    assertEquals(new BigDecimal("5.00"), jdbc.queryForObject(
        "select sum(mora) from detalle_pago where cuota_id in (?, ?)", BigDecimal.class, firstId, secondId));
    assertEquals(100.0, accounts.findById(accountId).orElseThrow().getDeuda());
    assertTrue(purchases.findById(firstPurchaseId).orElseThrow().getFinalCuotas());
    assertFalse(purchases.findById(nextPurchaseId).orElseThrow().getFinalCuotas());
    assertEquals(409, send("POST", paymentUrl + "/confirm",
        confirmation.replace("505.00", "500.00"), token).statusCode());

    cutoffs.issue(accountId, paidAt.atStartOfDay());
    var reported = json.readTree(send("GET", statementUrl, null, token).body()).get(0).get("entries");
    assertEquals(2, reported.size());
    assertEquals(0.0, reported.get(0).get("late").doubleValue());
    assertEquals(2.0, reported.get(0).get("paidLate").doubleValue());
    assertEquals(3.0, reported.get(1).get("paidLate").doubleValue());
    assertEquals(paidAt.toString(), reported.get(0).get("paidAt").asString());
    var ownedAccount = accounts.findById(accountId).orElseThrow();
    assertEquals(buyer.getId(), ownedAccount.getCliente().getId());
    assertEquals(shop.getId(), ownedAccount.getNegocio().getId());
  }

  @Test
  void sessionCsrfIsolationAndPersistence() throws Exception {
    var customerRole = role(2, "CLIENTE");
    var businessRole = role(3, "NEGOCIO");
    var a = client(user("11223344", customerRole));
    var other = client(user("55667788", customerRole));
    var shop = new Negocio();
    shop.setUsuario(user("20123456789", businessRole));
    shop.setRazonSocial("Tienda");
    shop.setDireccion("Lima");
    shop.setTelefono("999111222");
    shop.setEstado(true);
    shop = businesses.save(shop);
    var currency = new Moneda();
    currency.setNombre("Sol");
    currency.setSimbolo("S/");
    seedSol();
    Integer currencyId =
        jdbc.queryForObject("select id_moneda from moneda where nombre='Sol'", Integer.class);
    currency.setId(currencyId);
    CuentaCredito account = new CuentaCredito();
    account.setCliente(other);
    account.setNegocio(shop);
    account.setMoneda(currency);
    account.setDeuda(0.0);
    account.setLimite(1000.0);
    account.setMaxMeses(12);
    account.setDiaCorte(20);
    account.setDiaPago(25);
    account = accounts.save(account);
    Compra foreign = new Compra();
    foreign.setCuentaCredito(account);
    foreign.setContadoCredito(true);
    foreign = purchases.save(foreign);
    assertEquals(401, send("GET", "/api/cliente/mis-compras", null, null).statusCode());
    var login =
        send(
            "POST",
            "/api/auth/login",
            "{\"codigo\":\"11223344\",\"contrasena\":\"Password123\"}",
            null);
    assertEquals(200, login.statusCode(), login.body());
    var matcher = Pattern.compile("\"csrfToken\":\"([^\"]+)\"").matcher(login.body());
    assertTrue(matcher.find());
    String token = matcher.group(1);
    assertEquals(200, send("GET", "/api/auth/session", null, null).statusCode());
    assertEquals(
        404, send("GET", "/api/cliente/mis-compras/" + foreign.getId(), null, token).statusCode());
    assertEquals(
        403, send("POST", "/comprasPorCliente", other.getId().toString(), token).statusCode());
    assertEquals(403, send("PUT", "/api/cliente/mi-perfil", "{}", null).statusCode());
    var updated =
        send(
            "PUT",
            "/api/cliente/mi-perfil",
            "{\"nombre\":\"Elena\",\"apellido\":\"Perez\",\"telefono\":\"999123456\"}",
            token);
    assertEquals(200, updated.statusCode(), updated.body());
    assertEquals("Elena", clients.findById(a.getId()).orElseThrow().getNombre());
    assertEquals(204, send("POST", "/api/logout", "{}", token).statusCode());

    assertEquals(401, send("GET", "/api/auth/session", null, null).statusCode());
    var invalid =
        send("POST", "/api/auth/login", "{\"codigo\":\"11223344\",\"contrasena\":\"wrong\"}", null);
    assertEquals(401, invalid.statusCode(), invalid.body());
    var businessLogin =
        send(
            "POST",
            "/api/auth/login",
            "{\"codigo\":\"20123456789\",\"contrasena\":\"Password123\"}",
            null);
    assertEquals(200, businessLogin.statusCode(), businessLogin.body());
    String businessToken = field(businessLogin.body(), "csrfToken");
    assertEquals(403, send("GET", "/api/cliente/mis-compras", null, businessToken).statusCode());
    jdbc.update("""
        insert into unidad_medida(nombre)
        select 'unidad' where not exists (select 1 from unidad_medida where nombre = 'unidad')
        """);
    var agreement =
        send(
            "POST",
            "/api/clients",
            "{\"dni\":\"11223344\",\"name\":\"Elena"
                + " Perez\",\"currency\":\"PEN\",\"compensatoryRate\":1,\"rateType\":\"TE\",\"ratePeriod\":\"Mensual\",\"capitalizationPeriod\":\"Mensual\",\"lateRate\":36,\"lateRateType\":\"TN\",\"maxMonths\":12,\"creditLine\":10000,\"cutOffDay\":20,\"paymentDay\":25}",
            businessToken);
    assertEquals(200, agreement.statusCode(), agreement.body());

    String accountId = field(agreement.body(), "id");
    var edited = send("PUT", "/api/clients/" + accountId, agreement.body(), businessToken);
    assertEquals(200, edited.statusCode(), edited.body());
    assertEquals(
        360.0,
        jdbc.queryForObject(
            "select periodicidad from tasa where cc_id=? and normal_moratoria=true",
            Double.class,
            Integer.valueOf(accountId)));

    var product =
        send(
            "POST",
            "/api/catalog",
            "{\"name\":\"Producto real\",\"unit\":\"unidad\",\"cashPrice\":90,\"creditPrice\":100}",
            businessToken);
    assertEquals(200, product.statusCode(), product.body());
    String productId = field(product.body(), "id");
    String today = java.time.LocalDate.now(java.time.ZoneId.of("America/Lima")).toString();
    String request =
        "{\"accountId\":"
            + accountId
            + ",\"months\":3,\"mode\":\"installments\",\"purchaseDate\":\""
            + today
            + "\",\"items\":[{\"productId\":"
            + productId
            + ",\"quantity\":2}]}";
    var created = send("POST", "/api/business/purchases", request, businessToken);
    assertEquals(200, created.statusCode(), created.body());
    String purchaseId = field(created.body(), "id");
    Integer pid = Integer.valueOf(purchaseId);
    assertEquals(
        1, jdbc.queryForObject("select count(*) from item where compra_id=?", Integer.class, pid));
    assertEquals(
        3, jdbc.queryForObject("select count(*) from cuota where compra_id=?", Integer.class, pid));
    double debt = accounts.findById(Integer.valueOf(accountId)).orElseThrow().getDeuda();
    assertTrue(debt > 200);
    var schedule = send("GET", "/api/payments?purchaseId=" + purchaseId, null, businessToken);
    assertEquals(200, schedule.statusCode(), schedule.body());
    String installmentId = field(schedule.body(), "id");
    var paid =
        send(
            "POST",
            "/api/payments/" + installmentId + "/cancel",
            "{\"paidAt\":\"" + today + "\"}",
            businessToken);
    assertEquals(200, paid.statusCode(), paid.body());
    assertTrue(paid.body().contains("\"cancelled\":true"));
    assertTrue(paid.body().contains(today));
    assertEquals(
        400,
        send(
                "POST",
                "/api/payments/" + installmentId + "/cancel",
                "{\"paidAt\":\"" + today + "\"}",
                businessToken)
            .statusCode());
    assertTrue(accounts.findById(Integer.valueOf(accountId)).orElseThrow().getDeuda() < debt);

    var draft =
        send(
            "POST",
            "/preRegistrarCompra",
            "{\"ccId\":" + accountId + ",\"numMeses\":2,\"contadoCredito\":true}",
            businessToken);
    assertEquals(200, draft.statusCode(), draft.body());
    String draftId = field(draft.body(), "id");
    var draftItem =
        send(
            "POST",
            "/registrarItem",
            "{\"compraId\":" + draftId + ",\"productoId\":" + productId + ",\"cantidad\":1}",
            businessToken);
    assertEquals(200, draftItem.statusCode(), draftItem.body());
    var finalDraft = send("PUT", "/registrarCompra", "{\"id\":" + draftId + "}", businessToken);
    assertEquals(200, finalDraft.statusCode(), finalDraft.body());
    assertEquals(draftId, field(finalDraft.body(), "id"));
    assertEquals(
        2,
        jdbc.queryForObject(
            "select count(*) from cuota where compra_id=?",
            Integer.class,
            Integer.valueOf(draftId)));
    assertEquals(
        400,
        send("PUT", "/registrarCompra", "{\"id\":" + draftId + "}", businessToken).statusCode());
    assertEquals(
        400, send("DELETE", "/api/clients/" + accountId, null, businessToken).statusCode());
    long before = purchases.count();
    var refused =
        send(
            "POST",
            "/api/business/purchases",
            request.replace("\"quantity\":2", "\"quantity\":10000"),
            businessToken);
    assertEquals(400, refused.statusCode());
    assertEquals(before, purchases.count());
    send("POST", "/api/logout", "{}", businessToken);
    var customerLogin =
        send(
            "POST",
            "/api/auth/login",
            "{\"codigo\":\"11223344\",\"contrasena\":\"Password123\"}",
            null);
    assertEquals(200, customerLogin.statusCode());
    var report = send("GET", "/api/cliente/mis-compras/" + purchaseId, null, null);
    assertEquals(200, report.statusCode(), report.body());
    assertTrue(report.body().contains("Producto real"));

    String customerToken = field(customerLogin.body(), "csrfToken");
    assertTrue(
        send("GET", "/api/cliente/mis-acuerdos", null, customerToken).body().contains("Tienda"));
    assertEquals(403, send("GET", "/api/cliente/mis-acuerdos", null, null).statusCode());
    assertEquals(
        204,
        send(
                "DELETE",
                "/api/cliente/mi-cuenta",
                "{\"contrasenaActual\":\"Password123\"}",
                customerToken)
            .statusCode());
    assertFalse(clients.findById(a.getId()).orElseThrow().getEstado());
    assertEquals(
        401,
        send(
                "POST",
                "/api/auth/login",
                "{\"codigo\":\"11223344\",\"contrasena\":\"Password123\"}",
                null)
            .statusCode());
    var registration =
        send(
            "POST",
            "/api/auth/register",
            "{\"dni\":\"99887766\",\"nombre\":\"Lucia\",\"apellido\":\"Rios\",\"telefono\":\"999111222\",\"contrasena\":\"Password123\"}",
            null);
    assertEquals(200, registration.statusCode(), registration.body());
    assertTrue(users.findByCodigoAndEstado("99887766", true).getContrasena().startsWith("$2"));
    assertFalse(registration.body().contains("Password123"));
    assertEquals(
        409,
        send(
                "POST",
                "/api/auth/register",
                "{\"dni\":\"99887766\",\"nombre\":\"Lucia\",\"apellido\":\"Rios\",\"telefono\":\"999111222\",\"contrasena\":\"Password123\"}",
                null)
            .statusCode());
    var second = new Negocio();
    second.setUsuario(user("20987654321", businessRole));
    second.setRazonSocial("Otra tienda");
    second.setDireccion("Lima");
    second.setTelefono("999111222");
    second.setEstado(true);
    businesses.save(second);
    var secondLogin =
        send(
            "POST",
            "/api/auth/login",
            "{\"codigo\":\"20987654321\",\"contrasena\":\"Password123\"}",
            null);
    String secondToken = field(secondLogin.body(), "csrfToken");
    assertEquals(403, send("GET", "/api/purchases/" + purchaseId, null, secondToken).statusCode());
    assertEquals(
        403,
        send(
                "POST",
                "/api/payments/" + installmentId + "/cancel",
                "{\"paidAt\":\"" + today + "\"}",
                secondToken)
            .statusCode());
    assertEquals("[]", send("GET", "/api/purchases", null, secondToken).body());
  }
}
