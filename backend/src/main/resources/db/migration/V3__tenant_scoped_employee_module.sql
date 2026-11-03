CREATE TABLE departments (
 id UUID PRIMARY KEY, organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
 code VARCHAR(40) NOT NULL, name VARCHAR(120) NOT NULL, description VARCHAR(500),
 status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 CONSTRAINT uq_departments_org_code UNIQUE (organization_id, code),
 CONSTRAINT uq_departments_id_org UNIQUE (id, organization_id)
);
CREATE TABLE positions (
 id UUID PRIMARY KEY, organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
 code VARCHAR(40) NOT NULL, title VARCHAR(120) NOT NULL, description VARCHAR(500),
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 CONSTRAINT uq_positions_org_code UNIQUE (organization_id, code),
 CONSTRAINT uq_positions_id_org UNIQUE (id, organization_id)
);
CREATE TABLE employees (
 id UUID PRIMARY KEY, organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
 department_id UUID, position_id UUID, employee_number VARCHAR(40) NOT NULL,
 full_name VARCHAR(160) NOT NULL, email VARCHAR(254) NOT NULL, phone VARCHAR(40),
 employment_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
 CHECK (employment_status IN ('ACTIVE','ON_LEAVE','RESIGNED','TERMINATED')),
 hire_date DATE NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 CONSTRAINT uq_employees_org_number UNIQUE (organization_id, employee_number),
 CONSTRAINT uq_employees_org_email UNIQUE (organization_id, email),
 CONSTRAINT uq_employees_id_org UNIQUE (id, organization_id),
 CONSTRAINT fk_employees_department_org FOREIGN KEY (department_id, organization_id)
 REFERENCES departments(id, organization_id),
 CONSTRAINT fk_employees_position_org FOREIGN KEY (position_id, organization_id)
 REFERENCES positions(id, organization_id)
);
CREATE INDEX idx_employees_org_status ON employees(organization_id, employment_status);
CREATE INDEX idx_employees_org_name ON employees(organization_id, full_name);
CREATE INDEX idx_employees_org_department ON employees(organization_id, department_id);
CREATE INDEX idx_departments_org_status ON departments(organization_id, status);
