# CommerceX

CommerceX is the integrated Spring Boot + Thymeleaf + MySQL e-commerce application.

## What is included

- Responsive CommerceX storefront with polished Apple/Amazon-inspired visual language
- Search, category filtering and sorting
- Product details and reviews
- Authenticated cart with quantity/stock validation
- Coupon support
- Checkout with server-side totals
- Cash on Delivery order placement
- Inventory deduction and cancellation restoration
- Order history, details and status timeline
- User registration/login with BCrypt
- Separate admin login and role-based admin console
- Admin product, inventory, order and coupon management
- Forgot/reset password with 30-minute tokens
- SMTP-ready password reset email support
- MySQL/JPA
- CSRF protection
- Responsive mobile UI

## Run locally

Requirements:
- Java 17+
- MySQL 8+
- Maven 3.9+

Create a database:

```sql
CREATE DATABASE commercex;
```

Configure environment variables or edit `application.properties`:

```text
DB_URL=jdbc:mysql://localhost:3306/commercex?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=your_password
PORT=8080
```

If your MySQL root account has no password, pass an empty one:
`DB_PASSWORD= mvn spring-boot:run`

Storefront pricing is configurable and applied to both the cart preview and
the placed order:

```text
TAX_RATE=0.08
SHIPPING_FLAT=9.99
FREE_SHIPPING_THRESHOLD=75
```

Run:

```bash
mvn clean spring-boot:run
```

Open:

```text
http://localhost:8080/shop
```

## Default admin

```text
Email: admin@commercex.com
Password: Admin@12345
```

Change the admin password before production use.

## Production password reset email

Set:

```text
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=...
MAIL_PASSWORD=...
MAIL_FROM=...
APP_BASE_URL=https://your-domain.com
```

The application still exposes the generated reset link on the forgot-password page when mail is unavailable, which is useful during local testing.

## Docker

Build:

```bash
docker build -t commercex .
```

Run:

```bash
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host:3306/commercex?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -e DB_USERNAME="root" \
  -e DB_PASSWORD="your-password" \
  commercex
```

## Important production settings

Set a real MySQL database, a strong admin password, SMTP credentials for reset emails, and `APP_BASE_URL` to your HTTPS domain.

## Fixes in this build

- Order detail page no longer fails with `LazyInitializationException`; order
  items are fetched explicitly because `open-in-view` is disabled
- Product pages now display their reviews, and a product's headline rating is
  recalculated from them
- Review ratings are clamped to 1–5 instead of throwing a server error
- Deleting a product that has reviews no longer violates a foreign key
- Cancelling an order from the admin console restores stock, matching the
  customer-facing cancel; repeat cancels no longer double-restore
- Order status changes are restricted to known statuses
- Coupon creation rejects blank, zero-percent and duplicate codes
- `TAX_RATE`, `SHIPPING_FLAT` and `FREE_SHIPPING_THRESHOLD` are honoured; the
  cart preview and the placed order share one calculation
- Sale price is computed in one place, and can no longer go negative
- Registration validates the name and reports errors instead of failing at the
  database
- `/verify-email` renders a real page instead of a missing template
- Unhandled errors render a branded page; stack traces are logged, not shown
- Lombok updated so the Maven build works on current JDKs
- Default datasource URL matches the documented `commercex` database
