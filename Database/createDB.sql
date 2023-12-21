--Création du schéma
DROP SCHEMA IF EXISTS Countries CASCADE;
CREATE SCHEMA Countries;

--Sélectionne le schéma
SET search_path TO Countries;

--Ajout de la table
CREATE TABLE country(
    name VARCHAR(50),
    capital VARCHAR(50),
    population INTEGER,
    PRIMARY KEY(name)
);

--Remplissage
INSERT INTO country VALUES('Switzerland', 'Bern', 8796669);
INSERT INTO country VALUES('France', 'Paris', 64756584);
INSERT INTO country VALUES('India', 'New Delhi', 1428627663);
INSERT INTO country VALUES('USA', 'Washington', 339996564);
INSERT INTO country VALUES('Japan', 'Tokyo', 123294513);
INSERT INTO country VALUES('Italia', 'Roma', 58870763);
INSERT INTO country VALUES('Argentina', 'Buenos Aires', 45773884);
INSERT INTO country VALUES('Ghana', 'Accra', 34121985);
INSERT INTO country VALUES('Estonia', 'Tallinn', 1322766);
INSERT INTO country VALUES('Vanuatu', 'Port-Vila', 334506);