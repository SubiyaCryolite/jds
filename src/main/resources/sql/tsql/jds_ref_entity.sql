CREATE TABLE jds_ref_entity (
  id          BIGINT,
  name        NVARCHAR(64),
  caption     NVARCHAR(64),
  description NVARCHAR(256),
  parent      BIT,
  PRIMARY KEY (id)
);