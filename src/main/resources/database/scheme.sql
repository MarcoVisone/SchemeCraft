CREATE DATABASE IF NOT EXISTS schemecraft_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE schemecraft_db;

CREATE TABLE IF NOT EXISTS country (
    country_id VARCHAR(3) PRIMARY KEY,
    country_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS currency (
    currency_id VARCHAR(3) PRIMARY KEY,
    currency_name VARCHAR(100) NOT NULL UNIQUE,
    symbol VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS language (
    language_id VARCHAR(3) PRIMARY KEY,
    language_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS account (
    account_id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE,
    country_id VARCHAR(3) NULL,
    currency_id VARCHAR(3) NULL,
    language_id VARCHAR(3) NULL,
    bio VARCHAR(255) NULL,
    profile_image_path VARCHAR(255) DEFAULT 'uploads/avatars/default-avatar.png',
    banner_path VARCHAR(255) DEFAULT 'uploads/banners/default-banner.png',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_country FOREIGN KEY (country_id) REFERENCES country(country_id) ON DELETE SET NULL,
    CONSTRAINT fk_account_currency FOREIGN KEY (currency_id) REFERENCES currency(currency_id) ON DELETE SET NULL,
    CONSTRAINT fk_account_lang FOREIGN KEY (language_id) REFERENCES language(language_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS address (
    address_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    flag_default BOOLEAN NULL DEFAULT NULL CHECK (flag_default = TRUE),
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state_province VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country_id VARCHAR(3) NOT NULL,
    CONSTRAINT uq_default_address_account UNIQUE (flag_default, account_id),
    CONSTRAINT fk_address_account FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    CONSTRAINT fk_address_country FOREIGN KEY (country_id) REFERENCES country(country_id)
);

CREATE TABLE IF NOT EXISTS payment_method_type (
    type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS payment_method (
    payment_method_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    flag_default BOOLEAN NULL DEFAULT NULL CHECK (flag_default = TRUE),
    method_type INT NOT NULL,
    payment_token VARCHAR(255) NOT NULL,
    card_brand VARCHAR(30) NULL,
    card_last_four VARCHAR(4) NULL,
    card_expiration VARCHAR(7) NULL,
    payment_email VARCHAR(100) NULL,
    CONSTRAINT fk_payment_method_type FOREIGN KEY (method_type) REFERENCES payment_method_type(type_id),
    CONSTRAINT fk_payment_account FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    CONSTRAINT uq_default_payment_account UNIQUE (flag_default, account_id)
);

CREATE TABLE IF NOT EXISTS product (
    product_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    currency_id VARCHAR(3) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    discount DECIMAL(5, 2) DEFAULT 0 CHECK (discount >= 0 AND discount <= 100),
    description TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    stock_quantity INT CHECK (stock_quantity >= 0),
    is_active BOOLEAN DEFAULT TRUE,
    latest_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_account FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE RESTRICT,
    CONSTRAINT fk_product_currency FOREIGN KEY (currency_id) REFERENCES currency(currency_id),
    FULLTEXT KEY ft_product_search (product_name, description)
);
CREATE INDEX idx_product_price ON product(price);

CREATE TABLE IF NOT EXISTS product_image (
    image_id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS account_product(
    account_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, product_id),
    CONSTRAINT fk_account_product_account FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    CONSTRAINT fk_account_product_product FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS favorite (
    favorite_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_favorite_account FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_product FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE,
    CONSTRAINT uq_favorite_account_product UNIQUE (account_id, product_id)
);

CREATE TABLE IF NOT EXISTS review (
    review_id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_account FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    CONSTRAINT uq_review_account_product UNIQUE (account_id, product_id)
);

CREATE TABLE IF NOT EXISTS cart (
    cart_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_cart_account FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cart_item (
    cart_item_id VARCHAR(36) PRIMARY KEY,
    cart_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart(cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE,
    CONSTRAINT uq_cart_product UNIQUE (cart_id, product_id)
);

CREATE TABLE IF NOT EXISTS order_status (
    status_id INT AUTO_INCREMENT PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS order_table (
    order_id VARCHAR(36) PRIMARY KEY,
    currency_id VARCHAR(3) NOT NULL,
    method_type INT NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    status INT NOT NULL DEFAULT 1,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_table_method_type FOREIGN KEY (method_type) REFERENCES payment_method_type(type_id),
    CONSTRAINT fk_order_table_status FOREIGN KEY (status) REFERENCES order_status(status_id),
    CONSTRAINT fk_order_table_account FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE RESTRICT,
    CONSTRAINT fk_order_table_currency FOREIGN KEY (currency_id) REFERENCES currency(currency_id)
);
CREATE INDEX idx_order_created_at ON order_table(created_at);

CREATE TABLE IF NOT EXISTS order_item (
    order_item_id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    discount DECIMAL(5, 2) DEFAULT 0 CHECK (discount >= 0 AND discount <= 100),
    product_id VARCHAR(36) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    tax DECIMAL(10, 2) NOT NULL CHECK (tax >= 0),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES order_table(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS category (
    category_id VARCHAR(36) PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS product_category (
    product_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36) NOT NULL,
    parent_category_id VARCHAR(36) NULL,
    PRIMARY KEY (product_id, category_id),
    CONSTRAINT fk_product_category_parent FOREIGN KEY (parent_category_id) REFERENCES category(category_id) ON DELETE SET NULL,
    CONSTRAINT fk_product_category_product FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE,
    CONSTRAINT fk_product_category_category FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_version (
    version_id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    version VARCHAR(20) NOT NULL,
    download_count INT DEFAULT 0,
    minecraft_version VARCHAR(20) NOT NULL,
    changelog TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_version_product FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
);

CREATE OR REPLACE VIEW v_product AS
SELECT
    p.*,
    COALESCE(SUM(pv.download_count), 0) AS total_download_count
FROM product p
         LEFT JOIN product_version pv ON p.product_id = pv.product_id
GROUP BY p.product_id;
