CREATE TABLE JdsStoreBlob(
	FieldId         BIGINT,
	EntityGuid      TEXT,
	Value           BLOB,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);