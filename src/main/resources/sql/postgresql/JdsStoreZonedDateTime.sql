CREATE TABLE JdsStoreZonedDateTime(
	FieldId     BIGINT,
	EntityGuid    VARCHAR(48),
	Value       BIGINT,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);