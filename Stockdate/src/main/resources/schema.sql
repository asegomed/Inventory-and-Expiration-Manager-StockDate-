-- StockDate database schema (SQLite). Executed at start-up; IF NOT EXISTS keeps existing data.
-- Dates are stored as ISO text (YYYY-MM-DD) so they sort correctly.

CREATE TABLE IF NOT EXISTS category (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
     id INTEGER PRIMARY KEY AUTOINCREMENT,
     username TEXT NOT NULL UNIQUE,
     password_hash TEXT NOT NULL,
     role TEXT NOT NULL CHECK (role IN ('ADMIN', 'VOLUNTEER'))
);

CREATE TABLE IF NOT EXISTS batch (
     id INTEGER PRIMARY KEY AUTOINCREMENT,
     product_name TEXT NOT NULL,
     category_id INTEGER NOT NULL REFERENCES category(id),
     quantity INTEGER NOT NULL,
     storage_location TEXT NOT NULL,
     expiration_date TEXT NOT NULL,
     expiration_date_type TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS stock_movement (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      batch_id INTEGER NOT NULL REFERENCES batch(id),
      user_id INTEGER NOT NULL REFERENCES users(id),
      type TEXT NOT NULL CHECK (type IN ('DISPATCH', 'DISCARD')),
      quantity INTEGER NOT NULL CHECK (quantity > 0),
      reason TEXT,
      created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
      CHECK (type <> 'DISCARD' OR (reason IS NOT NULL AND trim(reason) <> ''))
);
