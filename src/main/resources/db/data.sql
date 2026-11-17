
USE task_planner;

INSERT INTO tasks (title, due_date, priority, completed) VALUES
    ('Підготувати звіт з лабораторної',  '2026-11-20', 'HIGH',   FALSE),
    ('Купити продукти',                  '2026-11-18', 'LOW',    FALSE),
    ('Записатися до лікаря',             '2026-11-22', 'MEDIUM', FALSE),
    ('Повторити теорію з баз даних',     '2026-11-25', 'HIGH',   FALSE),
    ('Оплатити інтернет',                '2026-11-19', 'MEDIUM', TRUE),
    ('Прибрати в кімнаті',               '2026-11-21', 'LOW',    FALSE),
    ('Підготуватися до контрольної',     '2026-11-27', 'HIGH',   FALSE),
    ('Полагодити велосипед',             '2026-12-02', 'LOW',    FALSE),
    ('Написати курсову: розділ 1',       '2026-12-05', 'HIGH',   FALSE),
    ('Привітати друга з днем народження','2026-11-30', 'MEDIUM', TRUE);

INSERT INTO task_history (task_id, action, details)
SELECT id, 'CREATED', CONCAT('Створено задачу: ', title) FROM tasks;

INSERT INTO task_history (task_id, action, details)
SELECT id, 'COMPLETED', 'Задачу позначено як виконану' FROM tasks WHERE completed;
