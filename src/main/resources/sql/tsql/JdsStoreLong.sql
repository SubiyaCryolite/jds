CREATE TABLE JdsStoreLong(
	FieldId     BIGINT,
	Uuid  NVARCHAR(48) NOT NULL,
	Value       BIGINT,
	PRIMARY KEY (FieldId,Uuid),
	CONSTRAINT fk_JdsStoreLong_ParentUuid FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE
);