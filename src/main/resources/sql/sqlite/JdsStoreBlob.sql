CREATE TABLE JdsStoreBlob(
	FieldId         BIGINT,
	Uuid      TEXT,
	Value           BLOB,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);