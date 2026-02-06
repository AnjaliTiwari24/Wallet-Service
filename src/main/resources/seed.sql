-- Insert Assets
INSERT INTO assets (code, name, description, type, active, created_at, updated_at)
VALUES
    ('GOLD_COINS', 'Gold Coins', 'Premium in-game currency', 'GOLD_COINS', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('LOYALTY_POINTS', 'Loyalty Points', 'User loyalty rewards', 'LOYALTY_POINTS', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CREDIT_TOKENS', 'Credit Tokens', 'Service credit tokens', 'CREDIT_TOKENS', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- System Wallets (Treasury and Bonus Pool)
INSERT INTO wallets (user_id, asset_id, balance, system_wallet_id, is_system_wallet, created_at, updated_at, version)
SELECT NULL, a.id, 1000000.00, 'TREASURY', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM assets a WHERE a.code = 'GOLD_COINS';

INSERT INTO wallets (user_id, asset_id, balance, system_wallet_id, is_system_wallet, created_at, updated_at, version)
SELECT NULL, a.id, 500000.00, 'BONUS_POOL', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM assets a WHERE a.code = 'GOLD_COINS';

INSERT INTO wallets (user_id, asset_id, balance, system_wallet_id, is_system_wallet, created_at, updated_at, version)
SELECT NULL, a.id, 100000.00, 'TREASURY', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM assets a WHERE a.code = 'LOYALTY_POINTS';

INSERT INTO wallets (user_id, asset_id, balance, system_wallet_id, is_system_wallet, created_at, updated_at, version)
SELECT NULL, a.id, 50000.00, 'BONUS_POOL', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM assets a WHERE a.code = 'LOYALTY_POINTS';

-- Initialize wallets for existing users (john.doe@example.com and any other users)
INSERT INTO wallets (user_id, asset_id, balance, is_system_wallet, created_at, updated_at, version)
SELECT u.id, a.id, 500.00, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM users u, assets a
WHERE u.email = 'john.doe@example.com' AND a.code = 'GOLD_COINS'
ON CONFLICT DO NOTHING;

INSERT INTO wallets (user_id, asset_id, balance, is_system_wallet, created_at, updated_at, version)
SELECT u.id, a.id, 1000.00, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM users u, assets a
WHERE u.email = 'john.doe@example.com' AND a.code = 'LOYALTY_POINTS'
ON CONFLICT DO NOTHING;

INSERT INTO wallets (user_id, asset_id, balance, is_system_wallet, created_at, updated_at, version)
SELECT u.id, a.id, 100.00, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM users u, assets a
WHERE u.email = 'john.doe@example.com' AND a.code = 'CREDIT_TOKENS'
ON CONFLICT DO NOTHING;
