CREATE TABLE jds_entity_binding (
  parent_uuid     NVARCHAR(96),
  child_uuid      NVARCHAR(96),
  field_id        BIGINT,
  child_entity_id BIGINT,
  CONSTRAINT fk_jds_entity_binding_parent_uuid FOREIGN KEY (parent_uuid) REFERENCES jds_entity_overview (uuid),
  CONSTRAINT fk_jds_entity_binding_child_uuid FOREIGN KEY (child_uuid) REFERENCES jds_entity_overview (uuid)
);