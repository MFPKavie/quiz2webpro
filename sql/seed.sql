USE passiton;

-- admin login: admin@campus.ac.th / Demo1234!  Change it before anyone else can reach the site.
INSERT INTO users (name, email, password_hash, faculty, line_id, role) VALUES
('Admin', 'admin@campus.ac.th', 'pbkdf2_sha256$600000$EHcH+yD5P9aiQO2PxXWsiw==$SEgvg8WgzMLOBTkBaveiNjpsNwtMYWh1V3Z8/jwa4Io=', NULL, NULL, 'admin');

INSERT INTO categories (name, slug) VALUES
('Textbooks', 'textbooks'), ('Electronics', 'electronics'), ('Dorm & Home', 'dorm-home'),
('Clothing & Uniforms', 'clothing'), ('Sports & Bikes', 'sports'), ('Other', 'other');
