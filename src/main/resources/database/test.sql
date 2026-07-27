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

INSERT INTO country (country_id, country_name, is_active, tax) VALUES
                                                                   ('ITA', 'Italy',          TRUE, 22.00),
                                                                   ('FRA', 'France',         TRUE, 20.00),
                                                                   ('USA', 'United States',  TRUE, 7.50),
                                                                   ('JPN', 'Japan',          TRUE, 10.00),
                                                                   ('GBR', 'United Kingdom', TRUE, 20.00);

INSERT INTO currency (currency_id, currency_name, is_active, symbol) VALUES
                                                                         ('EUR', 'Euro',          TRUE, '€'),
                                                                         ('USD', 'US Dollar',     TRUE, '$'),
                                                                         ('GBP', 'British Pound', TRUE, '£'),
                                                                         ('JPY', 'Japanese Yen',  TRUE, '¥');

INSERT INTO language (language_id, language_name) VALUES
                                                      ('IT', 'Italian'),
                                                      ('EN', 'English');

INSERT INTO order_status (status_id, status_name) VALUES
                                                      (1, 'PENDING'),
                                                      (2, 'PAID'),
                                                      (3, 'SHIPPED'),
                                                      (4, 'CANCELLED'),
                                                      (5, 'PENDING_VERIFICATION');

INSERT INTO payment_method_type (type_id, is_active, type_name) VALUES
                                                                    (1, TRUE, 'Credit card/Debit card'),
                                                                    (2, TRUE, 'PayPal');

INSERT INTO account (account_id, username, email, country_id, currency_id, language_id, is_admin, password_hash) VALUES
                                                                                                                     ('acc-0000-0000-0000-000000000001', 'admin',           'admin@schemecraft.com',    'ITA', 'EUR', 'IT', TRUE,  '$2a$12$MB3FOZBoOCwE0hPT2Ek1AeRDnU/sn/uV0IPpm1cbhYzxJMrYyMJ3S'), -- Pass: Admin123
                                                                                                                     ('acc-0000-0000-0000-000000000002', 'builder_alex',    'alex.builder@gmail.com',   'USA', 'USD', 'EN', FALSE, '$2a$10$vU2xXH4mT2gK5Z1iF3iM7t5A2Y8w/3Y/1Y/7W/3Y/1Y/7W/3Y/1Y/'),
                                                                                                                     ('acc-0000-0000-0000-000000000003', 'redstone_guru',   'marco.redstone@libero.it', 'ITA', 'EUR', 'IT', FALSE, '$2a$10$vU2xXH4mT2gK5Z1iF3iM7t5A2Y8w/3Y/1Y/7W/3Y/1Y/7W/3Y/1Y/'),
                                                                                                                     ('acc-0000-0000-0000-000000000004', 'pixel_master',    'jean.dupont@orange.fr',    'FRA', 'EUR', 'EN', FALSE, '$2a$10$vU2xXH4mT2gK5Z1iF3iM7t5A2Y8w/3Y/1Y/7W/3Y/1Y/7W/3Y/1Y/'),
                                                                                                                     ('acc-0000-0000-0000-000000000005', 'crafter_kenji',   'kenji.sato@yahoo.jp',      'JPN', 'JPY', 'EN', FALSE, '$2a$10$vU2xXH4mT2gK5Z1iF3iM7t5A2Y8w/3Y/1Y/7W/3Y/1Y/7W/3Y/1Y/');

