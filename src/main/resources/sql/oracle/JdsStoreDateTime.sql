CREATE TABLE JdsStoreDateTime(
	FieldId     NUMBER(19),
	Uuid  NVARCHAR2(96),
	Value       TIMESTAMP,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
)