CREATE TABLE JdsStoreDateTime(
	FieldId         BIGINT,
	EntityGuid      TEXT,
	Value           TIMESTAMP,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);