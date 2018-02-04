CREATE TABLE jds_entity_overview
(
  composite_key         VARCHAR2(195),
  uuid                  VARCHAR2(96),
  uuid_location         VARCHAR2(56),
  uuid_location_version INTEGER,
  version               BIGINT,
  live                  BOOLEAN,
  PRIMARY KEY (composite_key)
);