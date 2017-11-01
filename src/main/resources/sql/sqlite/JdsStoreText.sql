CREATE TABLE JdsStoreText(
	FieldId     BIGINT,
	Uuid    TEXT,
	Value       TEXT,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE NO ACTION --we use REPLACE INTO that is not an upsert :(
);