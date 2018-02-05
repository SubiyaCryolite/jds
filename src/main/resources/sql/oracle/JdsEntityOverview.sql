CREATE TABLE jds_entity_overview (
  composite_key          NVARCHAR2(128),
  uuid                   NVARCHAR2(64),
  uuid_location          NVARCHAR2(45),
  uuid_location_version NUMBER(10),
  version                NUMBER(19),
  live                   NUMBER(3),
  PRIMARY KEY (composite_key)
)