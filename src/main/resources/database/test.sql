-- =============================================================================
-- TEST.SQL - Self-contained fake population for local/dev testing
-- Mirrors init.sql structure (TRUNCATE + lookups + admin) and adds:
-- customer accounts, addresses, payment methods, 15 products (all owned
-- by admin), images, versions, categories, cart, favorites, reviews, orders.
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
                                                                   ('ITA', 'Italy',          TRUE, 22.00),
                                                                   ('USA', 'United States',  TRUE, 0.00),
                                                                   ('GBR', 'United Kingdom', TRUE, 20.00),
                                                                   ('DEU', 'Germany',        TRUE, 19.00),
                                                                   ('FRA', 'France',         TRUE, 20.00),
                                                                   ('ESP', 'Spain',          TRUE, 21.00),
                                                                   ('CAN', 'Canada',         TRUE, 5.00),
                                                                   ('AUS', 'Australia',      TRUE, 10.00);

-- =============================================================================
-- 2. Currencies
-- =============================================================================
INSERT INTO currency (currency_id, currency_name, is_active, symbol) VALUES
                                                                         ('EUR', 'Euro',             TRUE, '€'),
                                                                         ('USD', 'US Dollar',        TRUE, '$'),
                                                                         ('GBP', 'British Pound',    TRUE, '£'),
                                                                         ('CAD', 'Canadian Dollar',  TRUE, 'C$'),
                                                                         ('AUD', 'Australian Dollar',TRUE, 'A$');

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
-- 6. Admin account (same as init.sql, admin owns everything it uploads)
-- =============================================================================
INSERT INTO account (
    account_id, username, email, country_id, currency_id, language_id,
    banner_path, bio, created_at, is_active, is_admin, password_hash, profile_image_path
) VALUES (
             'acc-0000-0000-0000-000000000001',
             'admin',
             'admin@schemecraft.com',
             'ITA', 'EUR', 'EN',
             'uploads/banners/default-banner.png',
             'System Administrator',
             CURRENT_TIMESTAMP,
             TRUE, TRUE,
             '$2a$12$MB3FOZBoOCwE0hPT2Ek1AeRDnU/sn/uV0IPpm1cbhYzxJMrYyMJ3S',
             'uploads/avatars/default-avatar.png'
         );

