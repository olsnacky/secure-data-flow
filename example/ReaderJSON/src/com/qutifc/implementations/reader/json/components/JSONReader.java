package com.qutifc.implementations.reader.json.components;

import com.qutifc.interfaces.components.Parser;
import com.qutifc.interfaces.components.Reader;
import com.qutifc.interfaces.components.Record;
import com.qutifc.interfaces.components.Scanner;
import com.qutifc.securitycontracts.annotations.MapsTo;

public class JSONReader implements Reader {
	@MapsTo(FieldName = "t")
	private String text;
	@MapsTo(FieldName = "r")
	private Record[] records;
	@MapsTo(FieldName = "state")
	private int readPosition;
	@MapsTo(FieldName = "state")
	private int numRecords;

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void read() {
		Scanner scanner = Scanner.get(this.type());
		Parser parser = Parser.get(this.type());

		scanner.setText(this.text);
		scanner.scan();
		parser.setTokens(scanner.getTokens());

		parser.parse();

		this.records = parser.getRecords();
		this.numRecords = parser.getRecordCount();
	}

	@Override
	public Record getNext() {
		if (this.readPosition < this.numRecords) {
			Record record = this.records[this.readPosition];
			this.readPosition = this.readPosition + 1;
			return record;
		}

		return null;
	}

	@Override
	public String type() {
		return "json";
	}
}
