CREATE TABLE JdsStoreFloat(
	FieldId     BIGINT,
	Uuid  VARCHAR(48),
	Value       FLOAT,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);