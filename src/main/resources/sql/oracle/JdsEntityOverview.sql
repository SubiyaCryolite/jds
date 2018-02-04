CREATE TABLE jds_entity_overview (
  composite_key          NVARCHAR2(195),
  uuid                   NVARCHAR2(96),
  uuid_location          NVARCHAR2(56),
  uuid_location_version NUMBER(10),
  version                NUMBER(19),
  live                   NUMBER(3),
  PRIMARY KEY (composite_key)
)