CREATE TABLE JdsStoreZonedDateTime(
	FieldId     NUMBER(19),
	EntityGuid  NVARCHAR2(48),
	Value       NUMBER(19),
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
)