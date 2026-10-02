-- Runs ONCE, only when the mysql-data volume is empty (first start).
-- Pattern: one database + one least-privilege user per service (database-per-service).
-- DEV ONLY passwords. In production, passwords come from a secret manager / env vars.
-- Adding a database later: run these statements manually, or `docker compose down -v`
-- to recreate the volume (this wipes all local data).

CREATE DATABASE IF NOT EXISTS auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS seller_verification_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS catalog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS inventory_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS negotiation_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS membership_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS rental_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS payment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS delivery_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS review_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS notification_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'auth_svc'@'%' IDENTIFIED BY 'auth_dev_pw';
CREATE USER IF NOT EXISTS 'seller_verification_svc'@'%' IDENTIFIED BY 'seller_verification_dev_pw';
CREATE USER IF NOT EXISTS 'catalog_svc'@'%' IDENTIFIED BY 'catalog_dev_pw';
CREATE USER IF NOT EXISTS 'inventory_svc'@'%' IDENTIFIED BY 'inventory_dev_pw';
CREATE USER IF NOT EXISTS 'negotiation_svc'@'%' IDENTIFIED BY 'negotiation_dev_pw';
CREATE USER IF NOT EXISTS 'order_svc'@'%' IDENTIFIED BY 'order_dev_pw';
CREATE USER IF NOT EXISTS 'membership_svc'@'%' IDENTIFIED BY 'membership_dev_pw';
CREATE USER IF NOT EXISTS 'rental_svc'@'%' IDENTIFIED BY 'rental_dev_pw';
CREATE USER IF NOT EXISTS 'payment_svc'@'%' IDENTIFIED BY 'payment_dev_pw';
CREATE USER IF NOT EXISTS 'delivery_svc'@'%' IDENTIFIED BY 'delivery_dev_pw';
CREATE USER IF NOT EXISTS 'review_svc'@'%' IDENTIFIED BY 'review_dev_pw';
CREATE USER IF NOT EXISTS 'notification_svc'@'%' IDENTIFIED BY 'notification_dev_pw';

GRANT ALL PRIVILEGES ON auth_db.* TO 'auth_svc'@'%';
GRANT ALL PRIVILEGES ON seller_verification_db.* TO 'seller_verification_svc'@'%';
GRANT ALL PRIVILEGES ON catalog_db.* TO 'catalog_svc'@'%';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'inventory_svc'@'%';
GRANT ALL PRIVILEGES ON negotiation_db.* TO 'negotiation_svc'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'order_svc'@'%';
GRANT ALL PRIVILEGES ON membership_db.* TO 'membership_svc'@'%';
GRANT ALL PRIVILEGES ON rental_db.* TO 'rental_svc'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'payment_svc'@'%';
GRANT ALL PRIVILEGES ON delivery_db.* TO 'delivery_svc'@'%';
GRANT ALL PRIVILEGES ON review_db.* TO 'review_svc'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'notification_svc'@'%';
FLUSH PRIVILEGES;
