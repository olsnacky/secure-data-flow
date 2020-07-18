package com.qutifc.contracts.writer.components;

import com.qutifc.interfaces.components.Reader;
import com.qutifc.interfaces.components.Record;
import com.qutifc.interfaces.components.Writer;

public class WriterContract implements Writer {
	private Reader r;
	private String t;

	@Override
	public void setReader(Reader reader) {
		this.r = reader;
	}

	@Override
	public void write() {
		if (this.r != null) {
			Record record = this.r.getNext();
			if (record != null) {
				this.t = "";
			}
		}
	}

	@Override
	public String getText() {
		return this.t;
	}

	@Override
	public String type() {
		return "contract";
	}
}
