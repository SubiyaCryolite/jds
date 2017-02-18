CREATE TABLE JdsStoreDateTime(
	FieldId     BIGINT,
	ActionId    VARCHAR(48),
	Value       TIMESTAMP,
	PRIMARY KEY (FieldId,ActionId)
);