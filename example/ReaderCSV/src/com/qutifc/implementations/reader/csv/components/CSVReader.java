package com.qutifc.implementations.reader.csv.components;

import com.qutifc.interfaces.components.Reader;
import com.qutifc.interfaces.components.Record;
import com.qutifc.securitycontracts.annotations.MapsTo;

public class CSVReader implements Reader {
	@MapsTo(FieldName = "t")
	private String text;
	@MapsTo(FieldName = "r")
	private Record[] records;
	@MapsTo(FieldName = "state")
	private int readPosition;
	@MapsTo(FieldName = "state")
	private int numRecords;
	@MapsTo(FieldName = "state")
	private int numColumns;

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void read() {
		this.splitEntry(-1, '\n', true, false);
		// we assume the column headers are in the first line
		String[] keys = this.splitEntry(0, ',', false, true);

		this.initialiseRecords(this.numRecords);
		int i = 0;
		while (i < this.numRecords) {
			String[] values = this.splitEntry(i + 1, ',', false, false);
			this.records[i] = this.createRecord(keys, values);
			i = i + 1;
		}

		this.readPosition = 0;
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

	private String[] getEntries() {
		return this.splitEntry(-1, '\n', false, false);
	}

	private String[] splitEntry(int entryIndex, char delimiter, boolean setRecordCount, boolean setColumnCount) {
		String text;
		if (entryIndex == -1) {
			text = this.text;
		} else {
			String[] entries = this.getEntries();
			text = entries[entryIndex];
		}

		int i = 0;
		int splitCount = 1;
		int textLength = text.length();

		while (i < textLength) {
			char c = text.charAt(i);
			if (c == delimiter) {
				splitCount = splitCount + 1;
			}

			i = i + 1;
		}

		if (setRecordCount) {
			this.numRecords = splitCount - 1;
		} else if (setColumnCount) {
			this.numColumns = splitCount;
		}

		String[] result = new String[splitCount];
		String currentResult = "";
		int resultPosition = 0;
		i = 0;
		while (i < textLength) {
			char c = text.charAt(i);
			if (c == delimiter) {
				result[resultPosition] = currentResult;
				currentResult = "";
				resultPosition = resultPosition + 1;
			} else {
				currentResult = currentResult + c;
			}

			i = i + 1;
		}

		result[resultPosition] = currentResult;

		return result;
	}

	private Record createRecord(String[] keys, String[] values) {
		Record record = Record.get();
		record.setKeys(keys);
		record.setValues(values);
		record.setColumnCount(this.numColumns);
		return record;
	}

	private void initialiseRecords(int size) {
		this.records = new Record[size];
	}

	@Override
	public String type() {
		return "csv";
	}
}
