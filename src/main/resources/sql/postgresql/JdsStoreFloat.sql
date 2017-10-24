CREATE TABLE JdsStoreFloat(
	FieldId     BIGINT,
	Uuid    VARCHAR(48),
	Value       REAL,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);