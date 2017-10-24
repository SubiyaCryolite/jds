CREATE TABLE JdsStoreText(
	FieldId     BIGINT,
	Uuid    TEXT,
	Value       TEXT,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);