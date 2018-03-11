CREATE TABLE jds_entity_overview
(
  uuid           VARCHAR(128),
  edit_version   INTEGER,
  entity_id      BIGINT,
  entity_version BIGINT,
  PRIMARY KEY (uuid, edit_version),
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE
);