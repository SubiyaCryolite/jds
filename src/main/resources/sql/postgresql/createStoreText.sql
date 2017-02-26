CREATE TABLE JdsStoreText(
	FieldId     BIGINT,
	EntityGuid  VARCHAR(48),
	Value       TEXT,
	PRIMARY KEY (FieldId,EntityGuid)
);