CREATE TABLE jds_entity_binding
(
  parent_uuid          NVARCHAR(128),
  parent_uuid_location NVARCHAR(45),
  parent_edit_version  INTEGER,
  child_uuid           NVARCHAR(128),
  child_uuid_location  NVARCHAR(45),
  child_edit_version   INTEGER,
  child_attribute_id   BIGINT,
  CONSTRAINT jds_entity_binding_uc UNIQUE (parent_uuid, parent_uuid_location, parent_edit_version, child_uuid, child_uuid_location, child_edit_version)
);