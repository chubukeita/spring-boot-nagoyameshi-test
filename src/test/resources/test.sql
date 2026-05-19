INSERT INTO roles (id, name) VALUES
(1, 'ROLE_FREE_MEMBER'),
(2, 'ROLE_PAID_MEMBER'),
(3, 'ROLE_ADMIN');

INSERT INTO users (
    id, name, furigana, postal_code, address, phone_number, birthday, occupation, email, password, role_id, enabled
) VALUES
(1, '侍 太郎', 'サムライ タロウ', '1010022', '東京都千代田区神田練塀町300番地', '09012345678', '1990-01-01', 'エンジニア', 'taro.samurai@example.com', '$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO', 1, true),
(2, '侍 次郎', 'サムライ ジロウ', '1010022', '東京都千代田区神田練塀町300番地', '09012345679', '1991-02-02', 'デザイナー', 'jiro.samurai@example.com', '$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO', 2, true),
(3, '侍 花子', 'サムライ ハナコ', '1010022', '東京都千代田区神田練塀町300番地', '09012345670', '1992-03-03', 'マーケティング', 'hanako.samurai@example.com', '$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO', 3, true);

INSERT INTO companies (
    id, name, postal_code, address, representative, establishment_date, capital, business, number_of_employees
) VALUES
(1, 'NAGOYAMESHI株式会社', '1010022', '東京都千代田区神田練塀町300番地 住友不動産秋葉原駅前ビル5F', '侍 太郎', '2015年3月19日', '110,000千円', '飲食店等の情報提供サービス', '83名');

INSERT INTO categories (id, name) VALUES
(1, '居酒屋'),
(2, '和食'),
(3, 'うどん'),
(4, '丼物'),
(5, 'ラーメン'),
(6, 'おでん'),
(7, '揚げ物');

INSERT INTO restaurants (
    id, name, image, description, lowest_price, highest_price, postal_code, address, opening_time, closing_time, seating_capacity
) VALUES
(1, 'NAGOYA BURGER 名駅店', 'sample1.jpg', '名古屋老舗のお店。老舗の味をご堪能ください。', 3000, 4000, '4500000', '愛知県名古屋市中区栄X-XX-XX', '10:00:00', '20:00:00', 50),
(2, '焼肉小山', 'sample2.jpg', '焼肉レビュー検証用の店舗データです。', 2000, 5000, '4500000', '愛知県名古屋市中区栄X-XX-XX', '11:00:00', '21:00:00', 60);

INSERT INTO category_restaurant (id, restaurant_id, category_id) VALUES
(1, 1, 1),
(2, 2, 1);

INSERT INTO terms (id, content) VALUES
(1, 'テスト用利用規約です。内容は255文字以内にしています。');

INSERT INTO reviews (id, content, score, restaurant_id, user_id) VALUES
(1, '名古屋では有名な格安で焼肉食べ放題のお店。タイミングよく仕事で行く機会があったので、地元の友人と一緒に来店しました。店内は広くゆったりとできます。', 3, 2, 2),
(2, 'また来店したいと思えるお店でした。', 4, 2, 1);

INSERT INTO favorites (id, restaurant_id, user_id) VALUES
(1, 1, 2),
(21, 1, 1);

INSERT INTO reservations (id, reserved_datetime, number_of_people, restaurant_id, user_id) VALUES
(1, '2050-01-02 12:00:00', 2, 1, 2),
(21, '2050-01-03 19:00:00', 4, 1, 1);
