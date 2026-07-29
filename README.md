# SchemeCraft

[![Java](https://img.shields.io/badge/Java-8%2B-%23ED8B00.svg?style=flat&logo=java&logoColor=white)](https://www.java.com/)
[![Apache Tomcat](https://img.shields.io/badge/Apache%20Tomcat-9.x-%23F8DC75.svg?style=flat&logo=apache-tomcat&logoColor=black)](https://tomcat.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-5.7%2B-%234479A1.svg?style=flat&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Educational%20Use%20Only-%23FF0000.svg?style=flat)](https://github.com/yourusername/schemecraft/blob/main/LICENSE)

**SchemeCraft** is a web marketplace for **Minecraft schematics**, designed for the *Tecnologie e Sviluppo per il Web (TSW)* course at the **University of Salerno**. Users can browse, purchase, and download schematics, while administrators manage products, categories, and orders.

---

## Authors

| Name            | Student ID   | Role               |
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
```

---

## Installation & Setup

### Prerequisites
- **JDK 8 or later** (tested with OpenJDK 11).
- **Apache Tomcat 9.x**.
- **MySQL Server** (5.7 or later / 8.x).
- A **MySQL user** with `CREATE`, `INSERT`, `UPDATE`, and `DELETE` privileges on a dedicated database.

---

### Step-by-Step Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/schemecraft.git
   cd schemecraft
   ```

2. **Create the Database**
   - Open your MySQL client and run:
     ```sql
     SOURCE src/main/resources/database/init.sql;
     ```
   - This creates the `schemecraft_db` schema, tables, lookup data, and triggers.

3. **(Optional) Load Test Data**
   - Populate the database with sample data:
     ```sql
     SOURCE src/main/resources/database/test.sql;
     ```

4. **Configure the Database Connection Pool**
   - Edit `src/main/webapp/META-INF/context.xml` and replace `DB_USERNAME` and `DB_PASSWORD` with your MySQL credentials:
     ```xml
     <?xml version="1.0" encoding="UTF-8"?>
     <Context>
         <Resource name="jdbc/SchemeCraftDB"
                   auth="Container"
                   type="javax.sql.DataSource"
                   factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
                   maxTotal="100"
                   maxIdle="30"
                   minIdle="10"
                   maxWaitMillis="10000"
                   initialSize="10"
                   removeAbandonedOnBorrow="true"
                   removeAbandonedTimeout="60"
                   validationQuery="SELECT 1"
                   testOnBorrow="true"
                   driverClassName="com.mysql.cj.jdbc.Driver"
                   url="jdbc:mysql://localhost:3306/schemecraft_db?useSSL=false&amp;allowPublicKeyRetrieval=true&amp;serverTimezone=UTC&amp;characterEncoding=UTF-8"
                   username="YOUR_DB_USERNAME"
                   password="YOUR_DB_PASSWORD"/>
     </Context>
     ```
   - Ensure the **MySQL JDBC driver** (`mysql-connector-java`) is available in Tomcat’s `lib` folder or included in your project’s dependencies.

5. **Deploy the Application**
   - Build the project (e.g., with Maven or by copying the `webapp` folder to Tomcat’s `webapps` as an exploded WAR).
   - If using an IDE (Eclipse, IntelliJ), configure the Tomcat server and deploy the `schemecraft` context.

6. **Start Tomcat**
   - Access the application at:
     ```
     http://localhost:8080/schemecraft/
     ```

---

## Default Credentials (If Test Data Loaded)

| Role        | Username | Password |
|-------------|----------|----------|
| Admin       | `admin`  | `admin`  |
| Regular User| `user`   | `user`   |

⚠️ **Warning**: These credentials are created by `test.sql`. **Change them immediately** in production environments.

---

## Screenshots

*(Add screenshots of the application here to showcase its UI. Example:)*
- **Homepage**
- **Product Catalog**
- **Admin Dashboard**
- **Checkout Process**

---

## Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m "Add your feature"`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a **Pull Request**.

---

## Notes & Limitations

- **Payment Simulation**: Payments are simulated using a fake gateway. No real money is charged.
- **No Automated Tests**: Unit/integration tests are out of scope for this academic project.
- **No CI/CD Pipeline**: Deployment is manual.
- **Admin User Management**: Admin accounts must be created directly in the database.
- **Banner Feature**: The banner feature may be removed in future versions.

---
---

## License

This project is **for educational purposes only**. All rights reserved.
© 2026 Marco Visone, Oriolo Stefano.
