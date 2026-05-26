-- ============================================================
-- Distributed Order Management System — Database Initialization
-- PostgreSQL 16+
-- ============================================================

-- -----------------------------------------------
-- Product Service Tables
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    price           DECIMAL(12, 2) NOT NULL CHECK (price > 0),
    category        VARCHAR(100) NOT NULL,
    image_url       VARCHAR(500),
    active          BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_active ON products(active);

-- -----------------------------------------------
-- Cart Service Tables
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS carts (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_carts_user_id ON carts(user_id);

CREATE TABLE IF NOT EXISTS cart_items (
    id              BIGSERIAL PRIMARY KEY,
    cart_id         BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id      BIGINT NOT NULL,
    product_name    VARCHAR(255) NOT NULL,
    price           DECIMAL(12, 2) NOT NULL,
    quantity        INT NOT NULL CHECK (quantity > 0),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);

-- -----------------------------------------------
-- Order Service Tables
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    total_amount        DECIMAL(12, 2) NOT NULL,
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    order_type          VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    shipping_address    VARCHAR(500) NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE TABLE IF NOT EXISTS order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id      BIGINT NOT NULL,
    product_name    VARCHAR(255) NOT NULL,
    price           DECIMAL(12, 2) NOT NULL,
    quantity        INT NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- -----------------------------------------------
-- Inventory Service Tables
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS inventory (
    id                  BIGSERIAL PRIMARY KEY,
    product_id          BIGINT NOT NULL UNIQUE,
    available_quantity  INT NOT NULL DEFAULT 0 CHECK (available_quantity >= 0),
    reserved_quantity   INT NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_inventory_product_id ON inventory(product_id);

CREATE TABLE IF NOT EXISTS stock_reservations (
    id              BIGSERIAL PRIMARY KEY,
    inventory_id    BIGINT NOT NULL REFERENCES inventory(id),
    order_id        BIGINT,
    quantity        INT NOT NULL CHECK (quantity > 0),
    status          VARCHAR(50) NOT NULL DEFAULT 'RESERVED',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP
);

-- -----------------------------------------------
-- Seed Data: 20 Products
-- -----------------------------------------------
INSERT INTO products (name, description, price, category, image_url) VALUES
('MacBook Pro 16"',           'Apple M3 Pro chip, 18GB RAM, 512GB SSD',           2499.99, 'ELECTRONICS',  'https://placehold.co/400x400?text=MacBook+Pro'),
('iPhone 15 Pro Max',         '256GB, Natural Titanium, A17 Pro chip',            1199.99, 'ELECTRONICS',  'https://placehold.co/400x400?text=iPhone+15'),
('Sony WH-1000XM5',           'Wireless noise-cancelling headphones',             349.99,  'ELECTRONICS',  'https://placehold.co/400x400?text=Sony+XM5'),
('Samsung 65" OLED TV',       '4K Smart TV with Tizen OS',                        1799.99, 'ELECTRONICS',  'https://placehold.co/400x400?text=Samsung+TV'),
('iPad Air M2',               '11-inch, 128GB, Wi-Fi, Space Gray',               599.99,  'ELECTRONICS',  'https://placehold.co/400x400?text=iPad+Air'),
('Nike Air Max 270',          'Men''s running shoes, black/white',                 159.99,  'CLOTHING',     'https://placehold.co/400x400?text=Nike+Air+Max'),
('Levi''s 501 Original Jeans', 'Classic straight fit, medium wash',                79.99,   'CLOTHING',     'https://placehold.co/400x400?text=Levis+501'),
('North Face Puffer Jacket',  'Water-resistant, 700 fill down',                   299.99,  'CLOTHING',     'https://placehold.co/400x400?text=North+Face'),
('Ray-Ban Aviator Sunglasses','Classic gold frame, green lens',                   163.00,  'ACCESSORIES',  'https://placehold.co/400x400?text=Ray-Ban'),
('Apple Watch Series 9',      'GPS, 45mm, Midnight Aluminum Case',               429.00,  'ACCESSORIES',  'https://placehold.co/400x400?text=Apple+Watch'),
('Dyson V15 Detect',          'Cordless vacuum with laser dust detection',        749.99,  'HOME',         'https://placehold.co/400x400?text=Dyson+V15'),
('Instant Pot Duo 7-in-1',    '6 Quart pressure cooker',                         89.99,   'HOME',         'https://placehold.co/400x400?text=Instant+Pot'),
('KitchenAid Stand Mixer',    'Artisan Series, 5-Quart, Empire Red',             379.99,  'HOME',         'https://placehold.co/400x400?text=KitchenAid'),
('Herman Miller Aeron Chair', 'Size B, fully loaded, graphite',                   1395.00, 'FURNITURE',    'https://placehold.co/400x400?text=Aeron+Chair'),
('IKEA KALLAX Shelf Unit',    '4x4, white, modular storage',                      199.99,  'FURNITURE',    'https://placehold.co/400x400?text=IKEA+KALLAX'),
('The Great Gatsby',          'F. Scott Fitzgerald, paperback edition',            12.99,   'BOOKS',        'https://placehold.co/400x400?text=Great+Gatsby'),
('Clean Code',                'Robert C. Martin, software craftsmanship',          39.99,   'BOOKS',        'https://placehold.co/400x400?text=Clean+Code'),
('Designing Data-Intensive Applications', 'Martin Kleppmann, O''Reilly',          49.99,   'BOOKS',        'https://placehold.co/400x400?text=DDIA'),
('PlayStation 5 Console',     'Digital Edition, 1TB SSD',                          449.99,  'ELECTRONICS',  'https://placehold.co/400x400?text=PS5'),
('Nintendo Switch OLED',      '7-inch OLED screen, white Joy-Con',               349.99,  'ELECTRONICS',  'https://placehold.co/400x400?text=Switch+OLED');

-- -----------------------------------------------
-- Seed Data: Inventory for all 20 products
-- -----------------------------------------------
INSERT INTO inventory (product_id, available_quantity, reserved_quantity, version) VALUES
(1,  50, 0, 0),  (2,  120, 0, 0), (3,  200, 0, 0), (4,  30, 0, 0),
(5,  80, 0, 0),  (6,  300, 0, 0), (7,  250, 0, 0), (8,  100, 0, 0),
(9,  150, 0, 0), (10, 90, 0, 0),  (11, 60, 0, 0),  (12, 400, 0, 0),
(13, 45, 0, 0),  (14, 25, 0, 0),  (15, 180, 0, 0), (16, 500, 0, 0),
(17, 350, 0, 0), (18, 200, 0, 0), (19, 75, 0, 0),  (20, 110, 0, 0);
