-- =============================================================================
-- INIT.SQL - Seed lookup tables and initial admin account
-- =============================================================================

USE schemecraft_db;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE order_item;
TRUNCATE TABLE order_table;
TRUNCATE TABLE review;
TRUNCATE TABLE cart;
TRUNCATE TABLE favorite;
TRUNCATE TABLE account_product;
TRUNCATE TABLE product_category;
TRUNCATE TABLE category;
TRUNCATE TABLE product_version;
TRUNCATE TABLE product_image;
TRUNCATE TABLE product;
TRUNCATE TABLE payment_method;
TRUNCATE TABLE address;
TRUNCATE TABLE remember_token;
TRUNCATE TABLE account;
TRUNCATE TABLE payment_method_type;
TRUNCATE TABLE order_status;
TRUNCATE TABLE language;
TRUNCATE TABLE currency;
TRUNCATE TABLE country;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 1. Countries
-- =============================================================================
INSERT INTO country (country_id, country_name, is_active, tax) VALUES
                                                                   ('ITA', 'Italy',       TRUE, 22.00),
                                                                   ('USA', 'United States',TRUE, 0.00),
                                                                   ('GBR', 'United Kingdom', TRUE, 20.00),
                                                                   ('DEU', 'Germany', TRUE, 19.00),
                                                                   ('FRA', 'France', TRUE, 20.00),
                                                                   ('ESP', 'Spain', TRUE, 21.00),
                                                                   ('CAN', 'Canada', TRUE, 5.00),
                                                                   ('AUS', 'Australia', TRUE, 10.00);

-- =============================================================================
-- 2. Currencies
-- =============================================================================
INSERT INTO currency (currency_id, currency_name, is_active, symbol) VALUES
                                                                         ('EUR', 'Euro', TRUE, '€'),
                                                                         ('USD', 'US Dollar', TRUE, '$'),
                                                                         ('GBP', 'British Pound', TRUE, '£'),
                                                                         ('CAD', 'Canadian Dollar', TRUE, 'C$'),
                                                                         ('AUD', 'Australian Dollar', TRUE, 'A$');

-- =============================================================================
-- 3. Languages
-- =============================================================================
INSERT INTO language (language_id, language_name) VALUES
                                                      ('EN', 'English');

-- =============================================================================
-- 4. Order Statuses
-- =============================================================================
INSERT INTO order_status (status_id, status_name) VALUES
                                                      (1, 'PENDING'),
                                                      (2, 'PAID'),
                                                      (3, 'SHIPPED'),
                                                      (4, 'CANCELLED'),
                                                      (5, 'PENDING_VERIFICATION');

-- =============================================================================
-- 5. Payment Method Types
-- =============================================================================
INSERT INTO payment_method_type (type_id, is_active, type_name) VALUES
                                                                    (1, TRUE, 'Credit card/Debit card'),
                                                                    (2, TRUE, 'PayPal');

-- =============================================================================
-- 6. Initial Admin Account
-- =============================================================================
INSERT INTO account (
    account_id,
    username,
    email,
    country_id,
    currency_id,
    language_id,
    banner_path,
    bio,
    created_at,
    is_active,
    is_admin,
    password_hash,
    profile_image_path
) VALUES (
             'acc-0000-0000-0000-000000000001',
             'admin',
             'admin@schemecraft.com',
             'ITA',
             'EUR',
             'EN',
             'uploads/banners/default-banner.png',
             'System Administrator',
             CURRENT_TIMESTAMP,
             TRUE,
             TRUE,
             '$2a$12$MB3FOZBoOCwE0hPT2Ek1AeRDnU/sn/uV0IPpm1cbhYzxJMrYyMJ3S',
             'uploads/avatars/default-avatar.png'
         );
