CREATE TABLE JdsStoreDouble(
	FieldId     BIGINT,
	EntityGuid  VARCHAR(48),
	Value       DOUBLE,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);