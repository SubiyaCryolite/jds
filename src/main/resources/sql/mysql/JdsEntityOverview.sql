CREATE TABLE jds_entity_overview
(
  uuid          VARCHAR(96),
  date_created  DATETIME DEFAULT CURRENT_TIMESTAMP,
  date_modified DATETIME DEFAULT CURRENT_TIMESTAMP,
  version       BIGINT,
  live          BOOLEAN,
  PRIMARY KEY (uuid)
);