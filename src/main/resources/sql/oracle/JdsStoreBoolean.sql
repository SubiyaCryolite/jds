CREATE TABLE JdsStoreBoolean(
	FieldId     NUMBER(19),
	Uuid  NVARCHAR2(48),
	Value       NUMBER(3),
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
)