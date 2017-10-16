CREATE TABLE JdsStoreBlob(
	FieldId     BIGINT,
	EntityGuid  VARCHAR(48),
	Value       BLOB,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);