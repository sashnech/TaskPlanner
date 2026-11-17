
CREATE DATABASE IF NOT EXISTS task_planner
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE task_planner;

DROP TABLE IF EXISTS task_history;
DROP TABLE IF EXISTS tasks;

CREATE TABLE tasks (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    title     VARCHAR(120) NOT NULL,
    due_date  DATE         NOT NULL,
    priority  ENUM ('LOW', 'MEDIUM', 'HIGH') NOT NULL,
    completed BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_tasks PRIMARY KEY (id),
    CONSTRAINT chk_tasks_title CHECK (CHAR_LENGTH(TRIM(title)) > 0)
);

CREATE INDEX idx_tasks_due_date ON tasks (due_date);


CREATE TABLE task_history (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    task_id    BIGINT       NOT NULL,
    action     ENUM ('CREATED', 'UPDATED', 'COMPLETED') NOT NULL,
    details    VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_task_history PRIMARY KEY (id),
    CONSTRAINT fk_task_history_task FOREIGN KEY (task_id)
        REFERENCES tasks (id) ON DELETE CASCADE
);

CREATE INDEX idx_task_history_task ON task_history (task_id);
