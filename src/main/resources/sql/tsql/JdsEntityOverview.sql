CREATE TABLE jds_entity_overview (
  uuid          NVARCHAR(96),
  date_created  DATETIME,
  date_modified DATETIME,
  version       BIGINT,
  live          BIT,
  PRIMARY KEY (uuid)
);