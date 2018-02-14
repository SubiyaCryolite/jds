CREATE TABLE jds_ref_entity (
  id          BIGINT,
  name        VARCHAR(256),
  caption     VARCHAR(256),
  description VARCHAR(256),
  parent      BOOLEAN,
  PRIMARY KEY (id)
);