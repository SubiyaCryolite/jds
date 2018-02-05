CREATE TABLE jds_entity_instance
(
  entity_composite_key TEXT,
  entity_id            BIGINT,
  CONSTRAINT unique_entity_inheritance UNIQUE (entity_composite_key, entity_id),
  FOREIGN KEY (entity_composite_key) REFERENCES jds_entity_overview (composite_key)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED,
  FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE
    DEFERRABLE INITIALLY DEFERRED
);