CREATE TABLE JdsStoreBoolean(
	FieldId     NUMBER(19),
	EntityGuid  NVARCHAR2(48),
	Value       SMALLINT,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
)