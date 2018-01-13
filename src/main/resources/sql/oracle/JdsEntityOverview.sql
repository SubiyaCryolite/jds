CREATE TABLE jds_entity_overview (
  uuid          NVARCHAR2(96),
  date_created  TIMESTAMP,
  date_modified TIMESTAMP,
  version       NUMBER(19),
  live          NUMBER(3),
  PRIMARY KEY (uuid)
)