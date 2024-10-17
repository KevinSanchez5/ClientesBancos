CREATE TABLE BankCards (
    number VARCHAR(255) PRIMARY KEY,
    clientId BIGINT NOT NULL,
    expirationDate DATE NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);