-- =============================================================================
-- 7. Customer accounts
-- Password hash for all test customers is the same bcrypt hash as admin
-- (i.e. plaintext "admin123" per the original hash) purely for test convenience.
-- =============================================================================
INSERT INTO account (
    account_id, username, email, country_id, currency_id, language_id,
    banner_path, bio, created_at, is_active, is_admin, password_hash, profile_image_path
) VALUES
      ('acc-0000-0000-0000-000000000002', 'steve_builder', 'steve@example.com', 'USA', 'USD', 'EN',
       'uploads/banners/default-banner.png', 'Redstone enthusiast', CURRENT_TIMESTAMP, TRUE, FALSE,
       '$2a$12$MB3FOZBoOCwE0hPT2Ek1AeRDnU/sn/uV0IPpm1cbhYzxJMrYyMJ3S', 'uploads/avatars/default-avatar.png'),
      ('acc-0000-0000-0000-000000000003', 'alex_crafter', 'alex@example.com', 'GBR', 'GBP', 'EN',
       'uploads/banners/default-banner.png', 'Building medieval castles', CURRENT_TIMESTAMP, TRUE, FALSE,
       '$2a$12$MB3FOZBoOCwE0hPT2Ek1AeRDnU/sn/uV0IPpm1cbhYzxJMrYyMJ3S', 'uploads/avatars/default-avatar.png'),
      ('acc-0000-0000-0000-000000000004', 'giulia_mc', 'giulia@example.com', 'ITA', 'EUR', 'EN',
       'uploads/banners/default-banner.png', NULL, CURRENT_TIMESTAMP, TRUE, FALSE,
       '$2a$12$MB3FOZBoOCwE0hPT2Ek1AeRDnU/sn/uV0IPpm1cbhYzxJMrYyMJ3S', 'uploads/avatars/default-avatar.png'),
      ('acc-0000-0000-0000-000000000005', 'hans_redstone', 'hans@example.com', 'DEU', 'EUR', 'EN',
       'uploads/banners/default-banner.png', 'Technical builds only', CURRENT_TIMESTAMP, TRUE, FALSE,
       '$2a$12$MB3FOZBoOCwE0hPT2Ek1AeRDnU/sn/uV0IPpm1cbhYzxJMrYyMJ3S', 'uploads/avatars/default-avatar.png'),
      ('acc-0000-0000-0000-000000000006', 'marie_build', 'marie@example.com', 'FRA', 'EUR', 'EN',
       'uploads/banners/default-banner.png', NULL, CURRENT_TIMESTAMP, TRUE, FALSE,
       '$2a$12$MB3FOZBoOCwE0hPT2Ek1AeRDnU/sn/uV0IPpm1cbhYzxJMrYyMJ3S', 'uploads/avatars/default-avatar.png'),
      ('acc-0000-0000-0000-000000000007', 'carlos_esp', 'carlos@example.com', 'ESP', 'EUR', 'EN',
       'uploads/banners/default-banner.png', NULL, CURRENT_TIMESTAMP, TRUE, FALSE,
       '$2a$12$MB3FOZBoOCwE0hPT2Ek1AeRDnU/sn/uV0IPpm1cbhYzxJMrYyMJ3S', 'uploads/avatars/default-avatar.png'),
      ('acc-0000-0000-0000-000000000008', 'inactive_user', 'inactive@example.com', 'ITA', 'EUR', 'EN',
       'uploads/banners/default-banner.png', 'Deactivated test account', CURRENT_TIMESTAMP, FALSE, FALSE,
       '$2a$12$MB3FOZBoOCwE0hPT2Ek1AeRDnU/sn/uV0IPpm1cbhYzxJMrYyMJ3S', 'uploads/avatars/default-avatar.png');

-- =============================================================================
-- 8. Addresses (each active customer gets one default address)
-- =============================================================================
INSERT INTO address (address_id, account_id, country_id, city, flag_default, is_active, postal_code, state_province, street_address) VALUES
                                                                                                                                         ('addr-0000-0000-0000-000000000001', 'acc-0000-0000-0000-000000000002', 'USA', 'Austin',   TRUE, TRUE, '73301', 'Texas',       '123 Creeper Lane'),
                                                                                                                                         ('addr-0000-0000-0000-000000000002', 'acc-0000-0000-0000-000000000003', 'GBR', 'London',    TRUE, TRUE, 'SW1A 1AA', 'Greater London', '45 Diamond Street'),
                                                                                                                                         ('addr-0000-0000-0000-000000000003', 'acc-0000-0000-0000-000000000004', 'ITA', 'Milano',    TRUE, TRUE, '20100', 'MI',          'Via Redstone 10'),
                                                                                                                                         ('addr-0000-0000-0000-000000000004', 'acc-0000-0000-0000-000000000005', 'DEU', 'Berlin',    TRUE, TRUE, '10115', 'Berlin',      'Blockstraße 7'),
                                                                                                                                         ('addr-0000-0000-0000-000000000005', 'acc-0000-0000-0000-000000000006', 'FRA', 'Paris',     TRUE, TRUE, '75001', 'Île-de-France', '5 Rue du Nether'),
                                                                                                                                         -- second, non-default address for one customer to exercise the unique-default constraint
                                                                                                                                         ('addr-0000-0000-0000-000000000006', 'acc-0000-0000-0000-000000000002', 'USA', 'Dallas',    NULL, TRUE, '75201', 'Texas',       '99 Ender Ave');

