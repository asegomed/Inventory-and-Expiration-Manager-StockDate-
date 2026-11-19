.bail on
PRAGMA foreign_keys = ON;
BEGIN;

ALTER TABLE batch RENAME TO batch_old;

.read src/main/resources/schema.sql

INSERT INTO category (name)
SELECT DISTINCT category
FROM batch_old;

INSERT INTO batch (
    id, product_name, category_id, quantity,
    storage_location, expiration_date, expiration_date_type
)
SELECT b.id, b.product_name, c.id, b.quantity,
       b.storage_location, b.expiration_date, b.expiration_date_type
FROM batch_old b
         JOIN category c ON c.name = b.category;

DROP TABLE batch_old;

COMMIT;