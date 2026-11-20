package com.upc.cuentix;

import static org.junit.jupiter.api.Assertions.*;

import com.upc.cuentix.entities.*;
import com.upc.cuentix.repositories.*;
import java.net.*;
import java.net.http.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

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
  @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
  private final HttpClient http =
      HttpClient.newBuilder()
          .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
          .build();

  private HttpResponse<String> send(String method, String url, String body, String token)
      throws Exception {
    var b =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + url))
            .header("Content-Type", "application/json");
    if (token != null) b.header("X-Cuentix-CSRF", token);
    return http.send(
        b.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : body)).build(),
        HttpResponse.BodyHandlers.ofString());
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
    jdbc.update("insert into moneda(nombre,simbolo) values ('Sol','S/')");
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
    jdbc.update("insert into unidad_medida(nombre) values ('unidad')");
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
