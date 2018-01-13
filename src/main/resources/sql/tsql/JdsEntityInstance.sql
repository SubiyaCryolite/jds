CREATE TABLE jds_entity_instance (
  uuid      NVARCHAR(96),
  entity_id BIGINT,
  CONSTRAINT unique_entity_inheritance UNIQUE (uuid, entity_id) --deliberately left out reference in case of shady individuals
);