CREATE TABLE jds_entity_overview
(
  id           TEXT,
  edit_version   INTEGER,
  entity_id      INTEGER,
  PRIMARY KEY (id, edit_version),
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED
);