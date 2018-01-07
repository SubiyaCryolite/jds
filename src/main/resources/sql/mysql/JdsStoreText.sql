CREATE TABLE JdsStoreText(
	FieldId     BIGINT,
	Uuid  VARCHAR(96),
	Value       TEXT,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);