CREATE TABLE jds_entity_overview (
  uuid                  NVARCHAR(128),
  uuid_location         NVARCHAR(45),
  edit_version INTEGER,
  entity_id             BIGINT,
  entity_version        BIGINT,
  live                  BIT,
  last_edit             DATETIME,
  PRIMARY KEY (uuid, uuid_location, edit_version),
  CONSTRAINT jds_entity_overview_fk_entity_id FOREIGN KEY (entity_id)
  REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE
);