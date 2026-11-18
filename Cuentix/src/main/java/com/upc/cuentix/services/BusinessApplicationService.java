package com.upc.cuentix.services;

import com.upc.cuentix.config.ResourceAccess;
import com.upc.cuentix.entities.*;
import com.upc.cuentix.repositories.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service
@Transactional
public class BusinessApplicationService {
  private final ResourceAccess access;
  private final CuentaCreditoRepository accounts;
  private final ClienteRepository clients;
  private final NegocioRepository businesses;
  private final ProductoRepository products;
  private final CompraRepository purchases;
  private final CuotaRepository installments;
  private final ItemRepository items;
  private final TasaRepository rates;
  private final EntityManager em;

  public BusinessApplicationService(
      ResourceAccess access,
      CuentaCreditoRepository accounts,
      ClienteRepository clients,
      NegocioRepository businesses,
      ProductoRepository products,
      CompraRepository purchases,
      CuotaRepository installments,
      ItemRepository items,
      TasaRepository rates,
      EntityManager em) {
    this.access = access;
    this.accounts = accounts;
    this.clients = clients;
    this.businesses = businesses;
    this.products = products;
    this.purchases = purchases;
    this.installments = installments;
    this.items = items;
    this.rates = rates;
    this.em = em;
  }

  public record Context(String id, String name) {}

  public record ClientView(
      String id,
      String dni,
      String name,
      String currency,
      double compensatoryRate,
      String rateType,
      String ratePeriod,
      String capitalizationPeriod,
      double lateRate,
      String lateRateType,
      int maxMonths,
      double creditLine,
      int cutOffDay,
      int paymentDay,
      Double usedCredit) {}

  public record ProductView(
      Integer id, String name, String unit, double cashPrice, double creditPrice) {}

  public record PurchasedProduct(String name, Integer quantity, String unit) {}

  public record PurchaseView(
      String id,
      String accountId,
      String clientId,
      String businessId,
      String currency,
      String purchaseDate,
      double principal,
      double graceInterest,
      String mode,
      int installments,
      List<PurchasedProduct> products) {}

  public record PaymentView(
      String id,
      String purchaseId,
      String clientId,
      String businessId,
      double lateAnnualRate,
      String purchaseDate,
      int number,
      int totalInstallments,
      String dueDate,
      double interest,
      double amortization,
      double installmentAmount,
      int lateDays,
      double lateAmount,
      boolean cancelled,
      String paidAt,
      List<PurchasedProduct> products) {}

  public record PaymentRequest(String paidAt, String businessId) {}

  public record ItemRequest(Integer productId, Integer quantity) {}

  public record PurchaseRequest(
      Integer accountId,
      Integer months,
      String mode,
      String purchaseDate,
      List<ItemRequest> items) {}

  private ResponseStatusException missing() {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso no encontrado");
  }

  private Negocio business(HttpSession session) {
    Integer id = (Integer) session.getAttribute("usuarioId");
    return businesses.findAll().stream()
        .filter(
            n ->
                Boolean.TRUE.equals(n.getEstado())
                    && n.getUsuario() != null
                    && n.getUsuario().getId().equals(id))
        .findFirst()
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Seleccione una cuenta de negocio"));
  }

  public Context context(HttpSession s) {
    Negocio n = business(s);
    return new Context(n.getId().toString(), n.getRazonSocial());
  }

  private String currency(CuentaCredito c) {
    String v = c.getMoneda().getSimbolo();
    return "S/".equals(v) || "S/.".equals(v)
        ? "PEN"
        : "$".equals(v) ? "USD" : c.getMoneda().getNombre();
  }

  private Tasa rate(CuentaCredito c, boolean late) {
    return rates.getTasaByCuentaCredito_IdAndNomrmalMoratoria(c.getId(), late);
  }