INSERT INTO address (address_id, account_id, country_id, city, flag_default, is_active, postal_code, state_province, street_address) VALUES
                                                                                                                                         ('addr-001', 'acc-0000-0000-0000-000000000001', 'ITA', 'Rome',        TRUE, TRUE, '00100',    'RM',                   'Via Giovanni 10'),
                                                                                                                                         ('addr-002', 'acc-0000-0000-0000-000000000001', 'ITA', 'Milan',       NULL, TRUE, '20100',    'MI',                   'Via Vittorio Emanuele 45'),
                                                                                                                                         ('addr-003', 'acc-0000-0000-0000-000000000002', 'USA', 'New York',    TRUE, TRUE, '10001',    'NY',                   '5th Avenue 123'),
                                                                                                                                         ('addr-004', 'acc-0000-0000-0000-000000000002', 'USA', 'Los Angeles', NULL, TRUE, '90001',    'CA',                   'Sunset Boulevard 742'),
                                                                                                                                         ('addr-005', 'acc-0000-0000-0000-000000000003', 'ITA', 'Florence',    TRUE, TRUE, '50100',    'FI',                   'Piazza Signora 5'),
                                                                                                                                         ('addr-006', 'acc-0000-0000-0000-000000000003', 'ITA', 'Bologna',     NULL, TRUE, '40100',    'BO',                   'Via Giuseppe 12'),
                                                                                                                                         ('addr-007', 'acc-0000-0000-0000-000000000004', 'FRA', 'Paris',       TRUE, TRUE, '75001',    'Île-de-France',        'Rue de Rivoli 88'),
                                                                                                                                         ('addr-008', 'acc-0000-0000-0000-000000000004', 'FRA', 'Lyon',        NULL, TRUE, '69001',    'Auvergne-Rhône-Alpes', 'Place 3'),
                                                                                                                                         ('addr-009', 'acc-0000-0000-0000-000000000005', 'JPN', 'Tokyo',       TRUE, TRUE, '100-0001', 'Tokyo',                'Chiyoda-ku 1-1'),
                                                                                                                                         ('addr-100', 'acc-0000-0000-0000-000000000005', 'JPN', 'Osaka',       NULL, TRUE, '530-0001', 'Osaka',                'Um 2-4-9');

INSERT INTO payment_method (payment_method_id, account_id, method_type, card_brand, card_expiration, card_last_four, flag_default, payment_email, payment_token) VALUES
                                                                                                                                                                     ('pm-001', 'acc-0000-0000-0000-000000000001', 1, 'Visa',       '12/28', '4242', TRUE, NULL,                       'tok_visa_admin_01'),
                                                                                                                                                                     ('pm-002', 'acc-0000-0000-0000-000000000001', 2, NULL,         NULL,    NULL,   NULL, 'admin@schemecraft.com',    'tok_pp_admin_02'),
                                                                                                                                                                     ('pm-003', 'acc-0000-0000-0000-000000000002', 1, 'Mastercard', '08/27', '8888', TRUE, NULL,                       'tok_mc_alex_01'),
                                                                                                                                                                     ('pm-004', 'acc-0000-0000-0000-000000000002', 2, NULL,         NULL,    NULL,   NULL, 'alex.builder@gmail.com',   'tok_pp_alex_02'),
                                                                                                                                                                     ('pm-005', 'acc-0000-0000-0000-000000000003', 1, 'Visa',       '05/26', '1111', TRUE, NULL,                       'tok_visa_marco_01'),
                                                                                                                                                                     ('pm-006', 'acc-0000-0000-0000-000000000003', 2, NULL,         NULL,    NULL,   NULL, 'marco.redstone@libero.it', 'tok_pp_marco_02'),
                                                                                                                                                                     ('pm-007', 'acc-0000-0000-0000-000000000004', 1, 'Amex',       '10/29', '3005', TRUE, NULL,                       'tok_amex_jean_01'),
                                                                                                                                                                     ('pm-008', 'acc-0000-0000-0000-000000000004', 2, NULL,         NULL,    NULL,   NULL, 'jean.dupont@orange.fr',    'tok_pp_jean_02'),
                                                                                                                                                                     ('pm-009', 'acc-0000-0000-0000-000000000005', 1, 'Visa',       '01/30', '9999', TRUE, NULL,                       'tok_visa_kenji_01'),
                                                                                                                                                                     ('pm-010', 'acc-0000-0000-0000-000000000005', 2, NULL,         NULL,    NULL,   NULL, 'kenji.sato@yahoo.jp',      'tok_pp_kenji_02');

