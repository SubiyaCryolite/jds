CREATE TABLE JdsStoreBlob(
	FieldId     BIGINT,
	Uuid    VARCHAR(96),
	Value       BYTEA,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);