  private ClientView client(CuentaCredito c) {
    Tasa r = rate(c, false), m = rate(c, true);
    Cliente u = c.getCliente();
    return new ClientView(
        c.getId().toString(),
        u.getUsuario().getCodigo(),
        u.getNombre() + " " + u.getApellido(),
        currency(c),
        r == null
            ? 0
            : (Boolean.TRUE.equals(r.getNominalEfectiva())
                ? (Math.pow(1 + FinancialMath.annual(r), 1.0 / 12) - 1) * 100
                : r.getPorcentaje() * 30 / r.getPeriodicidad() * 100),
        r != null && Boolean.TRUE.equals(r.getNominalEfectiva()) ? "TE" : "TN",
        "Mensual",
        r != null && Double.valueOf(1).equals(r.getCapitalizacion()) ? "Diaria" : "Mensual",
        m == null
            ? 0
            : (Boolean.TRUE.equals(m.getNominalEfectiva())
                    ? FinancialMath.annual(m)
                    : m.getPorcentaje() * 360 / m.getPeriodicidad())
                * 100,
        "TN",
        c.getMaxMeses(),
        c.getLimite(),
        c.getDiaCorte(),
        c.getDiaPago(),
        c.getDeuda());
  }

  public List<ClientView> clients() {
    return accounts.findAll().stream()
        .filter(
            c -> Boolean.TRUE.equals(c.getEstado()) && access.ownsBusiness(c.getNegocio().getId()))
        .map(this::client)
        .toList();
  }

  public ClientView client(Integer id) {
    access.account(id);
    return client(accounts.findById(id).orElseThrow(this::missing));
  }

  private void apply(ClientView d, CuentaCredito c) {
    if (d.maxMonths() < 1
        || d.maxMonths() > 120
        || !Double.isFinite(d.creditLine())
        || d.creditLine() <= 0
        || d.cutOffDay() < 1
        || d.cutOffDay() > 31
        || d.paymentDay() < 1
        || d.paymentDay() > 31) throw new IllegalArgumentException("Condiciones invalidas");
    if (c.getDeuda() != null && d.creditLine() < c.getDeuda())
      throw new IllegalArgumentException("La linea no cubre la deuda");
    c.setMaxMeses(d.maxMonths());
    c.setLimite(d.creditLine());
    c.setDiaCorte(d.cutOffDay());
    c.setDiaPago(d.paymentDay());
  }

  private void writeRate(
      CuentaCredito c, double percentage, String type, String period, String cap, boolean late) {
    if (!Double.isFinite(percentage) || percentage < 0 || !("TE".equals(type) || "TN".equals(type)))
      throw new IllegalArgumentException("Tasa invalida");
    Tasa t = rate(c, late);
    if (t == null) t = new Tasa();
    t.setCuentaCredito(c);
    t.setNomrmalMoratoria(late);
    t.setPorcentaje(percentage / 100);
    t.setNominalEfectiva("TE".equals(type));
    t.setPeriodicidad("Anual".equals(period) ? 360.0 : "Diaria".equals(period) ? 1.0 : 30.0);
    t.setCapitalizacion("Diaria".equals(cap) ? 1.0 : 30.0);
    rates.save(t);
  }

