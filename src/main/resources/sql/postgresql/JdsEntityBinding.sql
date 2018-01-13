CREATE TABLE jds_entity_binding
(
  parent_uuid     VARCHAR(96),
  child_uuid      VARCHAR(96),
  field_id        BIGINT,
  child_entity_id BIGINT,
  FOREIGN KEY (parent_uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE,
  FOREIGN KEY (child_uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
);