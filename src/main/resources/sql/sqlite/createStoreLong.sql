CREATE TABLE JdsStoreLong(
	FieldId         BIGINT,
	EntityGuid      TEXT,
	Value           INTEGER,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);