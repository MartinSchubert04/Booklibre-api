/*-- Vista que lista usuarios con más de 2 reservas devueltas
-- Se considera "devuelta" una reserva cuya fecha until_date ya pasó

CREATE OR REPLACE VIEW usuarios_con_mas_de_2_reservas_devueltas AS
SELECT
    u.id,
    u.full_name,
    u.email,
    COUNT(r.id) AS cantidad_reservas_devueltas
FROM usuarios u
JOIN reservaciones r ON u.id = r.reader_id
WHERE r.until_date IS NOT NULL
  AND r.until_date < CURRENT_DATE
GROUP BY u.id, u.full_name, u.email
HAVING COUNT(r.id) > 2;

-- Inserto reservas de prueba
--INSERT INTO reservaciones (book_id, reader_id, owner_id, from_date, until_date, rating)*/
--VALUES
-- Usuario 1 → 3 devueltas ✅
--(1, 1, 2, CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE - INTERVAL '5 days', 4.5),
--(2, 1, 2, CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE - INTERVAL '15 days', 5.0),
--(3, 1, 2, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '25 days', 4.0);*/