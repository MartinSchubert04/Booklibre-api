-- El rating y el comment de una reserva ahora viven únicamente en MongoDB
-- (embebidos en el documento Book, dentro de props.reservations[]).
-- La columna rating/comment de la tabla reservaciones (PostgreSQL) queda obsoleta.
ALTER TABLE reservaciones DROP COLUMN IF EXISTS rating;
ALTER TABLE reservaciones DROP COLUMN IF EXISTS comment;
