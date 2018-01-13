CREATE TABLE jds_entity_binding (
  parent_uuid     NVARCHAR2(96),
  child_uuid      NVARCHAR2(96),
  field_id        NUMBER(19),
  child_entity_id NUMBER(19),
  FOREIGN KEY (parent_uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE,
  FOREIGN KEY (child_uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
)