package com.qutifc.implementations.record.implementation.components;

import com.qutifc.interfaces.components.Record;
import com.qutifc.securitycontracts.annotations.MapsTo;

public class RecordImpl implements Record {
	@MapsTo(FieldName = "keys")
	private String[] keys;
	@MapsTo(FieldName = "values")
	private String[] values;
	@MapsTo(FieldName = "columnCount")
	private int numColumns;

	@Override
	public void setKeys(String[] keys) {
		this.keys = keys;
	}

	@Override
	public void setValues(String[] values) {
		this.values = values;
	}

	@Override
	public void setColumnCount(int columns) {
		this.numColumns = columns;
	}

	@Override
	public String[] getKeys() {
		return this.keys;
	}

	@Override
	public String[] getValues() {
		return this.values;
	}

	@Override
	public int getColumnCount() {
		return this.numColumns;
	}
}
