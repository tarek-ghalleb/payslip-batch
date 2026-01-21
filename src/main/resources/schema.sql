DROP TABLE IF EXISTS employees;

CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100),
    position VARCHAR(50),
    base_salary DECIMAL(10, 2),
    worked_hours INTEGER,
    department VARCHAR(50),
    employee_number VARCHAR(20),
    active BOOLEAN DEFAULT true
);

INSERT INTO employees (first_name, last_name, email, position, base_salary, worked_hours, department, employee_number, active) VALUES
('Jean', 'Dupont', 'jean.dupont@company.com', 'Développeur Senior', 4500.00, 151, 'IT', 'EMP001', true),
('Marie', 'Martin', 'marie.martin@company.com', 'Chef de Projet', 5200.00, 151, 'Management', 'EMP002', true),
('Pierre', 'Durand', 'pierre.durand@company.com', 'Développeur Junior', 3200.00, 151, 'IT', 'EMP003', true),
('Sophie', 'Bernard', 'sophie.bernard@company.com', 'Responsable RH', 4800.00, 151, 'RH', 'EMP004', true),
('Luc', 'Petit', 'luc.petit@company.com', 'Designer UX', 3800.00, 151, 'Design', 'EMP005', true);