INSERT INTO product (product_id, account_id, currency_id, product_name, description, price, discount, stock_quantity) VALUES
                                                                                                                          ('prod-001', 'acc-0000-0000-0000-000000000001', 'EUR', 'Medieval Castle',                 'A majestic medieval castle complete with finished interiors, watchtowers, and a moat.',              15.00,   10.00, 3),
                                                                                                                          ('prod-002', 'acc-0000-0000-0000-000000000001', 'USD', 'Cyberpunk Spawn Hub',             'A massive Cyberpunk-style multiplayer server hub with neon lights and holographic details.',         25.00,   0.00,  NULL),
                                                                                                                          ('prod-003', 'acc-0000-0000-0000-000000000001', 'EUR', 'Iron Farm v4',                    'Compact, ultra-high efficiency design for automated iron ingot production.',                         5.00,    0.00,  NULL),
                                                                                                                          ('prod-004', 'acc-0000-0000-0000-000000000001', 'GBP', 'Renaissance Cathedral',           'Detailed replica of a Renaissance cathedral featuring rose windows and artistic stained glass.',     18.50,   15.00, NULL),
                                                                                                                          ('prod-005', 'acc-0000-0000-0000-000000000001', 'JPY', 'Traditional Japanese Village',    'Set of traditional houses, pagodas, and Zen gardens perfect for survival or RPG worlds.',            1200.00, 0.00,  NULL),
                                                                                                                          ('prod-006', 'acc-0000-0000-0000-000000000001', 'EUR', 'Redstone Supercomputer',          'Fully functional 8-bit calculator built entirely out of Redstone with a display.',                   12.00,   20.00, NULL),
                                                                                                                          ('prod-007', 'acc-0000-0000-0000-000000000001', 'USD', '18th-Century Pirate Ship',        'Buoyant and highly detailed, complete with cannons, cargo hold, and captain\'s quarters.',           8.50,    0.00,  40),
                                                                                                                          ('prod-008', 'acc-0000-0000-0000-000000000001', 'EUR', 'Fantasy Floating Bases',          'Magical flying island featuring air waterfalls and elven architecture.',                             10.00,   5.00,  NULL),
                                                                                                                          ('prod-009', 'acc-0000-0000-0000-000000000001', 'EUR', 'Integrated Gold and XP Farm',     'Nether farm featuring automatic sorting for nuggets, ingots, and broken swords.',                    7.50,    0.00,  NULL),
                                                                                                                          ('prod-010', 'acc-0000-0000-0000-000000000001', 'USD', 'Modern Glass & Steel Skyscraper', 'Multi-level modern building with furnished interiors, a water elevator, and a city-view penthouse.', 14.00,   0.00,  25),
                                                                                                                          ('prod-011', 'acc-0000-0000-0000-000000000001', 'GBP', 'Gladiator PvP Arena',             'Massive circular arena for PvP combat, equipped with remotely triggered traps.',                     9.00,    10.00, NULL),
                                                                                                                          ('prod-012', 'acc-0000-0000-0000-000000000001', 'EUR', 'Forest Elven Fort',               'Structure seamlessly integrated among giant trees with suspension bridges.',                         11.50,   0.00,  NULL),
                                                                                                                          ('prod-013', 'acc-0000-0000-0000-000000000001', 'JPY', 'Orbital Space Station',           'Gigantic space base with living modules, hydroponic greenhouses, and a shuttle hangar.',             2000.00, 25.00, 10),
                                                                                                                          ('prod-014', 'acc-0000-0000-0000-000000000001', 'EUR', 'Redstone Logic Gates Portfolio',  'Collection of 20 compact logic circuits ready to copy into your own worlds.',                        4.00,    0.00,  NULL),
                                                                                                                          ('prod-015', 'acc-0000-0000-0000-000000000001', 'USD', 'Underwater Temple of Atlantis',   'Submarine base protected by a glass dome with an automatic drainage system.',                        16.00,   0.00,  0); -- Out of stock

