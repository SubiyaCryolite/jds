CREATE TABLE jds_entity_overview (
  composite_key         NVARCHAR2(195),
  uuid                  NVARCHAR2(96),
  uuid_location         NVARCHAR2(56),
  uuid_location_version INTEGER,
  version               BIGINT,
  live                  BIT,
  PRIMARY KEY (composite_key)
);