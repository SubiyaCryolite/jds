CREATE TABLE jds_entity_instance
(
  entity_uuid TEXT,
  entity_id   BIGINT,
  CONSTRAINT unique_entity_inheritance UNIQUE (entity_uuid, entity_id),
  FOREIGN KEY (entity_uuid) REFERENCES jds_entity_overview (uuid)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED,
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED
);