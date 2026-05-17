INSERT INTO roles (id, name) VALUES (1, 'ROLE_FREE_MEMBER');

INSERT INTO users (id, name,furigana,postal_code,address, phone_number, birthday, occupation, email, password, role_id, enabled) VALUES (1, '侍 太郎', 'サムライ タロウ', '1010022', '東京都千代田区神田練塀町300番地', '09012345678', '1990-01-01', 'エンジニア', 'taro.samurai@example.com', '$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO', 1, true);
