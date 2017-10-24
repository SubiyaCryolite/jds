CREATE TABLE JdsStoreZonedDateTime(
	FieldId     BIGINT,
	Uuid  VARCHAR(48),
	Value       TIMESTAMP WITH TIME ZONE,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);