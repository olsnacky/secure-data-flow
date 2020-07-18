package com.qutifc.contracts.reader.components;

import com.qutifc.interfaces.components.Reader;
import com.qutifc.interfaces.components.Record;

public class ReaderContract implements Reader {
	private String t;
	private Record[] r;
	private int state;

	@Override
	public void setText(String text) {
		this.t = text;
	}

	@Override
	public void read() {
		String[] entries = null;
		if (this.t != null) {
			entries = new String[0];
			this.r = new Record[0];
			this.state = 0;
		}

		Record record = Record.get();
		record.setKeys(entries);
		record.setValues(entries);

		if (this.state != 0) {
			this.r[0] = record;
		}
	}

	@Override
	public Record getNext() {
		this.state = 0;
		return this.r[this.state];
	}

	@Override
	public String type() {
		return "contract";
	}
}
