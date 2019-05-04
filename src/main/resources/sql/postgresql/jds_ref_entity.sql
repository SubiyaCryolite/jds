CREATE TABLE jds_ref_entity (
  id          BIGINT,
  name        VARCHAR(64),
  caption     VARCHAR(64),
  description VARCHAR(256),
  parent      BOOLEAN,
  PRIMARY KEY (id)
);