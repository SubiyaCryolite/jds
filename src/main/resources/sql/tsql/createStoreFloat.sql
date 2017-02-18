CREATE TABLE JdsStoreFloat(
	FieldId     BIGINT,
	ActionId    NVARCHAR(48),
	Value       REAL,
	PRIMARY KEY (FieldId,ActionId)
);