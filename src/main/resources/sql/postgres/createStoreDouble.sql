CREATE TABLE JdsStoreDouble(
	FieldId     BIGINT,
	EntityGuid    VARCHAR(48),
	Value       FLOAT,
	PRIMARY KEY (FieldId,EntityGuid)
);