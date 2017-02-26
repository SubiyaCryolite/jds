CREATE TABLE JdsStoreDateTime(
	FieldId     BIGINT,
	EntityGuid    VARCHAR(48),
	Value       TIMESTAMP,
	PRIMARY KEY (FieldId,EntityGuid)
);