INSERT INTO product_version (version_id, product_id, version, minecraft_version, file_path, changelog, download_count) VALUES
                                                                                                                           ('ver-001', 'prod-001', '1.0.0', '1.19.4', '', 'Initial castle release',                            10),
                                                                                                                           ('ver-002', 'prod-001', '1.1.0', '1.20.1', '', 'Added interior decorations and automatic lighting', 25),
                                                                                                                           ('ver-003', 'prod-002', '1.0.0', '1.20.4', '', 'Cyberpunk hub release',                             45),
                                                                                                                           ('ver-004', 'prod-003', '1.0.0', '1.18.2', '', 'Base iron farm design',                             12),
                                                                                                                           ('ver-005', 'prod-003', '2.0.0', '1.20.1', '', 'Villager mechanics update',                         30),
                                                                                                                           ('ver-006', 'prod-004', '1.0.0', '1.20.1', '', 'Initial cathedral version',                         8),
                                                                                                                           ('ver-007', 'prod-005', '1.0.0', '1.20.4', '', 'Complete pagodas and houses set',                   15),
                                                                                                                           ('ver-008', 'prod-006', '1.0.0', '1.19.4', '', 'Logic calculator v1',                               5),
                                                                                                                           ('ver-009', 'prod-006', '1.1.0', '1.20.1', '', 'Redstone tick/delay optimization',                  12),
                                                                                                                           ('ver-010', 'prod-006', '1.2.0', '1.20.4', '', 'Support for new display features',                  18),
                                                                                                                           ('ver-011', 'prod-007', '1.0.0', '1.20.1', '', 'Floating ship release',                             22),
                                                                                                                           ('ver-012', 'prod-008', '1.0.0', '1.19.2', '', 'Floating islands v1',                               7),
                                                                                                                           ('ver-013', 'prod-009', '1.0.0', '1.20.4', '', 'High-speed gold farm',                              50),
                                                                                                                           ('ver-014', 'prod-010', '1.0.0', '1.20.1', '', 'Furnished skyscraper',                              14),
                                                                                                                           ('ver-015', 'prod-011', '1.0.0', '1.20.4', '', 'PvP arena with traps',                              9),
                                                                                                                           ('ver-016', 'prod-012', '1.0.0', '1.20.1', '', 'Elven fort for giant forests',                      11),
                                                                                                                           ('ver-017', 'prod-013', '1.0.0', '1.20.4', '', 'Modular space station',                             3),
                                                                                                                           ('ver-018', 'prod-014', '1.0.0', '1.19.4', '', 'Logic gates compendium',                            40),
                                                                                                                           ('ver-019', 'prod-015', '1.0.0', '1.20.4', '', 'Ready-to-use underwater dome',                      19);

INSERT INTO category (category_id, category_name, parent_category_id, description) VALUES
                                                                                       ('cat-001', 'Historical Architecture', NULL,      'Buildings and monuments inspired by real historical eras.'),
                                                                                       ('sub-001', 'Castles and Fortresses',  'cat-001', 'Castles, bastions, and military fortifications.'),
                                                                                       ('sub-002', 'Religious Buildings',     'cat-001', 'Cathedrals, churches, and ancient temples.'),
                                                                                       ('cat-002', 'Redstone and Automation', NULL,      'Advanced circuitry, farms, and smart mechanisms.'),
                                                                                       ('sub-003', 'Automatic Farms',         'cat-002', 'Automated farming and resource harvesting systems.'),
                                                                                       ('sub-004', 'Redstone Computing',      'cat-002', 'Calculators, logic gates, and displays.'),
                                                                                       ('cat-003', 'Futuristic and Sci-Fi',   NULL,      'Worlds and structures set in the future or outer space.'),
                                                                                       ('sub-005', 'Cyberpunk',               'cat-003', 'Neon cities and dystopian hubs.'),
                                                                                       ('sub-006', 'Space',                   'cat-003', 'Space stations and planetary bases.'),
                                                                                       ('cat-004', 'Fantasy and Magic',       NULL,      'Fantasy worlds, elven villages, and floating islands.'),
                                                                                       ('sub-007', 'Flying Islands',          'cat-004', 'Structures floating in the void or sky.'),
                                                                                       ('cat-005', 'Modern Structures',       NULL,      'Skyscrapers, contemporary cities, and current infrastructure.'),
                                                                                       ('sub-008', 'Skyscrapers',             'cat-005', 'Modern buildings with high vertical development.');

