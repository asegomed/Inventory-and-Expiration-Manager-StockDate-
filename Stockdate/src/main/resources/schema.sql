-- StockDate database schema (SQLite). Executed at start-up; IF NOT EXISTS keeps existing data.
-- Dates are stored as ISO text (YYYY-MM-DD) so they sort correctly.

CREATE TABLE IF NOT EXISTS batch
(
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    product_name         TEXT    NOT NULL,
    category             TEXT    NOT NULL,
    quantity             INTEGER NOT NULL,
    storage_location     TEXT    NOT NULL,
    expiration_date      TEXT    NOT NULL,
    expiration_date_type TEXT    NOT NULL
);
