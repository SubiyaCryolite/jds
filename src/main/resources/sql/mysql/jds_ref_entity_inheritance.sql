CREATE TABLE jds_ref_entity_inheritance (
  parent_entity_id INTEGER,
  child_entity_id  INTEGER,
  PRIMARY KEY (parent_entity_id, child_entity_id),
  FOREIGN KEY (parent_entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE,
  FOREIGN KEY (child_entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE
);