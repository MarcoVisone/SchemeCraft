CREATE DATABASE IF NOT EXISTS schemecraft_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE schemecraft_db;

CREATE TABLE IF NOT EXISTS country (
    country_id VARCHAR(3) PRIMARY KEY,
    country_name VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    tax DECIMAL(10, 2) NOT NULL CHECK (tax >= 0)
);

CREATE TABLE IF NOT EXISTS currency (
    currency_id VARCHAR(3) PRIMARY KEY,
    currency_name VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
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
    country_id VARCHAR(3) NULL,
    currency_id VARCHAR(3) NULL,
    language_id VARCHAR(3) NULL,
    banner_path VARCHAR(255) DEFAULT 'uploads/banners/default-banner.png',
    bio VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    is_admin BOOLEAN DEFAULT FALSE,
    password_hash VARCHAR(255) NOT NULL,
    profile_image_path VARCHAR(255) DEFAULT 'uploads/avatars/default-avatar.png',
    CONSTRAINT fk_account_country FOREIGN KEY (country_id) REFERENCES country(country_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_account_currency FOREIGN KEY (currency_id) REFERENCES currency(currency_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_account_lang FOREIGN KEY (language_id) REFERENCES language(language_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS address (
    address_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    country_id VARCHAR(3) NULL,
    city VARCHAR(100) NOT NULL,
    flag_default BOOLEAN NULL DEFAULT NULL CHECK (flag_default = TRUE),
    is_active BOOLEAN DEFAULT TRUE,
    postal_code VARCHAR(20) NOT NULL,
    state_province VARCHAR(100) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    CONSTRAINT uq_default_address_account UNIQUE (flag_default, account_id),
    CONSTRAINT fk_address_account FOREIGN KEY (account_id) REFERENCES account(account_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_address_country FOREIGN KEY (country_id) REFERENCES country(country_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS payment_method_type (
    type_id INT AUTO_INCREMENT PRIMARY KEY,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    type_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS payment_method (
    payment_method_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    method_type INT NOT NULL,
    card_brand VARCHAR(30) NULL,
    card_expiration VARCHAR(7) NULL,
    card_last_four VARCHAR(4) NULL,
    flag_default BOOLEAN NULL DEFAULT NULL CHECK (flag_default = TRUE),
    payment_email VARCHAR(100) NULL,
    payment_token VARCHAR(255) NOT NULL,
    CONSTRAINT fk_payment_account FOREIGN KEY (account_id) REFERENCES account(account_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_payment_method_type FOREIGN KEY (method_type) REFERENCES payment_method_type(type_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_default_payment_account UNIQUE (flag_default, account_id)
);

CREATE TABLE IF NOT EXISTS product (
    product_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    currency_id VARCHAR(3) NOT NULL,
    average_rating DECIMAL(3, 2) DEFAULT 0.00 NOT NULL CHECK (average_rating >= 0 AND average_rating <= 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    discount DECIMAL(5, 2) DEFAULT 0 CHECK (discount >= 0 AND discount <= 100),
    description TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    latest_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    product_name VARCHAR(100) NOT NULL,
    stock_quantity INT CHECK (stock_quantity >= 0),
    total_downloads INT DEFAULT 0 NOT NULL CHECK (total_downloads >= 0),
    total_reviews INT DEFAULT 0 NOT NULL CHECK (total_reviews >= 0),
    CONSTRAINT fk_product_account FOREIGN KEY (account_id) REFERENCES account(account_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_product_currency FOREIGN KEY (currency_id) REFERENCES currency(currency_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FULLTEXT KEY ft_product_search (product_name, description),
    INDEX idx_product_price (price),
    INDEX idx_product_rating (average_rating),
    INDEX idx_product_total_downloads (total_downloads),
    INDEX idx_product_total_reviews (total_reviews)
);

CREATE TABLE IF NOT EXISTS product_image (
    image_id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES product(product_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS account_product(
    account_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, product_id),
    CONSTRAINT fk_account_product_account FOREIGN KEY (account_id) REFERENCES account(account_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_account_product_product FOREIGN KEY (product_id) REFERENCES product(product_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS favorite (
    account_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (account_id, product_id),
    CONSTRAINT fk_favorite_account FOREIGN KEY (account_id) REFERENCES account(account_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_favorite_product FOREIGN KEY (product_id) REFERENCES product(product_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS review (
    account_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    PRIMARY KEY (product_id, account_id),
    CONSTRAINT fk_review_account FOREIGN KEY (account_id) REFERENCES account(account_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES product(product_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_review_rating (rating)
);

CREATE TABLE IF NOT EXISTS category (
    category_id VARCHAR(36) PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    parent_category_name VARCHAR(100) NULL,
    description TEXT,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_name) REFERENCES category(category_name)
        ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS product_category (
    category_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (product_id, category_id),
    CONSTRAINT fk_product_category_category FOREIGN KEY (category_id) REFERENCES category(category_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_product_category_product FOREIGN KEY (product_id) REFERENCES product(product_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS product_version (
    version_id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    changelog TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    download_count INT DEFAULT 0,
    file_path VARCHAR(255) NOT NULL,
    minecraft_version VARCHAR(20) NOT NULL,
    version VARCHAR(20) NOT NULL,
    CONSTRAINT fk_product_version_product FOREIGN KEY (product_id) REFERENCES product(product_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS cart (
    account_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (account_id, product_id),
    CONSTRAINT fk_cart_account FOREIGN KEY (account_id) REFERENCES account(account_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES product(product_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS order_status (
    status_id INT AUTO_INCREMENT PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS order_table (
    order_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    address_id VARCHAR(36) NOT NULL,
    currency_id VARCHAR(3) NOT NULL,
    method_type INT NOT NULL,
    status INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    transaction_id VARCHAR(100) NOT NULL,
    CONSTRAINT fk_order_table_account FOREIGN KEY (account_id) REFERENCES account(account_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_order_table_address FOREIGN KEY (address_id) REFERENCES address(address_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_order_table_currency FOREIGN KEY (currency_id) REFERENCES currency(currency_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_order_table_method_type FOREIGN KEY (method_type) REFERENCES payment_method_type(type_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_order_table_status FOREIGN KEY (status) REFERENCES order_status(status_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_order_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS order_item (
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    discount DECIMAL(5, 2) DEFAULT 0 CHECK (discount >= 0 AND discount <= 100),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    tax DECIMAL(10, 2) NOT NULL CHECK (tax >= 0),
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES order_table(order_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(product_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

DELIMITER $$

CREATE TRIGGER tg_country_soft_delete
    AFTER UPDATE ON country
    FOR EACH ROW
BEGIN
    IF OLD.is_active = TRUE AND NEW.is_active = FALSE THEN
        UPDATE account
        SET country_id = NULL
        WHERE country_id = OLD.country_id;
    END IF;
END$$

CREATE TRIGGER tg_currency_soft_delete
    AFTER UPDATE ON currency
    FOR EACH ROW
BEGIN
    IF OLD.is_active = TRUE AND NEW.is_active = FALSE THEN
        UPDATE account
        SET currency_id = NULL
        WHERE currency_id = OLD.currency_id;
    END IF;
END$$

CREATE TRIGGER tg_review_insert
    AFTER INSERT ON review
    FOR EACH ROW
BEGIN
    UPDATE product
    SET
        average_rating = (SELECT COALESCE(AVG(rating), 0.00) FROM review WHERE product_id = NEW.product_id),
        total_reviews = (SELECT COUNT(*) FROM review WHERE product_id = NEW.product_id)
    WHERE product_id = NEW.product_id;
END$$

CREATE TRIGGER tg_review_update
    AFTER UPDATE ON review
    FOR EACH ROW
BEGIN
    IF OLD.rating <> NEW.rating THEN
        UPDATE product
        SET
            average_rating = (SELECT COALESCE(AVG(rating), 0.00) FROM review WHERE product_id = NEW.product_id)
        WHERE product_id = NEW.product_id;
    END IF;
END$$

CREATE TRIGGER tg_review_delete
    AFTER DELETE ON review
    FOR EACH ROW
BEGIN
    UPDATE product
    SET
        average_rating = (SELECT COALESCE(AVG(rating), 0.00) FROM review WHERE product_id = OLD.product_id),
        total_reviews = (SELECT COUNT(*) FROM review WHERE product_id = OLD.product_id)
    WHERE product_id = OLD.product_id;
END$$

CREATE TRIGGER after_version_download_update
    AFTER UPDATE ON product_version
    FOR EACH ROW
BEGIN
    IF OLD.download_count <> NEW.download_count THEN
        UPDATE product
        SET total_downloads = (SELECT COALESCE(SUM(download_count), 0) FROM product_version WHERE product_id = NEW.product_id)
        WHERE product_id = NEW.product_id;
    END IF;
END$$

CREATE TRIGGER after_version_download_insert
    AFTER INSERT ON product_version
    FOR EACH ROW
BEGIN
    UPDATE product
    SET total_downloads = (SELECT COALESCE(SUM(download_count), 0) FROM product_version WHERE product_id = NEW.product_id)
    WHERE product_id = NEW.product_id;
END$$

DELIMITER ;
