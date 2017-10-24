CREATE TABLE JdsStoreZonedDateTime(
	FieldId         BIGINT,
	Uuid      TEXT,
	Value           BIGINT,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);