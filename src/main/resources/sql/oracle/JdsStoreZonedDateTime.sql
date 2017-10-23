CREATE TABLE JdsStoreZonedDateTime(
	FieldId     NUMBER(19),
	EntityGuid  NVARCHAR2(48),
	Value       TIMESTAMP WITH TIME ZONE,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
)