-- =============================================================================
-- 9. Payment methods (mix of card and PayPal, one default each)
-- =============================================================================
INSERT INTO payment_method (payment_method_id, account_id, method_type, card_brand, card_expiration, card_last_four, flag_default, payment_email, payment_token) VALUES
                                                                                                                                                                     ('pm-0000-0000-0000-000000000001', 'acc-0000-0000-0000-000000000002', 1, 'VISA', '2027-08', '4242', TRUE, NULL, 'tok_test_visa_0001'),
                                                                                                                                                                     ('pm-0000-0000-0000-000000000002', 'acc-0000-0000-0000-000000000003', 2, NULL, NULL, NULL, TRUE, 'alex@example.com', 'tok_test_paypal_0002'),
                                                                                                                                                                     ('pm-0000-0000-0000-000000000003', 'acc-0000-0000-0000-000000000004', 1, 'MASTERCARD', '2026-11', '5588', TRUE, NULL, 'tok_test_mc_0003'),
                                                                                                                                                                     ('pm-0000-0000-0000-000000000004', 'acc-0000-0000-0000-000000000005', 1, 'VISA', '2028-01', '1111', TRUE, NULL, 'tok_test_visa_0004'),
                                                                                                                                                                     ('pm-0000-0000-0000-000000000005', 'acc-0000-0000-0000-000000000006', 2, NULL, NULL, NULL, TRUE, 'marie@example.com', 'tok_test_paypal_0005'),
                                                                                                                                                                     -- second, non-default payment method for one customer
                                                                                                                                                                     ('pm-0000-0000-0000-000000000006', 'acc-0000-0000-0000-000000000002', 2, NULL, NULL, NULL, NULL, 'steve@example.com', 'tok_test_paypal_0006');

-- =============================================================================
-- 10. Categories (with parent/child hierarchy via parent_category_id)
-- =============================================================================
INSERT INTO category (category_id, category_name, parent_category_id, description) VALUES
                                                                                       ('cat-0000-0000-0000-000000000001', 'Buildings',   NULL, 'Standalone structures and buildings'),
                                                                                       ('cat-0000-0000-0000-000000000002', 'Redstone',    NULL, 'Redstone contraptions and machinery'),
                                                                                       ('cat-0000-0000-0000-000000000003', 'Landscaping', NULL, 'Terraforming and landscape design'),
                                                                                       ('cat-0000-0000-0000-000000000004', 'Castles',     'cat-0000-0000-0000-000000000001', 'Medieval castles and fortresses'),
                                                                                       ('cat-0000-0000-0000-000000000005', 'Modern Houses','cat-0000-0000-0000-000000000001', 'Modern-style residential builds'),
                                                                                       ('cat-0000-0000-0000-000000000006', 'Farms',       'cat-0000-0000-0000-000000000002', 'Automatic farms');