  public ClientView createClient(ClientView d, HttpSession session) {
    Cliente u = clients.findByUsuario_Codigo(d.dni());
    if (u == null || !Boolean.TRUE.equals(u.getEstado()))
      throw new IllegalArgumentException("El cliente debe registrarse primero con su DNI");
    CuentaCredito c = new CuentaCredito();
    c.setCliente(u);
    c.setNegocio(business(session));
    c.setEstado(true);
    c.setDeuda(0.0);
    var currencies = em.createQuery("select m from Moneda m", Moneda.class).getResultList();
    c.setMoneda(
        currencies.stream()
            .filter(
                m ->
                    ("PEN".equals(d.currency()) && m.getSimbolo().startsWith("S/"))
                        || ("USD".equals(d.currency()) && "$".equals(m.getSimbolo()))
                        || d.currency().equals(m.getNombre()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Moneda no configurada")));
    apply(d, c);
    c = accounts.save(c);
    writeRate(
        c, d.compensatoryRate(), d.rateType(), d.ratePeriod(), d.capitalizationPeriod(), false);
    writeRate(c, d.lateRate(), d.lateRateType(), "Anual", "Diaria", true);
    return client(c);
  }

  public ClientView updateClient(Integer id, ClientView d) {
    access.account(id);
    CuentaCredito c = em.find(CuentaCredito.class, id, LockModeType.PESSIMISTIC_WRITE);
    if (!c.getCliente().getUsuario().getCodigo().equals(d.dni()))
      throw new IllegalArgumentException("El DNI de la cuenta no se modifica");
    if (!currency(c).equals(d.currency()))
      throw new IllegalArgumentException("La moneda requiere una cuenta nueva");
    apply(d, c);
    writeRate(
        c, d.compensatoryRate(), d.rateType(), d.ratePeriod(), d.capitalizationPeriod(), false);
    writeRate(c, d.lateRate(), d.lateRateType(), "Anual", "Diaria", true);
    return client(accounts.save(c));
  }

  public void deleteClient(Integer id) {
    access.account(id);
    CuentaCredito c = accounts.findById(id).orElseThrow(this::missing);
    if (c.getDeuda() > 0)
      throw new IllegalArgumentException("No puede desactivar una cuenta con deuda");
    c.setEstado(false);
    accounts.save(c);
  }

  private ProductView product(Producto p) {
    return new ProductView(
        p.getId(),
        p.getNombre(),
        p.getUnidadMedida().getNombre(),
        p.getPrecioContado(),
        p.getPrecioLista());
  }

  public List<ProductView> catalog(HttpSession s) {
    Integer id = business(s).getId();
    return products.findByNegocio_IdAndEstado(id, true).stream().map(this::product).toList();
  }

  public ProductView createProduct(ProductView d, HttpSession s) {
    Producto p = new Producto();
    p.setNegocio(business(s));
    p.setDescripcion("");
    p.setMarca("");
    p.setProovedor("");
    p.setImagenURL("");
    p.setEstado(true);
    return saveProduct(p, d);
  }

  public ProductView updateProduct(Integer id, ProductView d) {
    access.product(id);
    return saveProduct(products.findById(id).orElseThrow(this::missing), d);
  }

  private ProductView saveProduct(Producto p, ProductView d) {
    if (d.name() == null
        || d.name().isBlank()
        || !Double.isFinite(d.cashPrice())
        || !Double.isFinite(d.creditPrice())
        || d.cashPrice() <= 0
        || d.creditPrice() <= 0) throw new IllegalArgumentException("Producto invalido");
    var unit =
        em
            .createQuery(
                "select u from UnidadMedida u where lower(u.nombre)=lower(:name)",
                UnidadMedida.class)
            .setParameter("name", d.unit())
            .getResultList()
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unidad no configurada"));
    p.setNombre(d.name().trim());
    p.setUnidadMedida(unit);
    p.setPrecioContado(d.cashPrice());
    p.setPrecioLista(d.creditPrice());
    return product(products.save(p));
  }

  public void deleteProduct(Integer id) {
    access.product(id);
    Producto p = products.findById(id).orElseThrow(this::missing);
    p.setEstado(false);
    products.save(p);
  }

  private List<PurchasedProduct> productList(Compra c) {
    return items.findByCompra_Id(c.getId()).stream()
        .map(
            i ->
                new PurchasedProduct(
                    i.getProducto().getNombre(),
                    i.getCantidad(),
                    i.getProducto().getUnidadMedida().getNombre()))
        .toList();
  }

  private String iso(java.util.Date d) {
    return d == null ? null : new java.sql.Date(d.getTime()).toLocalDate().toString();
  }

  private double zero(Double d) {
    return d == null ? 0 : d;
  }

  private PurchaseView purchase(Compra c) {
    CuentaCredito a = c.getCuentaCredito();
    return new PurchaseView(
        c.getId().toString(),
        a.getId().toString(),
        a.getId().toString(),
        a.getNegocio().getId().toString(),
        currency(a),
        c.getFecha() == null ? null : c.getFecha().toString(),
        zero(c.getTotalInicial()),
        FinancialMath.money(zero(c.getTotalGracia()) - zero(c.getTotalInicial())).doubleValue(),
        c.getNumMeses() != null && c.getNumMeses() > 1 ? "installments" : "single",
        c.getNumMeses() == null ? 0 : c.getNumMeses(),
        productList(c));
  }

  public List<PurchaseView> purchases() {
    return purchases.findAll().stream()
        .filter(c -> access.ownsBusiness(c.getCuentaCredito().getNegocio().getId()))
        .map(this::purchase)
        .toList();
  }

  public PurchaseView purchase(Integer id) {
    access.purchase(id);
    return purchase(purchases.findById(id).orElseThrow(this::missing));
  }

  public PaymentView quoteForReport(Cuota q) {
    return payment(q);
  }

  private PaymentView payment(Cuota q) {
    return payment(q, LocalDate.now(ZoneId.of("America/Lima")));
  }

  private PaymentView payment(Cuota q, LocalDate reference) {
    Compra c = q.getCompra();
    CuentaCredito a = c.getCuentaCredito();
    String due = iso(q.getFechaVencimiento());
    LocalDate today =
        Boolean.TRUE.equals(q.getCancelado()) && q.getFechaPago() != null
            ? q.getFechaPago()
            : reference;
    LocalDate date = LocalDate.parse(due);
    int days =
        Math.max(
            0,
            (today.getYear() - date.getYear()) * 360
                + (today.getMonthValue() - date.getMonthValue()) * 30
                + Math.min(today.getDayOfMonth(), 30)
                - Math.min(date.getDayOfMonth(), 30));
    double annual = zero(c.getTasaMora()) * 100;
    double mora =
        Boolean.TRUE.equals(q.getCancelado())
            ? zero(q.getMoras())
            : FinancialMath.lateAmount(zero(q.getTotal()), annual, days);
    return new PaymentView(
        q.getId().toString(),
        c.getId().toString(),
        a.getId().toString(),
        a.getNegocio().getId().toString(),
        annual,
        c.getFecha().toString(),
        q.getNum(),
        c.getNumMeses(),
        due,
        zero(q.getIntereses()),
        FinancialMath.money(zero(q.getTotal()) - zero(q.getIntereses())).doubleValue(),
        zero(q.getTotal()),
        days,
        mora,
        Boolean.TRUE.equals(q.getCancelado()),
        q.getFechaPago() == null ? null : q.getFechaPago().toString(),
        productList(c));
  }

  public List<PaymentView> payments(Integer clientId, Integer purchaseId) {
    if (clientId != null) access.account(clientId);
    if (purchaseId != null) access.purchase(purchaseId);
    return installments.findAll().stream()
        .filter(q -> access.ownsBusiness(q.getCompra().getCuentaCredito().getNegocio().getId()))
        .filter(q -> clientId == null || q.getCompra().getCuentaCredito().getId().equals(clientId))
        .filter(q -> purchaseId == null || q.getCompra().getId().equals(purchaseId))
        .map(this::payment)
        .toList();
  }

  public PaymentView payment(Integer id) {
    Cuota q = installments.findById(id).orElseThrow(this::missing);
    access.purchase(q.getCompra().getId());
    return payment(q);
  }

  public PaymentView cancel(Integer id, PaymentRequest d) {
    Cuota q = installments.findById(id).orElseThrow(this::missing);
    access.purchase(q.getCompra().getId());
    CuentaCredito a =
        em.find(
            CuentaCredito.class,
            q.getCompra().getCuentaCredito().getId(),
            LockModeType.PESSIMISTIC_WRITE);
    em.refresh(q, LockModeType.PESSIMISTIC_WRITE);
    if (Boolean.TRUE.equals(q.getCancelado()))
      throw new IllegalArgumentException("Cuota ya cancelada");
    if (d.paidAt() == null) throw new IllegalArgumentException("Indique fecha");
    LocalDate paid = LocalDate.parse(d.paidAt());
    if (paid.isAfter(LocalDate.now(ZoneId.of("America/Lima")))
        || paid.isBefore(q.getCompra().getFecha()))
      throw new IllegalArgumentException("Fecha de pago invalida");
    PaymentView quote = payment(q, paid);
    q.setFechaPago(paid);
    q.setMoras(quote.lateAmount());
    q.setCancelado(true);
    installments.save(q);
    a.setDeuda(FinancialMath.money(Math.max(0, a.getDeuda() - q.getTotal())).doubleValue());
    accounts.save(a);
    Compra c = q.getCompra();
    c.setFinalCuotas(
        installments.findByCompra_Id(c.getId()).stream()
            .allMatch(v -> Boolean.TRUE.equals(v.getCancelado())));
    purchases.save(c);
    return payment(q);
  }

  public com.upc.cuentix.dto.CompraDTO finalizeDraft(Integer id) {
    access.purchase(id);
    Compra draft = purchases.findById(id).orElseThrow(this::missing);
    if (draft.getFecha() != null) throw new IllegalArgumentException("Compra ya finalizada");
    var rows =
        items.findByCompra_Id(id).stream()
            .map(i -> new ItemRequest(i.getProducto().getId(), i.getCantidad()))
            .toList();
    createPurchase(
        new PurchaseRequest(
            draft.getCuentaCredito().getId(),
            draft.getNumMeses(),
            draft.getNumMeses() != null && draft.getNumMeses() > 1 ? "installments" : "single",
            LocalDate.now(ZoneId.of("America/Lima")).toString(),
            rows),
        draft);
    var result = new com.upc.cuentix.dto.CompraDTO();
    result.setId(draft.getId());
    result.setCcId(draft.getCuentaCredito().getId());
    result.setFecha(java.sql.Date.valueOf(draft.getFecha()));
    result.setTotalInicial(draft.getTotalInicial());
    result.setTotalFinal(draft.getTotalFinal());
    result.setIntereses(draft.getIntereses());
    result.setInteresesGracia(
        FinancialMath.money(draft.getTotalGracia() - draft.getTotalInicial()).doubleValue());
    result.setMoras(draft.getMoras());
    result.setNumMeses(draft.getNumMeses());
    result.setContadoCredito(true);
    result.setFinalCuotas(false);
    result.setTasa(draft.getTasa());
    result.setTasaMora(draft.getTasaMora());
    return result;
  }

  public PurchaseView createPurchase(PurchaseRequest d) {
    return createPurchase(d, null);
  }

  private PurchaseView createPurchase(PurchaseRequest d, Compra draft) {
    if (d.accountId() == null || d.items() == null || d.items().isEmpty())
      throw new IllegalArgumentException("Indique cuenta y productos");
    access.account(d.accountId());
    CuentaCredito a = em.find(CuentaCredito.class, d.accountId(), LockModeType.PESSIMISTIC_WRITE);
    if (draft != null) {
      em.refresh(draft, LockModeType.PESSIMISTIC_WRITE);
      if (draft.getFecha() != null) throw new IllegalArgumentException("Compra ya finalizada");
    }
    if (!Boolean.TRUE.equals(a.getEstado()) || !Boolean.TRUE.equals(a.getCliente().getEstado()))
      throw new IllegalArgumentException("Cuenta inactiva");
    int months = "single".equals(d.mode()) ? 1 : d.months() == null ? 0 : d.months();
    if (d.mode() == null
        || !Set.of("single", "installments").contains(d.mode())
        || months < 1
        || months > a.getMaxMeses()) throw new IllegalArgumentException("Plazo invalido");
    if (d.purchaseDate() == null) throw new IllegalArgumentException("Indique fecha");
    LocalDate date = LocalDate.parse(d.purchaseDate());
    if (date.isAfter(LocalDate.now(ZoneId.of("America/Lima"))))
      throw new IllegalArgumentException("Fecha futura");
    List<Item> selected = new ArrayList<>();
    BigDecimal principal = BigDecimal.ZERO;
    Set<Integer> seen = new HashSet<>();
    for (ItemRequest row : d.items()) {
      if (row == null
          || row.productId() == null
          || row.quantity() == null
          || row.quantity() < 1
          || !seen.add(row.productId()))
        throw new IllegalArgumentException("Item invalido o duplicado");
      Producto product = products.findById(row.productId()).orElseThrow(this::missing);
      if (!Boolean.TRUE.equals(product.getEstado())
          || !product.getNegocio().getId().equals(a.getNegocio().getId()))
        throw new IllegalArgumentException("Producto no autorizado");
      principal =
          principal.add(
              BigDecimal.valueOf(product.getPrecioLista())
                  .multiply(BigDecimal.valueOf(row.quantity())));
      Item i = new Item();
      i.setProducto(product);
      i.setCantidad(row.quantity());
      selected.add(i);
    }
    principal = principal.setScale(2, java.math.RoundingMode.HALF_UP);
    double tea = FinancialMath.annual(rate(a, false));
    Tasa lateRate = rate(a, true);
    if (lateRate == null) throw new IllegalArgumentException("Tasa moratoria no configurada");
    FinancialMath.annual(lateRate);
    double late =
        Boolean.TRUE.equals(lateRate.getNominalEfectiva())
            ? FinancialMath.annual(lateRate)
            : lateRate.getPorcentaje() * 360 / lateRate.getPeriodicidad();
    LocalDate start = FinancialMath.paymentDate(date, a.getDiaCorte(), a.getDiaPago());
    double grace =
        FinancialMath.compound(
            principal.doubleValue(), tea, java.time.temporal.ChronoUnit.DAYS.between(date, start));
    List<FinancialMath.Term> terms =
        "single".equals(d.mode())
            ? List.of(
                new FinancialMath.Term(
                    FinancialMath.money(grace),
                    FinancialMath.money(grace - principal.doubleValue()),
                    principal))
            : FinancialMath.french(grace, tea, months);
    double total =
        terms.stream()
            .map(FinancialMath.Term::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .doubleValue();
    if (total > a.getLimite() - a.getDeuda())
      throw new IllegalArgumentException(
          "La compra supera la linea disponible, incluidos intereses");
    Compra c = draft == null ? new Compra() : draft;
    c.setCuentaCredito(a);
    c.setFecha(date);
    c.setContadoCredito(true);
    c.setNumMeses(months);
    c.setTasa(tea);
    c.setTasaMora(late);
    c.setTotalInicial(principal.doubleValue());
    c.setTotalGracia("single".equals(d.mode()) ? principal.doubleValue() : grace);
    c.setIntereses(FinancialMath.money(total - c.getTotalGracia()).doubleValue());
    c.setTotalFinal(total);
    c.setMoras(0.0);
    c.setFinalCuotas(false);
    c = purchases.save(c);
    if (draft != null) items.deleteAll(items.findByCompra_Id(c.getId()));
    for (Item i : selected) {
      i.setCompra(c);
      items.save(i);
    }
    for (int n = 0; n < terms.size(); n++) {
      var term = terms.get(n);
      Cuota q = new Cuota();
      q.setCompra(c);
      q.setNum(n + 1);
      q.setFechaVencimiento(
          java.sql.Date.valueOf("single".equals(d.mode()) ? start : start.plusMonths(n + 1)));
      q.setIntereses(term.interest().doubleValue());
      q.setMoras(0.0);
      q.setTotal(term.amount().doubleValue());
      q.setCancelado(false);
      installments.save(q);
    }
    a.setDeuda(FinancialMath.money(a.getDeuda() + total).doubleValue());
    accounts.save(a);
    return purchase(c);
  }
}
