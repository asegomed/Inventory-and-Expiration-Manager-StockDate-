
CREATE TABLE IF NOT EXISTS batch (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     product_name TEXT NOT NULL,
                                     category TEXT NOT NULL,
                                     quantity INTEGER NOT NULL,
                                     storage_location TEXT NOT NULL,
                                     expiration_date TEXT NOT NULL,
                                     expiration_date_type TEXT NOT NULL
);
