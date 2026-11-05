CREATE TABLE IF NOT EXISTS search_requests (
    id         UUID                     PRIMARY KEY,
    thesis     VARCHAR(2000)            NOT NULL,
    reason     VARCHAR(5000)            NOT NULL,
    status     VARCHAR(20)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
