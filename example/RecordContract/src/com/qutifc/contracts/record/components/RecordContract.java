package com.qutifc.contracts.record.components;

import com.qutifc.interfaces.components.Record;

public class RecordContract implements Record {
	private String[] keys;
	private String[] values;
	private int columnCount;

	@Override
	public void setKeys(String[] keys) {
		this.keys = keys;
	}

	@Override
	public void setValues(String[] values) {
		this.values = values;
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
		return this.columnCount;
	}

	@Override
	public void setColumnCount(int columns) {
		this.columnCount = columns;
	}
}
