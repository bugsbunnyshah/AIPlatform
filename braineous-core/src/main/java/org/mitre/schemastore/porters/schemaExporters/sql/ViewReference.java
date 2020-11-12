package org.mitre.schemastore.porters.schemaExporters.sql;


public class ViewReference extends org.mitre.schemastore.porters.schemaExporters.sql.RdbAttribute
{
	private org.mitre.schemastore.porters.schemaExporters.sql.RdbAttribute _toAtt; // attribute that's referenced in the view
	private org.mitre.schemastore.porters.schemaExporters.sql.View _toView;

	public ViewReference(Rdb schema, Table fromTable, String fromAtt, org.mitre.schemastore.porters.schemaExporters.sql.View refView, String toAtt, RdbValueType type)
			throws NoRelationFoundException {
		super( schema, fromTable, fromAtt, type );
		_toView = refView;
		setReferencedAttribute( refView );
	}

	public org.mitre.schemastore.porters.schemaExporters.sql.RdbAttribute getReferencedAttribute() {
		return _toAtt;
	}

	public void setReferencedAttribute( org.mitre.schemastore.porters.schemaExporters.sql.View view ) {
		RdbAttribute toPK = view.getPrimaryKey();
		if ( toPK != null )
			_toAtt = toPK;
	}
	
	public View getReferencedView() {
		return _toView;
	}

}
