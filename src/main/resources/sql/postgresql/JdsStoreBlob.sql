CREATE TABLE JdsStoreBlob(
	FieldId     BIGINT,
	Uuid    VARCHAR(48),
	Value       BYTEA,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);