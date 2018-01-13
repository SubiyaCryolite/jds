CREATE TABLE jds_entity_overview
(
  uuid          VARCHAR(96),
  date_created  TIMESTAMP,
  date_modified TIMESTAMP,
  version       BIGINT,
  live          BOOLEAN,
  PRIMARY KEY (uuid)
);