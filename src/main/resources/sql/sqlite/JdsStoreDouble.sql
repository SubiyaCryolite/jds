CREATE TABLE JdsStoreDouble(
	FieldId         BIGINT,
	Uuid      TEXT,
	Value           DOUBLE,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);