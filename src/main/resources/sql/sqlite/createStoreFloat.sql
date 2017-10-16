CREATE TABLE JdsStoreFloat(
	FieldId         BIGINT,
	EntityGuid      TEXT,
	Value           REAL,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);