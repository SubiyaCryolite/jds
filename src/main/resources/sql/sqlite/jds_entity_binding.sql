CREATE TABLE jds_entity_binding
(
  parent_uuid         TEXT,
  parent_edit_version INTEGER,
  child_uuid          TEXT,
  child_edit_version  INTEGER,
  child_attribute_id  BIGINT,
  CONSTRAINT jds_entity_binding_uc UNIQUE (parent_uuid, parent_edit_version, child_uuid, child_edit_version),
  FOREIGN KEY (parent_uuid, parent_edit_version)
  REFERENCES jds_entity_overview (uuid, edit_version)
    ON DELETE CASCADE,
  FOREIGN KEY (child_uuid, child_edit_version)
  REFERENCES jds_entity_overview (uuid, edit_version)
    ON DELETE CASCADE
);