CREATE TABLE jds_entity_binding
(
  parent_composite_key VARCHAR(128),
  child_composite_key  VARCHAR(128),
  field_id             BIGINT,
  child_entity_id      BIGINT,
  FOREIGN KEY (parent_composite_key) REFERENCES jds_entity_overview (composite_key) ON DELETE CASCADE,
  FOREIGN KEY (child_composite_key) REFERENCES jds_entity_overview (composite_key) ON DELETE CASCADE
);