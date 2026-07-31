CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    username VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE currencies (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    symbol VARCHAR(10) NOT NULL
);

CREATE TABLE portfolios (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE portfolio_assets (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    ticker VARCHAR(10) NOT NULL,
    asset_type VARCHAR(50) NOT NULL,
    currency_id BIGINT NOT NULL REFERENCES currencies(id)
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL REFERENCES portfolio_assets(id) ON DELETE CASCADE,
    type VARCHAR(10) NOT NULL,
    transaction_datetime TIMESTAMPTZ NOT NULL,
    price DECIMAL(18,2) NOT NULL CHECK (price > 0),
    quantity DECIMAL(18,10) NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ DEFAULT now()
);

INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

INSERT INTO currencies (code, name, symbol) VALUES
    ('RUB', 'Russian Ruble', '₽'),
    ('USD', 'US Dollar', '$'),
    ('EUR', 'Euro', '€'),
    ('GBP', 'British Pound', '£'),
    ('BTC', 'Bitcoin', '₿'),
    ('ETH', 'Ethereum', 'Ξ'),
    ('USDT', 'Tether', '₮');