CREATE TABLE jds_entity_overview
(
  uuid           NVARCHAR2(128),
  edit_version   NUMBER(10),
  entity_id      NUMBER(19),
  entity_version NUMBER(19),
  PRIMARY KEY (uuid, edit_version),
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id)
  ON DELETE CASCADE
)