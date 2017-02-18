CREATE TABLE JdsStoreText(
	FieldId     BIGINT,
	ActionId    VARCHAR(48),
	Value       TEXT,
	PRIMARY KEY (FieldId,ActionId)
);