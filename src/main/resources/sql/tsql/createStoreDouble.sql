CREATE TABLE JdsStoreDouble(
	FieldId     BIGINT,
	EntityGuid    NVARCHAR(48),
	Value       FLOAT,
	PRIMARY KEY (FieldId,EntityGuid)
);