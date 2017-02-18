CREATE TABLE JdsStoreDateTime(
	FieldId     BIGINT,
	ActionId    NVARCHAR(48),
	Value       DATETIME,
	PRIMARY KEY (FieldId,ActionId)
);