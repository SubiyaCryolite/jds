CREATE TABLE jds_ref_entity (
  id          NUMBER(19),
  name        NVARCHAR2(256),
  caption     NVARCHAR2(256),
  description NVARCHAR2(256),
  parent      NUMBER(3),
  PRIMARY KEY (id)
)