INSERT INTO product_category (product_id, category_id) VALUES
                                                           ('prod-001', 'cat-001'),
                                                           ('prod-001', 'sub-001'),
                                                           ('prod-002', 'cat-003'),
                                                           ('prod-002', 'sub-005'),
                                                           ('prod-003', 'cat-002'),
                                                           ('prod-003', 'sub-003'),
                                                           ('prod-004', 'cat-001'),
                                                           ('prod-004', 'sub-002'),
                                                           ('prod-005', 'cat-001'),
                                                           ('prod-006', 'cat-002'),
                                                           ('prod-006', 'sub-004'),
                                                           ('prod-007', 'cat-001'),
                                                           ('prod-008', 'cat-004'),
                                                           ('prod-008', 'sub-007'),
                                                           ('prod-009', 'cat-002'),
                                                           ('prod-009', 'sub-003'),
                                                           ('prod-010', 'cat-005'),
                                                           ('prod-010', 'sub-008'),
                                                           ('prod-011', 'cat-001'),
                                                           ('prod-012', 'cat-004'),
                                                           ('prod-013', 'cat-003'),
                                                           ('prod-013', 'sub-006'),
                                                           ('prod-014', 'cat-002'),
                                                           ('prod-014', 'sub-004'),
                                                           ('prod-015', 'cat-004');

