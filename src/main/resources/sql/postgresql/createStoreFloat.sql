CREATE TABLE JdsStoreFloat(
	FieldId     BIGINT,
	EntityGuid    VARCHAR(48),
	Value       REAL,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);