CREATE TABLE jds_entity_overview (
  composite_key         NVARCHAR(128),
  uuid                  NVARCHAR(64),
  uuid_location         NVARCHAR(45),
  uuid_location_version INTEGER,
  entity_id             BIGINT,
  entity_version        BIGINT,
  live                  BIT,
  last_edit             DATETIME,
  PRIMARY KEY (composite_key)
);