/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.schemastore.porters.schemaImporters.hcatalog.gson;

/**
 *
 * @author mgreer
 */
public class ArrayDataType extends org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HiveDataType {
    
    org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HiveDataType listType;
    
    public ArrayDataType(org.mitre.schemastore.porters.schemaImporters.hcatalog.gson.HiveDataType list) {
        super(DataType.ARRAY);
        listType = list;
    }
    
    public HiveDataType getListType() {
        return listType;
    }
    
}
