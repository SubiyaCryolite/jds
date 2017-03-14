CREATE TABLE JdsStoreText(
	FieldId     BIGINT,
	EntityGuid  VARCHAR(48),
	Value       TEXT,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);