-- =============================================================================
-- 11. Products (15 total, all uploaded by and owned by admin)
-- =============================================================================
INSERT INTO product (product_id, account_id, currency_id, average_rating, created_at, discount, description, is_active, latest_update, price, product_name, stock_quantity, total_downloads, total_reviews) VALUES
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000001', 'acc-0000-0000-0000-000000000001', 'EUR', 0.00, CURRENT_TIMESTAMP, 0,    'A grand medieval castle with working drawbridge and full interior.', TRUE, CURRENT_TIMESTAMP, 12.99, 'Medieval Castle',            NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000002', 'acc-0000-0000-0000-000000000001', 'EUR', 0.00, CURRENT_TIMESTAMP, 10.00,'Fully automatic wheat and carrot farm with hopper collection.',       TRUE, CURRENT_TIMESTAMP, 6.50,  'Auto Crop Farm',             NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000003', 'acc-0000-0000-0000-000000000001', 'USD', 0.00, CURRENT_TIMESTAMP, 0,    'Sleek modern villa with pool and glass walls.',                       TRUE, CURRENT_TIMESTAMP, 9.99,  'Modern Villa',               NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000004', 'acc-0000-0000-0000-000000000001', 'EUR', 0.00, CURRENT_TIMESTAMP, 0,    'Compact 4x4 piston door with hidden lever mechanism.',                TRUE, CURRENT_TIMESTAMP, 3.50,  'Redstone Piston Door',       NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000005', 'acc-0000-0000-0000-000000000001', 'EUR', 0.00, CURRENT_TIMESTAMP, 15.00,'Realistic mountain terrain with rivers and waterfalls.',              TRUE, CURRENT_TIMESTAMP, 8.00,  'Mountain Landscape',         NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000006', 'acc-0000-0000-0000-000000000001', 'GBP', 0.00, CURRENT_TIMESTAMP, 0,    'Cozy suburban house with garden and garage.',                         TRUE, CURRENT_TIMESTAMP, 5.99,  'Suburban House',             NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000007', 'acc-0000-0000-0000-000000000001', 'EUR', 0.00, CURRENT_TIMESTAMP, 0,    'Fully automatic iron farm, no zombie required.',                      TRUE, CURRENT_TIMESTAMP, 7.25,  'Automatic Iron Farm',        NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000008', 'acc-0000-0000-0000-000000000001', 'EUR', 0.00, CURRENT_TIMESTAMP, 0,    'Towering wizard tower with spiral staircase and library.',            TRUE, CURRENT_TIMESTAMP, 11.00, 'Wizard Tower',               NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000009', 'acc-0000-0000-0000-000000000001', 'EUR', 0.00, CURRENT_TIMESTAMP, 20.00,'Massive stone fortress with defensive walls and towers.',             TRUE, CURRENT_TIMESTAMP, 14.99, 'Stone Fortress',             NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000010', 'acc-0000-0000-0000-000000000001', 'USD', 0.00, CURRENT_TIMESTAMP, 0,    'Compact TNT cannon with adjustable range.',                           TRUE, CURRENT_TIMESTAMP, 4.25,  'TNT Cannon',                 NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000011', 'acc-0000-0000-0000-000000000001', 'EUR', 0.00, CURRENT_TIMESTAMP, 0,    'Terraced rice fields and zen garden.',                                TRUE, CURRENT_TIMESTAMP, 6.75,  'Zen Garden',                 NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000012', 'acc-0000-0000-0000-000000000001', 'EUR', 0.00, CURRENT_TIMESTAMP, 0,    'Small windmill farmhouse with functional decoration.',                TRUE, CURRENT_TIMESTAMP, 4.99,  'Windmill Farmhouse',         NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000013', 'acc-0000-0000-0000-000000000001', 'EUR', 0.00, CURRENT_TIMESTAMP, 0,    'Deprecated build kept for catalog history, no longer promoted.',      FALSE,CURRENT_TIMESTAMP, 2.99,  'Old Watchtower (Deprecated)',NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000014', 'acc-0000-0000-0000-000000000001', 'EUR', 0.00, CURRENT_TIMESTAMP, 0,    'Underground base with hidden entrance and storage hall.',             TRUE, CURRENT_TIMESTAMP, 9.50,  'Hidden Underground Base',    NULL, 0, 0),
                                                                                                                                                                                                                ('prod-0000-0000-0000-000000000015', 'acc-0000-0000-0000-000000000001', 'EUR', 0.00, CURRENT_TIMESTAMP, 5.00, 'Floating sky island with waterfall and tree house.',                  TRUE, CURRENT_TIMESTAMP, 8.50,  'Sky Island',                 NULL, 0, 0);

