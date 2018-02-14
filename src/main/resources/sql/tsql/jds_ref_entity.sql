CREATE TABLE jds_ref_entity (
  id          BIGINT,
  name        NVARCHAR(256),
  caption     NVARCHAR(256),
  description NVARCHAR(256),
  parent      BIT,
  PRIMARY KEY (id)
);