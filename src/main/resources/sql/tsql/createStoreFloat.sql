CREATE TABLE JdsStoreFloat(
	FieldId     BIGINT,
	EntityGuid    NVARCHAR(48),
	Value       REAL,
	PRIMARY KEY (FieldId,EntityGuid)
);