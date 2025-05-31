CREATE TABLE capability (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    id_technology INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO capability (name, description) VALUES
('Backend', 'Aplicaciones logica de negocio'),
('Frontend', 'Aplicaciones parte visual');

CREATE TABLE capability_bootcamp (
    id SERIAL PRIMARY KEY,
    id_bootcamp BIGINT NOT NULL,
    id_capability BIGINT NOT NULL,
    CONSTRAINT fk_capability
      FOREIGN KEY (id_capability) REFERENCES capability(id)
);