CREATE TABLE jds_entity_overview
(
  composite_key         TEXT,
  uuid                  TEXT,
  uuid_location         TEXT,
  uuid_location_version INTEGER,
  version               BIGINT,
  live                  BOOLEAN,
  PRIMARY KEY (composite_key)
);