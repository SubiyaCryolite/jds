CREATE TABLE jds_ref_entity_inheritance (
  parent_entity_id INTEGER,
  child_entity_id  INTEGER,
  PRIMARY KEY (parent_entity_id, child_entity_id),
  CONSTRAINT fk_jds_ref_entity_inheritance_parent_entity_id FOREIGN KEY (parent_entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE NO ACTION,
  CONSTRAINT fk_jds_ref_entity_inheritance_child_entity_id FOREIGN KEY (child_entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE NO ACTION
);