CREATE TABLE JdsStoreZonedDateTime(
	FieldId     BIGINT,
	EntityGuid  VARCHAR(48),
	Value       TIMESTAMP WITH TIME ZONE,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);