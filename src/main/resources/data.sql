MERGE INTO GENRE
    KEY(GENRE_ID)
    VALUES (1, 'Анимационный фильм' ), (2, 'Боевик'), (3, 'Детектив'), (4, 'Драма'), (5, 'Комедия'), (6, 'Триллер'), (7, 'Хоррор'), (8,'Не определен' );

MERGE INTO RATING
    KEY(RATING_ID)
    VALUES (1,  'G' ), (2, 'PG'), (3, 'PG-13'), (4, 'R'), (5, 'NC-17'), (6, 'Не определен');