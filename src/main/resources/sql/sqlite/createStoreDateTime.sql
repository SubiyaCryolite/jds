CREATE TABLE JdsStoreDateTime(
	FieldId     BIGINT,
	EntityGuid    TEXT,
	Value       TIMESTAMP,
	PRIMARY KEY (FieldId,EntityGuid)
);