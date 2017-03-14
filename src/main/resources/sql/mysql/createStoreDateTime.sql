CREATE TABLE JdsStoreDateTime(
	FieldId     BIGINT,
	EntityGuid  VARCHAR(48),
	Value       DATETIME,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);