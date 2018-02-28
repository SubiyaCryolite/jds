CREATE TABLE jds_entity_overview (
  composite_key         NVARCHAR(128),
  uuid                  NVARCHAR(64),
  uuid_location         NVARCHAR(45),
  uuid_location_version INTEGER,
  parent_uuid           NVARCHAR(64),
  parent_composite_key  NVARCHAR(128),
  entity_id             BIGINT,
  entity_version        BIGINT,
  live                  BIT,
  last_edit             DATETIME,
  field_id              BIGINT,
  PRIMARY KEY (composite_key),
  CONSTRAINT jds_entity_overview_fk_entity_id FOREIGN KEY (entity_id) REFERENCES jds_ref_entity (id)
    ON DELETE CASCADE
);