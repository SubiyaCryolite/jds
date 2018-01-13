CREATE TABLE jds_entity_overview
(
  uuid          TEXT,
  date_created  TIMESTAMP,
  date_modified TIMESTAMP,
  version       BIGINT,
  live          BOOLEAN,
  PRIMARY KEY (uuid)
);