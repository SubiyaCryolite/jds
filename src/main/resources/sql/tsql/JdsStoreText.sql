CREATE TABLE jds_store_text (
  field_id BIGINT,
  uuid     NVARCHAR(96),
  value    NVARCHAR(MAX),
  PRIMARY KEY (field_id, uuid)
);
ALTER TABLE jds_store_text
  ADD CONSTRAINT fk_jds_store_text_parent_uuid FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid)
  ON DELETE CASCADE;