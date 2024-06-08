DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS requests;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS items (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1024),
    is_available boolean DEFAULT FALSE,
    owner_id BIGINT,
    request_id BIGINT,
    CONSTRAINT fk_userId
            FOREIGN KEY(owner_id)
            REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    start_date TIMESTAMP WITHOUT TIME ZONE,
    end_date TIMESTAMP WITHOUT TIME ZONE,
    item_id BIGINT,
    booker_id BIGINT,
    status VARCHAR(16) NOT NULL,
    CONSTRAINT fk_itemId
                FOREIGN KEY(item_id)
                REFERENCES items(id),
    CONSTRAINT fk_bookerId
                FOREIGN KEY(booker_id)
                REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    description VARCHAR(1024) NOT NULL,
    requestor_id BIGINT,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_requestorId
          FOREIGN KEY(requestor_id)
          REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    text VARCHAR(1024),
    item_id BIGINT,
    author_id BIGINT,
    created_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_itemId_comments
          FOREIGN KEY(item_id)
          REFERENCES items(id),
    CONSTRAINT fk_authorId
          FOREIGN KEY(author_id)
          REFERENCES users(id)
);