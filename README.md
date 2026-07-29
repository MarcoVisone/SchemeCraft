# SchemeCraft

[![Java](https://img.shields.io/badge/Java-8%2B-%23ED8B00.svg?style=flat&logo=java&logoColor=white)](https://www.java.com/)
[![Apache Tomcat](https://img.shields.io/badge/Apache%20Tomcat-9.x-%23F8DC75.svg?style=flat&logo=apache-tomcat&logoColor=black)](https://tomcat.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-5.7%2B-%234479A1.svg?style=flat&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Educational%20Use%20Only-%23FF0000.svg?style=flat)](https://github.com/yourusername/schemecraft/blob/main/LICENSE)

**SchemeCraft** is a web marketplace for **Minecraft schematics**, designed for the *Tecnologie e Sviluppo per il Web (TSW)* course at the **University of Salerno**. Users can browse, purchase, and download schematics, while administrators manage products, categories, and orders.

---

## Authors
   Name            | Student ID   | Role               |
 |-----------------|--------------|--------------------|
 | Marco Visone    | 0512122246   | Developer          |
 | Oriolo Stefano  | 0512121782   | Developer          |

---

## Features

### 🌍 Visitors (Not Logged In)
- Browse the **product catalog** with filters (category, price range, popularity).
- Search products by **name** or **description**.
- View **product details** (images, description, Minecraft version, price, average rating, reviews).
- Add products to a **cookie-based shopping cart** (persistent across sessions).
- Manage the cart (update quantities, remove items).

### 👤 Registered Users (All Visitor Features +)
- **Register** and **log in** (with optional *Remember Me* via cookies).
- Complete the **checkout process** (simulated payment through a fake gateway).
- Manage **personal profile** (update info, change password, upload avatar).
- Manage **saved addresses** and **payment methods**.
- View **order history** and **order details**.
- Write **reviews** and **ratings** for purchased products.
- Add products to a **favorites/wishlist**.

### 🛡️ Administrators
- **Product Management**: Create, edit, delete products; update stock, price, and discounts.
- **Category Management**: Add, edit, delete categories.
- **Order Management**: View all orders and update their status.

### ⚙️ Additional Features
- **Automated Updates**: Product download counters and average ratings are updated via **MySQL triggers**.
- **Media Uploads**: Support for **banner** and **profile image** uploads.
- **Responsive UI**: Fully responsive design for all devices.

---

## Technologies
 | Technology       | Purpose                          | Version       |
 |------------------|----------------------------------|---------------|
 | **Java**         | Backend logic (Servlets)         | 8+            |
 | **Apache Tomcat**| Web server                       | 9.x           |
 | **MySQL**        | Database                         | 5.7+ / 8.x    |
 | **JSP / JSTL**   | Server-side views                | -             |
 | **JavaScript**   | Client-side interactions         | Plain JS      |
 | **CSS**          | Styling                          | Custom        |
 | **Logback**      | Logging                          | -             |

---

## Project Structure

```plaintext
src/
├── main/
│   ├── java/com/xyra/schemecraft/
│   │   ├── connection/         # ConnectionPool (JDBC DataSource)
│   │   ├── constant/           # Constants for validation, services, DB
│   │   ├── controller/         # Servlets (Account, Admin, Auth, Cart, Favorite, Order, Product, Review)
│   │   ├── dao/                # Data Access Objects
│   │   ├── dto/                # Data Transfer Objects
│   │   ├── exception/          # Custom exceptions
│   │   ├── filter/             # Filters (Encoding, Authentication, Admin, RememberMe)
│   │   ├── model/              # JavaBeans (entities)
│   │   ├── service/            # Business logic
│   │   │   └── gateway/        # Fake payment gateway and tokenization
│   │   └── util/               # Utility classes (Cookie, FileUpload, JSON, etc.)
│   ├── resources/
│   │   ├── database/           # init.sql (schema + lookup data), test.sql (sample data)
│   │   └── logback.xml         # Logging configuration
│   └── webapp/
│       ├── WEB-INF/
│       │   ├── account/        # Account-related JSPs
│       │   ├── admin/          # Admin panel JSPs
│       │   ├── auth/           # Login/Register JSPs
│       │   ├── cart/           # Cart page
│       │   ├── catalog/        # Catalog page
│       │   ├── fragments/      # Header/Footer components
│       │   └── views/          # Product detail page
│       ├── css/                # Stylesheets
│       ├── js/                 # Client-side JavaScript
│       ├── icons/              # Static icons
│       ├── images/             # Static images
│       ├── media/              # Video backgrounds
│       ├── META-INF/
│       │   └── context.xml     # DB connection pool configuration
│       ├── WEB-INF/web.xml     # Deployment descriptor
│       └── index.jsp           # Home page
└── test/                       # (Empty, no automated tests)
