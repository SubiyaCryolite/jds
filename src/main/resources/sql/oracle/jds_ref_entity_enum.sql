CREATE TABLE jds_ref_entity_enum (
  entity_id NUMBER(19),
  field_id  NUMBER(19),
  PRIMARY KEY (entity_id, field_id),
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id) ON DELETE CASCADE,
  FOREIGN KEY (field_id) REFERENCES jds_ref_field (id) ON DELETE CASCADE
)