-- =============================================================================
-- 12. Product-category assignments
-- =============================================================================
INSERT INTO product_category (category_id, product_id) VALUES
                                                           ('cat-0000-0000-0000-000000000004', 'prod-0000-0000-0000-000000000001'), -- Castle -> Castles
                                                           ('cat-0000-0000-0000-000000000001', 'prod-0000-0000-0000-000000000001'), -- Castle -> Buildings
                                                           ('cat-0000-0000-0000-000000000006', 'prod-0000-0000-0000-000000000002'), -- Crop farm -> Farms
                                                           ('cat-0000-0000-0000-000000000002', 'prod-0000-0000-0000-000000000002'), -- Crop farm -> Redstone
                                                           ('cat-0000-0000-0000-000000000005', 'prod-0000-0000-0000-000000000003'), -- Villa -> Modern Houses
                                                           ('cat-0000-0000-0000-000000000002', 'prod-0000-0000-0000-000000000004'), -- Piston door -> Redstone
                                                           ('cat-0000-0000-0000-000000000003', 'prod-0000-0000-0000-000000000005'), -- Mountain -> Landscaping
                                                           ('cat-0000-0000-0000-000000000005', 'prod-0000-0000-0000-000000000006'), -- Suburban house -> Modern Houses
                                                           ('cat-0000-0000-0000-000000000006', 'prod-0000-0000-0000-000000000007'), -- Iron farm -> Farms
                                                           ('cat-0000-0000-0000-000000000001', 'prod-0000-0000-0000-000000000008'), -- Wizard tower -> Buildings
                                                           ('cat-0000-0000-0000-000000000004', 'prod-0000-0000-0000-000000000009'), -- Fortress -> Castles
                                                           ('cat-0000-0000-0000-000000000002', 'prod-0000-0000-0000-000000000010'),-- TNT cannon -> Redstone
                                                           ('cat-0000-0000-0000-000000000003', 'prod-0000-0000-0000-000000000011'),-- Zen garden -> Landscaping
                                                           ('cat-0000-0000-0000-000000000001', 'prod-0000-0000-0000-000000000012'),-- Windmill -> Buildings
                                                           ('cat-0000-0000-0000-000000000001', 'prod-0000-0000-0000-000000000013'),-- Old watchtower -> Buildings
                                                           ('cat-0000-0000-0000-000000000001', 'prod-0000-0000-0000-000000000014'),-- Underground base -> Buildings
                                                           ('cat-0000-0000-0000-000000000003', 'prod-0000-0000-0000-000000000015');-- Sky island -> Landscaping

-- =============================================================================
-- 13. Product images
-- All products reuse the same 2 placeholder cover images stored under
-- webapp/uploads/products/. See notes at the end of this file for exact paths.
-- =============================================================================
INSERT INTO product_image (image_id, product_id, image_path, display_order) VALUES
                                                                                ('img-0000-0000-0000-000000000001', 'prod-0000-0000-0000-000000000001', 'uploads/products/default-cover-1.png', 0),
                                                                                ('img-0000-0000-0000-000000000002', 'prod-0000-0000-0000-000000000001', 'uploads/products/default-cover-2.png', 1),
                                                                                ('img-0000-0000-0000-000000000003', 'prod-0000-0000-0000-000000000002', 'uploads/products/default-cover-1.png', 0),
                                                                                ('img-0000-0000-0000-000000000004', 'prod-0000-0000-0000-000000000003', 'uploads/products/default-cover-2.png', 0),
                                                                                ('img-0000-0000-0000-000000000005', 'prod-0000-0000-0000-000000000004', 'uploads/products/default-cover-1.png', 0),
                                                                                ('img-0000-0000-0000-000000000006', 'prod-0000-0000-0000-000000000005', 'uploads/products/default-cover-2.png', 0),
                                                                                ('img-0000-0000-0000-000000000007', 'prod-0000-0000-0000-000000000006', 'uploads/products/default-cover-1.png', 0),
                                                                                ('img-0000-0000-0000-000000000008', 'prod-0000-0000-0000-000000000007', 'uploads/products/default-cover-2.png', 0),
                                                                                ('img-0000-0000-0000-000000000009', 'prod-0000-0000-0000-000000000008', 'uploads/products/default-cover-1.png', 0),
                                                                                ('img-0000-0000-0000-000000000010', 'prod-0000-0000-0000-000000000009', 'uploads/products/default-cover-2.png', 0),
                                                                                ('img-0000-0000-0000-000000000011', 'prod-0000-0000-0000-000000000010', 'uploads/products/default-cover-1.png', 0),
                                                                                ('img-0000-0000-0000-000000000012', 'prod-0000-0000-0000-000000000011', 'uploads/products/default-cover-2.png', 0),
                                                                                ('img-0000-0000-0000-000000000013', 'prod-0000-0000-0000-000000000012', 'uploads/products/default-cover-1.png', 0),
                                                                                ('img-0000-0000-0000-000000000014', 'prod-0000-0000-0000-000000000013', 'uploads/products/default-cover-2.png', 0),
                                                                                ('img-0000-0000-0000-000000000015', 'prod-0000-0000-0000-000000000014', 'uploads/products/default-cover-1.png', 0),
                                                                                ('img-0000-0000-0000-000000000016', 'prod-0000-0000-0000-000000000015', 'uploads/products/default-cover-2.png', 0);

