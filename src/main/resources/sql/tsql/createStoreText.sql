CREATE TABLE JdsStoreText(
	FieldId     BIGINT,
	EntityGuid    NVARCHAR(48),
	Value       NVARCHAR(MAX),
	PRIMARY KEY (FieldId,EntityGuid)
);