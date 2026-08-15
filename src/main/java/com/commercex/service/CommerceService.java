package com.commercex.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;

import com.commercex.model.AppUser;
import com.commercex.model.Coupon;
import com.commercex.model.Order;
import com.commercex.model.OrderItem;
import com.commercex.model.PasswordResetToken;
import com.commercex.model.Product;
import com.commercex.repository.AppUserRepository;
import com.commercex.repository.CouponRepository;
import com.commercex.repository.OrderRepository;
import com.commercex.repository.PasswordResetTokenRepository;
import com.commercex.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommerceService {

    private final AppUserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;

    private final ProductRepository products;
    private final OrderRepository orders;
    private final CouponRepository coupons;

    private final JavaMailSender mailSender;

    @Value("${commercex.mail.from:no-reply@commercex.local}")
    private String mailFrom;

    @Value("${commercex.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${commercex.tax-rate:0.08}")
    private BigDecimal taxRate;

    @Value("${commercex.shipping-flat:9.99}")
    private BigDecimal shippingFlat;

    @Value("${commercex.free-shipping-threshold:75}")
    private BigDecimal freeShippingThreshold;


    // =========================================================
    // SALE PRICE
    // =========================================================

    public BigDecimal salePrice(Product product) {

        if (product == null) {
            return BigDecimal.ZERO;
        }

        // Single source of truth, so the cart, the product page
        // and the placed order can never disagree on price.
        return product.getSalePrice();
    }


    // =========================================================
    // ORDER TOTALS
    // =========================================================

    /**
     * Totals for a basket, derived from a server-side subtotal.
     */
    public record Totals(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal tax,
            BigDecimal shipping,
            BigDecimal total) {
    }

    /**
     * Applies the coupon, tax, and shipping rules in one place so the
     * cart preview and the placed order always produce the same figures.
     */
    public Totals computeTotals(
            BigDecimal subtotal,
            Coupon coupon) {

        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;

        if (coupon != null && coupon.getPercent() != null) {

            discount =
                    subtotal
                            .multiply(coupon.getPercent())
                            .divide(
                                    BigDecimal.valueOf(100),
                                    2,
                                    RoundingMode.HALF_UP
                            )
                            .min(subtotal);
        }

        BigDecimal taxable =
                subtotal
                        .subtract(discount)
                        .max(BigDecimal.ZERO);

        BigDecimal tax =
                taxable
                        .multiply(taxRate)
                        .setScale(2, RoundingMode.HALF_UP);

        BigDecimal shipping;

        if (taxable.signum() == 0 ||
                taxable.compareTo(freeShippingThreshold) >= 0) {

            shipping = BigDecimal.ZERO;

        } else {

            shipping = shippingFlat;
        }

        BigDecimal total =
                taxable
                        .add(tax)
                        .add(shipping)
                        .setScale(2, RoundingMode.HALF_UP);

        return new Totals(
                subtotal.setScale(2, RoundingMode.HALF_UP),
                discount.setScale(2, RoundingMode.HALF_UP),
                tax,
                shipping.setScale(2, RoundingMode.HALF_UP),
                total
        );
    }


    /**
     * Resolves an active coupon by code, or null when the code is
     * missing or does not match an active coupon.
     */
    public Coupon findActiveCoupon(String code) {

        if (code == null || code.isBlank()) {
            return null;
        }

        return coupons
                .findByCodeIgnoreCaseAndActiveTrue(code.trim())
                .orElse(null);
    }


    // =========================================================
    // REGISTER USER
    // =========================================================

    @Transactional
    public AppUser register(
            String name,
            String email,
            String password) {

        name = name == null
                ? ""
                : name.trim();

        email = email == null
                ? ""
                : email.trim().toLowerCase();

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Name is required"
            );
        }

        if (email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required"
            );
        }

        if (password == null ||
                password.isBlank()) {

            throw new IllegalArgumentException(
                    "Password is required"
            );
        }

        if (users
                .findByEmailIgnoreCase(email)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Email already registered"
            );
        }

        AppUser user =
                AppUser.builder()
                        .name(name)
                        .email(email)
                        .password(
                                encoder.encode(password)
                        )
                        .role(AppUser.Role.USER)
                        .enabled(true)
                        .build();

        return users.save(user);
    }


    // =========================================================
    // CREATE PASSWORD RESET TOKEN
    // =========================================================

    @Transactional
    public String createResetToken(String email) {

        if (email == null ||
                email.isBlank()) {

            return null;
        }

        email = email.trim().toLowerCase();

        AppUser user =
                users.findByEmailIgnoreCase(email)
                        .orElse(null);

        if (user == null) {
            return null;
        }

        tokens.findAll()
                .stream()
                .filter(token ->
                        token.getUser() != null &&
                        token.getUser()
                                .getId()
                                .equals(user.getId())
                )
                .forEach(tokens::delete);

        String tokenValue =
                UUID.randomUUID().toString();

        PasswordResetToken resetToken =
                new PasswordResetToken(
                        null,
                        tokenValue,
                        user,
                        LocalDateTime.now()
                                .plusMinutes(30)
                );

        tokens.save(resetToken);

        return tokenValue;
    }


    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @Transactional
    public boolean resetPassword(
            String token,
            String newPassword) {

        if (token == null ||
                token.isBlank()) {

            return false;
        }

        if (newPassword == null ||
                newPassword.isBlank()) {

            return false;
        }

        PasswordResetToken resetToken =
                tokens.findByToken(token)
                        .orElse(null);

        if (resetToken == null) {
            return false;
        }

        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            tokens.delete(resetToken);

            return false;
        }

        AppUser user =
                resetToken.getUser();

        if (user == null) {
            return false;
        }

        user.setPassword(
                encoder.encode(newPassword)
        );

        users.save(user);

        // One-time token
        tokens.delete(resetToken);

        return true;
    }


    // =========================================================
    // PLACE ORDER
    // =========================================================

    @Transactional
    public Order placeOrder(
            AppUser user,
            Map<Long, Integer> cart,
            String customerName,
            String email,
            String phone,
            String address,
            String city,
            String state,
            String zip,
            String country,
            String paymentMethod,
            String couponCode) {

        // -----------------------------------------------------
        // BASIC VALIDATION
        // -----------------------------------------------------

        if (user == null) {
            throw new IllegalArgumentException(
                    "User is required."
            );
        }

        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cart is empty."
            );
        }

        if (customerName == null ||
                customerName.isBlank()) {

            throw new IllegalArgumentException(
                    "Customer name is required."
            );
        }

        if (email == null ||
                email.isBlank()) {

            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        if (address == null ||
                address.isBlank()) {

            throw new IllegalArgumentException(
                    "Address is required."
            );
        }


        // -----------------------------------------------------
        // CREATE ORDER
        // -----------------------------------------------------

        Order order = new Order();

        order.setUser(user);

        order.setCustomerName(
                customerName.trim()
        );

        order.setEmail(
                email.trim().toLowerCase()
        );

        order.setPhone(phone);

        order.setAddress(address);

        order.setCity(city);

        order.setState(state);

        order.setZip(zip);

        order.setCountry(country);

        order.setPaymentMethod(
                paymentMethod == null ||
                        paymentMethod.isBlank()
                        ? "COD"
                        : paymentMethod
        );

        order.setStatus("CONFIRMED");

        order.setPaymentStatus("PENDING");


        BigDecimal subtotal =
                BigDecimal.ZERO;


        // -----------------------------------------------------
        // CART ITEMS
        // -----------------------------------------------------

        for (Map.Entry<Long, Integer> entry :
                cart.entrySet()) {

            Long productId =
                    entry.getKey();

            Integer quantity =
                    entry.getValue();


            if (productId == null) {
                throw new IllegalArgumentException(
                        "Invalid product."
                );
            }


            if (quantity == null ||
                    quantity <= 0) {

                throw new IllegalArgumentException(
                        "Invalid quantity."
                );
            }


            Product product =
                    products.findById(productId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Product not found: "
                                                    + productId
                                    )
                            );


            // -------------------------------------------------
            // ACTIVE PRODUCT
            // -------------------------------------------------

            if (!product.isActive()) {

                throw new IllegalArgumentException(
                        product.getName()
                                + " is no longer available."
                );
            }


            // -------------------------------------------------
            // STOCK
            // -------------------------------------------------

            if (product.getStock() == null ||
                    product.getStock() < quantity) {

                throw new IllegalArgumentException(
                        "Insufficient stock for "
                                + product.getName()
                );
            }


            // -------------------------------------------------
            // SERVER-SIDE PRICE
            // -------------------------------------------------

            BigDecimal unitPrice =
                    salePrice(product);


            BigDecimal itemTotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(quantity)
                    );


            subtotal =
                    subtotal.add(itemTotal);


            // -------------------------------------------------
            // ORDER ITEM
            // -------------------------------------------------

            OrderItem orderItem =
                    new OrderItem(
                            product.getId(),
                            product.getName(),
                            product.getImageUrl(),
                            unitPrice,
                            quantity
                    );

            order.getItems().add(orderItem);


            // -------------------------------------------------
            // DEDUCT STOCK
            // -------------------------------------------------

            product.setStock(
                    product.getStock() - quantity
            );

            products.save(product);
        }


        // -----------------------------------------------------
        // COUPON, TAX, SHIPPING, TOTAL
        // -----------------------------------------------------

        Totals totals =
                computeTotals(
                        subtotal,
                        findActiveCoupon(couponCode)
                );


        // -----------------------------------------------------
        // SET ORDER TOTALS
        // -----------------------------------------------------

        order.setSubtotal(totals.subtotal());

        order.setDiscount(totals.discount());

        order.setTax(totals.tax());

        order.setShipping(totals.shipping());

        order.setTotal(totals.total());


        // -----------------------------------------------------
        // SAVE ORDER
        // -----------------------------------------------------

        return orders.save(order);
    }



    // =========================================================
    // PASSWORD RESET EMAIL
    // =========================================================

    public String createAndSendPasswordResetLink(String email) {

        String token = createResetToken(email);

        if (token == null) {
            return null;
        }

        String resetUrl =
                baseUrl + "/reset-password?token=" + token;

        try {
            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setFrom(mailFrom);
            message.setTo(email.trim().toLowerCase());
            message.setSubject("CommerceX - Reset your password");
            message.setText(
                    "We received a request to reset your CommerceX password.\n\n"
                    + "Open this link within 30 minutes:\n"
                    + resetUrl
                    + "\n\n"
                    + "If you did not request this, you can ignore this email."
            );

            mailSender.send(message);

        } catch (Exception ignored) {
            // The reset URL remains valid for local/dev use.
        }

        return resetUrl;
    }
}
