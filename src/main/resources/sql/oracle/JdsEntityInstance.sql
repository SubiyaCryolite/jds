CREATE TABLE jds_entity_instance (
  uuid      NVARCHAR2(96),
  entity_id NUMBER(19),
  CONSTRAINT unique_entity_inheritance UNIQUE (uuid, entity_id)
)