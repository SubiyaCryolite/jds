CREATE TABLE JdsStoreLong(
	FieldId     BIGINT,
	EntityGuid    NVARCHAR(48),
	Value       INTEGER,
	PRIMARY KEY (FieldId,EntityGuid)
);