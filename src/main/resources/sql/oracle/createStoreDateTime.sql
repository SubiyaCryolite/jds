CREATE TABLE JdsStoreDateTime(
	FieldId     NUMBER(19),
	EntityGuid  NVARCHAR2(48),
	Value       DATE,
	PRIMARY KEY (FieldId,EntityGuid),
	FOREIGN KEY (EntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
)