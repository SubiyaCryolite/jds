CREATE TABLE JdsStoreDateTime(
	FieldId         BIGINT,
	Uuid      NVARCHAR(48) NOT NULL,
	Value           DATETIME,
	PRIMARY KEY (FieldId,Uuid),
	CONSTRAINT fk_JdsStoreDateTime_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);