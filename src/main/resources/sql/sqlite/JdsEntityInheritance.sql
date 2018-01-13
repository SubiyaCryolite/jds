CREATE TABLE jds_ref_entity_inheritance (
  parent_entity_id BIGINT,
  child_entity_id  BIGINT,
  PRIMARY KEY (parent_entity_id, child_entity_id)
);