CREATE TABLE JdsStoreDateTime(
	FieldId         BIGINT,
	Uuid      TEXT,
	Value           TIMESTAMP,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE NO ACTION --we use REPLACE INTO that is not an upsert :(
);