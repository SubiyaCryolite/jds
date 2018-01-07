CREATE TABLE JdsStoreDateTime(
	FieldId     BIGINT,
	Uuid  VARCHAR(96),
	Value       DATETIME,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);