CREATE TABLE JdsStoreText(
	FieldId     BIGINT,
	EntityGuid    TEXT,
	Value       TEXT,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);