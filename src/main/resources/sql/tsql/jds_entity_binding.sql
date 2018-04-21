CREATE TABLE jds_entity_binding
(
  parent_uuid          NVARCHAR(128),
  parent_edit_version  INTEGER,
  child_uuid           NVARCHAR(128),
  child_edit_version   INTEGER,
  child_attribute_id   BIGINT,
  CONSTRAINT jds_entity_binding_uc UNIQUE (parent_uuid, parent_edit_version, child_uuid, child_edit_version)
);