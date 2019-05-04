CREATE TABLE jds_ref_field (
  id           BIGINT,
  caption      VARCHAR(64),
  description  VARCHAR(256),
  type_ordinal INT,
  PRIMARY KEY (id),
  FOREIGN KEY (type_ordinal) REFERENCES jds_ref_field_type (ordinal)
    ON DELETE CASCADE
);