CREATE TABLE jds_entity_overview
(
  id           NVARCHAR2(36),
  edit_version   NUMBER(10),
  entity_id      NUMBER(10),
  PRIMARY KEY (id, edit_version),
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id)
  ON DELETE CASCADE
)