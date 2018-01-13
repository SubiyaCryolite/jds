CREATE TABLE jds_entity_binding (
  parent_uuid     TEXT,
  child_uuid      TEXT,
  field_id        BIGINT,
  child_entity_id BIGINT,
  FOREIGN KEY (parent_uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity,
    FOREIGN KEY (child_uuid) REFERENCES jds_entity_overview(uuid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);