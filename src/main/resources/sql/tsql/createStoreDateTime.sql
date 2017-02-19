CREATE TABLE JdsStoreDateTime(
	FieldId     BIGINT,
	EntityGuid    NVARCHAR(48),
	Value       DATETIME,
	PRIMARY KEY (FieldId,EntityGuid)
);