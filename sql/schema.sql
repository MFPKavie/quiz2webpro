DROP DATABASE IF EXISTS passiton;
CREATE DATABASE passiton CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE passiton;

CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(80)  NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(120) NOT NULL,
    faculty       VARCHAR(80),
    line_id       VARCHAR(40),
    avatar        VARCHAR(80),
    role          ENUM('student','admin') NOT NULL DEFAULT 'student',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE listings (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    seller_id      INT NOT NULL,
    category_id    INT NOT NULL,
    title          VARCHAR(100) NOT NULL,
    description    TEXT NOT NULL,
    price          INT NOT NULL CHECK (price BETWEEN 0 AND 20000000),
    item_condition ENUM('new','like_new','good','fair') NOT NULL,
    status         ENUM('available','reserved','sold') NOT NULL DEFAULT 'available',
    meetup_spot    VARCHAR(100),
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_listing_seller   FOREIGN KEY (seller_id)   REFERENCES users(id),
    -- RESTRICT: a category that still has listings cannot be deleted (rule 8)
    CONSTRAINT fk_listing_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    INDEX idx_listing_status_created (status, created_at)
);

CREATE TABLE listing_photos (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    listing_id INT NOT NULL,
    file_name  VARCHAR(80) NOT NULL,
    sort_order TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_photo_listing FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE
);

-- the bytes of every uploaded image (listing photos and avatars), keyed by its random file name
CREATE TABLE photo_files (
    file_name VARCHAR(80) PRIMARY KEY,
    data      MEDIUMBLOB NOT NULL
);

CREATE TABLE offers (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    listing_id INT NOT NULL,
    buyer_id   INT NOT NULL,
    amount     INT NOT NULL CHECK (amount > 0),
    message    VARCHAR(300),
    status     ENUM('pending','accepted','declined','withdrawn') NOT NULL DEFAULT 'pending',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_offer_listing FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE,
    CONSTRAINT fk_offer_buyer   FOREIGN KEY (buyer_id)   REFERENCES users(id)
);

CREATE TABLE wishlist (
    user_id    INT NOT NULL,
    listing_id INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, listing_id),
    CONSTRAINT fk_wish_user    FOREIGN KEY (user_id)    REFERENCES users(id),
    CONSTRAINT fk_wish_listing FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE
);

CREATE TABLE reviews (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    listing_id  INT NOT NULL,
    reviewer_id INT NOT NULL,
    rating      TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     VARCHAR(500),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_review_once (listing_id, reviewer_id),
    -- RESTRICT: a reviewed sale cannot be deleted (rule 7)
    CONSTRAINT fk_review_listing  FOREIGN KEY (listing_id)  REFERENCES listings(id) ON DELETE RESTRICT,
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id)
);

CREATE TABLE reports (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    listing_id  INT NOT NULL,
    reporter_id INT NOT NULL,
    reason      VARCHAR(300) NOT NULL,
    status      ENUM('open','dismissed') NOT NULL DEFAULT 'open',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_listing  FOREIGN KEY (listing_id)  REFERENCES listings(id) ON DELETE CASCADE,
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES users(id)
);
