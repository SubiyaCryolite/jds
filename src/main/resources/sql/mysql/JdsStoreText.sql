CREATE TABLE JdsStoreText(
	FieldId     BIGINT,
	Uuid  VARCHAR(48),
	Value       TEXT,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);