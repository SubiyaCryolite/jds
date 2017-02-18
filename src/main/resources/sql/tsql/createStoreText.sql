CREATE TABLE JdsStoreText(
	FieldId     BIGINT,
	ActionId    NVARCHAR(48),
	Value       NVARCHAR(MAX),
	PRIMARY KEY (FieldId,ActionId)
);