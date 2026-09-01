package com.commercex.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.commercex.model.AppUser;
import com.commercex.model.Coupon;
import com.commercex.model.Product;
import com.commercex.repository.AppUserRepository;
import com.commercex.repository.CouponRepository;
import com.commercex.repository.ProductRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(
            AppUserRepository users,
            ProductRepository products,
            CouponRepository coupons,
            PasswordEncoder encoder) {

        return args -> {

            // =====================================================
            // ADMIN USER
            // =====================================================

            if (users
                    .findByEmailIgnoreCase("admin@commercex.com")
                    .isEmpty()) {

                AppUser admin =
                        AppUser.builder()
                                .name("CommerceX Admin")
                                .email("admin@commercex.com")
                                .password(
                                        encoder.encode(
                                                "Admin@12345"
                                        )
                                )
                                .role(AppUser.Role.ADMIN)
                                .enabled(true)
                                .build();

                users.save(admin);
            }


            // =====================================================
            // SAMPLE PRODUCTS
            // =====================================================

            if (products.count() < 17) {

                Product headphones =
                        Product.builder()
                                .name("AirSound Pro Headphones")
                                .description(
                                        "Premium wireless headphones with adaptive noise cancellation and spatial audio."
                                )
                                .price(new BigDecimal("14999.00"))
                                .discountPercent(new BigDecimal("10"))
                                .category("Electronics")
                                .brand("CommerceX Audio")
                                .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=1200")
                                .stock(40)
                                .rating(4.8)
                                .active(true)
                                .build();

                products.save(headphones);

                Product smartwatch =
                        Product.builder()
                                .name("Ultra Smart Watch")
                                .description(
                                        "Elegant fitness and notification smartwatch with a bright always-on display."
                                )
                                .price(new BigDecimal("19999.00"))
                                .discountPercent(new BigDecimal("5"))
                                .category("Electronics")
                                .brand("CommerceX")
                                .imageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=1200")
                                .stock(25)
                                .rating(4.7)
                                .active(true)
                                .build();

                products.save(smartwatch);

                Product shoes =
                        Product.builder()
                                .name("Aero Runner X")
                                .description(
                                        "Lightweight everyday running shoes designed for comfort and long-distance support."
                                )
                                .price(new BigDecimal("8999.00"))
                                .discountPercent(BigDecimal.ZERO)
                                .category("Fashion")
                                .brand("Aero")
                                .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=1200")
                                .stock(50)
                                .rating(4.6)
                                .active(true)
                                .build();

                products.save(shoes);

                Product backpack =
                        Product.builder()
                                .name("Urban Laptop Backpack")
                                .description(
                                        "Minimal commuter backpack with a padded laptop compartment and water-resistant shell."
                                )
                                .price(new BigDecimal("6999.00"))
                                .discountPercent(new BigDecimal("15"))
                                .category("Accessories")
                                .brand("Urban")
                                .imageUrl("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=1200")
                                .stock(35)
                                .rating(4.5)
                                .active(true)
                                .build();

                products.save(backpack);

                Product airFryer =
                        Product.builder()
                                .name("Air Fryer")
                                .description("Fast, even-cooking air fryer with multiple presets and non-stick basket.")
                                .price(new BigDecimal("1599.20"))
                                .discountPercent(new BigDecimal("20"))
                                .category("Cooking")
                                .brand("Philips")
                                .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=1200")
                                .stock(12)
                                .rating(4.5)
                                .active(true)
                                .build();

                products.save(airFryer);

                Product speaker =
                        Product.builder()
                                .name("Smart Home Speaker")
                                .description("Compact smart speaker with clear sound and voice assistant integration.")
                                .price(new BigDecimal("2499.00"))
                                .discountPercent(new BigDecimal("15"))
                                .category("Electronics")
                                .brand("CommerceX Audio")
                                .imageUrl("https://images.unsplash.com/photo-1518444025366-5f76e0a0c5f1?w=1200")
                                .stock(20)
                                .rating(4.6)
                                .active(true)
                                .build();

                products.save(speaker);

                Product airFryerPro =
                        Product.builder()
                                .name("Air Fryer Pro Max")
                                .description("Compact countertop fryer with rapid air circulation and family-sized cooking power.")
                                .price(new BigDecimal("12999.00"))
                                .discountPercent(new BigDecimal("20"))
                                .category("Home Appliances")
                                .brand("Philips")
                                .imageUrl("https://images.unsplash.com/photo-1556911220-bff31c812dba?w=1200")
                                .stock(18)
                                .rating(4.4)
                                .active(true)
                                .build();

                products.save(airFryerPro);

                Product bluetoothSpeaker =
                        Product.builder()
                                .name("EchoBeam Bluetooth Speaker")
                                .description("Portable speaker with deep bass, immersive sound, and all-day battery life.")
                                .price(new BigDecimal("5999.00"))
                                .discountPercent(new BigDecimal("12"))
                                .category("Electronics")
                                .brand("Echo")
                                .imageUrl("https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=1200")
                                .stock(22)
                                .rating(4.7)
                                .active(true)
                                .build();

                products.save(bluetoothSpeaker);

                Product watch =
                        Product.builder()
                                .name("Titan Edge Smart Watch")
                                .description("Premium smartwatch with health tracking, GPS, and a sleek stainless-steel finish.")
                                .price(new BigDecimal("11999.00"))
                                .discountPercent(new BigDecimal("8"))
                                .category("Wearables")
                                .brand("Titan")
                                .imageUrl("https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=1200")
                                .stock(27)
                                .rating(4.6)
                                .active(true)
                                .build();

                products.save(watch);

                Product lamp =
                        Product.builder()
                                .name("Nova Desk Lamp")
                                .description("Minimal desk lamp with touch controls, warm light, and a modern matte finish.")
                                .price(new BigDecimal("3499.00"))
                                .discountPercent(new BigDecimal("10"))
                                .category("Home Decor")
                                .brand("Nova")
                                .imageUrl("https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=1200")
                                .stock(31)
                                .rating(4.5)
                                .active(true)
                                .build();

                products.save(lamp);

                Product hoodie =
                        Product.builder()
                                .name("Active Core Hoodie")
                                .description("Ultra-soft everyday hoodie with a relaxed fit, premium cotton blend, and brushed lining.")
                                .price(new BigDecimal("4999.00"))
                                .discountPercent(new BigDecimal("18"))
                                .category("Apparel")
                                .brand("CoreFit")
                                .imageUrl("https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=1200")
                                .stock(42)
                                .rating(4.8)
                                .active(true)
                                .build();

                products.save(hoodie);

                Product kettle =
                        Product.builder()
                                .name("Volt Electric Kettle")
                                .description("Fast-boiling kettle with stainless steel finish, auto shutoff, and temperature control.")
                                .price(new BigDecimal("2799.00"))
                                .discountPercent(new BigDecimal("9"))
                                .category("Kitchen")
                                .brand("Volt")
                                .imageUrl("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1200")
                                .stock(26)
                                .rating(4.4)
                                .active(true)
                                .build();

                products.save(kettle);

                Product camera =
                        Product.builder()
                                .name("Frame Mini Camera")
                                .description("Pocket-friendly camera for creators who want crisp vlogging and quick mobile sharing.")
                                .price(new BigDecimal("18999.00"))
                                .discountPercent(new BigDecimal("14"))
                                .category("Photography")
                                .brand("Frame")
                                .imageUrl("https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=1200")
                                .stock(9)
                                .rating(4.7)
                                .active(true)
                                .build();

                products.save(camera);

                // ---------------- SPORTS ----------------

                Product soccerBall =
                        Product.builder()
                                .name("Strike Pro Match Football")
                                .description("FIFA-quality stitched soccer ball with textured grip panels for consistent flight and control.")
                                .price(new BigDecimal("1499.00"))
                                .discountPercent(new BigDecimal("12"))
                                .category("Sports")
                                .brand("CommerceX Sports")
                                .imageUrl("https://images.unsplash.com/photo-1614632537190-23e4146777db?w=1200")
                                .stock(60)
                                .rating(4.6)
                                .active(true)
                                .build();

                products.save(soccerBall);

                Product basketball =
                        Product.builder()
                                .name("Court King Basketball")
                                .description("Indoor/outdoor composite leather basketball with deep channel grooves for a locked-in grip.")
                                .price(new BigDecimal("1799.00"))
                                .discountPercent(new BigDecimal("10"))
                                .category("Sports")
                                .brand("CommerceX Sports")
                                .imageUrl("https://images.unsplash.com/photo-1519861531473-9200262188bf?w=1200")
                                .stock(45)
                                .rating(4.7)
                                .active(true)
                                .build();

                products.save(basketball);

                Product dumbbellSet =
                        Product.builder()
                                .name("PowerCore Adjustable Dumbbell Set")
                                .description("Space-saving adjustable dumbbell pair with quick dial-a-weight changes from 5 to 25 kg.")
                                .price(new BigDecimal("8999.00"))
                                .discountPercent(new BigDecimal("15"))
                                .category("Sports")
                                .brand("PowerCore")
                                .imageUrl("https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=1200")
                                .stock(20)
                                .rating(4.8)
                                .active(true)
                                .build();

                products.save(dumbbellSet);

                Product yogaMat =
                        Product.builder()
                                .name("FlexFit Non-Slip Yoga Mat")
                                .description("Extra-thick 8mm eco-friendly yoga mat with a non-slip textured surface for stability in every pose.")
                                .price(new BigDecimal("1299.00"))
                                .discountPercent(new BigDecimal("8"))
                                .category("Sports")
                                .brand("FlexFit")
                                .imageUrl("https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=1200")
                                .stock(70)
                                .rating(4.5)
                                .active(true)
                                .build();

                products.save(yogaMat);

                Product runningShoes =
                        Product.builder()
                                .name("TrailBlazer Running Shoes")
                                .description("Lightweight breathable running shoes with responsive cushioning built for long-distance training.")
                                .price(new BigDecimal("3999.00"))
                                .discountPercent(new BigDecimal("18"))
                                .category("Sports")
                                .brand("TrailBlazer")
                                .imageUrl("https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=1200")
                                .stock(35)
                                .rating(4.6)
                                .active(true)
                                .build();

                products.save(runningShoes);
            }


            // =====================================================
            // SAMPLE COUPON
            // =====================================================

            if (coupons.count() == 0) {

                Coupon welcomeCoupon =
                        Coupon.builder()
                                .code("WELCOME10")
                                .percent(
                                        new BigDecimal("10")
                                )
                                .active(true)
                                .build();

                coupons.save(welcomeCoupon);
            }
        };
    }
}