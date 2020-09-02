package org.mitre.schemastore.porters.schemaExporters.sql;

public class ReferenceValue {
	private String _value;
	private org.mitre.schemastore.porters.schemaExporters.sql.DomainTable _domain;

	public ReferenceValue(org.mitre.schemastore.porters.schemaExporters.sql.DomainTable referenceTable, String value) {
		_domain = referenceTable;
		_value = value;
	}
	
	public String getValue() {
		return _value;
	}

	public DomainTable getDomain() {
		return _domain;
	}
}
