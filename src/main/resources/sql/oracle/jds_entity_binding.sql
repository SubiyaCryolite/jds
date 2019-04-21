CREATE TABLE jds_entity_binding
(
  parent_uuid         NVARCHAR2(36),
  parent_edit_version NUMBER(10),
  child_uuid          NVARCHAR2(36),
  child_edit_version  NUMBER(10),
  child_attribute_id  NUMBER(19),
  CONSTRAINT jds_entity_binding_uc UNIQUE (parent_uuid, parent_edit_version, child_uuid, child_edit_version),
  FOREIGN KEY (parent_uuid, parent_edit_version)
  REFERENCES jds_entity_overview (uuid, edit_version) ON DELETE CASCADE,
  FOREIGN KEY (child_uuid, child_edit_version)
  REFERENCES jds_entity_overview (uuid, edit_version) ON DELETE CASCADE
)