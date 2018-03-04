CREATE TABLE jds_entity_overview (
  uuid                  NVARCHAR(64),
  uuid_location         NVARCHAR(45),
  uuid_version INTEGER,
  entity_id             BIGINT,
  entity_version        BIGINT,
  live                  BIT,
  last_edit             DATETIME,
  PRIMARY KEY (uuid, uuid_location, uuid_version),
  CONSTRAINT jds_entity_overview_fk_entity_id FOREIGN KEY (entity_id)
  REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE
);