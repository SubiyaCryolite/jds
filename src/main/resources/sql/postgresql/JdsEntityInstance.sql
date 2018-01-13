CREATE TABLE jds_entity_instance
(
  uuid      VARCHAR(96),
  entity_id BIGINT,
  CONSTRAINT unique_entity_instance UNIQUE (uuid, entity_id)
);