-- =============================================================================
-- 14. Product versions
-- All versions reuse the same 2 placeholder schematic files stored under
-- webapp/uploads/schematics/. Download counts left at 0: the trigger
-- after_version_download_insert already recalculates product.total_downloads,
-- so it must not be set manually here.
-- =============================================================================
INSERT INTO product_version (version_id, product_id, changelog, created_at, download_count, file_path, minecraft_version, version) VALUES
                                                                                                                                       ('ver-0000-0000-0000-000000000001', 'prod-0000-0000-0000-000000000001', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-1.litematic', '1.20.4', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000002', 'prod-0000-0000-0000-000000000002', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-2.litematic', '1.20.4', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000003', 'prod-0000-0000-0000-000000000003', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-1.litematic', '1.20.1', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000004', 'prod-0000-0000-0000-000000000004', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-2.litematic', '1.19.4', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000005', 'prod-0000-0000-0000-000000000005', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-1.litematic', '1.20.4', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000006', 'prod-0000-0000-0000-000000000006', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-2.litematic', '1.20.4', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000007', 'prod-0000-0000-0000-000000000007', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-1.litematic', '1.20.1', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000008', 'prod-0000-0000-0000-000000000008', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-2.litematic', '1.20.4', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000009', 'prod-0000-0000-0000-000000000009', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-1.litematic', '1.19.4', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000010', 'prod-0000-0000-0000-000000000010', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-2.litematic', '1.20.4', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000011', 'prod-0000-0000-0000-000000000011', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-1.litematic', '1.20.4', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000012', 'prod-0000-0000-0000-000000000012', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-2.litematic', '1.20.1', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000013', 'prod-0000-0000-0000-000000000013', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-1.litematic', '1.18.2', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000014', 'prod-0000-0000-0000-000000000014', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-2.litematic', '1.20.4', '1.0'),
                                                                                                                                       ('ver-0000-0000-0000-000000000015', 'prod-0000-0000-0000-000000000015', 'Initial release.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-1.litematic', '1.20.4', '1.0'),
                                                                                                                                       -- one product with a second, updated version to exercise version history
                                                                                                                                       ('ver-0000-0000-0000-000000000016', 'prod-0000-0000-0000-000000000001', 'Fixed drawbridge redstone timing.', CURRENT_TIMESTAMP, 0, 'uploads/schematics/default-schematic-2.litematic', '1.20.4', '1.1');

-- =============================================================================
-- 15. Account_product: admin automatically owns every product it uploaded
-- =============================================================================
INSERT INTO account_product (account_id, product_id, unlocked_at)
SELECT 'acc-0000-0000-0000-000000000001', product_id, CURRENT_TIMESTAMP FROM product;

-- Customers also own a few products they "purchased" in past (PAID/SHIPPED) orders,
-- consistent with the orders inserted in section 18 below.
INSERT INTO account_product (account_id, product_id, unlocked_at) VALUES
                                                                      ('acc-0000-0000-0000-000000000002', 'prod-0000-0000-0000-000000000001', CURRENT_TIMESTAMP),
                                                                      ('acc-0000-0000-0000-000000000002', 'prod-0000-0000-0000-000000000004', CURRENT_TIMESTAMP),
                                                                      ('acc-0000-0000-0000-000000000003', 'prod-0000-0000-0000-000000000002', CURRENT_TIMESTAMP),
                                                                      ('acc-0000-0000-0000-000000000004', 'prod-0000-0000-0000-000000000008', CURRENT_TIMESTAMP),
                                                                      ('acc-0000-0000-0000-000000000005', 'prod-0000-0000-0000-000000000009', CURRENT_TIMESTAMP);

-- =============================================================================
-- 16. Favorites
-- =============================================================================
INSERT INTO favorite (account_id, product_id) VALUES
                                                  ('acc-0000-0000-0000-000000000002', 'prod-0000-0000-0000-000000000009'),
                                                  ('acc-0000-0000-0000-000000000002', 'prod-0000-0000-0000-000000000015'),
                                                  ('acc-0000-0000-0000-000000000003', 'prod-0000-0000-0000-000000000001'),
                                                  ('acc-0000-0000-0000-000000000004', 'prod-0000-0000-0000-000000000005'),
                                                  ('acc-0000-0000-0000-000000000006', 'prod-0000-0000-0000-000000000011');

-- =============================================================================
-- 17. Cart contents (transient, not yet purchased)
-- =============================================================================
INSERT INTO cart (account_id, product_id) VALUES
                                              ('acc-0000-0000-0000-000000000002', 'prod-0000-0000-0000-000000000010'),
                                              ('acc-0000-0000-0000-000000000003', 'prod-0000-0000-0000-000000000006'),
                                              ('acc-0000-0000-0000-000000000003', 'prod-0000-0000-0000-000000000012'),
                                              ('acc-0000-0000-0000-000000000006', 'prod-0000-0000-0000-000000000003');

-- =============================================================================
-- 18. Reviews
-- Ratings/review counts on `product` are recalculated automatically by
-- tg_review_insert, so total_reviews/average_rating above are correctly left at 0.
-- Only accounts that own the product review it, matching is_verified_purchase.
-- =============================================================================
INSERT INTO review (account_id, product_id, comment, created_at, is_verified_purchase, rating) VALUES
                                                                                                   ('acc-0000-0000-0000-000000000002', 'prod-0000-0000-0000-000000000001', 'Amazing castle, very detailed interior.', CURRENT_TIMESTAMP, TRUE, 5),
                                                                                                   ('acc-0000-0000-0000-000000000002', 'prod-0000-0000-0000-000000000004', 'Works perfectly, easy to install.',       CURRENT_TIMESTAMP, TRUE, 4),
                                                                                                   ('acc-0000-0000-0000-000000000003', 'prod-0000-0000-0000-000000000002', 'Great farm, could use better lighting.',  CURRENT_TIMESTAMP, TRUE, 4),
                                                                                                   ('acc-0000-0000-0000-000000000004', 'prod-0000-0000-0000-000000000008', 'Beautiful tower, worth the price.',       CURRENT_TIMESTAMP, TRUE, 5),
                                                                                                   ('acc-0000-0000-0000-000000000005', 'prod-0000-0000-0000-000000000009', 'Solid fortress, good for survival worlds.', CURRENT_TIMESTAMP, TRUE, 3);

-- =============================================================================
-- 19. Orders and order items
-- Mix of all order_status values. Tax/price/discount snapshot the product's
-- values at purchase time, as required by order_item structure.
-- =============================================================================

-- Order 1: PAID, single item (Steve bought the castle)
INSERT INTO order_table (order_id, account_id, address_id, currency_id, method_type, status, created_at, total_amount, transaction_id) VALUES
    ('ord-0000-0000-0000-000000000001', 'acc-0000-0000-0000-000000000002', 'addr-0000-0000-0000-000000000001', 'EUR', 1, 2, CURRENT_TIMESTAMP, 15.85, 'txn_test_0001');
INSERT INTO order_item (order_id, product_id, discount, price, tax) VALUES
    ('ord-0000-0000-0000-000000000001', 'prod-0000-0000-0000-000000000001', 0, 12.99, 2.86);

-- Order 2: SHIPPED, two items (Steve also bought the piston door)
INSERT INTO order_table (order_id, account_id, address_id, currency_id, method_type, status, created_at, total_amount, transaction_id) VALUES
    ('ord-0000-0000-0000-000000000002', 'acc-0000-0000-0000-000000000002', 'addr-0000-0000-0000-000000000001', 'EUR', 1, 3, CURRENT_TIMESTAMP, 4.27, 'txn_test_0002');
INSERT INTO order_item (order_id, product_id, discount, price, tax) VALUES
    ('ord-0000-0000-0000-000000000002', 'prod-0000-0000-0000-000000000004', 0, 3.50, 0.77);

-- Order 3: PENDING (Alex checking out, payment not yet confirmed)
INSERT INTO order_table (order_id, account_id, address_id, currency_id, method_type, status, created_at, total_amount, transaction_id) VALUES
    ('ord-0000-0000-0000-000000000003', 'acc-0000-0000-0000-000000000003', 'addr-0000-0000-0000-000000000002', 'GBP', 2, 1, CURRENT_TIMESTAMP, 6.50, 'txn_test_0003');
INSERT INTO order_item (order_id, product_id, discount, price, tax) VALUES
    ('ord-0000-0000-0000-000000000003', 'prod-0000-0000-0000-000000000002', 10.00, 6.50, 1.30);

-- Order 4: CANCELLED (Giulia cancelled before payment)
INSERT INTO order_table (order_id, account_id, address_id, currency_id, method_type, status, created_at, total_amount, transaction_id) VALUES
    ('ord-0000-0000-0000-000000000004', 'acc-0000-0000-0000-000000000004', 'addr-0000-0000-0000-000000000003', 'EUR', 1, 4, CURRENT_TIMESTAMP, 11.00, 'txn_test_0004');
INSERT INTO order_item (order_id, product_id, discount, price, tax) VALUES
    ('ord-0000-0000-0000-000000000004', 'prod-0000-0000-0000-000000000008', 0, 11.00, 2.42);

-- Order 5: PENDING_VERIFICATION (Hans, e.g. manual bank transfer awaiting confirmation)
INSERT INTO order_table (order_id, account_id, address_id, currency_id, method_type, status, created_at, total_amount, transaction_id) VALUES
    ('ord-0000-0000-0000-000000000005', 'acc-0000-0000-0000-000000000005', 'addr-0000-0000-0000-000000000004', 'EUR', 1, 5, CURRENT_TIMESTAMP, 14.99, 'txn_test_0005');
INSERT INTO order_item (order_id, product_id, discount, price, tax) VALUES
    ('ord-0000-0000-0000-000000000005', 'prod-0000-0000-0000-000000000009', 20.00, 14.99, 2.85);

-- Order 6: PAID, multi-item order (Marie buys two products at once)
INSERT INTO order_table (order_id, account_id, address_id, currency_id, method_type, status, created_at, total_amount, transaction_id) VALUES
    ('ord-0000-0000-0000-000000000006', 'acc-0000-0000-0000-000000000006', 'addr-0000-0000-0000-000000000005', 'EUR', 2, 2, CURRENT_TIMESTAMP, 15.24, 'txn_test_0006');
INSERT INTO order_item (order_id, product_id, discount, price, tax) VALUES
                                                                        ('ord-0000-0000-0000-000000000006', 'prod-0000-0000-0000-000000000011', 0, 6.75, 1.49),
                                                                        ('ord-0000-0000-0000-000000000006', 'prod-0000-0000-0000-000000000012', 0, 4.99, 1.10);