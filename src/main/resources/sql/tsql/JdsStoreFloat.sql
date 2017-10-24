CREATE TABLE JdsStoreFloat(
	FieldId         BIGINT,
	Uuid      NVARCHAR(48) NOT NULL,
	Value           REAL,
	PRIMARY KEY (FieldId,Uuid),
	CONSTRAINT fk_JdsStoreFloat_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);