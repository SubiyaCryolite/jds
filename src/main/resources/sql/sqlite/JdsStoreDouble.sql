CREATE TABLE JdsStoreDouble(
	FieldId         BIGINT,
	EntityGuid      TEXT,
	Value           DOUBLE,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);