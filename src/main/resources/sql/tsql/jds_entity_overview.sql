CREATE TABLE jds_entity_overview (
  uuid           NVARCHAR(128),
  edit_version   INTEGER,
  entity_id      BIGINT,
  PRIMARY KEY (uuid, edit_version),
  CONSTRAINT jds_entity_overview_fk_entity_id FOREIGN KEY (entity_id)
  REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE
);