INSERT INTO account_product (account_id, product_id) VALUES
                                                         ('acc-0000-0000-0000-000000000001', 'prod-001'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-002'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-003'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-004'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-005'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-006'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-007'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-008'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-009'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-010'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-011'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-012'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-013'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-014'),
                                                         ('acc-0000-0000-0000-000000000001', 'prod-015'),
                                                         ('acc-0000-0000-0000-000000000002', 'prod-001'),
                                                         ('acc-0000-0000-0000-000000000002', 'prod-002'),
                                                         ('acc-0000-0000-0000-000000000002', 'prod-010'),
                                                         ('acc-0000-0000-0000-000000000003', 'prod-003'),
                                                         ('acc-0000-0000-0000-000000000003', 'prod-006'),
                                                         ('acc-0000-0000-0000-000000000003', 'prod-009'),
                                                         ('acc-0000-0000-0000-000000000003', 'prod-014'),
                                                         ('acc-0000-0000-0000-000000000004', 'prod-004'),
                                                         ('acc-0000-0000-0000-000000000004', 'prod-007'),
                                                         ('acc-0000-0000-0000-000000000005', 'prod-005'),
                                                         ('acc-0000-0000-0000-000000000005', 'prod-011'),
                                                         ('acc-0000-0000-0000-000000000005', 'prod-013');

INSERT INTO favorite (account_id, product_id) VALUES
                                                  ('acc-0000-0000-0000-000000000001', 'prod-002'),
                                                  ('acc-0000-0000-0000-000000000002', 'prod-004'),
                                                  ('acc-0000-0000-0000-000000000002', 'prod-007'),
                                                  ('acc-0000-0000-0000-000000000003', 'prod-002'),
                                                  ('acc-0000-0000-0000-000000000003', 'prod-010'),
                                                  ('acc-0000-0000-0000-000000000003', 'prod-013'),
                                                  ('acc-0000-0000-0000-000000000004', 'prod-001'),
                                                  ('acc-0000-0000-0000-000000000005', 'prod-008'),
                                                  ('acc-0000-0000-0000-000000000005', 'prod-012');

INSERT INTO cart (account_id, product_id) VALUES
                                              ('acc-0000-0000-0000-000000000002', 'prod-004'),
                                              ('acc-0000-0000-0000-000000000002', 'prod-005'),
                                              ('acc-0000-0000-0000-000000000002', 'prod-006'),
                                              ('acc-0000-0000-0000-000000000003', 'prod-001'),
                                              ('acc-0000-0000-0000-000000000003', 'prod-002'),
                                              ('acc-0000-0000-0000-000000000003', 'prod-005'),
                                              ('acc-0000-0000-0000-000000000003', 'prod-010'),
                                              ('acc-0000-0000-0000-000000000004', 'prod-001'),
                                              ('acc-0000-0000-0000-000000000004', 'prod-002'),
                                              ('acc-0000-0000-0000-000000000005', 'prod-001'),
                                              ('acc-0000-0000-0000-000000000005', 'prod-002'),
                                              ('acc-0000-0000-0000-000000000005', 'prod-003');


INSERT INTO review (account_id, product_id, rating, comment, is_verified_purchase) VALUES
                                                                                       ('acc-0000-0000-0000-000000000002', 'prod-001', 5, 'Amazing castle! Incredible attention to detail.',                  TRUE),
                                                                                       ('acc-0000-0000-0000-000000000002', 'prod-002', 4, 'Great futuristic design, perfect for my server spawn.',            TRUE),
                                                                                       ('acc-0000-0000-0000-000000000003', 'prod-003', 5, 'Fantastic farm, produces iron at an impressive rate.',             TRUE),
                                                                                       ('acc-0000-0000-0000-000000000003', 'prod-006', 5, 'Super clean redstone circuits and very easy to understand!',       TRUE),
                                                                                       ('acc-0000-0000-0000-000000000004', 'prod-004', 4, 'Stunning cathedral, looks massive in my survival world.',          TRUE),
                                                                                       ('acc-0000-0000-0000-000000000005', 'prod-005', 5, 'Fantastic architectural details, exactly what I was looking for.', TRUE);

INSERT INTO order_table (order_id, account_id, address_id, currency_id, method_type, status, total_amount, transaction_id) VALUES
                                                                                                                 ('ord-001', 'acc-0000-0000-0000-000000000002', 'addr-003', 'USD', 1, 2, 43.38, 'TXN-USA-2026-001');

INSERT INTO order_item (order_id, product_id, price, discount, tax) VALUES
                                                                        ('ord-001', 'prod-001', 15.00, 10.00, 3.30),
                                                                        ('ord-001', 'prod-002', 25.00, 0.00, 1.88),
                                                                        ('ord-001', 'prod-010', 14.00, 0.00, 1.05);

INSERT INTO order_table (order_id, account_id, address_id, currency_id, method_type, status, total_amount, transaction_id) VALUES
                                                                                                                               ('ord-002', 'acc-0000-0000-0000-000000000003', 'addr-005', 'EUR', 2, 2, 28.50, 'TXN-ITA-2026-002');

INSERT INTO order_item (order_id, product_id, price, discount, tax) VALUES
                                                                        ('ord-002', 'prod-003', 5.00, 0.00, 1.10),
                                                                        ('ord-002', 'prod-006', 12.00, 20.00, 2.11),
                                                                        ('ord-002', 'prod-009', 7.50, 0.00, 1.65),
                                                                        ('ord-002', 'prod-014', 4.00, 0.00, 0.88);

INSERT INTO order_table (order_id, account_id, address_id, currency_id, method_type, status, total_amount, transaction_id) VALUES
                                                                                                                               ('ord-003', 'acc-0000-0000-0000-000000000004', 'addr-007', 'EUR', 1, 2, 24.22, 'TXN-FRA-2026-003');

INSERT INTO order_item (order_id, product_id, price, discount, tax) VALUES
                                                                        ('ord-003', 'prod-004', 18.50, 15.00, 3.15),
                                                                        ('ord-003', 'prod-007', 8.50, 0.00, 1.70);

INSERT INTO order_table (order_id, account_id, address_id, currency_id, method_type, status, total_amount, transaction_id) VALUES
                                                                                                                               ('ord-004', 'acc-0000-0000-0000-000000000005', 'addr-009', 'JPY', 1, 2, 3500.00, 'TXN-JPN-2026-004');

INSERT INTO order_item (order_id, product_id, price, discount, tax) VALUES
                                                                        ('ord-004', 'prod-005', 1200.00, 0.00, 120.00),
                                                                        ('ord-004', 'prod-011', 9.00, 10.00, 0.81),
                                                                        ('ord-004', 'prod-013', 2000.00, 25.00, 150.00);
