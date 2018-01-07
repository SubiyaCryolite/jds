CREATE TABLE JdsStoreText(
	FieldId     NUMBER(19),
	Uuid  NVARCHAR2(96),
	Value       NCLOB,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
)