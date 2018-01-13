CREATE TABLE jds_store_zoned_date_time (
  field_id BIGINT,
  uuid     VARCHAR(96),
  value    TIMESTAMP WITH TIME ZONE,
  PRIMARY KEY (field_id, uuid),
  FOREIGN KEY (uuid) REFERENCES jds_entity_overview (uuid) ON DELETE CASCADE
);